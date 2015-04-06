package fr.vuzi.fileexplorer.database.file;

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
		// TODO
		return new File[0];
	}
}
