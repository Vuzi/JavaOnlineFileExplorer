package fr.vuzi.fileexplorer.api.action.front;

import fr.vuzi.webframework.action.AActionNoCredentials;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionGetResourceHeader extends AActionNoCredentials {
	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionGetResourceHeader();
		action.setActionContext(context);

		return action;
	}

	@Override
	public void proceed() throws Exception {
	}
}
