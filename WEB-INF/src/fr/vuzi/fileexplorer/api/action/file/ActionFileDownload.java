package fr.vuzi.fileexplorer.api.action.file;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import fr.vuzi.fileexplorer.database.directory.DirectoryUtils;
import fr.vuzi.fileexplorer.database.file.File;
import fr.vuzi.fileexplorer.database.file.FileUtils;
import fr.vuzi.fileexplorer.database.user.User;
import fr.vuzi.fileexplorer.database.user.UserUtils;
import fr.vuzi.webframework.action.AAction;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionFileDownload extends AAction {

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public String[] getCredentials() {
		return new String[] { "user" };
	}
	
	@Override
	public boolean needRenderer() {
		return false;
	}
	
	@Override
	public IAction cloneAction(IContext context) {
		IAction action = new ActionFileDownload();
		action.setActionContext(context);
		
		return action;
	}

	@Override
	public void proceed() throws Exception {
		IContext c = getActionContext();
		User u = UserUtils.getSessionUser(c);
		
		// File info
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("_path"));
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;
		
		File file = FileUtils.getFile(u, path, name);
		
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
		c.getResponse().setHeader("Content-Disposition","attachment;filename=\"" + name + "\"");

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
