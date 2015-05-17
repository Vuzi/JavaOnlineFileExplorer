package fr.vuzi.fileexplorer.database.file;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

import fr.vuzi.fileexplorer.database.DataBase;
import fr.vuzi.fileexplorer.database.SortType;
import fr.vuzi.fileexplorer.database.directory.Directory;
import fr.vuzi.fileexplorer.database.directory.DirectoryUtils;
import fr.vuzi.fileexplorer.database.user.User;

/**
 * Collection of file related actions and utility methods
 * 
 * @author Vuzi
 *
 */
public class FileUtils {

	/**
	 * Singleton : no public constructor
	 */
	private FileUtils() {}
	
	/**
	 * Get the files contained in a directory
	 * @param u The owner of the directory
	 * @param d The container directory
	 * @return The found files
	 */
	public static File[] getFilesContained(User u, Directory d) {
		GridFS gfs = new GridFS(DataBase.getInstanceDb(), "files");
		
		ArrayList<File> files = new ArrayList<File>();

		BasicDBObject query = new BasicDBObject();
		query.put("owner", new ObjectId(u.UID));
		if(d.UID != null) // Root case
			query.put("parent", new ObjectId(d.UID));
		else
			query.put("parent", null);
		
		for(GridFSDBFile file : gfs.find(query)) {
			files.add(new File(file));
		}
		
		return files.toArray(new File[0]);
	}
	
	/**
	 * Get a data input stream for the given file
	 * @param f The file
	 * @return An input stream to the file, or null if the stream could not be created
	 */
	public static DataInputStream getFileData(File f) {
		GridFS gfs = new GridFS(DataBase.getInstanceDb(), "files");
		
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(f.UID));

		GridFSDBFile gfsFile = gfs.findOne(query);
		
