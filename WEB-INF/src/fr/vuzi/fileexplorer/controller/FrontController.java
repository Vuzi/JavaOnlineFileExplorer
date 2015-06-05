package fr.vuzi.fileexplorer.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.vuzi.fileexplorer.context.BasicAuthContext;
import fr.vuzi.fileexplorer.database.DataBase;
import fr.vuzi.fileexplorer.message.ErrorMessage;
import fr.vuzi.fileexplorer.message.GenericMessage;
import fr.vuzi.webframework.Configuration;
import fr.vuzi.webframework.context.IContext;
import fr.vuzi.webframework.controller.AFrontController;
import fr.vuzi.webframework.dispatcher.RewriteRule;
import fr.vuzi.webframework.renderer.RendererJSON;
import fr.vuzi.webframework.renderer.RendererVelocity;

public class FrontController extends AFrontController {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -323519238448815224L;

	@Override
	protected void errorHandle(IContext context, int httpCode, String message, Exception cause) {
		context.setActionClassname(null); // Remove action
		
		if(cause == null)
			context.setAttribute("model", new GenericMessage(true, httpCode, new ErrorMessage(httpCode, "Error : " + message)));
		else
			context.setAttribute("model", new GenericMessage(true, httpCode, new ErrorMessage(httpCode, "Error : " + cause.getMessage())));
		
		context.setStatus(httpCode);
		
		try {
			renderer.render(context);
		} catch (Exception e) {
			try {
				context.getResponseWriter().write("An error occured during the generation of a previous error message\n");
				context.getResponseWriter().write("Previous message : " + message);
			} catch (IOException e1) {
				e1.printStackTrace(); // IO exception : not possible to send anything
			}
			e.printStackTrace();
		}
	}
	
	@Override
	public void init() {
		super.init();
		
		// Start the database connection
		DataBase.init();
	}

	@Override
	protected void initRenderers() {
		// -- JSON renderer --
		renderer.addRenderer("json", new RendererJSON());
		
		// -- HTML renderer --
		//renderer.addRenderer("html", new RendererHTML());
		
		// -- XML renderer --
		//renderer.addRenderer("xml", new RendererXML());
		
		// -- Velocity renderer --
		renderer.addRenderer("velocity", new RendererVelocity(dispatcher));
		
		// -- Default type : JSON --
		renderer.setDefaultType("json");
	}
	
	@Override
	protected void initRewriterRules() {
		// == API ==
		// -- User account actions --
		// Login
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/login/?$", "GET|POST", "fr.vuzi.fileexplorer.api.action.user.ActionLogin"));
		
		// Logout
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/logout/?$", "GET|POST", "fr.vuzi.fileexplorer.api.action.user.ActionLogout"));
		
		// -- Search --
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/search/(.*)/?$", "GET|POST", "fr.vuzi.fileexplorer.api.action.search.ActionSearch", new String[] { "_path" }));
		
		// -- Directory actions --
		// Listing of a directory content (not recursively)
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/dir/(.*)/?$", "GET", "fr.vuzi.fileexplorer.api.action.directory.ActionDirShow", new String[] { "_path" }));
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/dir-id/(.*)/?$", "GET", "fr.vuzi.fileexplorer.api.action.directory.ActionDirShowByID", new String[] { "id" }));
		
		// Directory modification
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/dir/(.*)/?$", "POST", "fr.vuzi.fileexplorer.api.action.directory.ActionDirModification", new String[] { "_path" }));
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/dir/(.*)/?$", "PUT", "fr.vuzi.fileexplorer.api.action.directory.ActionDirCreation", new String[] { "_path" }));
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/dir/(.*)/?$", "DELETE", "fr.vuzi.fileexplorer.api.action.directory.ActionDirDeletion", new String[] { "_path" }));
		
		// Tree of all the user's directories
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/tree/?$", "GET", "fr.vuzi.fileexplorer.api.action.directory.ActionDirShowTree"));
		
		// -- File actions (bin) --
		// File download
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/file-bin/(.*)/?$", "GET", "fr.vuzi.fileexplorer.api.action.file.ActionFileDownload",  new String[] { "_path" }));
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/file-shared/(.*)/?$", "GET", "fr.vuzi.fileexplorer.api.action.file.ActionFileSharedDownload",  new String[] { "_id" }));

		// File upload
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/file-bin/(.*)/?$", "POST", "fr.vuzi.fileexplorer.api.action.file.ActionFileUpload", new String[] { "_path" }));

		// -- File actions --
		// File info
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/file/(.*)/?$", "GET", "fr.vuzi.fileexplorer.api.action.file.ActionFileShow",  new String[] { "_path" }));
		
		// File modification
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/file/(.*)/?$", "POST", "fr.vuzi.fileexplorer.api.action.file.ActionFileModification", new String[] { "_path" }));
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/file/(.*)/?$", "PUT", "fr.vuzi.fileexplorer.api.action.file.ActionFileCreation", new String[] { "_path" }));
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/file/(.*)/?$", "DELETE", "fr.vuzi.fileexplorer.api.action.file.ActionFileDeletion", new String[] { "_path" }));
		
		// -- Front --
		// Resources
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/resources/(.*)/?$", "GET", "fr.vuzi.fileexplorer.api.action.front.ActionGetResource", new String[] { "_path" }));
		// Front page
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/?(?:~(.*))?$", "GET|POST", "fr.vuzi.fileexplorer.api.action.front.ActionFileExplorer",  new String[] { "_path" }, "velocity"));
	}
	
	@Override
	protected IContext createContext(HttpServletRequest request, HttpServletResponse response) {
		return new BasicAuthContext(request, response); // Use custom session handling basic auth
	}
}
