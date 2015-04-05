package fr.vuzi.fileexplorer.database.file;

import java.util.Date;

import org.bson.types.ObjectId;

public class File {

	/**
	 * Name of the file
	 */
	public String name;
	
	/**
	 * Creation (upload) date
	 */
	public Date creationDate;
		
	/**
	 * Modification date
	 */
	public Date modificationDate; 
	
	/**
	 * Size of the file, in bytes
	 */
	public int size;
	
	/**
	 * Parent directory ID of the file
	 */
	public ObjectId parent;
	
	/**
	 * Filename used on the server side
	 */
	public String serverFilename;

	/**
	 * Constructor
	 * @param name
	 * @param creationDate
	 * @param modificationDate
	 * @param size
	 * @param parent
	 * @param serverFilename
	 */
	public File(String name, Date creationDate, Date modificationDate, int size, ObjectId parent, String serverFilename) {
		this.name = name;
		this.creationDate = creationDate;
		this.modificationDate = modificationDate;
		this.size = size;
		this.parent = parent;
		this.serverFilename = serverFilename;
	}

	/**
	 * Empty constructor
	 */
	public File() {
	}
	
}