		if(gfsFile == null)
			return null;
		else
			return  new DataInputStream(new BufferedInputStream(gfsFile.getInputStream()));
	}
	
	/**
	 * 
	 * @param u
	 * @param path
	 * @param name
	 * @return
	 */
	public static File getFile(User u, List<String> path, String filename) {
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("files.files");
		
		BasicDBObject query = new BasicDBObject();
		query.put("owner", new ObjectId(u.UID));
		query.put("filename", filename);
		
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
			return new File(d);
	}
	
	/**
	 * Create a file using the given informations
	 * @param u The user.
	 * @param d The directory containing the file.
	 * @param name
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static File createFile(User u, Directory d, String name, java.io.File file) throws Exception {

		if(!file.exists())
			return null;
		
		String mimeType = Files.probeContentType(Paths.get(file.getAbsolutePath()));
		InputStream in = new FileInputStream(file);
		
		GridFS gfs = new GridFS(DataBase.getInstanceDb(), "files");
		GridFSInputFile gfsFile = gfs.createFile(in);
		Date now = new Date();	

		gfsFile.setFilename(name);
		gfsFile.setContentType(mimeType);
		gfsFile.put("owner", new ObjectId(u.UID));
		gfsFile.put("path", d.name == null ? "/" : d.path + d.name + "/");
		gfsFile.put("creation", now);
		gfsFile.put("edit", now);
		gfsFile.put("parent", d.UID == null ? null : new ObjectId(d.UID));
		
		gfsFile.save();
		in.close();
		
		return new File(gfsFile);
	}

	/**
	 * Create the file using the given informations
	 * @param u The owner of the file
	 * @param d The containing directory
	 * @param filename The filename
	 * @param fileBase64 The file content, encoded in base 64
	 * @return The created file
	 */
	public static File createFileBase64(User u, Directory d, String filename, String fileBase64) throws Exception {

		String mimeType = Files.probeContentType(Paths.get(filename));

		GridFS gfs = new GridFS(DataBase.getInstanceDb(), "files");
		GridFSInputFile gfsFile = gfs.createFile(DatatypeConverter.parseBase64Binary(fileBase64));
		Date now = new Date();	

		gfsFile.setFilename(filename);
		gfsFile.setContentType(mimeType);
		gfsFile.put("owner", new ObjectId(u.UID));
		gfsFile.put("path", d.name == null ? "/" : d.path + d.name + "/");
		gfsFile.put("creation", now);
		gfsFile.put("edit", now);
		gfsFile.put("parent", d.UID == null ? null : new ObjectId(d.UID));
		
		gfsFile.save();

		return new File(gfsFile);
	}
	
	/**
	 * Return the parent directory of a file
	 * @param u The user
	 * @param f The file
	 * @return The parent directory
	 */
	public static Directory getParentDirectory(User u, File f) {
		return DirectoryUtils.getDirectory(u, f.parentUID);
	}
	
	/**
	 * Delete a file
	 * @param u The owner of the file
	 * @param f The file to delete
	 * @return The containing directory
	 */
	public static Directory deleteFile(User u, File f) {
		Directory d = DirectoryUtils.getDirectory(u, f.parentUID);
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("files.files");
		
		BasicDBObject query = new BasicDBObject();
		query.put("owner", new ObjectId(u.UID));
		query.put("_id", new ObjectId(f.UID));

		if(collection.deleteOne(query).wasAcknowledged()) {
			return d;
		} else {
			return null;
		}
	}
	
	/**
	 * Update the name of the given file
	 * @param u The owner
	 * @param f The file to rename
	 * @param newName The new name
	 * @return The renamed file
	 */
	public static File renameFile(User u, File f, String newName) {
		System.out.println(newName);
		
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("files.files");

		// Change name
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(f.UID));
		query.put("owner", new ObjectId(u.UID));
		
		BasicDBObject update = new BasicDBObject();
		update.append("$set", new BasicDBObject().append("filename", newName));
		
		collection.updateOne(query, update);

		// Update local version
		f.name = newName;

		return f;
	}
	
	/**
	 * Move the given file
	 * @param u The owner
	 * @param f The file to move
	 * @param newContainer The new container of the file
	 * @return The updated file
	 */
	public static File moveFile(User u, File f, Directory newContainer) {
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("files.files");

		// Change path
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(f.UID));
		query.put("owner", new ObjectId(u.UID));
		
		String newPath = newContainer.name == null ? "/" : newContainer.path + newContainer.name + "/";
		
		BasicDBObject update = new BasicDBObject();
		update.append("$set", new BasicDBObject().append("path", newPath).append("parent", newContainer.UID == null ? null : new ObjectId(newContainer.UID)));
		
		collection.updateOne(query, update);

		// Update local version
		f.path = newPath;
		f.parentUID = newContainer.UID;

		return f;
	}
	
	public static List<File> searchFiles(User u, String regex, Directory container) {
		return searchFiles(u, regex, container, false, SortType.NAME_ASC);
	}
	
	/**
	 * Return the list of found files
	 * @param u
	 * @param regex
	 * @param container
	 * @return
	 */
	public static List<File> searchFiles(User u, String regex, Directory container, boolean recursive, SortType sort) {
		MongoCollection<Document> collection = DataBase.getInstance().getCollection("files.files");
		ArrayList<File> files = new ArrayList<File>();
		
		BasicDBObject query = new BasicDBObject();
		query.put("owner", new ObjectId(u.UID));
		query.put("filename", new BasicDBObject("$regex", regex));
		if(recursive)
			query.put("path", new BasicDBObject("$regex", "^" + container.getInnerPath() ));
		else
			query.put("path", new BasicDBObject("$regex", "^" + container.getInnerPath() + "$" ));
		
		FindIterable<Document> results = collection.find(query);
		
		// Sort (from mongodb)
		switch(sort) {
		case SIZE_ASC:
			results = results.sort(new BasicDBObject("size", 1));
			break;
		case SIZE_DSC:
			results = results.sort(new BasicDBObject("size", -1));
			break;
		case NONE:
		default:
			break;
		}
		
		for(Document d : results) {
			files.add(new File(d));
		}

		// Sort (not handled by mongodb)
		switch(sort) {
		case NAME_ASC:
			files.sort(new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					return String.CASE_INSENSITIVE_ORDER.compare(f1.name, f2.name);
				}
			});
			break;
		case NAME_DSC:
			files.sort(new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					return String.CASE_INSENSITIVE_ORDER.compare(f2.name, f1.name);
				}
			});
			break;
		default:
			break;
		}
		
		return files;
	}
}
