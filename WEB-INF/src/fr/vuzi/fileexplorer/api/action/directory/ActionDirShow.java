package fr.vuzi.fileexplorer.api.action.directory;

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

public class ActionDirShow extends AAction {

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
		User u = (User) c.getSessionAttribute("user");
		
		// Directory info
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("path"));
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;
		
		// Directory retrieving
		Directory d = DirectoryUtils.getDirectory(u, path, name);
	
		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No directory found for '/" + c.getParameterUnique("path") + "'")));
		} else {
			d.directories = DirectoryUtils.getDirectoriesContained(u, d);
			d.files = FileUtils.getFilesContained(u, d);
			c.setAttribute("model", new GenericMessage(d));
		}
	}

	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionDirShow();
		action.setActionContext(context);
		
		return action;
	}

}
