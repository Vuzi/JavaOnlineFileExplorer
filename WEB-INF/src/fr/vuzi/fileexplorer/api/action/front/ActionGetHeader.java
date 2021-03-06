package fr.vuzi.fileexplorer.api.action.front;

import fr.vuzi.webframework.action.AActionNoCredentials;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionGetHeader extends AActionNoCredentials {
	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionGetHeader();
		action.setActionContext(context);

		return action;
	}

	@Override
	public void proceed() throws Exception {
	}
}
