import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser
{
	private static final String LOG_PATTERN = "(\\S+) (\\S+) (\\S+) \\[(\\d{1,2}\\/[a-zA-Z]+\\/\\d+:\\d{2}:\\d{2}:\\d{2} [+\\-] ?\\d{4})\\] \"(.{1,})\" (\\d+) (\\d+)";
	//"([.\d]+) (\S+) (\S+) \[(\d{1,2}\/[a-zA-Z]+\/\d+:\d{2}:\d{2}:\d{2} [+\-] ?\d{4})\] "(.{1,})" (\d+) (\d+)";
	private static final int LOG_FIELD_COUNT = 7;
	
	private static final String IPV4_PATTERN = "(\\d{1,3}\\.){3}\\d{1,3}";
		// (\d{1,3}\.){3}\d{1,3}
	
	/**
	 * Returns a log object parsed from the given log line
	 * @param logLine The log line to parse
	 * @return A log object containing the information from the log line
	 */
	public static Log parseLine(String logLine)
	{	
		Pattern pattern = Pattern.compile(LOG_PATTERN);
		Matcher matcher = pattern.matcher(logLine);
		if (matcher.find() && matcher.groupCount() == LOG_FIELD_COUNT)
		{
			Log log = new Log();
			
			log.host = matcher.group(1);//convertToUrl(matcher.group(1)));
			log.id = matcher.group(2);
			log.authUser = matcher.group(3);
			log.date = matcher.group(4);
			
			// Extract request
			log.request = matcher.group(5);
			String[] requestFields = log.request.split(" ");
			log.requestMethod = requestFields[0];
			log.requestUrl = requestFields[1];
			log.requestProtocol = requestFields[2];
			
			if (requestFields.length != 3)
				throw new RuntimeException("requestFields.length = " + requestFields.length);
			
			log.status = matcher.group(6);
			log.bytes = matcher.group(7);
			
			return log;
		}

		// Error
		return null;
	}
	
	private String convertToUrl(String host)
	{
		if (host == null)
			throw new IllegalArgumentException("Cannot convert a null host");
		
		try 
		{
			if (isIpAddress(host))
			{
				// Convert from IP to hostname
				InetAddress address = InetAddress.getByName(host);
				host = address.getHostName();
			}
		} 
		catch (UnknownHostException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return host;
	}
	
	/**
	 * Returns true if the given string is an IP address.
	 * @param host The host name to classify
	 * @return true if the string is an IP address
	 */
	private boolean isIpAddress(String host)
	{
		if (host == null)
			throw new IllegalArgumentException("Cannot classify a null string");
		
		// IPv6 address
		if (host.contains(":"))
			return true;
		
		// IPv4 address
		Pattern pattern = Pattern.compile(IPV4_PATTERN);
		Matcher matcher = pattern.matcher(host);
		if (matcher.matches())
			return true;
		
		// Host is not an IP address
		return false;
	}
}