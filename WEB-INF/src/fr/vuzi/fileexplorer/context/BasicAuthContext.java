package fr.vuzi.fileexplorer.context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.vuzi.fileexplorer.database.user.User;
import fr.vuzi.fileexplorer.database.user.UserUtils;
import fr.vuzi.webframework.context.Context;

/**
 * Context handling basic auth with our specific user
 * 
 * @author Vuzi
 *
 */
public class BasicAuthContext extends Context {

	public BasicAuthContext(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public void authentificate(String login, String password) throws Exception {
		User user = UserUtils.getUser(login, password);
		
		if(user != null) {
			setSessionAttribute("user", user);
			setSessionAttribute("user-id", user.UID);
			setSessionAttribute("user-cr", user.credentials);
		}
	}
}
