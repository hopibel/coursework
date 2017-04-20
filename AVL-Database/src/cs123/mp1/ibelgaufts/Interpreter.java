package cs123.mp1.ibelgaufts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpreter {
	public static void parse(Database db, Scanner input) {
		Map<String, Command> methods = new HashMap<String, Command>();
		methods.put("select", new select());
		methods.put("insert", new insert());
		methods.put("delete", new delete());
		methods.put("update", new update());
		methods.put("create", new create());
		methods.put("drop", new drop());
		methods.put("help", new help());

		Scanner command = new Scanner(input.nextLine());
		String cmd = command.next();
		
		if(methods.containsKey(cmd)) {
			methods.get(cmd).execute(db, command);
		} else {
			System.out.println("Unknown command: " + cmd);
		}
	}
}

// Strategy pattern command classes
class searchCommand {
	protected List<Integer> filterIndexes(Database db, String title, List<List<String>> filters) throws IllegalArgumentException, NoSuchElementException {
		Table table = db.getTable(title);
		if(table == null) {
			throw new NoSuchElementException("Table \"" + title + "\" not found");
		} else if(table.getHeaders().isEmpty()) {
			throw new IllegalArgumentException("Operations on empty tables are not supported.");
		}

		List<Integer> indexes = new ArrayList<Integer>();
		for(int i = 0; i < table.getRecordCount(); ++i) {
			indexes.add(i);
		}
		if(filters == null) {
			return indexes;
		}

		// Check that all filter fields exist
		if(filters != null) {
			for(List<String> filter : filters) {
				if(!table.getHeaders().contains(filter.get(0)) &&
				   !filter.get(0).equals("*") &&
				   !filter.get(0).equals("RowNo")) {
					throw new NoSuchElementException("Error: no such field: " + filter.get(0));
				}
			}
		}

		// List of relational operators for multiple filters
		List<RelationalOperator> cmp = new ArrayList<RelationalOperator>();
		for(List<String> filter : filters) {
			// Choose a comparator
			switch(filter.get(1)) {
			case "=":
				cmp.add(RelationalOperator.EQ);
				break;
			case "<":
				cmp.add(RelationalOperator.LT);
				break;
			case ">":
				cmp.add(RelationalOperator.GT);
				break;
			case "<=":
				cmp.add(RelationalOperator.LE);
				break;
			case ">=":
				cmp.add(RelationalOperator.GE);
				break;
			default:
				throw new IllegalArgumentException("Syntax error: " + Utils.join(filter.toArray(new String[0]), " "));
			}
		}

		List<List<String>> specialfilters = new ArrayList<List<String>>();
		List<RelationalOperator> specialcmp = new ArrayList<RelationalOperator>();
		if(filters != null) {
			for(int i = 0; i < filters.size(); ++i) {
				// AVL-optimized search for operators other than equality not yet implemented
				List<String> filter = filters.get(i);
				if(!filter.get(1).equals("=")) {
					specialfilters.add(filter);
					specialcmp.add(cmp.get(i));
					continue;
				}
				if(filter.get(0).equals("*")) {
					specialfilters.add(filter);
					specialcmp.add(cmp.get(i));
					continue;
				}
				if(filter.get(0).equals("RowNo")) {
					specialfilters.add(filter);
					specialcmp.add(cmp.get(i));
					continue;
				}
				if(filter.get(2).equals("*")) {
					specialfilters.add(filter);
					specialcmp.add(cmp.get(i));
					continue;
				}
				if(!db.hasIndex(title, filter.get(0))) {
					specialfilters.add(filter);
					specialcmp.add(cmp.get(i));
					continue;
				}
				indexes.retainAll(db.getIndexes(title, filter.get(0), filter.get(1), filter.get(2)));
			}
		}

		for(Iterator<Integer> it = indexes.iterator(); it.hasNext();) {
			Integer index = it.next();
			if(db.isUnused(title, index)) {
				continue;
			}
			Map<String, String> record = table.getRecord(index);
			boolean skip = false;
	
			for(int i = 0; i < specialfilters.size(); ++i) {
				List<String> filter = specialfilters.get(i);

				if(filter.get(0).equals("*")) {
					if(filter.get(2).equals("*")) {
						continue;
					} else if(!record.containsValue(filter.get(2))) {
						skip = true;
						break;
					}
				} else if(filter.get(0).equals("RowNo")) {
					if(filter.get(2).equals("*")) {
						continue;
					} else {
						try {
							if(!specialcmp.get(i).apply(index, Integer.parseInt(filter.get(2)))){
								skip = true;
								break;
							}
						} catch(NumberFormatException e) {
							throw new IllegalArgumentException("Syntax error: " + Utils.join(filter.toArray(new String[0]), " "));
						}
					}
				} else if(filter.get(2).equals("*")) {
					continue;
				} else if(!specialcmp.get(i).apply(table.getRecord(index).get(filter.get(0)), filter.get(2))) {
					skip = true;
					break;
				}
			}
			
			if(skip) {
				it.remove();
				continue;
			}
		}
	
		return indexes;
	}
}

