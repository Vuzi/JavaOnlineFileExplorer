package fr.vuzi.fileexplorer.api.action.file;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import fr.vuzi.fileexplorer.database.file.File;
import fr.vuzi.fileexplorer.database.file.FileUtils;
import fr.vuzi.webframework.action.AAction;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionFileSharedDownload extends AAction {

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public String[] getCredentials() {
		return new String[] { };
	}
	
	@Override
	public boolean needRenderer() {
		return false;
	}
	
	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionFileSharedDownload();
		action.setActionContext(context);
		
		return action;
	}

	@Override
	public void proceed() throws Exception {
		IContext c = getActionContext();
		
		// File info
		String id = c.getParameterUnique("_id");
		
		if(id == null) {
			c.getResponse().setStatus(404);
			return;	
		}
		
		// Get file
		File file = FileUtils.getFileShared(id);
		
		if(file == null) {
			c.getResponse().setStatus(404);
			return;	
		}
		
		DataInputStream in = FileUtils.getFileData(file);

		if(in == null) {
			c.getResponse().setStatus(500);
			return;	
		}
		
		// Prepare headers
		c.getResponse().setContentType("application/octet-stream");
		c.getResponse().setHeader("Content-Type", file.type);
		c.getResponse().setHeader("Content-Disposition","attachment;filename=\"" + file.name + "\"");

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
