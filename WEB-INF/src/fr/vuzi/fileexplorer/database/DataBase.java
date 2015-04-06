package fr.vuzi.fileexplorer.database;

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
	private static MongoDatabase mongoClient;
	
	/**
	 * Singleton : no public constructor
	 */
	private DataBase() {}
	
	/**
	 * Initialize the database singleton
	 */
	public static void init() {
		if(mongoClient == null)
			mongoClient = new MongoClient( "localhost" , 27017 ).getDatabase("mydb");
	}
	
	/**
	 * Return the database instance
	 * @return the database instance
	 */
	public static MongoDatabase getInstance() {
		return mongoClient;
	}

}
