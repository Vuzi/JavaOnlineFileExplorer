package fr.vuzi.fileexplorer.message;

/**
 * Generic error message, used only on error-case. Should be wrapped in a generic message	
 * @author Vuzi
 *
 */
public class ErrorMessage {
	
	/**
	 * Status code
	 */
	public int status;
	
	/**
	 * Error message
	 */
	public String message;
	
	/**
	 * Error message constructor
	 * @param status The status
	 * @param message The message
	 */
	public ErrorMessage(int status, String message) {
		super();
		this.status = status;
		this.message = message;
	}
	
}
