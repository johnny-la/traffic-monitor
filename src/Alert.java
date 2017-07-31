import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Alert 
{
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
	
	private int hits;
	private boolean recovery;	// True if this is a recovery alert. Else this is critical alert
	private long timestamp;
	private String date;
	
	/**
	 * Creates an alert for the current timestamp
	 * @param hits The number of website hits when the alert was triggered
	 * @param recovery True if this is a recovery alert. Else this is a critical alert
	 */
	public Alert(int hits, boolean recovery)
	{
		this.hits = hits;
		this.recovery = recovery;
		
		timestamp = System.currentTimeMillis();
		date = getCurrentDate();
	}
	
	/**
	 * Returns the current date in a human-readable format
	 * @return The current date 
	 */
	private String getCurrentDate()
	{
		Calendar calendar = Calendar.getInstance();
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
		return this.recovery == other.recovery && this.hits == other.hits;
	}
}
