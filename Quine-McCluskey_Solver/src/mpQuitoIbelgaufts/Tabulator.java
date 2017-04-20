package mpQuitoIbelgaufts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * static class that implements the Quine-McCluskey algorithm for simplifying boolean expressions
 * represented as a sum of minterms
 */
public class Tabulator
{

	/**
	 * @param minterms - minterms as base-10 integers
	 * @return A string containing the simplified form of the minterms
	 */
	public static String simplify(List<Integer> minterms)
	{
		List<BinaryArray> tables = new ArrayList<BinaryArray>();
		BinaryArray table = new BinaryArray(minterms);
		tables.add(table);
		// generate the next table until we start getting null
		while ((table = table.generateNext()) != null)
		{
			System.out.println(tables.get(tables.size()-1));
			tables.add(table);
		}
		System.out.println(tables.get(tables.size()-1));

		// get unused rows from tables, sort their component decimals
		List<BinaryArray.Row> tmp = new ArrayList<BinaryArray.Row>();
		List<BinaryArray.Row> unused = new ArrayList<BinaryArray.Row>();
		for (BinaryArray t : tables)
		{
			for (BinaryArray.Row row : t.getUnused())
			{
				Collections.sort(row.getDecimal());
				tmp.add(row);
			}
		}

		// remove duplicates
		for (int i = 0; i < tmp.size(); ++i)
		{
			boolean dup = false;
			for (int j = 0; j < unused.size(); ++j)
			{
				if (tmp.get(i).getDecimal().equals(unused.get(j).getDecimal()))
				{
					dup = true;
					break;
				}
			}
			if (!dup) unused.add(tmp.get(i));
		}

		// Find essential terms
		List<BinaryArray.Row> essential = new ArrayList<BinaryArray.Row>();
		for (int term : minterms)
		{
			List<BinaryArray.Row> rows = new ArrayList<BinaryArray.Row>();
			for (BinaryArray.Row row : unused)
			{
				if (row.getDecimal().contains(term)) rows.add(row);
			}

			if (rows.size() == 1)
			{
				// for every row in rows
				boolean dup = false;
				for (BinaryArray.Row row : essential)
				{
					if (row.equals(rows.get(0)))
					{
						dup = true;
						break;
					}
				}
				if (!dup) essential.add(rows.get(0));
			}
		}

		// create list of minterms not used yet
		List<Integer> missing = new ArrayList<Integer>(minterms);
		for (BinaryArray.Row row : essential)
		{
			missing.removeAll(row.getDecimal());
		}

		// add row that matches the most unused minterms until no unused minterms
		unused.removeAll(essential);
		while (missing.size() > 0)
		{
			BinaryArray.Row best = unused.get(0);
			int bestMatch = 0;
			for (BinaryArray.Row row : unused)
			{
				int match = 0;
				for (int i : row.getDecimal())
				{
					if (missing.contains(i)) 
					{
						++match;
					}
				}
				if (match > bestMatch)
				{
					best = row;
					bestMatch = match;
				}
			}
			essential.add(best);
			missing.removeAll(best.getDecimal());
		}

		String vars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		String output = "";

		if (essential.size() == 1 && essential.get(0).getBinary().equals(new String(new char[essential.get(0).getBinary().length()]).replace("\0", "-")))
		{
			output = "No rows used\nF = 1";
		}
		else
		{
			// in the unlikely event that we run out of letters, print directly as binary
			boolean overload = essential.get(0).getBinary().length() > vars.length();
			if (overload) output = "warning: not enough variables to represent terms\nF = ";

			String expression = "F = ";
			output += "Rows used:\n";
			for (BinaryArray.Row row : essential)
			{
				output += row.getDecimal() + "\n";
				String binary = row.getBinary();
				for (int i = 0; i < binary.length(); ++i)
				{
					if (binary.charAt(i) == '0')
					{
						expression += (overload ? "(" + i + ")" : vars.charAt(i)) + "'";
					} else if (binary.charAt(i) == '1')
					{
						expression += (overload ? "(" + i + ")" : vars.charAt(i));
					}
				}
				if (row != essential.get(essential.size() - 1))
				{
					expression += " + ";
				}
			}
			output += expression;
		}

		return output;
	}
}