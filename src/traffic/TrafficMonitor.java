package traffic;

import java.io.File;

import org.apache.commons.io.input.Tailer;

import traffic.log.LogParser;
import traffic.monitor.MetricManager;
import traffic.util.MetricPrinter;

public class TrafficMonitor
{
    /** Milliseconds to wait between reading the log file */
    private static final long FILE_READING_DELAY = 10;
    
    /** Milliseconds to wait between monitoring throughput */
    private static final long MONITOR_THROUGHPUT_INTERVAL = 100;
    /** Milliseconds to wait between printing metrics */
    private static final long METRIC_PRINT_INTERVAL = 2000;
    
    /** If RPS surpasses this value, log a warning */
    private static final double HIGH_TRAFFIC_RPS_THRESHOLD = 70.0;
    /** The time window (in milliseconds) for which high traffic is detected */
    private static final long HIGH_TRAFFIC_TIME_WINDOW = 12000;
    
    public static void main(String[] args) throws Exception
    {
        args = new String[]{"access_log"};
        if (args.length < 1)
        {
            System.out.println("Error: expecting a log file");
            return;
        }
        
        // Start a thread to manage metrics and monitor throughput
        MetricManager metricManager = new MetricManager(
                HIGH_TRAFFIC_RPS_THRESHOLD, 
                HIGH_TRAFFIC_TIME_WINDOW, 
                MONITOR_THROUGHPUT_INTERVAL);
        Thread metricManagerThread = new Thread(metricManager);
        metricManagerThread.start();
        
        // Create a reader for the log file
        File file = new File(args[0]);
        LogParser logParser = new LogParser(metricManager);
        Tailer tailer = Tailer.create(file, logParser);
        
        // Print metrics every 10-second interval
        MetricPrinter metricPrinter = new MetricPrinter(metricManager, METRIC_PRINT_INTERVAL);
        Thread metricPrinterThread = new Thread(metricPrinter);
        metricPrinterThread.start();
    }
}