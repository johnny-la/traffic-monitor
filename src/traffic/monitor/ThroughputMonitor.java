package traffic.monitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Monitors metrics and triggers relevant alerts 
 */
public class ThroughputMonitor implements Runnable
{
    /** Stores timestamps for all requests in the past "highTrafficTimeWindow" milliseconds */
    private Queue<Long> requestTimestamps;
    /** Listeners that are notified whenever this monitor triggers an alert */
    private ArrayList<AlertListener> alertListeners;
    
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
        alertListeners = new ArrayList<AlertListener>();
    }
    
    /**
     * Monitors throughput regularly to detect high traffic
     */
    public void run()
    {
        while (true)
        {
            update(System.currentTimeMillis());
            
            // Sleep for "delay" seconds
            try
            {
                Thread.sleep(delay);
            }
            catch (InterruptedException e)
            {
                System.out.println(e.getStackTrace());
            }
        }
    }
    
    /**
     * Monitors the requests per second, and logs a warning if the threshold is surpassed
     * @param currentTime The current time of the system
     */
    public void update(long currentTime)
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
     * Allows the monitor to track throughput.
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
     * Adds a listener that will be notified whenever an alert is triggered
     * @param listener The listener to notify
     */
    public void addAlertListener(AlertListener listener)
    {
    		alertListeners.add(listener);
    }
    
    /** 
     * Logs and stores the given alert 
     * @param hits The total number of hits when the alert was triggered
     * @param recovery If true, create a recovery alert. Otherwise, create a critical alert
     * @param currentTime The timestamp when the alert is triggered
     */
    private void addAlert(int hits, boolean recovery, long currentTime)
    {
        Alert alert = new Alert(hits, recovery, currentTime);
        // Notify listeners that the alert was triggered
        for (int i = 0; i < alertListeners.size(); i++) 
        {
        		alertListeners.get(i).alertTriggered(alert);
        }
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