interface Command {
	public void execute(Database db, Scanner args);
}

// TODO: move this monster to its own file?
// No time. Gotta go fast
class select extends searchCommand implements Command {
	public void execute(Database db, Scanner args) {
		/*
		 * print out selected fields
		 * select <comma-separated fields> from <table> where <field> <comparator> <value>
		 *
		 * "\\bfrom\\b" : match "from" surrounded by word boundaries
		 * "\\s*,\\s*" : match comma surrounded by zero or more whitespace characters
		 * "\\bwhere\\b" : match "where" surrounded by word boundaries
		 * "(?<![<>=])(?=([<>]=?|=))|(?![<>=])(?<=[<>=])" : match empty strings around relational operators
		 *
		 * No, I did not copy the last from somewhere.
		 * Yes, it was a pain in the ass.
		 */

		List<String> fields = null;
		String title = null;
		try {
			fields = Arrays.asList(args.useDelimiter("\\bfrom\\b").next().trim().split("\\s*,\\s*"));
			args.reset().next();
			title = args.useDelimiter("\\bwhere\\b").next().trim();
			args.reset();
		} catch(NoSuchElementException e) {
			System.out.println("Syntax error. Type \"help\" for command arguments");
			return;
		}

		List<List<String>> filters = null;
//		List<RelationalOperator> cmp = null;

		// handle where condition
		if(args.hasNext()) {
			args.next();
			filters = new ArrayList<List<String>>();
			for(String filter : Arrays.asList(args.nextLine().split("\\s*,\\s*"))) {
				filters.add(Arrays.asList(filter.split("(?<![<>=])(?=([<>]=?|=))|(?![<>=])(?<=[<>=])")));
			}
			// Valid filter should have 3 tokens
			for(List<String> filter : filters) {
				if(filter.size() != 3) {
					System.out.println("Syntax error: " + Utils.join(filter.toArray(new String[0]), " ").trim());
					return;
				}
			}

			for(List<String> filter : filters) {
				for(int i = 0; i < filter.size(); ++i) {
					filter.set(i, filter.get(i).trim());
				}
			}

/*			// List of relational operators for multiple filters
			cmp = new ArrayList<RelationalOperator>();
			for(List<String> filter : filters) {
				// Choose a comparator
				switch(filter.get(1)) {
				case "=":
					cmp.add(RelationalOperator.EQ);
					break;
				case "<":
					cmp.add(RelationalOperator.LT);
					break;
				case ">":
					cmp.add(RelationalOperator.GT);
					break;
				case "<=":
					cmp.add(RelationalOperator.LE);
					break;
				case ">=":
					cmp.add(RelationalOperator.GE);
					break;
				default:
					System.out.println("Syntax error: " + Utils.join(filter.toArray(new String[0]), " "));
					return;
				}
			}
*/
		}

		Table table = db.getTable(title);
		if(table == null) {
			System.out.println("Table \"" + title + "\" not found");
			return;
		} else if(table.getHeaders().isEmpty()) {
			System.out.println("Operations on empty tables are not supported.");
			return;
		}

		List<String> headers = new ArrayList<String>();
		if(fields.contains("*")) {
			headers = table.getHeaders();
		} else {
			for(String f : fields) {
				if(table.getHeaders().contains(f) || f.equals("*")) {
					headers.add(f);
				} else {
					System.out.println("Error: no such field: " + f);
					return;
				}
			}
		}

		// Check that all filter fields exist
		if(filters != null) {
			for(List<String> filter : filters) {
				if(!table.getHeaders().contains(filter.get(0)) &&
				   !filter.get(0).equals("*") &&
				   !filter.get(0).equals("RowNo")) {
					System.out.println("Error: no such field: " + filter.get(0));
					return;
				}
			}
		}

		System.out.println(Utils.join(headers.toArray(new String[0]), "\t"));

		List<Integer> indexes = null;
		try {
			indexes = filterIndexes(db, title, filters);
		} catch(IllegalArgumentException e) {
			System.out.println(e.getMessage());
			return;
		}
/*		List<Integer> indexes = new ArrayList<Integer>();

		for(int i = 0; i < table.getRecordCount(); ++i) {
			indexes.add(i);
		}

		if(filters != null) {
			for(List<String> filter : filters) {
				// AVL-optimized search for operators other than equality not yet implemented
				if(filter.get(1).equals("=") && !filter.get(0).equals("*") && !filter.get(0).equals("RowNo") && !filter.get(2).equals("*") && db.hasIndex(title, filter.get(0))) {
					indexes.retainAll(db.getIndexes(title, filter.get(0), filter.get(2)));
				}
			}
		}

		for(int i = 0; i < indexes.size(); ++i) {
			Map<String, String> record = table.getRecord(indexes.get(i));
			List<String> row = null;
			// Skip row if any filter evaluates to false
			if(filters != null) {
				boolean skip = true;
				for(int j = 0; j < filters.size(); ++j) {
					List<String> filter = filters.get(j);

					if(filter.get(0).equals("*")) {
						if(filter.get(2).equals("*")) {
							skip = false;
							break;
						} else if(record.containsValue(filter.get(2))) {
							skip = false;
							break;
						}
					} else if(filter.get(0).equals("RowNo")) {
						if(filter.get(2).equals("*")) {
							skip = false;
							break;
						} else if(cmp.get(j).apply(indexes.get(i), Integer.parseInt(filter.get(2)))) {
							skip = false;
							break;
						}
					} else if(filter.get(2).equals("*")) {
						skip = false;
						break;
					} else if(cmp.get(j).apply(table.getRecord(indexes.get(i)).get(filter.get(0)), filter.get(2))) {
						skip = false;
						break;
					}

					if(!filter.get(0).equals("*")) {
						if(filter.get(0).equals("RowNo")) {
							if(!cmp.get(j).apply(i, Integer.parseInt(filter.get(2)))) {
								skip = true;
								break;
							}
						} else if(filter.get(2).equals("*")) {
								break;
						} else if(!cmp.get(j).apply(table.getRecord(i).get(filter.get(0)), filter.get(2))) {
							skip = true;
							break;
						}
					} else {
						if(!filter.get(2).equals("*")) {
							if(!record.containsValue(filter.get(2))) {
								skip = true;
								break;
							}
						}
					}
				}

				if(skip) {
					continue;
				}
			}

//			if(condition != null) {
//				if(!condition.get(0).equals("*")) {
//					if(condition.get(0).equals("RowNo")) {
//						if(!cmp.apply(i, Integer.parseInt(condition.get(2)))) {
//							continue;
//						}
//					} else if(!cmp.apply(table.getRecord(i).get(condition.get(0)), condition.get(2))) {
//						continue;
//					}
//				} else if(!condition.contains("*")) {
//					continue;
//				}
//			}
			
			row = new ArrayList<String>();
			for(String f : headers) {
				row.add(record.get(f));
			}
*/
		for(int i : indexes) {
			if(db.isUnused(title, i)) {
				continue;
			}
			List<String> row = new ArrayList<String>();
			for(String f : headers) {
				row.add(table.getRecord(i).get(f));
			}
			System.out.println(Utils.join(row.toArray(new String[0]), "\t"));
		}

	}
}

