package traffic.util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import traffic.monitor.Alert;
import traffic.monitor.MetricManager;
import traffic.monitor.Metrics;

/**
 * Prints metrics at a desired interval
 */
public class MetricPrinter extends PrettyPrinter implements Runnable
{
    // Stores metrics analyzed from logs
    private MetricManager metricManager;
    
    // The amount of delay between printing each batch of metrics
    private long delay;
    
    public MetricPrinter(MetricManager metricManager, long delay)
    {
        this.metricManager = metricManager;
        this.delay = delay;
    }
    
    public void run()
    {
        sleep();
        
        while (true)
        {
            printMetrics();
            metricManager.flushMetrics();
            
            sleep();
        }
    }
    
    /**
     * Helper function that sleeps this thread for "delay" milliseconds
     */
    private void sleep()
    {
        try
        {
            Thread.sleep(delay);
        }
        catch (InterruptedException e)
        {
            System.out.println(e.getStackTrace());
        }
    }
    
    /**
     * Prints metrics that were analyzed from the logs
     */
    private void printMetrics()
    {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println();
        
        printMaxSites();
        printWorkMetrics();
        printStatusCodeReport();
        printAlertHistory();
    }
    
    /**
     * Prints the sites with the maximum requests
     */
    private void printMaxSites()
    {
        Metrics currentMetrics = metricManager.getCurrentMetrics();
        Metrics totalMetrics = metricManager.getTotalMetrics();
        
        startTable();
        
        addRow("", "Past 10 sec.", "Since start (" + totalMetrics.getTimeSinceStart() + "s)");
        addHorizontalSeparator();
        addRow();
        addRow("Website with most hits:");
        printMaxSite(currentMetrics);
        printMaxSite(totalMetrics);
        
        // Print sections of max site
        String[] currentMaxSections = currentMetrics.getMaxSiteSections();
        String[] totalMaxSections = totalMetrics.getMaxSiteSections();
        int maxSections = Math.max((currentMaxSections != null)? currentMaxSections.length:0,
                                   (totalMaxSections != null)? totalMaxSections.length:0);
        for (int i = 0; i < maxSections; i++)
        {
            addRow((i == 0)? "Sections hit:" : "");
            addEntry((i < currentMaxSections.length)? currentMaxSections[i] : "");
            addEntry((i < totalMaxSections.length)? totalMaxSections[i] : "");
        }
        
        addRow();
//        endTable();
    }
    
    /**
     * Prints the site with the maximum requests
     * @param metrics The metrics to extract data from
     * @param prettyPrinter Used to print the metrics
     */
    private void printMaxSite(Metrics metrics)
    {
        if (metrics.maxSite != null)
        {
            addEntry(metrics.maxSite.getName() + " (" + metrics.maxSiteHits + " hits)");
        }
        else
        {
            addEntry("None");
        }
    }
    
    /**
     * Prints work metrics (throughput, success rate)
     */
    private void printWorkMetrics()
    {
        Metrics currentMetrics = metricManager.getCurrentMetrics();
        Metrics totalMetrics = metricManager.getTotalMetrics();
        
        // RPS
//        startTable();
//        addRow("", "Past 10 sec.", "Since start (" + totalMetrics.getTimeSinceStart() + "s)");
        addRow("Total requests:",
                "" + currentMetrics.totalRequests,
                   "" + totalMetrics.totalRequests);
        addRow("Requests per second (avg):",
                "" + String.format("%.3f", currentMetrics.getRequestsPerSecond()),
                "" + String.format("%.3f", totalMetrics.getRequestsPerSecond()));
        
        // Success rate
        addRow("Success rate (2xx responses):",
                currentMetrics.getSuccessPercent(),
                totalMetrics.getSuccessPercent());
    }
    
    /**
     * Prints a report of status code counts
     */
    private void printStatusCodeReport()
    {
        Metrics currentMetrics = metricManager.getCurrentMetrics();
        Metrics totalMetrics = metricManager.getTotalMetrics();
        
        // Status codes
        addRow();
        addRow("Status code count:");
//        addRow("", "Past 10 sec.", "Since start (" + totalMetrics.getTimeSinceStart() + "s)");
        HashMap<Character, Integer> currentStatusCodes = currentMetrics.statusCodeCounts;
        HashMap<Character, Integer> totalStatusCodes = totalMetrics.statusCodeCounts;
        
        for (char statusCode : totalStatusCodes.keySet())
        {
            addRow(getStatusCodeMeaning(statusCode) + " " + statusCode + "xx");
            addEntry((currentStatusCodes.containsKey(statusCode))? currentStatusCodes.get(statusCode)+"" : "-");
            addEntry("" + totalStatusCodes.get(statusCode));
        }
        addRow();
        endTable();
    }
    
    /**
     * Prints a history of the alerts that were triggered
     */
    private void printAlertHistory()
    {
        // Alert history
        startTable();
        addRow("Alert History:");
        ArrayList<Alert> alerts = metricManager.getAlerts();
        for (int i = 0; i < alerts.size(); i++)
        {
            addRow(alerts.get(i).toString());
        }
        endTable();
    }
    
    /**
     * Returns the meaning behind the first digit of an HTTP status code
     * @param statusCode The first digit of an HTTP status code
     * @return The meaning behind the status code
     */
    private String getStatusCodeMeaning(char statusCode)
    {
        switch(statusCode)
        {
        case '1':
            return "Informational";
        case '2':
            return "Success ";
        case '3':
            return "Redirection";
        case '4':
            return "Client Error";
        case '5':
            return "Server Error";
        default:
            return "Unknown Status Code";
        }
    }
}
    