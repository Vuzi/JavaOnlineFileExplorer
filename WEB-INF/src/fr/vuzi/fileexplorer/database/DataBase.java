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
	private static MongoClient mongoClient;
	
	/**
	 * Singleton : no public constructor
	 */
	private DataBase() {}
	
	/**
	 * Initialize the database singleton
	 */
	public static void init() {
		if(mongoClient == null)
			mongoClient = new MongoClient( "localhost" , 27017 );
	}

	/**
	 * Return the database instance
	 * @return the database instance
	 */
	public static MongoDatabase getInstance() {
		return mongoClient.getDatabase("mydb");
	}

	/**
	 * Return the database instance
	 * @return the database instance
	 */
	@SuppressWarnings("deprecation")
	public static DB getInstanceDb() {
		// Used to store files
		Mongo mongo = new Mongo("localhost", 27017);
		DB db = mongo.getDB("mydb");
		//db.setWriteConcern(WriteConcern.SAFE) ;
		
		return db;
	}

}
