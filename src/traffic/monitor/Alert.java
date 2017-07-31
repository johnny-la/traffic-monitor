package traffic.monitor;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Alert 
{
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
    
    private int hits;
    private boolean recovery;    // True if this is a recovery alert. Else this is critical alert
    private long timestamp;
    private String date;
    
    /**
     * Creates an alert for the current timestamp
     * @param hits The number of website hits when the alert was triggered
     * @param recovery True if this is a recovery alert. Else this is a critical alert
     */
    public Alert(int hits, boolean recovery)
    {
        this(hits, recovery, System.currentTimeMillis());
    }
    
    /**
     * Creates an alert at the given timestamp
     * @param hits The number of website hits when the alert was triggered
     * @param recovery True if this is a recovery alert. Else this is a critical alert
     * @param timestamp The timestamp when this alert was triggered
     */
    public Alert(int hits, boolean recovery, long timestamp)
    {
        this.hits = hits;
        this.recovery = recovery;
        
        this.timestamp = timestamp;
        date = getDate(timestamp);
    }
    
    /**
     * Converts the timestamp into a human-readable date
     * @param timestamp The timestamp to convert to a date
     * @return The timestamp in date format
     */
    private String getDate(long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return DATE_FORMATTER.format(calendar.getTime());
    }
    
    public String toString()
    {
        String output = "";
        if (!recovery)
            output += "[CRITICAL] High traffic generated an alert";
        else
            output += "[RECOVERY] High traffic has recovered";
        
        output += " - hits = " + hits + " triggered at " + date;
        
        return output;
    }
    
    public boolean equals(Alert other)
    {
        return this.recovery == other.recovery 
                && this.hits == other.hits
                && this.timestamp == other.timestamp;
    }
}
