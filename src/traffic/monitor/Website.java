package traffic.monitor;
import java.util.ArrayList;
import java.util.HashSet;

public class Website
{
    /** The hostname of the website */
    private String name;
    /** The sections of this website that were hit */
    private HashSet<String> sections;
    /** The total number of times this website was hit */
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
     * Adds a section to the website
     * @param section A section of the website that was hit
     */
    public void addSection(String section)
    {
        sections.add(section);
    }
    
    /**
     * Returns a list of all website sections that received a request 
     * @return The sections that were hit
     */
    public HashSet<String> getSections()
    {
        return sections;
    }
    
    /**
     * Returns the hostname of the website
     * @return The website's hostname
     */
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