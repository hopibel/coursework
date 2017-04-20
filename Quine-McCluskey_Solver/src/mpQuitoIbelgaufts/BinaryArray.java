package mpQuitoIbelgaufts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Class that holds one table used with the Quine-McCluskey (QM) algorithm.
 * Contains methods for generating the next table.
 */
public class BinaryArray
{
	List<List<Row>> groups;
	List<Row> unused;

	/**
	 * Class that stores a row for use with the Quine-McCluskey algorithm.
	 * Stores the binary string and the minterms that went into it.
	 */
	class Row
	{
		private List<Integer> decimals;
		private String binary;
		private boolean used = false;

		/**
		 * @param b - the String representation of the binary number this Row represents
		 */
		public Row(BinaryString b)
		{
			decimals = new ArrayList<Integer>();
			decimals.add(b.toDecimal());
			binary = b.toString();
		}

		private Row()
		{
			decimals = new ArrayList<Integer>();
		}

		/**
		 * @param row - the Row to be compared with this one
		 * @return the number of differing bits. The Hamming distance between the two Rows' binary representations
		 */
		public int countDifferent(Row row)
		{
			int diff = 0;
			for (int i = 0; i < binary.length(); ++i)
			{
				if (binary.charAt(i) != row.binary.charAt(i))
				{
					++diff;
				}
			}
			return diff;
		}

		/**
		 * Merging simply creates a new Row with the minterms of two rows and a binary string where differing bits are replaced with hyphens '-'
		 * @param row - the Row to be merged with this one
		 * @return the merged Row
		 */
		public Row merge(Row row)
		{
			Row merged = new Row();

			// merge the binary strings, hyphens replacing differing bits
			String newbin = "";
			for (int i = 0; i < binary.length(); ++i)
			{
				if (binary.charAt(i) == row.binary.charAt(i))
				{
					newbin += binary.charAt(i);
				}
				else
				{
					newbin += '-';
				}
			}
			merged.binary = newbin;

			// decimals list contains all integers merged into the row
			List<Integer> newdec = new ArrayList<Integer>();
			newdec.addAll(decimals);
			newdec.addAll(row.decimals);
			Collections.sort(newdec);
			merged.decimals = newdec;
			
			return merged;
		}

		/**
		 * @return the list of minterms (base-10) that were merged to create this Row
		 */
		public List<Integer> getDecimal()
		{
			return decimals;
		}
		
		/**
		 * @return the binary representation of the Row as a String
		 */
		public String getBinary()
		{
			return binary;
		}

		private void setUsed()
		{
			used = true;
		}

		private boolean isUsed()
		{
			return used;
		}
	}

	private class BinaryString
	{
		private int decimal;
		private String binary;

		public BinaryString(int n)
		{
			decimal = n;
			binary = Integer.toBinaryString(n);
		}

		public BinaryString padTo(int bits)
		{
			binary = String.format("%" + bits + "s", binary).replace(' ', '0');
			return this;
		}
		
		public String toString()
		{
			return binary;
		}
		
		public int toDecimal()
		{
			return decimal;
		}
		
		public int countOnes()
		{
			int ones = 0;
			for (char c : binary.toCharArray())
			{
				ones += c == '1' ? 1 : 0;
			}
			return ones;
		}
		
		public int countBits()
		{
			int bits = 0;
			int n = decimal;
			while (n > 0)
			{
				++bits;
				n = n >> 1;
			}
			return bits > 0 ? bits : 1;
		}
	}

	/**
	 * @param minterms - list of minterms in base-10 for generating the first table for the QM algorithm
	 * All succeeding tables are generated via the generateNext() method.
	 */
	public BinaryArray(List<Integer> minterms)
	{
		groups = new ArrayList<List<Row>>();
		unused = new ArrayList<Row>();

		// sort minterms and remove duplicates
		Collections.sort(minterms);
		minterms = new ArrayList<Integer>(new LinkedHashSet<Integer>(minterms));
		List<BinaryString> binary = new ArrayList<BinaryString>();
		for (int i : minterms)
		{
			binary.add(new BinaryString(i));
		}
		// pad everything to the same number of bits
		int length = binary.get(binary.size() - 1).countBits();
		for (BinaryString b : binary)
		{
			b.padTo(length);
		}
		// group by number of ones
		for (int bits = 0; bits <= length; ++bits)
		{
			List<Row> group = new ArrayList<Row>();
			for (BinaryString b : binary)
			{
				if (b.countOnes() == bits)
				{
					group.add(new Row(b));
				}
			}
			if (group.size() > 0) groups.add(group);
		}
	}

	private BinaryArray()
	{
		groups = new ArrayList<List<Row>>();
		unused = new ArrayList<Row>();
	}

	/**
	 * Generate the next table following the QM algorithm.
	 * @return - BinaryArray array representing the next table
	 */
	public BinaryArray generateNext()
	{
		if (groups.size() == 1)
		{
			for (Row r : groups.get(0))
			{
				unused.add(r);
			}
			return null;
		}

		BinaryArray next = new BinaryArray();

		// for each group
		for (int g = 0; g < groups.size() - 1; ++g)
		{
			List<Row> group = new ArrayList<Row>();
			// for each row
			for (int r = 0; r < groups.get(g).size(); ++r)
			{
				Row row = groups.get(g).get(r);
				// for each row in the next group
				List<Row> gn = groups.get(g+1);
				for (int rn = 0; rn < gn.size(); ++rn)
				{
					if (row.countDifferent(gn.get(rn)) == 1)
					{
						group.add(row.merge(gn.get(rn)));
						row.setUsed();
						gn.get(rn).setUsed();
					}
				}
			}
			// remove duplicates because god help you otherwise if you want more than 5 minterms
			List<Row> tmp = new ArrayList<Row>();
			for (int i = 0; i < group.size(); ++i)
			{
				boolean dup = false;
				for (int j = 0; j < tmp.size(); ++j)
				{
					if (group.get(i).getDecimal().equals(tmp.get(j).getDecimal()))
					{
						dup = true;
						break;
					}
				}
				if (!dup) tmp.add(group.get(i));
			}
			next.groups.add(tmp);
		}

		for (List<Row> group : groups)
		{
			for (Row row : group)
			{
				if (!row.isUsed())
				{
					unused.add(row);
				}
			}
		}

		return next;
	}

	/**
	 * @return A list of all unused rows from this particular table. Empty if generateNext() has not been called
	 */
	public List<Row> getUnused()
	{
		return unused;
	}
	
	public String toString()
	{
		String output = "==========\n";
		for (List<Row> group : groups)
		{
			for (Row row : group)
			{
				output += "[";
				for (int minterm : row.getDecimal())
				{
					output += minterm;
					if (minterm != row.getDecimal().get(row.getDecimal().size() - 1)) output += " ";
				}
				output += "]" + (row.isUsed() ? "*" : "") + "\t" + row.getBinary() + "\n";
			}
			if (group.size() > 0) output += "==========\n";
		}
		return output;
	}
}