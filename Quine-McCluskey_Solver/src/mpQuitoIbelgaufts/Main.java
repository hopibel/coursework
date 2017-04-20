package mpQuitoIbelgaufts;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Provides simple console interface for Tabulator
 * minterms must be entered as a list of comma-separated integers
 * All other input will be rejected (except "exit", which exits)
 */
public class Main
{
	public static void main(String args[])
	{
		System.out.println("Quine-McCluskey solver");
		System.out.println("Type 'exit' to finish");
		boolean quit = false;
		String input;
		Scanner in = new Scanner(System.in);
		while (!quit)
		{
			List<Integer> minterms = new ArrayList<Integer>();
			boolean valid = false;
			while (!valid)
			{
				System.out.println("Enter minterms separated by commas");
				input = in.nextLine();
				if (input.equals("exit"))
				{
					quit = true;
					break;
				}
				try
				{
					valid = true;
					for (String s : input.split("\\s*,\\s*"))
					{
						int i = Integer.parseInt(s);
						if (i < 0)
						{
							System.out.println("Bad input: negative number");
							valid = false;
							break;
						}
						minterms.add(i);
					}
				}
				catch (NumberFormatException e)
				{
					System.out.println("Bad input: not a number");
					valid = false;
				}
			}
			if (!quit) System.out.println(Tabulator.simplify(minterms));
		}

		in.close();
	}
}
