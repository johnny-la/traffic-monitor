package traffic.monitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Monitors metrics and triggers relevant alerts 
 */
public class ThroughputMonitor
{
    /** Stores timestamps for all requests in the past "highTrafficTimeWindow" milliseconds */
    private Queue<Long> requestTimestamps;
    
    /** If average RPS surpasses this value, log a warning */
    private double highTrafficRpsThreshold;
    /** The time window (in milliseconds) for which high traffic is detected */
    private long highTrafficTimeWindow;
    /** True if the system is currently experiencing high traffic */
    private boolean highTrafficDetected;
    
    /** Every "delay" milliseconds, throughput is monitored for high traffic */
    private long delay;
    
    /**
     * Creates a monitor which analyzes throughput for critical values
     * @param highTrafficRpsThreshold If average RPS surpasses this value, create an alert 
     * @param highTrafficTimeWindow The time window (in milliseconds) for which high traffic is detected
     * @param delay Every "delay" milliseconds, throughput is monitored for high traffic
     */
    public ThroughputMonitor(double highTrafficRpsThreshold, long highTrafficTimeWindow, long delay)
    {
        this.highTrafficRpsThreshold = highTrafficRpsThreshold;
        this.highTrafficTimeWindow = highTrafficTimeWindow;
        this.delay = delay;

        requestTimestamps = new LinkedList<Long>();
    }
    
    /**
     * Monitors the requests per second, and logs a warning if a threshold is surpassed
     */
    public void monitorThroughput()
    {
        monitorThroughput(System.currentTimeMillis());
    }
    
    /**
     * Monitors the requests per second, and logs a warning if the threshold is surpassed
     * @param currentTime The current time of the system
     */
    public void monitorThroughput(long currentTime)
    {
        expireOldRequests(currentTime);
        double requestsPerSecond = getCurrentRps();

        // Log a warning if high traffic threshold is exceeded
        if (!highTrafficDetected && requestsPerSecond >= highTrafficRpsThreshold)
        {
            addAlert(requestTimestamps.size(), false, currentTime);
            highTrafficDetected = true;
        }
        // Recovery from high traffic
        else if (highTrafficDetected && requestsPerSecond < highTrafficRpsThreshold)
        {
            addAlert(requestTimestamps.size(), true, currentTime);
            highTrafficDetected = false;
        }
    }
    
    /** 
     * Adds a request performed at the given timestamp. 
     * Allows the manager to track throughput.
     * @param currentTime The timestamp when the request was created
     */
    public void addRequest(long currentTime)
    {
        requestTimestamps.offer(currentTime);
    }
    
    /** 
     * Returns the average requests per second in the high traffic time window
     * @return The average RPS being monitored for high traffic
     */
    public double getCurrentRps() 
    {
        return requestTimestamps.size() / (highTrafficTimeWindow/1000.0);
    }
    
    /**
     * Remove any request timestamps added more than "highTrafficTimeWindow" milliseconds ago
     * @param currentTime The current time of the system
     */
    private void expireOldRequests(long currentTime)
    {
        long expirationTimestamp = currentTime - highTrafficTimeWindow;
        int numRequests = requestTimestamps.size();
        for (int i = 0; i < numRequests; i++)
        {
            // All subsequent timestamps are within the expiration window
            if (requestTimestamps.peek() >= expirationTimestamp)
            {
                break;        
            }
            
            requestTimestamps.poll();
        }
    }
}
