import java.util.HashMap;
import java.util.HashSet;

/**
 * Stores metrics for HTTP request monitoring
 */
public class Metrics
{
	public HashMap<String, Website> websites;	// The websites which received requests
	public int maxSiteHits;	// Max hits for a website
	public Website maxSite;	// Website with max hits
	
	public HashMap<Character, Integer> statusCodeCounts;
	public int totalRequests;
	public int invalidLogs;
	
	public long startTime;	// The timestamp when the metrics started to be recorded
	
	public Metrics()
	{
		websites = new HashMap<String, Website>();
		statusCodeCounts = new HashMap<Character, Integer>();
		
		reset();
	}
	
	public void reset()
	{
		maxSiteHits = 0;
		maxSite = null;
		statusCodeCounts.clear();
		totalRequests = 0;
		invalidLogs = 0;
		
		startTime = System.currentTimeMillis();
	}
	
	/**
	 * Returns the sections that were hit for the max-requested website
	 * @return A list of sections for the max website. Returns ["None"] if no max site exists
	 */
	public String[] getMaxSiteSections()
	{
		if (maxSite != null && maxSite.getSections() != null)
		{
			// Convert the HashSet to a string array
			HashSet<String> sections = maxSite.getSections();
			return sections.toArray(new String[sections.size()]);
		}
		
		return new String[]{"None"};
	}
	
	/**
	 * Returns the percentage of requests that returned a 2xx status
	 * @return A percentage string of successful responses
	 */
	public String getSuccessPercent()
	{
		return String.format("%.2f", getSuccessRate()*100) + "%";
	}
	
	/**
	 * Returns the success rate of the requests recorded in these metrics
	 * @return The percentage of requests that returned a 2xx status code
	 */
	private double getSuccessRate()
	{
		return (statusCodeCounts.containsKey('2'))? statusCodeCounts.get('2')/(float)totalRequests: 0.0;
	}
	
	/**
	 * Returns the average requests per second since the start
	 * @return The average RPS since starting to record the metrics
	 */
	public double getRequestsPerSecond()
	{
		return totalRequests / getTimeSinceStart();
	}
	
	/**
	 * Returns the total amount of time the metrics have been recorded
	 * @return Seconds that the metrics have been recorded for
	 */
	public double getTimeSinceStart()
	{
		return (System.currentTimeMillis() - startTime) / 1000.0;
	}
}