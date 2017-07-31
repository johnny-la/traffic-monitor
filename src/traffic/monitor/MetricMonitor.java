package traffic.monitor;

import traffic.log.Log;

public interface MetricMonitor 
{
    /**
     * Updates the monitor and triggers relevant alerts
     * @param currentTime The current timestamp of the program
     */
    void update(long currentTime);
    
    /**
     * Monitors the given log and updates internal metrics
     * @param log The log to analyze
     */
    void monitor(Log log);
}
