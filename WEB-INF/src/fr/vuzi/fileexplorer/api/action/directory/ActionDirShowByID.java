package fr.vuzi.fileexplorer.api.action.directory;

import fr.vuzi.fileexplorer.database.directory.Directory;
import fr.vuzi.fileexplorer.database.directory.DirectoryUtils;
import fr.vuzi.fileexplorer.database.file.FileUtils;
import fr.vuzi.fileexplorer.database.user.User;
import fr.vuzi.fileexplorer.database.user.UserUtils;
import fr.vuzi.fileexplorer.message.ErrorMessage;
import fr.vuzi.fileexplorer.message.GenericMessage;
import fr.vuzi.webframework.action.AAction;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionDirShowByID extends AAction {

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
		// Context
		IContext c = getActionContext();
		User u = UserUtils.getSessionUser(c);
		
		// Directory info
		String id = c.getParameterUnique("id");
		
		// Directory retrieving
		Directory d = DirectoryUtils.getDirectory(u, id);
	
		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No directory found for '" + id + "'")));
			c.setStatus(404);
		} else {
			d.directories = DirectoryUtils.getDirectoriesContained(u, d);
			d.files = FileUtils.getFilesContained(u, d);
			c.setAttribute("model", new GenericMessage(d));
		}
	}

	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionDirShowByID();
		action.setActionContext(context);
		
		return action;
	}

}
