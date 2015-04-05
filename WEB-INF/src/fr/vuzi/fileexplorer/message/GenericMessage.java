package fr.vuzi.fileexplorer.message;

/**
 * Generic message used to be displayed with the simple renderer using reflexivity
 * to choose fields to display. This message should wrap any returned value from
 * the API of the web-site
 * 
 * @author Vuzi
 *
 */
public class GenericMessage {
	
	/**
	 * True if an error occurred, false otherwise
	 */
	public boolean error = false;
	
	/**
	 * HTTP code
	 */
	public int code = 200;
	
	/**
	 * The response's data, null if no data
	 */
	public Object data = null;

	/**
	 * Constructor in case of no error occurred
	 * @param data The response data to display
	 */
	public GenericMessage(Object data) {
		super();
		this.data = data;
	}

	/**
	 * Constructor in case of error
	 * @param error True if an error occurred, false otherwise
	 * @param error_detail The error detail, null if no error occurred
	 * @param data The data, may be null
	 */
	public GenericMessage(boolean error, int http_code, Object data) {
		super();
		this.error = error;
		this.code = http_code;
		this.data = data;
	}
	
}