class insert implements Command {
	/*
	 * Append new record
	 * insert <table> values(<comma delimited list>)
	 */
	public void execute(Database db, Scanner args) {
		Map<String, String> record = new HashMap<String, String>();

		String title = null;
		List<String> values = null;
		String line = null;
		try {
			title = args.useDelimiter("\\s*values\\s*").next().trim();

			// StackOverflow powers activate
			line = args.skip("\\s*values\\s*").nextLine().trim();
			Matcher m = Pattern.compile("\\((.*)\\)", Pattern.DOTALL).matcher(line);
			if(m.find()) {
				values = Arrays.asList(m.group(1).split("\\s*,\\s*"));
			}
//			System.out.println(values.toString());
		} catch(NoSuchElementException e) {
			System.out.println("Syntax error");
			return;
		}
		
		if(db.getTable(title) == null) {
			System.out.println("Error: no such table: " + title);
			return;
		}
		List<String> headers = db.getTable(title).getHeaders();
		int commas = 0;
		for(int i = 0; i < line.length(); ++i) {
			if(line.charAt(i) == ',') {
				++commas;
			}
		}
		if(commas + 1 != headers.size()) {
			System.out.println("Expected " + headers.size() + " values. Got " + (commas + 1) + " instead.");
			return;
		}
		
		for(int i = 0; i < values.size() && i < headers.size(); ++i) {
			record.put(headers.get(i), values.get(i));
		}

		int free = db.getUnused(title);
		if(free < db.getTable(title).getRecordCount()) {
			db.getTable(title).remove(free);
		}
		db.getTable(title).add(free, record);

		try {
			db.write(title);
		} catch (IOException e) {
			System.out.println("Couldn't write \"" + title + "\" to disk:" + e.getMessage());
		}
		
/*		try {
			db.updateCache(title);
		} catch (IOException e) {
			System.out.println("Couldn't write Indexes.txt: " + e.getMessage());
		}
*/

//		try {
//			fields = Arrays.asList(args.useDelimiter("\\bfrom\\b").next().trim().split("\\s*,\\s*"));
//			args.reset().next();
//		}
	}
}

