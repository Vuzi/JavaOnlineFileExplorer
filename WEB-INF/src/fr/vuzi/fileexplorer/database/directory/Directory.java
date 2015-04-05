package fr.vuzi.fileexplorer.database.directory;

import java.util.Date;

import org.bson.types.ObjectId;

import fr.vuzi.fileexplorer.database.file.File;

public class Directory {

	/**
	 * Name of the directory
	 */
	public String name;
	
	/**
	 * Last modification date
	 */
	public Date lastModification;
	
	/**
	 * Parent ID, if null this directory is a root directory
	 */
	public ObjectId parent;
	
	/**
	 * List of the contained directories' names
	 */
	public Directory[] directories;
	
	/**
	 * List of the contained files
	 */
	public File[] files;
}
