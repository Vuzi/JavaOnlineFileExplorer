package fr.vuzi.fileexplorer.api.action;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.output.WriterOutputStream;

import fr.vuzi.fileexplorer.database.directory.DirectoryUtils;
import fr.vuzi.fileexplorer.database.file.File;
import fr.vuzi.fileexplorer.database.file.FileUtils;
import fr.vuzi.fileexplorer.database.user.User;
import fr.vuzi.fileexplorer.message.ErrorMessage;
import fr.vuzi.fileexplorer.message.GenericMessage;
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
	public IAction cloneAction(IContext context) {
		IAction action = new ActionFileDownload();
		action.setActionContext(context);
		
		return action;
	}

	@Override
	public void proceed() throws Exception {
		IContext c = getActionContext();
		User u = (User) c.getSessionAttribute("user");
		
		// File info
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("path"));
		String name = path.size() > 0 ? path.remove(path.size() - 1) : null;
		
		File file = FileUtils.getFile(u, path, name);
		
		if(file == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No file '" + c.getParameterUnique("path") + "'")));
			return;	
		}
		
		InputStream in = FileUtils.getFileData(file);

		if(in == null) {
			c.setAttribute("model", new GenericMessage(true, 500, new ErrorMessage(500, "Error : Could not read file '" + c.getParameterUnique("path") + "' from database")));
			return;	
		}
		
		//c.getRequest().getc
		
		// Write the file to the output
		WriterOutputStream w = new WriterOutputStream(c.getResponseWriter());
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
         
        while ((bytesRead = in.read(buffer)) != -1) {
        	w.write(buffer, 0, bytesRead);
        }
	}
}
