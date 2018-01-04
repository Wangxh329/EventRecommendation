package rpc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import entity.Item;
import entity.Item.ItemBuilder;

public class RpcHelperTest {
	@Test
	public void testGetJSONArray() throws JSONException {
		Set<String> category = new HashSet<String>();
		category.add("category one");
		Item one = new ItemBuilder().setItemId("one").setLatitude(33.33).setRating(5).setCategories(category)
				.setLongitude(33.33).build();
		Item two = new ItemBuilder().setItemId("two").setLatitude(33.33).setRating(5).setCategories(category)
				.setLongitude(33.33).build();
		List<Item> listItem = new ArrayList<Item>();
		listItem.add(one);
		listItem.add(two);

		JSONArray jsonArray = new JSONArray();
		jsonArray.put(one.toJSONObject());
		jsonArray.put(two.toJSONObject());

		JSONAssert.assertEquals(jsonArray, RpcHelper.getJSONArray(listItem), true);
	}

	@Test
	public void testGetJSONArrayCornerCases() throws JSONException {
		Set<String> category = new HashSet<String>();
		category.add("category one");

		List<Item> listItem = new ArrayList<Item>();
		JSONArray jsonArray = new JSONArray();
		JSONAssert.assertEquals(jsonArray, RpcHelper.getJSONArray(listItem), true);

		Item one = new ItemBuilder().setItemId("one").setLatitude(33.33).setRating(5).setCategories(category)
				.setLongitude(33.33).build();
		Item two = new ItemBuilder().setItemId("two").setLatitude(33.33).setRating(5).setCategories(category)
				.setLongitude(33.33).build();
		listItem.add(one);
		listItem.add(two);

		jsonArray.put(one.toJSONObject());
		jsonArray.put(two.toJSONObject());
		JSONAssert.assertEquals(jsonArray, RpcHelper.getJSONArray(listItem), true);

		Item empty = new ItemBuilder().build();
		listItem.add(empty);
		jsonArray.put(empty.toJSONObject());
		JSONAssert.assertEquals(jsonArray, RpcHelper.getJSONArray(listItem), true);
	}

}
