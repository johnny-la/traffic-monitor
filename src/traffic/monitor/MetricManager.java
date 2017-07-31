package traffic.monitor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import traffic.log.Log;

public class MetricManager
{    
    /** Metrics recorded since the last flush */
    private Metrics currentMetrics;  
    /** Metrics recorded since this manager was created */
    private Metrics totalMetrics;    
    
    /** A list of objects that monitor throughput */
    private ArrayList<ThroughputMonitor> throughputMonitors;
    /** Stores all alerts for historical reasons */
    private ArrayList<Alert> alerts;
    
    /**
     * Creates a manager which analyzes logs and records metrics
     */
    public MetricManager()
    {
        currentMetrics = new Metrics();
        totalMetrics = new Metrics();
        
        alerts = new ArrayList<Alert>();
    }

    /**
     * Updates internal metrics based on the contents of the log line 
     * @param log The log line to analyze
     */
    public void analyze(Log log)
    {
        if (log == null)
            return;
        
        long currentTime = System.currentTimeMillis();
        
        analyze(log, currentMetrics);
        analyze(log, totalMetrics);
        
        // Add a request to each throughput monitor
        for (int i = 0; i < throughputMonitors.size(); i++)
        {
        		throughputMonitors.get(i).addRequest(currentTime);
        }
    }
    
    /**
     * Creates a monitor which analyzes throughput for critical values
     * @param highTrafficRpsThreshold If average RPS surpasses this value, create an alert 
     * @param highTrafficTimeWindow The time window (in milliseconds) for which high traffic is detected
     * @param delay Every "delay" milliseconds, throughput is monitored for high traffic
     */
    public void addThroughputMonitor(double highTrafficRpsThreshold, long highTrafficTimeWindow, long delay)
    {
    		// Create the throughput monitor
    		ThroughputMonitor monitor = new ThroughputMonitor(highTrafficRpsThreshold, highTrafficTimeWindow, delay);
    		throughputMonitors.add(monitor);
    		
    		// Listen to throughput alerts 
    		monitor.addAlertListener(new AlertListener() {
    			public void alertTriggered(Alert alert)
    			{
    				addAlert(alert);
    			}
    		});
    		
    		// Start monitoring throughput in a new thread
    		Thread monitorThread = new Thread(monitor);
    		monitorThread.start();
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
     * Logs and stores the given alert 
     * @param alert The alert to record
     */
    private void addAlert(Alert alert)
    {
        alerts.add(alert);
        // Log the alert right when it happens
        System.out.println(alert);
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