package fr.vuzi.fileexplorer.database.file;

import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.gridfs.GridFSFile;
import com.mongodb.gridfs.GridFSInputFile;

public class File {

	/**
	 * Directory ID
	 */
	public String UID;
	
	/**
	 * Name of the file
	 */
	public String name;
	
	/**
	 * Directory path, null if at root directory
	 */
	public String path;
	
	/**
	 * Creation (upload) date
	 */
	public Date creationDate;
		
	/**
	 * Modification date
	 */
	public Date modificationDate; 

	/**
	 * Mime type of the file
	 */
	public String type;
	/**
	 * Size of the file, in bytes
	 */
	public long size;
	
	/**
	 * Parent directory ID of the file
	 */
	public String parentUID;
	
	/**
	 * Shared UID
	 */
	public String shared;

	/**
	 * Empty constructor
	 */
	public File() {
	}

	/**
	 * Constructor
	 * @param gfsFile The gfsFile of the file
	 */
	public File(GridFSInputFile gfsFile) {
		this.UID = gfsFile.get("_id").toString();
		this.name = gfsFile.getFilename();
		this.type = gfsFile.getContentType();
		this.path = (String) gfsFile.get("path");
		this.creationDate = (Date) gfsFile.get("creation");
		this.modificationDate = (Date) gfsFile.get("edit");
		this.size = gfsFile.getLength();
		this.shared = (String) gfsFile.get("shared");
		
		ObjectId parent = (ObjectId) gfsFile.get("parent");
		if(parent != null)
			this.parentUID = parent.toString();
	}

	public File(GridFSFile gfsFile) {
		this.UID = gfsFile.get("_id").toString();
		this.name = gfsFile.get("filename") != null ? gfsFile.get("filename").toString() : "(null)";
		this.type = gfsFile.getContentType();
		this.path = (String) gfsFile.get("path");
		this.creationDate = (Date) gfsFile.get("creation");
		this.modificationDate = (Date) gfsFile.get("edit");
		this.size = (Long)gfsFile.get("length");
		this.shared = (String) gfsFile.get("shared");
		if(gfsFile.get("parent") != null)
			this.parentUID = gfsFile.get("parent").toString();
	}

	public File(Document d) {
		this.UID = d.getObjectId("_id").toString();
		this.name = d.getString("filename");
		this.type = d.getString("contentType");
		this.path = d.getString("path");
		this.creationDate = d.getDate("creation");
		this.modificationDate = d.getDate("edit");
		this.size = d.getLong("length");
		this.shared = d.getString("shared");
		if(d.getObjectId("parent") != null)
			this.parentUID = d.getObjectId("parent").toString();
	}
}
