package fr.vuzi.fileexplorer.database.directory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

import fr.vuzi.fileexplorer.database.DataBase;
import fr.vuzi.fileexplorer.database.user.User;

/**
 * Collection of directories related actions and utility methods
 * 
 * @author Vuzi
 *
 */
public class DirectoryUtils {

	/**
	 * Singleton : no public constructor
	 */
	private DirectoryUtils() {}
	
	/**
	 * Compute a path from a "/" separated string
	 * @param path The string to convert
	 * @return A list containing all the elements
	 */
	public static List<String> getPath(String path) {

		ArrayList<String> paths = new ArrayList<String>();
		
		if(path != null) {
			for(String tmp : path.split("/")) {
				if(!tmp.isEmpty())
					paths.add(tmp);
			}
		}
		
		return paths;
	}

	/**
	 * Get a directory by its path for the current user
	 * @param u The owner of the directory
	 * @param path The path
	 * @param name The name
	 * @return The directory, or null if none could be found
	 */
	public static Directory getDirectory(User u, List<String> path, String name) {
		
		if(name == null) {
			return new Directory(); // Root directory
		} else {
			MongoCollection<Document> collection = DataBase.getInstance().getCollection("directories");

			BasicDBObject query = new BasicDBObject();
			query.put("owner", new ObjectId(u.UID));
			query.put("name", name);
			
			if(path.isEmpty()) {
				query.put("path", null);
			} else {
				String pathRegex = "^/" + String.join("/", path) + "/$";
				query.put("path", new BasicDBObject("$regex", pathRegex));
			}

			Document d = collection.find(query).first();
			
			if(d == null)
				return null;
			else
				return new Directory(d);
		}
	}

	/**
	 * Get a directory by its ID for the current user
	 * @param u The owner of the directory
	 * @param id The directory ID
	 * @return The directory, or null if none could be found
	 */
	public static Directory getDirectory(User u, String id) {
		
		if(id == null) {
			return new Directory(); // Root directory
		} else {
			MongoCollection<Document> collection = DataBase.getInstance().getCollection("directories");

			BasicDBObject query = new BasicDBObject();
			query.put("owner", new ObjectId(u.UID));
			query.put("_id", new ObjectId(id));

			Document d = collection.find(query).first();
			
			if(d == null)
				return null;
			else
				return new Directory(d);
		}
	}
	
	/**
	 * Get the directories contained in a sub-directory
	 * @param u The owner of the directory
	 * @param dir The container directory
	 * @return The directories contained
	 */
	public static Directory[] getDirectoriesContained(User u, Directory dir) {

		MongoCollection<Document> collection = DataBase.getInstance().getCollection("directories");
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
	 * Return the parent directory of a directory
	 * @param u The owner
	 * @param path The directory path
	 * @param name The directory name
	 * @return The parent directory, or null if not found
	 */
	public static Directory getDirectoryParent(User u, List<String> path, String name) {
		
		if(name == null)
			return null; // Root doesn't have a parent
		
		if(path.size() > 0) {
			return getDirectory(u, path.subList(0, path.size() - 1), path.get(path.size() - 1));
		} else {
			return new Directory(); // Root directory
		}
	}
	
	/**
	 * Return the parent directory of a directory
	 * @param u The owner
	 * @param path The directory path
	 * @param name The directory name
	 * @return The parent directory, or null if not found
	 */
	public static Directory getDirectoryParent(User u, Directory d) {
		
		if(d.name == null)
			return null; // Root doesn't have a parent
		
		if(d.getPathList().size() > 0) {
			return getDirectory(u, d.getPathList().subList(0, d.getPathList().size() - 1), d.getPathList().get(d.getPathList().size() - 1));
		} else {
			return new Directory(); // Root directory
		}
	}
	
	/**
	 * Create a directory
	 * @param u The owner
	 * @param path The path
	 * @param name The name
	 * @return The newly created directory
	 */
	public static Directory createDirectory(User u, List<String> path, String name) {

		MongoCollection<Document> collection = DataBase.getInstance().getCollection("directories");
		Document newDir = new Document();
		Date now = new Date();

		newDir.put("owner", new ObjectId(u.UID));
		newDir.put("name", name);
		newDir.put("path", "/" + String.join("/", path) + "/");
		newDir.put("creation", now);
		newDir.put("edit", now);
		
		collection.insertOne(newDir);
		
		return new Directory(newDir);
	}
	
	/**
	 * Delete a directory
	 * @param u The owner
	 * @param path The path
	 * @param name The name
	 * @return The newly created directory
	 */
	public static Directory deleteDirectory(User u, Directory d) {

		MongoCollection<Document> collection = DataBase.getInstance().getCollection("directories");

		BasicDBObject query = new BasicDBObject();
		query.put("owner", new ObjectId(u.UID));
		query.put("name", d.name);
		query.put("path", d.path);
		
		if(collection.deleteOne(query).wasAcknowledged()) {
			return getDirectoryParent(u, d);
		} else {
			return null;
		}
	}

	/**
	 * Rename a directory
	 * @param u The owner
	 * @param d The directory
	 * @param newName The new name
	 * @return The renamed directory
	 */
	public static Directory renameDirectory(User u, Directory d, String newName) {

		MongoCollection<Document> collection = DataBase.getInstance().getCollection("directories");
		String newPath = d.path + newName;
		String oldPath = d.path + d.name;

		// Change name
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(d.UID));
		
		BasicDBObject update = new BasicDBObject();
		update.append("$set", new BasicDBObject().append("name", newName));
		
		collection.updateOne(query, update);
		
		// Update paths
		query = new BasicDBObject();
		query.put("owner", new ObjectId(u.UID));
		
		if(d.getPathList().size() > 0) {
			String pathRegex = "^/" + String.join("/", d.getPathList()) + "/" + d.name + "/";
			query.put("path", new BasicDBObject("$regex", pathRegex));
		} else {
			String pathRegex = "^/" + d.name + "/";
			query.put("path", new BasicDBObject("$regex", pathRegex));
		}
		
		for(Document doc : collection.find(query)) {
			query = new BasicDBObject();
			query.put("_id", doc.getObjectId("_id"));
			
			update = new BasicDBObject();
			update.append("$set", new BasicDBObject().append("path", newPath + doc.getString("path").substring(oldPath.length())));
		
			collection.updateOne(query, update);
		}
		
		// Return the modified element
		d.name = newName;
		return d;
	}

}
