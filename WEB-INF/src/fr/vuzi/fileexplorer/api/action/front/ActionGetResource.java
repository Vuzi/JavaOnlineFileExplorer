package fr.vuzi.fileexplorer.api.action.front;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import fr.vuzi.webframework.Configuration;
import fr.vuzi.webframework.action.AAction;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionGetResource extends AAction {

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public String[] getCredentials() {
		return new String[0];
	}
	
	@Override
	public boolean needRenderer() {
		return false;
	}
	
	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionGetResource();
		action.setActionContext(context);
		
		return action;
	}

	@Override
	public void proceed() throws Exception {
		IContext c = getActionContext();
		File ressource;
		
		if(this.getClass().getResource("/../resources/" + c.getParameterUnique("_path")) != null)
			ressource = new File(this.getClass().getResource("/../resources/" + c.getParameterUnique("_path")).getFile());
		else
			ressource = new File("__dummy");
		
		if(!ressource.exists() && !ressource.isFile()) {
			c.getResponseWriter().write("Error : no file " + c.getParameterUnique("_path"));
			c.getResponse().setStatus(404);
			return;	
		}
		
		DataInputStream in = new DataInputStream(new FileInputStream(ressource));
		
		// Prepare headers
		c.getResponse().setContentLength((int)ressource.length());
		if(ressource.getName().endsWith(".js"))
			c.getResponse().setHeader("Content-Type", "application/javascript");
		else
			c.getResponse().setHeader("Content-Type", Files.probeContentType(Paths.get(ressource.getAbsolutePath())));

		// Write the file to the output
		OutputStream out = c.getResponse().getOutputStream();
		
        byte[] buffer = new byte[4096];
        int byteReads = -1;
        
        while ((byteReads = in.read(buffer, 0, 4096)) > 0) {
        	out.write(buffer, 0, byteReads);
        }

        out.flush();
        out.close();
        in.close();
	}
}
