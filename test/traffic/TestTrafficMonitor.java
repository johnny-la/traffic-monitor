package traffic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import traffic.monitor.Alert;
import traffic.monitor.ThroughputMonitor;

public class TestTrafficMonitor 
{
    @Test
    public void testAlerts() 
    {
        testAlerts(10, 5000);
        testAlerts(500, 1000);
        testAlerts(1000, 120000); 
    }

    /**
     * Test alerts for the given parameters.
     * @param highTrafficRps If average RPS exceeds this value, trigger an alert
     * @param highTrafficTimeWindow The time window for which traffic is monitored
     */
    public void testAlerts(double highTrafficRps, long highTrafficTimeWindow) 
    {
        // The number of requests which will trigger the alert
        int thresholdRequestCount = (int) Math.ceil(highTrafficRps * (highTrafficTimeWindow / 1000.0));
        // The delay between sending each request
        double delay = (1000.0 / highTrafficRps);

        ThroughputMonitor monitor = new ThroughputMonitor(highTrafficRps, highTrafficTimeWindow, (long)delay);
        // The timestamps of each expected alert
        ArrayList<Long> alertTimes = new ArrayList<Long>(); 

        long startTime = System.currentTimeMillis();
        double currentTime = startTime;
        
        // Trigger the critical threshold
        int currentRequestCount = 0;
        for (; currentRequestCount < thresholdRequestCount; currentRequestCount++) 
        {
            monitor.addRequest((long)currentTime);
            monitor.update((long)currentTime);

            currentTime += delay;
        }
        alertTimes.add((long)(currentTime-delay));
        alertEquals(new Alert[]{ 
                new Alert(thresholdRequestCount, false, alertTimes.get(0))}, 
                monitor.getAlerts());

        // Sustain the RPS at the threshold and make sure no additional alerts are triggered
        currentTime += delay;
        for (int i = 0; i < thresholdRequestCount; i++) 
        {
            monitor.addRequest((long)currentTime);
            monitor.update((long)currentTime);

            // Don't increment the timer after the last request
            if (i != thresholdRequestCount - 1)
                currentTime += delay;

            alertEquals(new Alert[] { 
                    new Alert(thresholdRequestCount, false, alertTimes.get(0))}, 
                    monitor.getAlerts());
        }

        // Advance time, but not enough to decrease the RPS
        currentTime += delay;
        monitor.update((long)currentTime);
        alertEquals(new Alert[] { 
                new Alert(thresholdRequestCount, false, alertTimes.get(0))}, 
                monitor.getAlerts());

        // Go below the threshold
        currentTime += delay;
        currentRequestCount--;
        monitor.update((long) currentTime);
        alertTimes.add((long)currentTime);
        alertEquals(new Alert[] { 
                new Alert(thresholdRequestCount, false, alertTimes.get(0)), 
                new Alert(thresholdRequestCount - 1, true, alertTimes.get(1))},
                monitor.getAlerts());

        // Surpass the threshold
        int overshootRequestCount = 1 + thresholdRequestCount * 4;
        for (int i = 0; i < overshootRequestCount; i++) 
        {
            monitor.addRequest((long)currentTime);
        }
        monitor.update((long)currentTime);
        alertTimes.add((long)currentTime);
        alertEquals(new Alert[] { 
                new Alert(thresholdRequestCount, false, alertTimes.get(0)), 
                new Alert(thresholdRequestCount - 1, true, alertTimes.get(1)),
                new Alert(thresholdRequestCount * 5, false, alertTimes.get(2))}, 
                monitor.getAlerts());

        // Slide the traffic window until RPS = 0
        currentTime += highTrafficTimeWindow + 1;
        monitor.update((long)currentTime);
        alertTimes.add((long)currentTime);
        alertEquals(new Alert[] { 
                new Alert(thresholdRequestCount, false, alertTimes.get(0)), 
                new Alert(thresholdRequestCount - 1, true, alertTimes.get(1)),
                new Alert(thresholdRequestCount * 5, false, alertTimes.get(2)), 
                new Alert(0, true, alertTimes.get(3))}, 
                monitor.getAlerts());
    }

    /**
     * Returns true if the alert lists are equals
     * @param expected The expected alert result
     * @param actual The actual alert result
     */
    private void alertEquals(Alert[] expected, ArrayList<Alert> actual) 
    {
        assertEquals(expected.length, actual.size());

        for (int i = 0; i < expected.length; i++) 
        {
            assertTrue(expected[i].equals(actual.get(i)));
        }
    }
}
