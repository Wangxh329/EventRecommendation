package db.mysql;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;

public class MySQLTableCreation { // 只在初始时创建一遍，或DB内容乱了就再重新运行一遍创建新的DB
	// Run this as Java application to reset db schema.
	public static void main(String[] args) {
		try {
			// Ensure the driver is imported.
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection conn = null;

			// Step 1 Connect to MySQL.
			try {
				System.out.println("Connecting to \n" + MySQLDBUtil.URL);
				conn = DriverManager.getConnection(MySQLDBUtil.URL);
			} catch (SQLException e) {
				System.out.println("SQLException " + e.getMessage());
				System.out.println("SQLState " + e.getSQLState());
				System.out.println("VendorError " + e.getErrorCode());
			}
			if (conn == null) {
				return;
			}

			// Step 2 Drop tables in case they exist.
			Statement stmt = conn.createStatement(); // Java语句向mysql传执行的sql语句

			String sql = "DROP TABLE IF EXISTS history"; // 一定先删history，否则REFERENCE会出问题
			stmt.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS categories";
			stmt.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS items";
			stmt.executeUpdate(sql);

			sql = "DROP TABLE IF EXISTS users";
			stmt.executeUpdate(sql);

			// Step 3. Create new tables.
			sql = "CREATE TABLE items " + "(item_id VARCHAR(255) NOT NULL, " + " name VARCHAR(255), "
					+ "city VARCHAR(255), " + "state VARCHAR(255), " + "country VARCHAR(255), "
					+ "zipcode VARCHAR(255), " + "rating FLOAT," + "address VARCHAR(255), " + "latitude FLOAT, "
					+ " longitude FLOAT, " + "description VARCHAR(255), " + "snippet VARCHAR(255), "
					+ "snippet_url VARCHAR(255), " + "image_url VARCHAR(255)," + "url VARCHAR(255),"
					+ " PRIMARY KEY ( item_id ))";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE categories " + "(item_id VARCHAR(255) NOT NULL, " + " category VARCHAR(255), "
					+ " PRIMARY KEY ( item_id, category), " + "FOREIGN KEY (item_id) REFERENCES items(item_id))";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE users " + "(user_id VARCHAR(255) NOT NULL, " + " password VARCHAR(255) NOT NULL, "
					+ " first_name VARCHAR(255), last_name VARCHAR(255), " + " PRIMARY KEY ( user_id ))";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE history " + "(history_id bigint(20) unsigned NOT NULL AUTO_INCREMENT, "
					+ " user_id VARCHAR(255) NOT NULL , " + " item_id VARCHAR(255) NOT NULL, "
					+ " last_favor_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, " + " PRIMARY KEY (history_id),"
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id),"
					+ "FOREIGN KEY (user_id) REFERENCES users(user_id))";
			stmt.executeUpdate(sql); // 一定后create history，否则REFERENCE会出问题

//			// Step 4: insert data
//			// Create a fake user to test
//			sql = "INSERT INTO users " + "VALUES (\"hannah_wang\", \"b2fd75032b458ea5db99b903cbef081c\", \"Hannah\", \"Wang\")";
			System.out.println("Executing query:\n" + sql);
			stmt.executeUpdate(sql);

			System.out.println("Import is done successfully.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
