package fr.vuzi.fileexplorer.database;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import fr.vuzi.fileexplorer.database.directory.Directory;
import fr.vuzi.fileexplorer.database.user.User;

/**
 * Local mongo database
 * @author Vuzi
 *
 */
public class DataBase {

	/**
	 * The database client instance
	 */
	private final static MongoDatabase mongoClient = new MongoClient( "localhost" , 27017 ).getDatabase("mydb");
	
	/**
	 * Return the database instance
	 * @return the database instance
	 */
	public static MongoDatabase getInstance() {
		return mongoClient;
	}
	
	/**
	 * Return an user by its login and password
	 * @param login The login
	 * @param password The password
	 * @return The found user
	 * @throws Exception 
	 */
	public static User getUser(String login, String password) throws Exception {
		MongoCollection<Document> collection = mongoClient.getCollection("users");
		
		BasicDBObject query = new BasicDBObject();
		query.put("login", new BasicDBObject("$regex", login).append("$options", "i"));
		query.put("password", password);
		
		Document d = collection.find(query).first();
		
		if(d == null)
			return null;
		
		return new User(d);
	}
	
	public static Directory getDirectory(ObjectId userId, String path) {
		
		ArrayList<String> paths = new ArrayList<String>();
		
		for(String tmp : path.split("/")) {
			if(!tmp.isEmpty())
				paths.add(tmp);
		}
		
		// First step, get the user root directory

		if(paths.isEmpty()) {
			System.out.println("Root directory");
		} else {
			for(String tmp : paths)
				System.out.println(tmp);
		}
		
		return null;
	}
}
