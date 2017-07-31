import org.apache.commons.io.input.TailerListenerAdapter;

/**
 * Reads a log file in Apache format, and analyzes its contents by recording relevant metrics
 */
public class LogReader extends TailerListenerAdapter
{
	private MetricManager metricManager;
	
	public LogReader(MetricManager metricManager)
	{
		this.metricManager = metricManager;
	}
	
	public void handle(String line)
	{
		Log log = LogParser.parseLine(line);
		metricManager.analyze(log);
	}
}