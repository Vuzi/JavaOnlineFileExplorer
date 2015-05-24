package fr.vuzi.fileexplorer.api.action.front;

import fr.vuzi.webframework.action.AActionNoCredentials;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionIdentification extends AActionNoCredentials {

	@Override
	public IAction cloneAction(IContext context) {
		ActionIdentification action = new ActionIdentification();
		action.setActionContext(context);
		return action;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void proceed() throws Exception {
		
	}
}
