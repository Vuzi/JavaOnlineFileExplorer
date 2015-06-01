package fr.vuzi.fileexplorer.database.user;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

import fr.vuzi.fileexplorer.database.DataBase;
import fr.vuzi.webframework.context.IContext;

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
	 * Return an user by its login
	 * @param login The login
	 * @return The found user
	 * @throws Exception 
	 */
	public static User getUserByLogin(String login) throws Exception {
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("users");
		
		BasicDBObject query = new BasicDBObject();
		query.put("login", new BasicDBObject("$regex", login).append("$options", "i"));
		
		Document d = collection.find(query).first();
		
		if(d == null)
			return null;
		
		return new User(d);
	}
	
	/**
	 * Return an user by its login and password
	 * @param login The login
	 * @param password The password
	 * @return The found user
	 * @throws Exception 
	 */
	public static User getUser(String login, String password) throws Exception {
		// Get user by ID
		User u = getUserByLogin(login);
		
		if(u == null)
			return null;
		
		// Digest password
		MessageDigest mda;
		try {
			mda = MessageDigest.getInstance("SHA-512");
			password = String.format("%0128x", new BigInteger(1, mda.digest((password + u.creation.getTime()).getBytes())));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		
		// Test password
		if(u.password.equals(password))
			return u;
		else
			return null;
	}

	/**
	 * Return an user by its UID
	 * @param UID User UID
	 * @return
	 */
	public static User getUser(String UID) {
		if(UID == null)
			return null;
		
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("users");
		
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(UID));
		
		Document d = collection.find(query).first();
		
		if(d == null)
			return null;
		
		return new User(d);
	}
	
	/**
	 * Create a new user
	 * @param login
	 * @param password
	 * @param email
	 * @return
	 */
	public static User createUser(String login, String password, String email) {
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("users");
		Document newUser = new Document();
		Date now = new Date();

		// Digest password
		MessageDigest mda;
		try {
			mda = MessageDigest.getInstance("SHA-512");
			password = String.format("%0128x", new BigInteger(1, mda.digest((password + now.getTime()).getBytes())));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		
		// Credentials
		List<String> credentials = new ArrayList<String>();
		credentials.add("user");
		
		// Prepare
		newUser.put("login", login);
		newUser.put("password", password);
		newUser.put("creation", now);
		newUser.put("mail", email);
		newUser.put("credentials", credentials);
		
		// Create & return
		collection.insertOne(newUser);
				
		return new User(newUser);
	}

	public static User getSessionUser(IContext c) {
		return getUser((String)c.getSessionAttribute("user-uid"));
	}
	
}
