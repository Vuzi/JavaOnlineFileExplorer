package fr.vuzi.fileexplorer.database.directory;

import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import fr.vuzi.fileexplorer.database.file.File;

/**
 * Directory. If root directory, name, path, edit and creation will be set to null
 * values because the root directory have no name, no path and no dates
 * 
 * @author Vuzi
 *
 */
public class Directory {

	public Directory(Document d) {
		this.UID = d.getObjectId("_id").toString();
		
		ObjectId parent = d.getObjectId("parent");
		if(parent != null)
			this.parentUID = parent.toString();
		
		this.name = d.getString("name");
		this.edit = d.getDate("edit");
		this.creation = d.getDate("creation");
		this.path = d.getString("path");
		this.pathList = DirectoryUtils.getPath(path);
		
		if(path == null)
			this.path = "/";
	}
	
	public Directory() {
		this.path = "/";
	}

	/**
	 * Directory ID
	 */
	public String UID;
	
	/**
	 * Parent ID
	 */
	public String parentUID;

	/**
	 * Name of the directory
	 */
	public String name;

	/**
	 * Last modification date
	 */
	public Date edit;
	
	/**
	 * Creation date
	 */
	public Date creation;
	
	/**
	 * Directory path, null if at root directory
	 */
	public String path;
	
	/**
	 * List of the contained directories' names
	 */
	public Directory[] directories;
	
	/**
	 * List of the contained files
	 */
	public File[] files;
	
	private List<String> pathList;
	
	public String getInnerPath() {
		if(UID == null)
			return "/";
		else
			return path + name + "/";
	}
	
	public List<String> getPathList() {
		if(pathList == null)
			pathList = DirectoryUtils.getPath(path);
		
		return pathList;
	}
}
