package fr.vuzi.fileexplorer.api.action.front;

import java.util.regex.Pattern;

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
		
		if(c.getParameterUnique("login-submit") != null) {
			loginUser(c);
		} else if(c.getParameterUnique("subscribe-submit") != null) {
			subscribeUser(c);
		}
	}
	
	private void loginUser(IContext c) throws Exception {
		String login = c.getParameterUnique("login");
		String pass = c.getParameterUnique("password");
		
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
		} else {
			// Not all the required value are provided
			c.setAttribute("error", "Erreur : l'identifiant et le mot de passe ne peuvent pas être vides");
		}
	}
	
	private void subscribeUser(IContext c) throws Exception {
		String login = c.getParameterUnique("login");
		String pass = c.getParameterUnique("password");
		String pass2 = c.getParameterUnique("password2");
		String email = c.getParameterUnique("email");

		if(login != null && !login.isEmpty() && pass != null && !pass.isEmpty() && pass2 != null && !pass2.isEmpty() && email != null && !email.isEmpty()) {
			User user = UserUtils.getUserByLogin(login);

			if(user != null) {
				c.setAttribute("error", "Erreur : un utilisateur avec le login '" + login + "' existe déjà");
				return;
			}
			
			if(!Pattern.matches("[A-Za-z][A-Za-z0-9]{3,31}", login)) {
				c.setAttribute("error", "Erreur : le login '" + login + "' est incorrect");
				return;
			}
			
			if(!Pattern.matches("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", email)) {
				c.setAttribute("error", "Erreur : l'adresse mail est invalide");
				return;
			}
			
			if(!pass.equals(pass2)) {
				c.setAttribute("error", "Erreur : les deux mots de passe sont différents");
				return;
			}
			
			user = UserUtils.createUser(login, pass, email);
			 
			if(user == null) {
				c.setAttribute("error", "Erreur : impossible de créer l'utilisateur");
				return;
			}
			
			c.setSessionAttribute("user-uid", user.UID);
			c.setSessionAttribute("user-cr", user.credentials);
			
		} else {
			// Not all the required value are provided
			c.setAttribute("error", "Erreur : des champs requis sont vides");
		}
	}
}
