package fr.vuzi.fileexplorer.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

		if (cause == null)
			context.setAttribute("model", new GenericMessage(true, httpCode, new ErrorMessage(httpCode, "Error : " + message)));
		else
			context.setAttribute("model", new GenericMessage(true, httpCode, new ErrorMessage(httpCode, "Error : " + cause.getMessage())));

		context.getResponse().setStatus(httpCode);

		try {
			renderer.render(context);
		} catch (Exception e) {
			try {
				context.getResponseWriter().write("An error occured during the generation of a previous error message\n");
				context.getResponseWriter().write("Previous message : " + message);
			} catch (IOException e1) {
				e1.printStackTrace(); // IO exception : not possible to send
										// anything
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
		// renderer.addRenderer("html", new RendererHTML());

		// -- XML renderer --
		// renderer.addRenderer("xml", new RendererXML());

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
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/login/?$", "GET|POST", "fr.vuzi.fileexplorer.api.action.ActionLogin"));

		// Logout
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/logout/?$", "GET|POST", "fr.vuzi.fileexplorer.api.action.ActionLogout"));

		// -- Directory actions --
		// Listing of a directory content (not recursively)
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/dir/(.*)/?$", "GET", "fr.vuzi.fileexplorer.api.action.ActionDirShow",
				new String[] { "path" }));
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/dir-id/(.*)/?$", "GET", "fr.vuzi.fileexplorer.api.action.ActionDirShowByID",
				new String[] { "id" }));

		// Directory modification
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/dir/(.*)/?$", "POST", "fr.vuzi.fileexplorer.api.action.ActionDirModification",
				new String[] { "path" }));
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/dir/(.*)/?$", "PUT", "fr.vuzi.fileexplorer.api.action.ActionDirCreation",
				new String[] { "path" }));
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/dir/(.*)/?$", "DELETE", "fr.vuzi.fileexplorer.api.action.ActionDirDeletion",
				new String[] { "path" }));

		// Tree of all the user's directories
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/tree/?$", "GET", "fr.vuzi.fileexplorer.api.action.ActionDirShowTree"));

		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/test/?$", "GET|POST|PUT|DELETE", "fr.vuzi.webframework.action.ActionDefault"));
		
		// -- File actions --
		// File download
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/file/(.*)/?$", "GET", "fr.vuzi.fileexplorer.api.action.ActionFileShow",  new String[] { "path" }));
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/file-download/(.*)/?$", "GET", "fr.vuzi.fileexplorer.api.action.ActionFileDownload",  new String[] { "path" }));

		// File upload
		rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/file/(.*)/?$", "POST", "fr.vuzi.fileexplorer.api.action.ActionFileCreation", new String[] { "path" }));
		
		// File modification
		//rewriter.addRule(new RewriteRule(Configuration.URIroot + "/api/(?:dir/(?:[0-9]+)/)?file/([0-9]+)/?$", "POST", "fr.vuzi.fileexplorer.api.action.ActionFileModification", new String[] { "file-id" }));
	}

	@Override
	/**
	 * Creates into the user Tomcat directory a temporary xml file which contains the server configuration
	 */
	protected String getRootDirectory() {
		/*String rootDirectory = System.getProperty("user.dir")+"/tmp/FileExplorer";
		File tmpDirectory = new File(rootDirectory+"/WEB-INF/conf");
		tmpDirectory.mkdirs();
		
		String jsonConf =
				"{\n"
					+"\t\"root\" : \"C:/Users/Vuzi/Desktop/JEE/workspace/VuziWebFramework/\",\n"
					+"\t\"URI\"  : \"/FileExplorer\",\n"
					+"\t\"actions\" : [ \n"
						+"\t\t\"fr.vuzi.fileexplorer.api.action.ActionLogin\",\n"
						+"\t\t\"fr.vuzi.fileexplorer.api.action.ActionLogout\",\n"
						+"\t\t\"fr.vuzi.fileexplorer.api.action.ActionDirShow\",\n"
						+"\t\t\"fr.vuzi.fileexplorer.api.action.ActionDirShowByID\",\n"
						+"\t\t\"fr.vuzi.fileexplorer.api.action.ActionDirShowTree\",\n"
						+"\t\t\"fr.vuzi.fileexplorer.api.action.ActionDirModification\"\n"
					+"\t],\n"
					+"\t\"velocity_priority\" : {\n"
						+"\t\t\"jwf.template.velocity.VelocityHeaderView\" : true,\n"
						+"\t\t\"jwf.template.velocity.VelocityMainView\" : {\n"
							+"\t\t\t\"jwf.template.velocity.VelocityLeftPanelView\" : true,\n"
							+"\t\t\t\"__CURRENT__\" : true\n"
						+"\t\t},\n"
						+"\t\t\"jwf.template.velocity.VelocityFooterView\" : true\n"
					+"\t}\n"
				+"}\n";

		File jsonConfFile = new File(tmpDirectory+"/conf.json");
		FileWriter writer = null;
		try {
			jsonConfFile.createNewFile();
			writer = new FileWriter(jsonConfFile);
			writer.write(jsonConf);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rootDirectory;
		*/
		return "C:/Users/Vuzi/Desktop/JEE/workspace/FileExplorer/";
	}

	@Override
	protected IContext createContext(HttpServletRequest request, HttpServletResponse response) {
		return new BasicAuthContext(request, response); // Use custom session handling basic auth
	}
}
