package fr.vuzi.fileexplorer.api.action.search;

import java.util.ArrayList;
import java.util.List;

import fr.vuzi.fileexplorer.database.SortType;
import fr.vuzi.fileexplorer.database.directory.Directory;
import fr.vuzi.fileexplorer.database.directory.DirectoryUtils;
import fr.vuzi.fileexplorer.database.file.FileUtils;
import fr.vuzi.fileexplorer.database.user.User;
import fr.vuzi.fileexplorer.database.user.UserUtils;
import fr.vuzi.fileexplorer.message.ErrorMessage;
import fr.vuzi.fileexplorer.message.GenericMessage;
import fr.vuzi.webframework.action.AAction;
import fr.vuzi.webframework.action.IAction;
import fr.vuzi.webframework.context.IContext;

public class ActionSearch extends AAction {

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
		IAction action = new ActionSearch();
		action.setActionContext(context);
		
		return action;
	}

	@Override
	public void proceed() throws Exception {
		IContext c = getActionContext();
		User u = UserUtils.getSessionUser(c);
		
		// File info
		List<String> path = DirectoryUtils.getPath(c.getParameterUnique("_path"));
		Directory dir = DirectoryUtils.getDirectory(u, path);
		
		if(dir == null) {
			c.setAttribute("model", new GenericMessage(true, 404, new ErrorMessage(404, "Error : No directory '" + c.getParameterUnique("_path") + "' found")));
			c.setStatus(404);
			return;	
		}
		
		// Search info
		String searchRegex = c.getParameterUniqueOrElse("search", "*");
		String searchType = c.getParameterUniqueOrElse("element", "both");
		String searchRecursiveRaw = c.getParameterUniqueOrElse("recursive", "false");
		String sortTypeRaw = c.getParameterUniqueOrElse("order", "false");
		boolean searchRecursive = false;
		SortType sortType = SortType.NONE;

		// Recursive search
		switch(searchRecursiveRaw) {
		case "true" :
			searchRecursive = true;
			break;
		case "false" :
		default:
			break;
		}
		
		// Sort type
		switch(sortTypeRaw) {
		case "name_asc" :
			sortType = SortType.NAME_ASC;
			break;
		case "name_dsc" :
		case "name_desc" :
			sortType = SortType.NAME_DSC;
			break;
		case "size_asc" :
			sortType = SortType.SIZE_ASC;
			break;
		case "size_dsc" :
		case "size_desc" :
			sortType = SortType.SIZE_DSC;
			break;
		case "none" :
		default:
			break;
		}
		
		ArrayList<Object> ret = new ArrayList<Object>();
		
		switch(searchType) {
		case "both":
			ret.addAll(FileUtils.searchFiles(u, searchRegex, dir, searchRecursive, sortType));
			ret.addAll(DirectoryUtils.searchDirectories(u, searchRegex, dir, searchRecursive, sortType));
			break;
		case "file" :
			ret.addAll(FileUtils.searchFiles(u, searchRegex, dir, searchRecursive, sortType));
			break;
		case "directory" :
			ret.addAll(DirectoryUtils.searchDirectories(u, searchRegex, dir, searchRecursive, sortType));
			break;
		default:
			c.setAttribute("model", new GenericMessage(true, 400, new ErrorMessage(400, "Error : Unkown search type")));
			c.setStatus(400);
			return;	
		}
		
		c.setAttribute("model",  new GenericMessage(ret.toArray(new Object[0])));
	}
}
