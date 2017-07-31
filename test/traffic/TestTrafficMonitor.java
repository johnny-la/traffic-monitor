package traffic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import traffic.log.Log;
import traffic.metrics.Alert;
import traffic.metrics.MetricManager;

public class TestTrafficMonitor 
{
	// Send requests quicker to ensure an alert is captured
	private static final double REQUEST_DELAY_MULTIPLIER = 0.9;

	@Test
	public void testAlerts() 
	{
		testAlerts(10, 5000);
		testAlerts(500, 1000);
		testAlerts(1000, 120000);
	}

	public void testAlerts(double highTrafficRps, long highTrafficTimeWindow) 
	{
		int thresholdRequestCount = (int) Math.ceil(highTrafficRps * (highTrafficTimeWindow / 1000.0));
		double delay = (1000.0 / highTrafficRps);

		System.out.println("thresholdRequestCount = " + thresholdRequestCount + ", delay = " + delay);

		MetricManager metricManager = new MetricManager(highTrafficRps, highTrafficTimeWindow, (long) delay);
		Log log = new Log("1");

		long startTime = System.currentTimeMillis();
		double currentTime = startTime;

		// Trigger the critical threshold
		int currentRequestCount = 0;
		for (; currentRequestCount < thresholdRequestCount; currentRequestCount++) {
			metricManager.addRequest((long) currentTime);
			metricManager.monitorThroughput((long) currentTime);

			currentTime += delay;
			// System.out.println("CurrentTime = " + (currentTime-startTime));
		}
		System.out.println(
				"Current RPS = " + metricManager.getMonitorRps() + ", timeDiff = " + (currentTime - startTime));
		metricManager.monitorThroughput((long) currentTime);
		System.out.println(
				"Current RPS = " + metricManager.getMonitorRps() + ", timeDiff = " + (currentTime - startTime));
		alertEquals(new Alert[] { new Alert(thresholdRequestCount, false) }, metricManager.getAlerts());

		// Sustain the RPS at the threshold and make sure no additional alerts
		// are triggered
		currentTime += delay;
		for (int i = 0; i < thresholdRequestCount; i++) {
			metricManager.addRequest((long) currentTime);
			metricManager.monitorThroughput((long) currentTime);

			// Don't increment the timer after the last request
			if (i != thresholdRequestCount - 1)
				currentTime += delay;

			alertEquals(new Alert[] { new Alert(thresholdRequestCount, false) }, metricManager.getAlerts());
		}

		// Advance time, but not enough to decrease the RPS
		currentTime += delay;
		metricManager.monitorThroughput((long) currentTime);
		alertEquals(new Alert[] { new Alert(thresholdRequestCount, false) }, metricManager.getAlerts());

		// Go below the threshold
		currentTime += delay;
		currentRequestCount--;
		metricManager.monitorThroughput((long) currentTime);
		alertEquals(new Alert[] { new Alert(thresholdRequestCount, false), new Alert(thresholdRequestCount - 1, true) },
				metricManager.getAlerts());

		// Surpass the threshold
		int overshootRequestCount = 1 + thresholdRequestCount * 4;
		for (int i = 0; i < overshootRequestCount; i++) {
			metricManager.addRequest((long) currentTime);
		}
		metricManager.monitorThroughput((long) currentTime);
		alertEquals(new Alert[] { new Alert(thresholdRequestCount, false), new Alert(thresholdRequestCount - 1, true),
				new Alert(thresholdRequestCount * 5, false) }, metricManager.getAlerts());

		// Slide the traffic window until RPS = 0
		currentTime += highTrafficTimeWindow + 1;
		metricManager.monitorThroughput((long) currentTime);
		alertEquals(new Alert[] { new Alert(thresholdRequestCount, false), new Alert(thresholdRequestCount - 1, true),
				new Alert(thresholdRequestCount * 5, false), new Alert(0, true) }, metricManager.getAlerts());
	}

	private void alertEquals(Alert[] expected, ArrayList<Alert> actual) 
	{
		assertEquals(expected.length, actual.size());

		for (int i = 0; i < expected.length; i++) {
			assertTrue(expected[i].equals(actual.get(i)));
		}
	}
}
