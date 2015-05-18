package fr.vuzi.fileexplorer.api.action.directory;

import java.util.ArrayList;
import java.util.List;

import fr.vuzi.fileexplorer.database.directory.Directory;
import fr.vuzi.fileexplorer.database.directory.DirectoryUtils;
import fr.vuzi.fileexplorer.database.file.File;
import fr.vuzi.fileexplorer.database.file.FileUtils;
import fr.vuzi.fileexplorer.database.user.User;
import fr.vuzi.fileexplorer.message.ErrorMessage;
import fr.vuzi.fileexplorer.message.GenericMessage;
import fr.vuzi.webframework.action.AAction;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionDirModification extends AAction {

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
		User u = (User) c.getSessionAttribute("user");
		
		if(action == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : No action found for '/" + c.getParameterUnique("_path") + "'")));
			c.getResponse().setStatus(400);
			return;
		}
		
		switch(action) {
		case "delete":
		case "DELETE" :
			deleteAction(c, u);
			break;
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

	private void createAction(IContext c, User u) {
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("_path"));
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;
		
		// Test for root directory
		if(name == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Root directory always exists")));
			c.setStatus(400);
			return;
		}
		
		// Test that the parent directory exist
		Directory d = DirectoryUtils.getDirectoryParent(u, path, name);
		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Parent directory of '/" + c.getParameterUnique("_path") + "' does not exist")));
			c.setStatus(400);
			return;
		}
		
		// Test that the directory doesn't exist
		d = DirectoryUtils.getDirectory(u, path, name);
		if(d != null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Directory '/" + c.getParameterUnique("_path") + "' already exists")));
			c.setStatus(400);
			return;
		}
		
		d = DirectoryUtils.createDirectory(u, path, name.trim());
		c.setAttribute("model", new GenericMessage(d));
	}
	
	private void deleteAction(IContext c, User u) {
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("_path"));
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;
		
		// Test for root directory
		if(name == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Root directory can't be deleted")));
			c.setStatus(400);
			return;
		}
		
		// Test that the directory exist
		Directory d = DirectoryUtils.getDirectory(u, path, name);
		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Directory '/" + c.getParameterUnique("_path") + "' does not exist")));
			c.setStatus(400);
			return;
		}

		// Test if empty
		Directory[] directories = DirectoryUtils.getDirectoriesContained(u, d);
		File[] files = FileUtils.getFilesContained(u, d);
		
		if(files.length > 0 || directories.length > 0) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Directory '/" + c.getParameterUnique("_path") + "' is not empty")));
			c.setStatus(400);
			return;
		}
 		
		d = DirectoryUtils.deleteDirectory(u, d);
		d.directories = DirectoryUtils.getDirectoriesContained(u, d);
		d.files = FileUtils.getFilesContained(u, d);
		c.setAttribute("model", new GenericMessage(d));
	}
	
	private void renameAction(IContext c, User u) {
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("_path"));
		String newName = c.getParameterUnique("name");
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;

		// Test name
		if(newName == null || newName.length() <= 0 || newName.contains("/")) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : No valid new name given")));
			c.setStatus(400);
			return;
		}
		
		// Test for root directory
		if(name == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Root directory can't be renamed")));
			c.setStatus(400);
			return;
		}

		// Test the name
		if(DirectoryUtils.getDirectory(u, new ArrayList<String>(path), newName) != null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Directory '" + newName + "' already exist")));
			c.setStatus(400);
			return;
		}
		
		// Test that the directory exist
		Directory d = DirectoryUtils.getDirectory(u, path, name);
		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : Directory '/" + c.getParameterUnique("_path") + "' does not exist")));
			c.setStatus(404);
			return;
		}
		
		d = DirectoryUtils.renameDirectory(u, d, newName.trim());
		d.directories = DirectoryUtils.getDirectoriesContained(u, d);
		d.files = FileUtils.getFilesContained(u, d);
		c.setAttribute("model", new GenericMessage(d));
	}
	
	/**
	 * Change the directory of the directory
	 * @param c The action's context
	 * @param u The current user
	 */
	private void moveAction(IContext c, User u) {
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("_path"));
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;
		List<String> newPath = DirectoryUtils.getPath(c.getParameterUnique("path"));
		//String newName = newPath.size() > 0 ? newPath.remove(newPath.size() - 1) : null;
		
		Directory dir = DirectoryUtils.getDirectory(u, path, name);
		Directory newDir = DirectoryUtils.getDirectory(u, newPath, name);
		Directory container = DirectoryUtils.getDirectory(u, new ArrayList<String>(newPath));
		
		if(dir == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No directory '" + name + "' found")));
			c.setStatus(404);
			return;	
		}
		
		if(container == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No directory '" + c.getParameterUnique("path") + "' found")));
			c.setStatus(404);
			return;	
		}
		
		if(container.getInnerPath().startsWith(dir.getInnerPath())) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(400, "Error : Could not move directory inside itself")));
			c.setStatus(400);
			return;	
		}
		
		if(newDir != null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : A directory named '" + name + "' in '" + c.getParameterUnique("path") +  "' already exist")));
			c.setStatus(400);
			return;	
		}
		
		c.setAttribute("model", new GenericMessage(DirectoryUtils.moveDirectory(u, dir, container)));
	}
	
	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionDirModification();
		action.setActionContext(context);
		
		return action;
	}

}
