package fr.vuzi.fileexplorer.api.action.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import fr.vuzi.fileexplorer.database.directory.Directory;
import fr.vuzi.fileexplorer.database.directory.DirectoryUtils;
import fr.vuzi.fileexplorer.database.file.FileUtils;
import fr.vuzi.fileexplorer.database.user.User;
import fr.vuzi.fileexplorer.message.ErrorMessage;
import fr.vuzi.fileexplorer.message.GenericMessage;
import fr.vuzi.webframework.action.AAction;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionFileUpload extends AAction {

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
		IAction action = new ActionFileUpload();
		action.setActionContext(context);
		
		return action;
	}

	@Override
	public void proceed() throws Exception {
		IContext c = getActionContext();
		User u = (User) c.getSessionAttribute("user");
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("path"));
		
		// Directory retrieving
		Directory d = DirectoryUtils.getDirectory(u, new ArrayList<String>(path));

		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No directory to upload the file to")));
			c.setStatus(404);
			return;	
		}
		
		List<fr.vuzi.fileexplorer.database.file.File> files = new ArrayList<fr.vuzi.fileexplorer.database.file.File>();
		
		for(Entry<String, File> entry : c.getUploadedFiles().entrySet()) {
			File f = entry.getValue();
			String filename = entry.getKey();
			
			// Test if file already exist
			if(FileUtils.getFile(u, path, filename) != null) {
				c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : File '" + filename + "' already exists")));
				c.setStatus(400);
				return;	
			}
			
			// Create the file
			fr.vuzi.fileexplorer.database.file.File file = FileUtils.createFile(u, d, filename, f);

			if(file == null) {
				c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Couldn't create the file " + filename)));
				c.setStatus(400);
				return;	
			}
			
			files.add(file);
		}
		
		if(files.size() == 0) {
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : No file uploaded")));
			c.setStatus(400);
		} else if(files.size() == 1) {
			c.setAttribute("model", files.get(0));
		} else {
			c.setAttribute("model", files.toArray(new fr.vuzi.fileexplorer.database.file.File[0]));
		}
	}
}
