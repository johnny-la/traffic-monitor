import java.util.ArrayList;
import java.util.HashSet;

public class Website
{
	private String name;
	private HashSet<String> sections;
	private int hits;
	
	public Website()
	{
		sections = new HashSet<String>();
	}
	
	public Website(String name)
	{
		this();
		this.name = name;
	}
	
	/**
	 * Increments the number of times the website received a request
	 */
	public void incrementHits()
	{
		hits++;
	}
	
	/**
	 * Returns the number of times a request was sent to this website
	 */
	public int getHits()
	{
		return hits;
	}
	
	/**
	 * Adds a section of the form "pages" to the website
	 * @param section
	 */
	public void addSection(String section)
	{
		sections.add(section);
	}
	
	public HashSet<String> getSections()
	{
		return sections;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String toString()
	{
		StringBuffer output = new StringBuffer();
		for (String section : sections)
		{
			output.append(section + "\n");
		}
		return output.toString();
	}
}