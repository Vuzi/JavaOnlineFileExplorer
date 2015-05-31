package fr.vuzi.fileexplorer.api.action.front;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

import fr.vuzi.webframework.Utils;
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
		
		// Get the resource
		File resource;
		
		if(this.getClass().getResource("/../resources/" + c.getParameterUnique("_path")) != null)
			resource = new File(this.getClass().getResource("/../resources/" + c.getParameterUnique("_path")).getFile());
		else
			resource = new File("__dummy");
		
		if(!resource.exists() && !resource.isFile()) {
			// 404 : file not found
			c.getResponseWriter().write("Error : no file " + c.getParameterUnique("_path"));
			c.getResponse().setStatus(404);
			c.getResponse().getOutputStream().close();
			return;	
		}
		
		String modifiedSince = Utils.formatDate(resource.lastModified());
		
		// If the file is cached
		if(modifiedSince.equals(c .getRequest().getHeader("If-Modified-Since"))) {
			// 304 : no changes
			c.getResponse().setStatus(304);
			c.getResponse().getOutputStream().close();
			return;
		}
		
		DataInputStream in = new DataInputStream(new FileInputStream(resource));
		
		// Prepare headers
		c.getResponse().setHeader("Last-Modified", modifiedSince);
		c.getResponse().setHeader("Content-Type", "application/javascript");
		
		if(resource.getName().endsWith(".js"))
			c.getResponse().setHeader("Content-Type", "application/javascript");
		else
			c.getResponse().setHeader("Content-Type", Files.probeContentType(Paths.get(resource.getAbsolutePath())));

		// Write the file to the output
		OutputStream out = c.getResponse().getOutputStream();
		
		// Compress if possible
		if(c.supportEncoding("gzip")) {
			c.getResponse().addHeader("Content-Encoding", "gzip");
			out = new GZIPOutputStream(out);
		}
		
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
