package fr.vuzi.fileexplorer.database.directory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import fr.vuzi.fileexplorer.database.DataBase;
import fr.vuzi.fileexplorer.database.SortType;
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
	
	
	public static Directory getDirectoryTree(User u) {
		Directory root = getDirectory(u, null, null);
		Stack<Directory> stack = new Stack<Directory>();
		stack.add(root);
		
		while(!stack.isEmpty()) {
			// For current directory
			Directory current = stack.pop();
			current.directories = getDirectoriesContained(u, current);

			// Add all its sub-folders to the stack
			for(Directory tmp : current.directories)
				stack.add(tmp);
		}
		
		return root;
	}

	/**
	 * Get a directory by its path for the current user
	 * @param u The owner of the directory
	 * @param path The path
	 * @return The directory, or null if none could be found
	 */
	public static Directory getDirectory(User u, List<String> path) {
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;
		return getDirectory(u, path, name);
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
				query.put("path", "/");
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
			query.put("path", "/");
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
	public static Directory getDirectoryParent(User u, List<String> path) {
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;
		return getDirectoryParent(u, path, name);
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
	 * Update a directory name
	 * @param u The owner
	 * @param UID The UID
	 */
	public static void updateDirectoryDate(User u, String UID) {
		if(UID == null)
			return; // Nothing to update
		
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("directories");
		Date now = new Date();

		BasicDBObject query = new BasicDBObject();
		query.put("owner", new ObjectId(u.UID));
		query.put("_id", new ObjectId(UID));
		
		BasicDBObject update = new BasicDBObject();
		update.append("$set", new BasicDBObject().append("edit", now));
		
		collection.updateOne(query, update);
	}

	/**
	 * Update the directory date
	 * @param u The owner
	 * @param d The directory
	 */
	public static void updateDirectoryDate(User u, Directory d) {
		updateDirectoryDate(u, d.UID);
	}
	
	/**
	 * Create a directory
	 * @param u The owner
	 * @param path The path
	 * @param name The name
	 * @return The newly created directory
	 */
	public static Directory createDirectory(User u, Directory parent, String name) {

		MongoCollection<Document> collection = DataBase.getInstance().getCollection("directories");
		Document newDir = new Document();
		Date now = new Date();

		newDir.put("owner", new ObjectId(u.UID));
		newDir.put("name", name);
		newDir.put("path", parent.name != null ? parent.path + parent.name + "/" : "/");
		newDir.put("creation", now);
		newDir.put("edit", now);
		newDir.put("parent", parent.UID != null ? new ObjectId(parent.UID) : null);
		
		collection.insertOne(newDir);
		updateDirectoryDate(u, parent);
				
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

		if(d.name == null) // Root can't be deleted
			return d;
		
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("directories");

		BasicDBObject query = new BasicDBObject();
		query.put("owner", new ObjectId(u.UID));
		query.put("name", d.name);
		query.put("path", d.path);

		if(collection.deleteOne(query).wasAcknowledged()) {
			updateDirectoryDate(u, d.parentUID);
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

		if(d.name == null) // Root can't be renamed
			return d;
		
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("directories");
		MongoCollection<Document> collectionFiles = DataBase.getInstance().getCollection("files.files");
		Date now = new Date();
		String newPath = d.path + newName;
		String oldPath = d.path + d.name;

		// Change name
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(d.UID));
		query.put("owner", new ObjectId(u.UID));
		
		BasicDBObject update = new BasicDBObject();
		update.append("$set", new BasicDBObject().append("name", newName).append("edit", now));
		
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

		// Update directories
		for(Document doc : collection.find(query)) {
			BasicDBObject dirQuery = new BasicDBObject();
			dirQuery.put("_id", doc.getObjectId("_id"));
			
			update = new BasicDBObject();
			update.append("$set", new BasicDBObject().append("path", newPath + doc.getString("path").substring(oldPath.length())).append("edit", now));
		
			collection.updateOne(dirQuery, update);
		}
		
		// Update files
		for(Document doc : collectionFiles.find(query)) {
			BasicDBObject fileQuery = new BasicDBObject();
			fileQuery.put("_id", doc.getObjectId("_id"));
			
			update = new BasicDBObject();
			update.append("$set", new BasicDBObject().append("path", newPath + doc.getString("path").substring(oldPath.length())).append("edit", now));
		
			collectionFiles.updateOne(fileQuery, update);
		}
		
		// Return the modified element
		d.name = newName;
		return d;
	}
	
	/**
	 * Return the list of found directories
	 * @param u
	 * @param regex
	 * @param container
	 * @return
	 */
	public static List<Directory> searchDirectories(User u, String regex, Directory container, boolean recursive, SortType sort) {
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("directories");
		ArrayList<Directory> directories = new ArrayList<Directory>();
		
		BasicDBObject query = new BasicDBObject();
		query.put("owner", new ObjectId(u.UID));
		query.put("name", new BasicDBObject("$regex", regex));
		if(recursive)
			query.put("path", new BasicDBObject("$regex", "^" + container.getInnerPath() ));
		else
			query.put("path", new BasicDBObject("$regex", "^" + container.getInnerPath() + "$" ));

		FindIterable<Document> results = collection.find(query);
		
		// Sort
		// No sort from mongodb
		
		for(Document d : results) {
			directories.add(new Directory(d));
		}

		// Sort (not handled by mongodb)
		switch(sort) {
		case NAME_ASC:
			directories.sort(new Comparator<Directory>() {
				@Override
				public int compare(Directory d1, Directory d2) {
					return String.CASE_INSENSITIVE_ORDER.compare(d1.name, d2.name);
				}
			});
			break;
		case NAME_DSC:
			directories.sort(new Comparator<Directory>() {
				@Override
				public int compare(Directory d1, Directory d2) {
					return String.CASE_INSENSITIVE_ORDER.compare(d2.name, d1.name);
				}
			});
			break;
		default:
			break;
		}
		return directories;
	}

	/**
	 * Move a directory
	 * @param u
	 * @param d
	 * @param newContainer
	 * @return
	 */
	public static Directory moveDirectory(User u, Directory d, Directory newContainer) {
		
		if(d.UID == null)
			return d;
		
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("directories");
		MongoCollection<Document> collectionFiles = DataBase.getInstance().getCollection("files.files");
		Date now = new Date();
		
		// Change path
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(d.UID));
		query.put("owner", new ObjectId(u.UID));

		String oldPath = d.path;
		String newPath = newContainer.name == null ? "/" : newContainer.path + newContainer.name + "/";

		BasicDBObject update = new BasicDBObject();
		update.append("$set", new BasicDBObject().append("path", newPath).append("edit", now));

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

		// Update directories
		for(Document doc : collection.find(query)) {
			BasicDBObject dirQuery = new BasicDBObject();
			dirQuery.put("_id", doc.getObjectId("_id"));
			
			update = new BasicDBObject();
			update.append("$set", new BasicDBObject().append("path", newPath + doc.getString("path").substring(oldPath.length())).append("edit", now));
		
			collection.updateOne(dirQuery, update);
		}
		
		// Update files
		for(Document doc : collectionFiles.find(query)) {
			BasicDBObject fileQuery = new BasicDBObject();
			fileQuery.put("_id", doc.getObjectId("_id"));
			
			update = new BasicDBObject();
			update.append("$set", new BasicDBObject().append("path", newPath + doc.getString("path").substring(oldPath.length())).append("edit", now));
		
			collectionFiles.updateOne(fileQuery, update);
		}

		updateDirectoryDate(u, newContainer);
		
		// Update local version
		d.path = newPath;
		
		return d;
	}
}
