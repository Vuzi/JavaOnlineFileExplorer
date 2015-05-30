package fr.vuzi.fileexplorer.api.action.file;

import java.util.ArrayList;
import java.util.List;

import fr.vuzi.fileexplorer.database.directory.Directory;
import fr.vuzi.fileexplorer.database.directory.DirectoryUtils;
import fr.vuzi.fileexplorer.database.file.File;
import fr.vuzi.fileexplorer.database.file.FileUtils;
import fr.vuzi.fileexplorer.database.user.User;
import fr.vuzi.fileexplorer.database.user.UserUtils;
import fr.vuzi.fileexplorer.message.ErrorMessage;
import fr.vuzi.fileexplorer.message.GenericMessage;
import fr.vuzi.webframework.action.AAction;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

/**
 * File modification action
 * 
 * @author Vuzi
 *
 */
public class ActionFileModification extends AAction {

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public String[] getCredentials() {
		return new String[] { "user" };
	}

	@Override
	public void proceed() throws Exception {
		IContext c = getActionContext();
		String action = c.getParameterUnique("action");
		User u = UserUtils.getSessionUser(c);
		
		if(action == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : No action found for '/" + c.getParameterUnique("_path") + "'")));
			c.setStatus(400);
			return;
		}
		
		switch(action) {
		case "delete":
		case "DELETE" :
			deleteAction(c, u);
			break;
		case "upload" :
		case "UPLOAD" :
		case "create" :
		case "CREATE" :
			createAction(c, u);
			break;
		case "rename" :
		case "RENAME" :
			renameAction(c, u);
			break;
		case "move" :
		case "MOVE" :
			moveAction(c, u);
			break;
		default:
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Unkown action '" + action + "' for '/" + c.getParameterUnique("_path") + "'")));
			c.setStatus(400);
		}
	}

	/**
	 * Create a file using the base64 content given in 'data'
	 * @param c The action's context
	 * @param u The current user
	 * @throws Exception 
	 */
	private void createAction(IContext c, User u) throws Exception {
		
		// Test if data is sent with the file
		String fileBase64 = c.getParameterUnique("data");
		
		if(fileBase64 == null || fileBase64.isEmpty()) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : No file to upload")));
			c.setStatus(400);
			return;	
		}
		
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("_path"));
		String filename = path.size() > 0 ? path.remove(path.size() - 1) : null;
		
		// Directory retrieving
		Directory d = DirectoryUtils.getDirectory(u, new ArrayList<String>(path));
		
		// Test for parent directory
		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No directory to upload the file to")));
			c.setStatus(404);
			return;	
		}

		// Test if file already exist
		if(FileUtils.getFile(u, path, filename) != null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : File '" + filename + "' already exists")));
			c.setStatus(400);
			return;	
		}

		// Create the file
		File file = FileUtils.createFileBase64(u, d, filename, fileBase64);
		c.setAttribute("model", new GenericMessage(file));
	}
	
	/**
	 * Delete the given file
	 * @param c The action's context
	 * @param u The current user
	 */
	private void deleteAction(IContext c, User u) {
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("_path"));
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;

		File file = FileUtils.getFile(u, path, name);

		if(file == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No file '" + name + "' found")));
			c.setStatus(404);
			return;	
		}

		Directory d = FileUtils.deleteFile(u, file);
		
		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : The file '" + name + "' could not be deleted")));
			c.setStatus(404);
			return;	
		}
		
		d.directories = DirectoryUtils.getDirectoriesContained(u, d);
		d.files = FileUtils.getFilesContained(u, d);
		c.setAttribute("model", new GenericMessage(d));
	}
	
	/**
	 * Rename the given file
	 * @param c The action's context
	 * @param u The current user
	 */
	private void renameAction(IContext c, User u) {
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("_path"));
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;
		String newName = c.getParameterUnique("name");
		
		if(newName == null || newName.contains("/\\'\"")) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : No valid name provided")));
			c.setStatus(400);
			return;	
		}

		File file = FileUtils.getFile(u, path, name);
		File newFile = FileUtils.getFile(u, path, newName);

		if(file == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No file '" + name + "' found")));
			c.setStatus(404);
			return;	
		}
		
		if(newFile != null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : A file named '" + name + "' already exist")));
			c.setStatus(400);
			return;	
		}

		c.setAttribute("model", new GenericMessage(FileUtils.renameFile(u, file, newName)));
	}
	
	/**
	 * Change the directory of the file
	 * @param c The action's context
	 * @param u The current user
	 */
	private void moveAction(IContext c, User u) {
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("_path"));
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;
		List<String> newPath = DirectoryUtils.getPath(c.getParameterUnique("path"));
		String dirName = newPath.size() > 0 ? newPath.get(newPath.size() - 1) : null;
		
		File file = FileUtils.getFile(u, path, name);
		File newFile = FileUtils.getFile(u, newPath, name);
		Directory container = DirectoryUtils.getDirectory(u, newPath);

		if(file == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No file '" + name + "' found")));
			c.setStatus(404);
			return;	
		}
		
		if(container == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No directory '" + c.getParameterUnique("path") + "' found")));
			c.setStatus(404);
			return;	
		}
		
		if(newFile != null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : A file named '" + name + "' in '" + c.getParameterUnique("path") +  "' already exist")));
			c.setStatus(400);
			return;	
		}

		c.setAttribute("model", new GenericMessage(FileUtils.moveFile(u, file, container)));
	}

	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionFileModification();
		action.setActionContext(context);
		
		return action;
	}

}
