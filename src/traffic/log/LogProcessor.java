package traffic.log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.TailerListenerAdapter;

import traffic.monitor.MetricManager;

/**
 * Reads a log file in Apache format, parses its contents and processes its metrics
 */
public class LogProcessor extends TailerListenerAdapter
{
    /** Regex which extracts information about a log line */
    private static final String LOG_PATTERN = "(\\S+) (\\S+) (\\S+) \\[(\\d{1,2}\\/[a-zA-Z]+\\/\\d+:\\d{2}:\\d{2}:\\d{2} [+\\-] ?\\d{4})\\] \"(.{1,})\" (\\d+) (\\d+)";
    /** The number of fields in a log line */
    private static final int LOG_FIELD_COUNT = 7;
    
    /** Monitors metrics from a log file */
    private MetricManager metricManager;
    
    /**
     * Creates a process for logs
     * @param metricManager Manager that will store metrics processed from a log file
     */
    public LogProcessor(MetricManager metricManager)
    {
        this.metricManager = metricManager;
    }
    
    /**
     * Called whenever a line is added to the log file
     * @param line The line added to the log file
     */
    public void handle(String line)
    {
        Log log = parseLine(line);
        metricManager.analyze(log);
    }
    
    /**
     * Returns a log object parsed from the given log line
     * @param logLine The log line to parse
     * @return A log object containing the information from the log line
     */
    private Log parseLine(String logLine)
    {    
        Pattern pattern = Pattern.compile(LOG_PATTERN);
        Matcher matcher = pattern.matcher(logLine);
        if (matcher.find() && matcher.groupCount() == LOG_FIELD_COUNT)
        {
            Log log = new Log();
            
            log.host = matcher.group(1);
            log.id = matcher.group(2);
            log.authUser = matcher.group(3);
            log.date = matcher.group(4);
            
            // Extract request
            log.request = matcher.group(5);
            String[] requestFields = log.request.split(" ");
            log.requestMethod = requestFields[0];
            log.requestUrl = requestFields[1];
            log.requestProtocol = requestFields[2];
            
            log.status = matcher.group(6);
            log.bytes = matcher.group(7);
            
            return log;
        }

        // Error
        return null;
    }
}