class delete extends searchCommand implements Command {
	public void execute(Database db, Scanner args) {
		//delete matching records
		String title = null;
		try {
			title = args.useDelimiter("\\bwhere\\b").next().trim();
			args.reset();
		} catch(NoSuchElementException e) {
			System.out.println("Syntax error. Type \"help\" for command arguments");
			return;
		}

		List<List<String>> filters = null;
//		List<RelationalOperator> cmp = null;

		// handle where condition
		if(args.hasNext()) {
			args.next();
			filters = new ArrayList<List<String>>();
			for(String filter : Arrays.asList(args.nextLine().split("\\s*,\\s*"))) {
				filters.add(Arrays.asList(filter.split("(?<![<>=])(?=([<>]=?|=))|(?![<>=])(?<=[<>=])")));
			}

			// Valid filter should have 3 tokens
			for(List<String> filter : filters) {
				if(filter.size() != 3) {
					System.out.println("Syntax error: " + Utils.join(filter.toArray(new String[0]), " ").trim());
					return;
				}
			}

			for(List<String> filter : filters) {
				for(int i = 0; i < filter.size(); ++i) {
					filter.set(i, filter.get(i).trim());
				}
			}

/*			// List of relational operators for multiple filters
			cmp = new ArrayList<RelationalOperator>();
			for(List<String> filter : filters) {
				// Choose a comparator
				switch(filter.get(1)) {
				case "=":
					cmp.add(RelationalOperator.EQ);
					break;
				case "<":
					cmp.add(RelationalOperator.LT);
					break;
				case ">":
					cmp.add(RelationalOperator.GT);
					break;
				case "<=":
					cmp.add(RelationalOperator.LE);
					break;
				case ">=":
					cmp.add(RelationalOperator.GE);
					break;
				default:
					System.out.println("Syntax error: " + Utils.join(filter.toArray(new String[0]), " "));
					return;
				}
			}
*/		}

		Table table = db.getTable(title);
		if(table == null) {
			System.out.println("Table \"" + title + "\" not found");
			return;
		} else if(table.getHeaders().isEmpty()) {
			System.out.println("Operations on empty tables are not supported.");
			return;
		}

		// Check that all filter fields exist
		if(filters != null) {
			for(List<String> filter : filters) {
				if(!table.getHeaders().contains(filter.get(0)) &&
				   !filter.get(0).equals("*") &&
				   !filter.get(0).equals("RowNo")) {
					System.out.println("Error: no such field: " + filter.get(0));
					return;
				}
			}
		}

		List<Integer> indexes = null;
		try {
			indexes = filterIndexes(db, title, filters);
		} catch(IllegalArgumentException e) {
			System.out.println(e.getMessage());
			return;
		}
//		Table modified = new Table(table.getHeaders());

		for(int i = 0; i < table.getRecordCount(); ++i) {
			if(indexes.contains(i)) {
				db.setUnused(title, i);
//				continue;
			}
//			modified.add(table.getRecord(i));
		}

/*		List<Integer> indexes = new ArrayList<Integer>();

		for(int i = 0; i < table.getRecordCount(); ++i) {
			indexes.add(i);
		}

		if(filters != null) {
			for(List<String> filter : filters) {
				// AVL-optimized search for operators other than equality not yet implemented
				if(filter.get(1).equals("=") && !filter.get(0).equals("*") && !filter.get(0).equals("RowNo") && !filter.get(2).equals("*") && db.hasIndex(title, filter.get(0))) {
					indexes.retainAll(db.getIndexes(title, filter.get(0), filter.get(2)));
				}
			}
		}

		// Thanks to RowNo we can't can't delete in-place with iterators
		// RowNo changes if previous elements are deleted, so we have to make a copy of the table
		Table modified = new Table(table.getHeaders());
		for(int i = 0; i < table.getRecordCount(); ++i) {
			// Skip row if any filter evaluates to false
			boolean delete = true;

			if(!indexes.contains(i)) {
				delete = false;
			} else if(filters != null) {
				for(int j = 0; j < filters.size(); ++j) {
					List<String> filter = filters.get(j);

					if(filter.get(0).equals("*")) {
						if(filter.get(2).equals("*")) {
//							delete = true;
//							break;
						} else if(!table.getRecord(i).containsValue(filter.get(2))) {
							delete = false;
							break;
						}
					} else if(filter.get(0).equals("RowNo")) {
						if(filter.get(2).equals("*")) {
//							delete = true;
//							break;
						} else if(!cmp.get(j).apply(i, Integer.parseInt(filter.get(2)))) {
							delete = false;
							break;
						}
					} else if(filter.get(2).equals("*")) {
//						delete = true;
//						break;
					} else if(!cmp.get(j).apply(table.getRecord(i).get(filter.get(0)), filter.get(2))) {
						delete = false;
						break;
					}
				}

				if(delete) continue;
			}

			modified.add(table.getRecord(i));
		}
*/
//		db.addTable(title, modified, true);
/*		try {
			db.write(title);
		} catch (IOException e) {
			System.out.println("Couldn't write table \"" + title + "\": " + e.getMessage());
		}
*/		
//		try {
//			db.updateCache(title);
//		} catch (IOException e) {
//			System.out.println("Couldn't write Indexes.txt: " + e.getMessage());
//		}

	}
}

