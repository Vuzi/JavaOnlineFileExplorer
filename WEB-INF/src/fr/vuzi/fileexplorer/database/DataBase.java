package fr.vuzi.fileexplorer.database;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * Local Mongo database
 * @author Vuzi
 *
 */
public class DataBase {

	/**
	 * The database client instance
	 */
	private static MongoDatabase mongoDatabase;
	private static DB mongoDb;
	
	/**
	 * Singleton : no public constructor
	 */
	private DataBase() {}
	
	/**
	 * Initialize the database singleton
	 */
	@SuppressWarnings("deprecation")
	public static void init() {
		if(mongoDatabase == null)
			mongoDatabase = new MongoClient( "localhost" , 27017 ).getDatabase("mydb");

		// Used to store files
		if(mongoDb == null)
			mongoDb = new Mongo("localhost", 27017).getDB("mydb");
	}

	/**
	 * Return the database instance
	 * @return the database instance
	 */
	public static MongoDatabase getInstance() {
		return mongoDatabase;
	}

	/**
	 * Return the database instance
	 * @return the database instance
	 */
	public static DB getInstanceDb() {
		// Used to store files
		return mongoDb;
	}

}
