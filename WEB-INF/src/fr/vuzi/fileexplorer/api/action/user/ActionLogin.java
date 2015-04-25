package fr.vuzi.fileexplorer.api.action.user;

import fr.vuzi.fileexplorer.database.user.User;
import fr.vuzi.fileexplorer.database.user.UserUtils;
import fr.vuzi.fileexplorer.message.ErrorMessage;
import fr.vuzi.fileexplorer.message.GenericMessage;
import fr.vuzi.webframework.action.AActionNoCredentials;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionLogin extends AActionNoCredentials {

	@Override
	public IAction cloneAction(IContext context) {
		ActionLogin action = new ActionLogin();
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
		String login = c.getParameterUnique("login");
		String pass = c.getParameterUnique("password");
		
		if(login != null && login.length() > 0 && pass != null && pass.length() > 0) {
			User user = UserUtils.getUser(login, pass);
			if(user != null) {
				// Save in the session and send the connected user in the response
				c.setSessionAttribute("user", user);
				c.setSessionAttribute("user-cr", user.credentials);
				
				c.setAttribute("model", new GenericMessage(user));
				
			} else {
				// No user with the provided values
				c.setAttribute("model", new GenericMessage(true, 403, new ErrorMessage(403, "Error : No user found with this login/password")));
			}
		} else {
			// Not all the required value are provided
			c.setAttribute("model", new GenericMessage(true, 403, new ErrorMessage(403, "Error : Not enought data sended")));
		}
		
	}

}
