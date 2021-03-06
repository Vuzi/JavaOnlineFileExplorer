package fr.vuzi.fileexplorer.api.action.file;

import fr.vuzi.webframework.action.AAction;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

/**
 * File modification action
 * 
 * @author Vuzi
 *
 */
public class ActionFileDeletion extends AAction {

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
		getActionContext().setParamater("action", new String[]{ "DELETE" });
		
		ActionFileModification action = new ActionFileModification();
		action.setActionContext(getActionContext());
		action.proceed();
	}

	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionFileDeletion();
		action.setActionContext(context);
		
		return action;
	}

}
