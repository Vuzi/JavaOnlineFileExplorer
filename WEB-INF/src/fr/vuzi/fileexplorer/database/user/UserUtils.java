package fr.vuzi.fileexplorer.database.user;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

import fr.vuzi.fileexplorer.database.DataBase;

/**
 * Collection of users related actions and utility methods
 * 
 * @author Vuzi
 *
 */
public class UserUtils {

	/**
	 * Singleton : no public constructor
	 */
	private UserUtils() {}
	
	/**
	 * Return an user by its login and password
	 * @param login The login
	 * @param password The password
	 * @return The found user
	 * @throws Exception 
	 */
	public static User getUser(String login, String password) throws Exception {
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("users");
		
		BasicDBObject query = new BasicDBObject();
		query.put("login", new BasicDBObject("$regex", login).append("$options", "i"));
		query.put("password", password);
		
		Document d = collection.find(query).first();
		
		if(d == null)
			return null;
		
		return new User(d);
	}
	
}
