package traffic.utils;
import java.util.ArrayList;
import java.util.Arrays;

public class PrettyPrinter
{
	/** Minimum number of spaces in a column */
	private static final int MIN_COLUMN_LENGTH = 10;
	/** Padding after longest element in column */
	private static final int COLUMN_PADDING = 5;	
	
	/** If a row contains this entry, print a horizontal separator in this row */
	private static final String HORIZONTAL_SEPARATOR_CODE = "---";
	
	private ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
	
	/**
	 * Starts a new table and waits for incoming rows
	 */
	public void startTable()
	{
		table.clear();
	}
	
	/**
	 * Adds a row to the current table
	 */
	public void addRow(String... columns)
	{
		table.add(new ArrayList<String>(Arrays.asList(columns)));
	}
	
	/**
	 * Adds an element to the current row
	 */
	public void addEntry(String entry)
	{
		table.get(table.size()-1).add(entry);
	}
	
	/**
	 * Prints the table that was built
	 */
	public void endTable()
	{
		// Determine the length of each column
		ArrayList<Integer> columnLengths = new ArrayList<Integer>();
		int maxColumns = 0;
		
		for (int i = 0; i < table.size(); i++)
		{
			ArrayList<String> row = table.get(i);
			for (int j = 0; j < row.size(); j++)
			{
				String entry = row.get(j);

				int columnLength = entry.length() + COLUMN_PADDING;
				if (columnLengths.size() <= j)
				{
					columnLengths.add(Math.max(MIN_COLUMN_LENGTH, columnLength));
				}
				else
				{
					columnLengths.set(j, Math.max(columnLengths.get(j), columnLength));
				}
			}
			
			if (row.size() > maxColumns)
				maxColumns = row.size();
		}
		
		// Print the table
		printHorizontalSeparator(columnLengths);
		System.out.println();
		for (int i = 0; i < table.size(); i++)
		{
			ArrayList<String> row = table.get(i);
			System.out.print("| ");
			for (int j = 0; j < maxColumns; j++)
			{			
				String entry = (j < row.size())? row.get(j) : "";
				int numSpaces = columnLengths.get(j) - entry.length();
				
				// Print horizontal separator
				if (entry.equals(HORIZONTAL_SEPARATOR_CODE))
				{
					printHorizontalSeparator(columnLengths);
					break;
				}
				
				System.out.print(entry);
				printSpaces(numSpaces); // Add space padding to the column
				System.out.print(" | ");
			}
			System.out.println();
		}
		
		printHorizontalSeparator(columnLengths);
		System.out.println();
	}
	
	/**
	 * Adds a horizontal separator in the table
	 */
	public void addHorizontalSeparator()
	{
		addRow(HORIZONTAL_SEPARATOR_CODE);
	}

	/**
	 * Prints horizontal bar separators for each column
	 * @param columnLengths The length of each column of horizontal bars
	 */
	private void printHorizontalSeparator(ArrayList<Integer> columnLengths)
	{
		for (int j = 0; j < columnLengths.size(); j++)
		{
			for (int k = 0; k < columnLengths.get(j)/1.85; k++)
			{
				System.out.print("--");
			}
		}
	}
	
	/**
	 * Prints the given number of spaces
	 * @param numSpaces The number of spaces to print
	 */
	private void printSpaces(int numSpaces)
	{
		for (int i = 0; i < numSpaces; i++)
			System.out.print(" ");
	}
}