class update extends searchCommand implements Command {
	public void execute(Database db, Scanner args) {
		// set values
		// update <table> set <field> = <value>, <field> = <value> where <field> <comparator> <value>

		List<List<String>> assign = null;
		String title = null;
		try {
			title = args.useDelimiter("\\bset\\b").next().trim();
			assign = new ArrayList<List<String>>();
			for(String foo : Arrays.asList(args.skip("\\bset\\b").useDelimiter("\\bwhere\\b").next().trim().split("\\s*,\\s*"))) {
				assign.add(Arrays.asList(foo.split("\\s*=\\s*")));
			}
			args.reset();
		} catch(NoSuchElementException e) {
			System.out.println("Syntax error. Type \"help\" for command arguments");
			return;
		}

		// Valid assignment should have 2 tokens
		for(List<String> x : assign) {
			if(x.size() != 2) {
				System.out.println("Syntax error: " + Utils.join(x.toArray(new String[0]), " = ").trim());
				return;
			}
		}

		for(List<String> x : assign) {
			for(int i = 0; i < x.size(); ++i) {
				x.set(i, x.get(i).trim());
			}
		}

		List<List<String>> filters = null;
//		List<RelationalOperator> cmp = null;

		// handle where condition
		if(args.hasNext()) {
			args.skip("\\bwhere\\b");
			filters = new ArrayList<List<String>>();
			for(String filter : Arrays.asList(args.nextLine().split("\\s*,\\s*"))) {
				filters.add(Arrays.asList(filter.split("(?<![<>=])(?=([<>]=?|=))|(?![<>=])(?<=[<>=])")));
			}

			// Valid filter should have 3 tokens
			for(List<String> filter : filters) {
				if(filter.size() != 3) {
					System.out.println("Syntax error: " + Utils.join(filter.toArray(new String[0]), " ").trim());
					return;
				}
			}
			
			for(List<String> filter : filters) {
				for(int i = 0; i < filter.size(); ++i) {
					filter.set(i, filter.get(i).trim());
				}
			}

/*			// List of relational operators for multiple filters
			cmp = new ArrayList<RelationalOperator>();
			for(List<String> filter : filters) {
				// Choose a comparator
				switch(filter.get(1)) {
				case "=":
					cmp.add(RelationalOperator.EQ);
					break;
				case "<":
					cmp.add(RelationalOperator.LT);
					break;
				case ">":
					cmp.add(RelationalOperator.GT);
					break;
				case "<=":
					cmp.add(RelationalOperator.LE);
					break;
				case ">=":
					cmp.add(RelationalOperator.GE);
					break;
				default:
					System.out.println("Syntax error: " + Utils.join(filter.toArray(new String[0]), " "));
					return;
				}
			}
*/
		}
		
		Table table = db.getTable(title);
		if(table == null) {
			System.out.println("Table \"" + title + "\" not found");
			return;
		} else if(table.getHeaders().isEmpty()) {
			System.out.println("Operations on empty tables are not supported.");
			return;
		}

/*		List<String> headers = new ArrayList<String>();
		if(fields.contains("*")) {
			headers = table.getHeaders();
		} else {
			for(String f : fields) {
				if(table.getHeaders().contains(f) || f.equals("*")) {
					headers.add(f);
				} else {
					System.out.println("Error: no such field: " + f);
					return;
				}
			}
		}
*/

		// Check that the fields to be updated exist
		if(!assign.isEmpty()) {
			for(List<String> a : assign) {
				if(!table.getHeaders().contains(a.get(0))) {
					System.out.println("Error: no such field: " + a.get(0));
					return;
				}
			}
		}

		// Check that all filter fields exist
		if(filters != null) {
			for(List<String> filter : filters) {
				if(!table.getHeaders().contains(filter.get(0)) &&
//				   !filter.get(0).equals("*") &&
				   !filter.get(0).equals("RowNo")) {
					System.out.println("Error: no such field: " + filter.get(0));
					return;
				}
			}
		}

		List<Integer> indexes = null;
		try {
			indexes = filterIndexes(db, title, filters);
		} catch(IllegalArgumentException e) {
			System.out.println(e.getMessage());
			return;
		}
		for(Integer i : indexes) {
			for(List<String> x : assign) {
				table.set(i, x.get(0), x.get(1));
			}
		}

/*		List<Integer> indexes = new ArrayList<Integer>();

		for(int i = 0; i < table.getRecordCount(); ++i) {
			indexes.add(i);
		}

		if(filters != null) {
			for(List<String> filter : filters) {
				// AVL-optimized search for operators other than equality not yet implemented
				if(filter.get(1).equals("=") && !filter.get(0).equals("*") && !filter.get(0).equals("RowNo") && !filter.get(2).equals("*") && db.hasIndex(title, filter.get(0))) {
					indexes.retainAll(db.getIndexes(title, filter.get(0), filter.get(2)));
				}
			}
		}


		for(int i = 0; i < table.getRecordCount(); ++i) {
			// Skip row if any filter evaluates to false
			boolean update = true;
			
			if(!indexes.contains(i)) {
				update = false;
			} else if(filters != null) {
				for(int j = 0; j < filters.size(); ++j) {
					List<String> filter = filters.get(j);
//TODO:select, why is all this commented?
//					if(filter.get(0).equals("*")) {
//						if(filter.get(2).equals("*")) {
//							update = true;
//							break;
//						} else if(record.containsValue(filter.get(2))) {
//							update = true;
//							break;
//						}
					if(filter.get(0).equals("RowNo")) {
//						if(filter.get(2).equals("*")) {
//							update = true;
//							break;
						if(cmp.get(j).apply(i, Integer.parseInt(filter.get(2)))) {
							update = true;
							break;
						}
//					} else if(filter.get(2).equals("*")) {
//						update = true;
//						break;
					} else if(cmp.get(j).apply(table.getRecord(i).get(filter.get(0)), filter.get(2))) {
						update = true;
						break;
					}
				}

				if(!update) {
					continue;
				}
			}

			for(List<String> x : assign) {
				table.set(i, x.get(0), x.get(1));
			}
			
			try {
				db.write(title);
			} catch (IOException e) {
				System.out.println("Couldn't write table \"" + title + "\": " + e.getMessage());
			}
		}
*/

/*		try {
			db.write(title);
		} catch (IOException e) {
			System.out.println("Couldn't write table \"" + title + "\": " + e.getMessage());
		}
		try {
			db.updateCache(title);
		} catch (IOException e) {
			System.out.println("Couldn't write Indexes.txt: " + e.getMessage());
		}
*/	}
}

