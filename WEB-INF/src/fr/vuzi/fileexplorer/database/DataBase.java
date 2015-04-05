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
import fr.vuzi.fileexplorer.database.file.File;
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
	
	/**
	 * Compute a path from a "/" separated string
	 * @param path The string to convert
	 * @return A list containing all the elements
	 */
	private static List<String> getPath(String path) {

		ArrayList<String> paths = new ArrayList<String>();
		
		for(String tmp : path.split("/")) {
			if(!tmp.isEmpty())
				paths.add(tmp);
		}
		
		return paths;
	}
	
	/**
	 * Get the directories contained in a sub-directory
	 * @param u The owner of the directory
	 * @param dir The container directory
	 * @return The directories contained
	 */
	public static Directory[] getDirectoriesContained(User u, Directory dir) {

		MongoCollection<Document> collection = mongoClient.getCollection("directories");
		ArrayList<Directory> directories = new ArrayList<Directory>();

		BasicDBObject query = new BasicDBObject();

		if(dir.name == null)
			query.put("path", null);
		else if(dir.path == null)
			query.put("path","/" + dir.name + "/");
		else
			query.put("path", dir.path + dir.name + "/");
		query.put("owner", new ObjectId(u.UID));

		for(Document d : collection.find(query)) {
			directories.add(new Directory(d));
		}
		
		return directories.toArray(new Directory[0]);
	}
	
	/**
	 * Get the files contained in a directory
	 * @param u The owner of the directory
	 * @param d The container directory
	 * @return The found files
	 */
	public static File[] getFilesContained(User u, Directory d) {
		// TODO
		return new File[0];
	}
	
	/**
	 * Get a directory by its path for the current user
	 * @param u The owner of the directory
	 * @param rawPath The raw path
	 * @return The directory, or null if none could be found
	 */
	public static Directory getDirectory(User u, String rawPath) {
		List<String> paths = getPath(rawPath);
		
		if(paths.isEmpty()) {
			return new Directory(); // Root directory
		} else {
			MongoCollection<Document> collection = mongoClient.getCollection("directories");
			String dir = paths.remove(paths.size() - 1);

			BasicDBObject query = new BasicDBObject();
			query.put("owner", new ObjectId(u.UID));
			query.put("name", dir);
			
			if(paths.isEmpty()) {
				query.put("path", null);
			} else {
				String path = "^/" + String.join("/", paths) + "/$";
				query.put("path", new BasicDBObject("$regex", path));
			}

			Document d = collection.find(query).first();
			
			if(d == null)
				return null;
			else
				return new Directory(d);
		}
	}
}
