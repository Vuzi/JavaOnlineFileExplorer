package fr.vuzi.fileexplorer.database.user;

import java.util.List;

import org.bson.Document;

/**
 * User
 * 
 * @author Vuzi
 *
 */
public class User {

	public String UID;
	public String login;
	public String password; // TODO change
	
	public String[] credentials;
	
	/**
	 * User constructor with Document retrieve from the database
	 * @param d The document
	 */
	@SuppressWarnings("unchecked")
	public User(Document d) {
		login = d.getString("login");
		password = d.getString("password");
		UID = d.getObjectId("_id").toString();
		credentials = ((List<String>) d.get("credentials")).toArray(new String[0]);
	}

	/**
	 * User constructor using fields
	 * @param uID
	 * @param login
	 * @param password
	 * @param credentials
	 */
	public User(String uID, String login, String password, String[] credentials) {
		this.UID = uID;
		this.login = login;
		this.password = password;
		this.credentials = credentials;
	}
	
	/**
	 * Empty user constructor
	 */
	public User() {
		
	}
	
}
