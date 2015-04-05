package fr.vuzi.fileexplorer.api.action;

import fr.vuzi.fileexplorer.database.DataBase;
import fr.vuzi.fileexplorer.database.directory.Directory;
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
		IContext c = getActionContext();
		User u = (User) c.getSessionAttribute("user");
		
		Directory d = DataBase.getDirectory(u, c.getParameterUnique("path"));
	
		if(d == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No directory found for '/" + c.getParameterUnique("path") + "'")));
		} else {
			d.directories = DataBase.getDirectoriesContained(u, d);
			d.files = DataBase.getFilesContained(u, d);
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
