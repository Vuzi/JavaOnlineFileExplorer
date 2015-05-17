package fr.vuzi.fileexplorer.api.action.front;

import fr.vuzi.fileexplorer.message.GenericMessage;
import fr.vuzi.webframework.action.AActionNoCredentials;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionFileExplorer extends AActionNoCredentials {

	@Override
	public IAction cloneAction(IContext context) {
		ActionFileExplorer action = new ActionFileExplorer();
		action.setActionContext(context);
		return action;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void proceed() throws Exception {
		IContext c = getActionContext();
		c.setAttribute("model", new GenericMessage(c.getParameterUnique("_path")));
	}
}