class create implements Command {
	public void execute(Database db, Scanner args) {
		//cache record indices for faster lookups
		String header = null;
		String table = null;
		try {
			args.skip("\\s+index\\s+");
			table = args.useDelimiter("\\.").next().trim();
			header = args.next().trim();
			args.reset();
		} catch(NoSuchElementException e) {
			System.out.println("Syntax error. Type \"help\" for command arguments");
			return;
		}

		db.addIndex(table, header);
/*		try {
			db.writeIndex();
		} catch (IOException e) {
			System.out.println("Couldn't write Indexes.txt:" + e.getMessage());
		}
*/	}
}

class drop implements Command {
	public void execute(Database db, Scanner args) {
		//delete an index
		String header = null;
		String table = null;
		try {
			args.skip("\\s+index\\s+");
			table = args.useDelimiter("\\.").next().trim();
			header = args.next().trim();
			args.reset();
		} catch(NoSuchElementException e) {
			System.out.println("Syntax error. Type \"help\" for command arguments");
			return;
		}

		db.removeIndex(table, header);
		try {
			db.writeIndex();
		} catch (IOException e) {
			System.out.println("Couldn't write Indexes.txt:" + e.getMessage());
		}
	}
}

class help implements Command {
	public void execute(Database db, Scanner args) {
		if(args.hasNext()) {
			System.out.println("Syntax error. Type \"help\" by itself for command arguments.");
			return;
		}
		
		System.out.println(
		"Comparators: < <= = >= >\n" +
		"select <comma-separated fields> from <table> where <field> <comparator> <value>, ...\n" +
		"\tPrints specified fields from matching records in table that match the filters\n" +
		"insert <table> values(<comma-separated values>)\n" +
		"\tAdds the comma-separated values at the end of table\n" +
		"delete <table> where <field> <comparator> <value>, ...\n" +
		"\tDelete all records that match the filters\n" +
		"update <table> set <field> = <value>, <field> = <value> where <field> <comparator> <value>, ...\n" +
		"\tSet specified values in all records that match the filters\n" +
		"create index <table>.<field>\n" +
		"\tCache indices of field in table for faster lookup\n" +
		"drop index <table>.<field>\n" +
		"\tDelete index cache for field in table\n" +
		"help\n" +
		"\tThis help text\n" +
		"exit\n" +
		"\tQuit the program\n"
		);
	}
}
