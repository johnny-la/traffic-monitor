/**
 * A container for a log parsed from in Apache common log format.
 */
public class Log
{
	public String host;
	public String id;
	public String authUser;
	public String date;
	public String request;
	public String requestMethod, requestUrl, requestProtocol;
	public String status;
	public String bytes;
	
	public Log() {}
	
	/**
	 * Populate the log fields with the placeholder string.
	 */
	public Log(String placeholder)
	{
		host = id = authUser = date = request = 
				requestMethod = requestUrl = 
				requestProtocol = status = bytes = placeholder;
	}
}