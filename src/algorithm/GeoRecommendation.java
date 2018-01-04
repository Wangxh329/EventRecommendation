package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

// Recommendation based on geo distance and similar categories.
// 从用户favorite中找到所有categories，然后根据经纬度和categories查找，最后根据经纬度远近recommend
public class GeoRecommendation {
	public List<Item> recommendItems(String userId, double lat, double lon) {
		DBConnection conn = DBConnectionFactory.getDBConnection();

		Set<String> favoriteItems = conn.getFavoriteItemIds(userId); // step 1

		Set<String> allCategories = new HashSet<>(); // step 2
		for (String item : favoriteItems) {
			allCategories.addAll(conn.getCategories(item)); // db queries
		}
		allCategories.remove("Undefined"); // tune category set
		if (allCategories.isEmpty()) {
			allCategories.add("");
		}

		Set<Item> recommendedItems = new HashSet<>(); // step 3
		for (String category : allCategories) {
			List<Item> items = conn.searchItems(userId, lat, lon, category); // call external API
			recommendedItems.addAll(items); // deduplication
		}

		// use list instead of set because we will have ranking based on distance, and set cannot be sorted
		List<Item> filteredItems = new ArrayList<>(); // step 4
		for (Item item : recommendedItems) {
			if (!favoriteItems.contains(item.getItemId())) { // delete items user liked before
				filteredItems.add(item);
			}
		}

		// step 5. perform ranking of these items based on distance.
		Collections.sort(filteredItems, new Comparator<Item>() {
			@Override
			public int compare(Item item1, Item item2) {
				double distance1 = getDistance(item1.getLatitude(), item1.getLongitude(), lat, lon);
				double distance2 = getDistance(item2.getLatitude(), item2.getLongitude(), lat, lon);
				// return the increasing order of distance.
				if (distance1 == distance2) {
					return 0;
				}
				return distance1 < distance2 ? -1 : 1;
			}
		});

		return filteredItems;
	}

	// Calculate the distances between two geolocations.
	// Reference Source : http://andrew.hedges.name/experiments/haversine/
	private static double getDistance(double lat1, double lon1, double lat2, double lon2) {
		double dlon = lon2 - lon1;
		double dlat = lat2 - lat1;
		double a = Math.sin(dlat / 2 / 180 * Math.PI) * Math.sin(dlat / 2 / 180 * Math.PI)
				+ Math.cos(lat1 / 180 * Math.PI) * Math.cos(lat2 / 180 * Math.PI) * Math.sin(dlon / 2 / 180 * Math.PI)
						* Math.sin(dlon / 2 / 180 * Math.PI);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		// Radius of earth in miles.
		double R = 3961;
		return R * c;
	}
}
