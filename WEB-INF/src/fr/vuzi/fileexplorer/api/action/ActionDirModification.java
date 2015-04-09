package fr.vuzi.fileexplorer.api.action;

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
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : No action found for '/" + c.getParameterUnique("path") + "'")));
			return;
		}
		
		switch(action) {
		case "delete":
		case "DELETE" :
			System.out.println("Deletion of /" + c.getParameterUnique("path"));
			deleteAction(c, u);
			break;
		case "create" :
		case "CREATE" :
			System.out.println("Creation of /" + c.getParameterUnique("path"));
			createAction(c, u);
			break;
		case "rename" :
		case "RENAME" :
			System.out.println("Renaming of /" + c.getParameterUnique("path"));
			renameAction(c, u);
			break;
		default:
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Unkown action '" + action + "' for '/" + c.getParameterUnique("path") + "'")));
		}
	}

	private void createAction(IContext c, User u) {
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("path"));
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;
		
		// Test for root directory
		if(name == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Root directory always exists")));
			return;
		}
		
		// Test that the parent directory exist
		Directory d = DirectoryUtils.getDirectoryParent(u, path, name);
		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Parent directory of '/" + c.getParameterUnique("path") + "' does not exist")));
			return;
		}
		
		// Test that the directory doesn't exist
		d = DirectoryUtils.getDirectory(u, path, name);
		if(d != null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Directory '/" + c.getParameterUnique("path") + "' already exists")));
			return;
		}
		
		d = DirectoryUtils.createDirectory(u, path, name.trim());
		c.setAttribute("model", new GenericMessage(d));
	}
	
	private void deleteAction(IContext c, User u) {
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("path"));
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;
		
		// Test for root directory
		if(name == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Root directory can't be deleted")));
			return;
		}
		
		// Test that the directory exist
		Directory d = DirectoryUtils.getDirectory(u, path, name);
		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Directory '/" + c.getParameterUnique("path") + "' does not exist")));
			return;
		}

		// Test if empty
		Directory[] directories = DirectoryUtils.getDirectoriesContained(u, d);
		File[] files = FileUtils.getFilesContained(u, d);
		
		if(files.length > 0 || directories.length > 0) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Directory '/" + c.getParameterUnique("path") + "' is not empty")));
			return;
		}
 		
		d = DirectoryUtils.deleteDirectory(u, d);
		d.directories = DirectoryUtils.getDirectoriesContained(u, d);
		d.files = FileUtils.getFilesContained(u, d);
		c.setAttribute("model", new GenericMessage(d));
	}
	
	private void renameAction(IContext c, User u) {
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("path"));
		String newName = c.getParameterUnique("name");
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;

		// Test name
		if(newName == null || newName.length() <= 0 || newName.contains("/")) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : No valid new name given")));
			return;
		}
		
		// Test for root directory
		if(name == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Root directory can't be renamed")));
			return;
		}
		
		// Test that the directory exist
		Directory d = DirectoryUtils.getDirectory(u, path, name);
		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Directory '/" + c.getParameterUnique("path") + "' does not exist")));
			return;
		}
		
		d = DirectoryUtils.renameDirectory(u, d, newName.trim());
		d.directories = DirectoryUtils.getDirectoriesContained(u, d);
		d.files = FileUtils.getFilesContained(u, d);
		c.setAttribute("model", new GenericMessage(d));
	}

	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionDirModification();
		action.setActionContext(context);
		
		return action;
	}

}