/** 
 * Save data from Tomcat Logs to MongoDB.
 */
package offline;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import db.mongodb.MongoDBUtil;

public class Purify {
	public static void main(String[] args) {
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
		// copy-paste some logs of tomcat server
		String fileName = "/Users/hamburger_w/Desktop/LaiOffer/Projects/workspace/EventRecommend/TomcatLog/tomcat_log.txt";

		try {
			db.getCollection("logs").drop();

			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader); // file may be too large and the read() is
																		   // costly, so read piece by piece
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				// Sample input:
				// 73.223.210.212 - - [19/Aug/2017:22:00:24 +0000] "GET
				// /EventRecommend/history?user_id=hannah_wang HTTP/1.1" 200 11410
				List<String> values = Arrays.asList(line.split(" "));

				String ip = values.size() > 0 ? values.get(0) : null;
				String timestamp = values.size() > 3 ? values.get(3) : null;
				String method = values.size() > 5 ? values.get(5) : null;
				String url = values.size() > 6 ? values.get(6) : null;
				String status = values.size() > 8 ? values.get(8) : null;

				Pattern pattern = Pattern.compile("\\[(.+?):(.+)"); // create pattern date : time
				Matcher matcher = pattern.matcher(timestamp); // create Matcher instance to store match result
				matcher.find(); // begin matching

				db.getCollection("logs")
						.insertOne(new Document().append("ip", ip).append("date", matcher.group(1))
								.append("time", matcher.group(2)).append("method", method.substring(1))
								.append("url", url).append("status", status));
			}
			System.out.println("Import Done!");
			bufferedReader.close();
			mongoClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
