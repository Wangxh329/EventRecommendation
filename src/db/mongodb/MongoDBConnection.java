package db.mongodb;

import static com.mongodb.client.model.Filters.eq;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.ExternalAPI;
import external.ExternalAPIFactory;

public class MongoDBConnection implements DBConnection {
	private static MongoDBConnection instance; // Singleton pattern
	private static MongoClient mongoClient;
	private static MongoDatabase db;

	public static DBConnection getInstance() {
		if (instance == null) {
			instance = new MongoDBConnection();
		}
		// Connects to local mongodb server.
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
		return instance;
	}

	private MongoDBConnection() {
		// // Connects to local mongodb server.
		// mongoClient = new MongoClient();
		// db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
	}

	@Override
	public void close() {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$push", new Document("favorite", new Document("$each", itemIds))));
		// 向某个document的某个field数组中添加用$push,用$each一个一个添加
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		db.getCollection("users").updateOne(new Document("user_id", userId),
				new Document("$pullAll", new Document("favorite", itemIds)));
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		Set<String> favoriteItemsIds = new HashSet<String>();
		// db.users.find({user_id:1111})
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first().containsKey("favorite")) {
			@SuppressWarnings("unchecked") // 表示下方的强制转换没有问题，不要warning
			List<String> list = (List<String>) iterable.first().get("favorite");
			favoriteItemsIds.addAll(list);
		}
		return favoriteItemsIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		Set<String> itemIds = getFavoriteItemIds(userId);
		Set<Item> favoriteItems = new HashSet<>();
		for (String itemId : itemIds) {
			FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
			Document doc = iterable.first();
			ItemBuilder builder = new ItemBuilder();
			builder.setItemId(doc.getString("item_id"));
			builder.setName(doc.getString("name"));
			builder.setCity(doc.getString("city"));
			builder.setState(doc.getString("state"));
			builder.setCountry(doc.getString("country"));
			builder.setZipcode(doc.getString("zipcode"));
			builder.setRating(doc.getDouble("rating"));
			builder.setAddress(doc.getString("address"));
			builder.setLatitude(doc.getDouble("latitude"));
			builder.setLongitude(doc.getDouble("longitude"));
			builder.setDescription(doc.getString("description"));
			builder.setSnippet(doc.getString("snippet"));
			builder.setSnippetUrl(doc.getString("snippet_url"));
			builder.setImageUrl(doc.getString("image_url"));
			builder.setUrl(doc.getString("url"));
			builder.setCategories(getCategories(itemId));

			favoriteItems.add(builder.build());
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		Set<String> categories = new HashSet<String>();
		// db.users.find({user_id:hannah_wang})
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
		if (iterable.first().containsKey("categories")) {
			@SuppressWarnings("unchecked") // 表示下方的强制转换没有问题，不要warning
			List<String> list = (List<String>) iterable.first().get("categories");
			categories.addAll(list);
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(String userId, double lat, double lon, String term) {
		// Connect to external API
		ExternalAPI api = ExternalAPIFactory.getExternalAPI(); // moved here
		List<Item> items = api.search(lat, lon, term);
		for (Item item : items) {
			// Save the item into our own db.
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		UpdateOptions options = new UpdateOptions().upsert(true); // item_id已存在，则update，否则insert
		db.getCollection("items").updateOne(new Document().append("item_id", item.getItemId()),
				new Document("$set",
						new Document().append("item_id", item.getItemId()).append("name", item.getName())
								.append("city", item.getCity()).append("state", item.getState())
								.append("country", item.getCountry()).append("zip_code", item.getZipcode())
								.append("rating", item.getRating()).append("address", item.getAddress())
								.append("latitude", item.getLatitude()).append("longitude", item.getLongitude())
								.append("description", item.getDescription()).append("snippet", item.getSnippet())
								.append("snippet_url", item.getSnippetUrl()).append("image_url", item.getImageUrl())
								.append("url", item.getUrl()).append("categories", item.getCategories())),
				options); // 修改一个document用$set
	}

	@Override
	public String getFullname(String userId) {
		FindIterable<Document> iterable = db.getCollection("users").find(new Document("user_id", userId));
		Document document = iterable.first();
		String firstName = document.getString("first_name");
		String lastName = document.getString("last_name");
		return firstName + " " + lastName;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		FindIterable<Document> iterable = db.getCollection("users").find(new Document("user_id", userId));
		Document document = iterable.first();
		if (iterable.first() == null) {
			return false;
		}
		return document.getString("password").equals(password);
	}

	@Override
	public boolean register(String userId, String password, String firstname, String lastname) {
		// db.users.find({user_id:userId})
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first() != null) {
			return false;
		} else {
			db.getCollection("users").insertOne(new Document().append("first_name", firstname).append("last_name", lastname)
					.append("password", password).append("user_id", userId));
		}
		return true;
	}
}
