package traffic.monitor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import traffic.log.Log;

public class MetricManager implements Runnable
{    
    /** Metrics recorded since the last flush */
    private Metrics currentMetrics;  
    /** Metrics recorded since this manager was created */
    private Metrics totalMetrics;    
    
    /** Stores timestamps for all requests in the past "highTrafficTimeWindow" milliseconds */
    private Queue<Long> requestTimestamps;
    /** Stores all alerts for historical reasons */
    private ArrayList<Alert> alerts;
    
    /** If average RPS surpasses this value, log a warning */
    private double highTrafficRpsThreshold;
    /** The time window (in milliseconds) for which high traffic is detected */
    private long highTrafficTimeWindow;
    /** True if the system is currently experiencing high traffic */
    private boolean highTrafficDetected;
    
    /** Every "delay" milliseconds, throughput is monitored for high traffic */
    private long delay;
    
    /**
     * Creates a manager which analyzes logs and monitors metrics
     * @param highTrafficRpsThreshold If average RPS surpasses this value, create an alert 
     * @param highTrafficTimeWindow The time window (in milliseconds) for which high traffic is detected
     * @param delay Every "delay" milliseconds, throughput is monitored for high traffic
     */
    public MetricManager(double highTrafficRpsThreshold, long highTrafficTimeWindow, long delay)
    {
        this.highTrafficRpsThreshold = highTrafficRpsThreshold;
        this.highTrafficTimeWindow = highTrafficTimeWindow;
        this.delay = delay;
        
        currentMetrics = new Metrics();
        totalMetrics = new Metrics();
        
        requestTimestamps = new LinkedList<Long>();
        alerts = new ArrayList<Alert>();
    }
    
    /**
     * Monitors throughput regularly to detect high traffic
     */
    public void run()
    {
        while (true)
        {
            monitorThroughput();
            
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
     * Updates internal metrics based on the contents of the log line 
     * @param log The log line to analyze
     */
    public void analyze(Log log)
    {
        if (log == null)
            return;
        
        analyze(log, currentMetrics);
        analyze(log, totalMetrics);
        
        addRequest(System.currentTimeMillis());
    }
    
    /**
     * Updates the given metrics based on the contents of the log line 
     * @param log The log line to analyze
     * @param metrics The metrics to update
     */
    private void analyze(Log log, Metrics metrics)
    {    
        updateWebsiteMetrics(log, metrics);
        updateStatusCodeMetrics(log, metrics);
        
        metrics.totalRequests++;
    }
    
    /**
     * Updates website metrics based on the given loglog
     * @param log The log line to analyze
     * @param metrics The metrics to update
     */
    private void updateWebsiteMetrics(Log log, Metrics metrics)
    {
        String host = log.host;
        String section = getWebsiteSection(log);
        
        if (section == null)
            return;
        
        if (!metrics.websites.containsKey(host))
        {
            metrics.websites.put(host, new Website(host));
        }
        
        Website website = metrics.websites.get(host);
        website.addSection(section);
        website.incrementHits();
        
        if (website.getHits() > metrics.maxSiteHits)
        {
            // Update site with max hits
            metrics.maxSiteHits = website.getHits();
            metrics.maxSite = website;
        }
    }
    
    /**
     * Updates status code metrics based on the status contained in the log
     * @param log The log line to analyze
     * @param metrics The metrics to update
     */
    private void updateStatusCodeMetrics(Log log, Metrics metrics)
    {
        if (log.status == null || log.status.length() < 1)
            return;
        
        // Increment the status code count
        char statusCodeFirstDigit = log.status.charAt(0);
        if (!metrics.statusCodeCounts.containsKey(statusCodeFirstDigit))
        {
            metrics.statusCodeCounts.put(statusCodeFirstDigit, 0);
        }
        metrics.statusCodeCounts.put(statusCodeFirstDigit, metrics.statusCodeCounts.get(statusCodeFirstDigit)+1);
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
     * Flushes all current metrics
     */
    public void flushMetrics()
    {
        currentMetrics.reset();
    }
    
    /**
     * Returns all alerts that were triggered
     * @return A list of historical alerts
     */
    public ArrayList<Alert> getAlerts()
    {
        return alerts;
    }
    
    /**
     * Returns all metrics recorded since the last flush
     * @return The metrics recorded since the last flush
     */
    public Metrics getCurrentMetrics()
    {
        return currentMetrics;
    }
    
    /**
     * Returns all metrics recorded since this manager was created
     * @return A summary of all captured metrics 
     */
    public Metrics getTotalMetrics()
    {
        return totalMetrics;
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
     * Logs and stores the given alert 
     * @param hits The total number of hits when the alert was triggered
     * @param recovery If true, create a recovery alert. Otherwise, create a critical alert
     * @param currentTime The timestamp when the alert is triggered
     */
    private void addAlert(int hits, boolean recovery, long currentTime)
    {
        Alert alert = new Alert(hits, recovery, currentTime);
        alerts.add(alert);
        // Log the alert right when it happens
        System.out.println(alert);
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
    
    /**
     * Extracts the website section which was hit in this log
     * @param log The log line to analyze
     * @return The website section contained in this log
     */
    private String getWebsiteSection(Log log)
    {
        if (log == null || log.requestUrl == null || log.host == null)
            return null;
        
        String[] urlFields = log.requestUrl.split("/");
        String section = log.host;
        if (urlFields.length > 1)
        {
            section += urlFields[0] + "/" + urlFields[1];
        }
        
        return section;
    }
}