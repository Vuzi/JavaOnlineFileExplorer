package fr.vuzi.fileexplorer.api.action.user;

import fr.vuzi.fileexplorer.message.GenericMessage;
import fr.vuzi.webframework.action.AActionNoCredentials;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionLogout extends AActionNoCredentials {

	@Override
	public IAction cloneAction(IContext context) {
		ActionLogout action = new ActionLogout();
		action.setActionContext(context);
		return action;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void proceed() throws Exception {
		// Reset session
		getActionContext().resetSession();
		getActionContext().setAttribute("model", new GenericMessage("You are now disconnected"));
	}

}
