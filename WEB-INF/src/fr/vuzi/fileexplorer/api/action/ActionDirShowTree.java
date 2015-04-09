package fr.vuzi.fileexplorer.api.action;

import fr.vuzi.fileexplorer.database.directory.Directory;
import fr.vuzi.fileexplorer.database.directory.DirectoryUtils;
import fr.vuzi.fileexplorer.database.user.User;
import fr.vuzi.fileexplorer.message.GenericMessage;
import fr.vuzi.webframework.action.AAction;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionDirShowTree extends AAction {

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
		
		// Directory retrieving
		Directory d = DirectoryUtils.getDirectoryTree(u);

		c.setAttribute("model", new GenericMessage(d));	
	}

	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionDirShowTree();
		action.setActionContext(context);
		
		return action;
	}

}
