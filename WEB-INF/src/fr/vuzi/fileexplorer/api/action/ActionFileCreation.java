package fr.vuzi.fileexplorer.api.action;

import java.io.File;
import java.util.List;

import fr.vuzi.fileexplorer.database.directory.Directory;
import fr.vuzi.fileexplorer.database.directory.DirectoryUtils;
import fr.vuzi.fileexplorer.database.file.FileUtils;
import fr.vuzi.fileexplorer.database.user.User;
import fr.vuzi.fileexplorer.message.ErrorMessage;
import fr.vuzi.fileexplorer.message.GenericMessage;
import fr.vuzi.webframework.action.AAction;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionFileCreation extends AAction {

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public String[] getCredentials() {
		return new String[] { "user" };
	}
	
	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionFileCreation();
		action.setActionContext(context);
		
		return action;
	}

	@Override
	public void proceed() throws Exception {
		IContext c = getActionContext();
		User u = (User) c.getSessionAttribute("user");
		File[] files = c.getUploadedFiles();
		
		if(files.length <= 0) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(400, "Error : No file uploaded")));
			return;
		}
		
		// TODO
		File f = files[0];
		
		// Directory & file info
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("path"));
		String filename = path.size() > 0 ? path.remove(path.size() - 1) : null;
		String dirname = path.size() > 0 ? path.remove(path.size() - 1) : null;
		
		// Directory retrieving
		Directory d = DirectoryUtils.getDirectory(u, path, dirname);
		
		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(400, "Error : No directory to upload the file to")));
			return;	
		}
		
		// Create the file
		fr.vuzi.fileexplorer.database.file.File file = FileUtils.createFile(u, d, filename, f);

		if(file == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(400, "Error : Couldn't create the file " + filename)));
			return;	
		}
		
		c.setAttribute("model", file);
	}
}
