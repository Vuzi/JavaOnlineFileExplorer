package fr.vuzi.fileexplorer.api.action.front;

import fr.vuzi.fileexplorer.database.user.User;
import fr.vuzi.fileexplorer.database.user.UserUtils;
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
		String login = c.getParameterUnique("login");
		String pass = c.getParameterUnique("password");

		c.getResponse().addHeader("Cache-Control", "private, must-revalidate");
		
		
		//c.getResponse().addHeader("Content-Encoding", "gzip");
		
		if(login != null && !login.isEmpty() && pass != null && !pass.isEmpty()) {
			User user = UserUtils.getUser(login, pass);
			if(user != null) {
				// Save in the session and send the connected user in the response
				c.setSessionAttribute("user-uid", user.UID);
				c.setSessionAttribute("user-cr", user.credentials);
			} else {
				// No user with the provided values
				c.setAttribute("error", "Erreur : identifiant et/ou mot de passe invalide");
			}
		} else if (c.getRequest().getMethod() == "POST"){
			// Not all the required value are provided
			c.setAttribute("error", "Erreur : l'identifiant et le mot de passe ne peuvent pas être vides");
		}
	}
}
