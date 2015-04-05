package fr.vuzi.fileexplorer.api.action;

import fr.vuzi.fileexplorer.database.DataBase;
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
		DataBase.getDirectory(null, getActionContext().getParameterUnique("path"));
	}

	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionDirShow();
		action.setActionContext(context);
		
		return action;
	}

}
