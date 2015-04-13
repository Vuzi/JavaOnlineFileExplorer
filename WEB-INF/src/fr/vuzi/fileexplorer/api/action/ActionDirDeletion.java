package fr.vuzi.fileexplorer.api.action;

import fr.vuzi.webframework.action.AAction;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionDirDeletion extends AAction {

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public String[] getCredentials() {
		return new String[] { "user" };
	}
	
	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionDirDeletion();
		action.setActionContext(context);
		
		return action;
	}

	@Override
	public void proceed() throws Exception {
		getActionContext().setParamater("action", new String[]{ "DELETE" });
		
		ActionDirModification action = new ActionDirModification();
		action.setActionContext(getActionContext());
		action.proceed();
	}
}
