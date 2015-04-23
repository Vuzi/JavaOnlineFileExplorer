package fr.vuzi.fileexplorer.database.file;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.mongodb.gridfs.GridFSInputFile;

import fr.vuzi.fileexplorer.database.DataBase;
import fr.vuzi.fileexplorer.database.directory.Directory;
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

		GridFS gfs = new GridFS(DataBase.getInstanceDb(), "files");
		
		BasicDBObject query = new BasicDBObject();
		query.put("owner", new ObjectId(u.UID));
		query.put("filename", filename);
		
		if(path.isEmpty()) {
			query.put("path", null);
		} else {
			String pathRegex = "^/" + String.join("/", path) + "/$";
			query.put("path", new BasicDBObject("$regex", pathRegex));
		}

		GridFSFile gfsFile = gfs.findOne(query);
		
		if(gfsFile == null)
			return null;
		else
			return new File(gfsFile);
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
		gfsFile.put("path", (d.path == null ? "/" : d.path) + d.name + "/");
		gfsFile.put("creation", now);
		gfsFile.put("edit", now);
		gfsFile.put("parent", new ObjectId(d.UID));
		
		gfsFile.save();
		in.close();
		
		return new File(gfsFile);
	}
}
