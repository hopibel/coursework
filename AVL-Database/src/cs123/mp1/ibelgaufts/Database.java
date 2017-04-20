package cs123.mp1.ibelgaufts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
	private Map<String, Table> db;
	private String dbpath;
	private Map<String, AVLtree<IndexCache>> cache;
	private Map<String, List<Integer>> unused;
	
	private class IndexCache implements Comparable<IndexCache> {
		private String data;
		private List<Integer> indexes;

		IndexCache(String field) {
			data = field;
			indexes = new ArrayList<Integer>();
		}
		
		public void add(int index) {
			indexes.add(index);
		}
		
		public List<Integer> getIndexes() {
			return indexes;
		}

		@Override
		public int compareTo(IndexCache o) {
			return data.compareTo(o.data);
		}

		public String toString() {
			return data;
		}
	}

/* Might add support for creating empty database instead of just importing
	public Database() {
		db = new HashMap<String, Table>();
	}
*/
	// Build from csv files in a given directory
	public Database(String path) throws IOException {
		/* Get list of CSV files in path */
		dbpath = path;
		File dir = new File(path);
		if(!dir.exists()) {
			throw new IOException(dir.getAbsolutePath() + " does not exist.");
		}
		
		unused = new HashMap<String, List<Integer>>();
		
		File[] files = dir.listFiles();
		db = new HashMap<String, Table>();

		for(File f : files) {
			if(f.isFile() && f.getName().toLowerCase().endsWith(".csv")) {
				addTable(f.getName().replaceFirst("[.][^.]+$", ""), new Table(new CSVReader(f.getAbsolutePath())));
			}
		}

		cache = new HashMap<String, AVLtree<IndexCache>>();
		List<String> toIndex;
		if(new File("Indexes.txt").exists()) {
			BufferedReader in = new BufferedReader(new FileReader("Indexes.txt"));
			toIndex = new ArrayList<String>();
			String line;
			while((line = in.readLine()) != null) {
				toIndex.add(line);
			}
			in.close();

			for(String table : db.keySet()) {
				for(String header : db.get(table).getHeaders()) {
					if(toIndex.contains(table + "." + header)) {
						cache.put(table + "." + header, generateCache(db.get(table), header));
					}
				}
			}
		}
	}
	
	public void updateCache(String table) throws IOException {
		for(String key : cache.keySet()) {
			if(key.startsWith(table)) {
				String[] tmp = key.split("\\.");
				addIndex(tmp[0], tmp[1]);
			}
		}
	}
	
	public List<Integer> getIndexes(String table, String header, String comparison, String value) {
		List<Integer> result = new ArrayList<Integer>();
		AVLtree<IndexCache> tree = cache.get(table + "." + header);
		if(comparison.equals("=")) {
			IndexCache tmp = tree.search(new IndexCache(value));
			if(tmp != null) {
				result = tmp.getIndexes();
			}
//			return result;
		}/* else if(comparison.equals("<")) {
			List<IndexCache> tmp = tree.searchLT(new IndexCache(value), false);
			for(IndexCache c : tmp) {
				result.addAll(c.getIndexes());
			}
		} else if(comparison.equals("<=")) {
			List<IndexCache> tmp = tree.searchLT(new IndexCache(value), true);
			for(IndexCache c : tmp) {
				result.addAll(c.getIndexes());
			}
		} else if(comparison.equals(">")) {
			List<IndexCache> tmp = tree.searchGT(new IndexCache(value), false);
			for(IndexCache c : tmp) {
				result.addAll(c.getIndexes());
			}
		} else if(comparison.equals(">=")) {
			List<IndexCache> tmp = tree.searchGT(new IndexCache(value), true);
			for(IndexCache c : tmp) {
				result.addAll(c.getIndexes());
			}
		}
*/
		return result;
	}

	public boolean hasIndex(String table, String header) {
		return cache.containsKey(table + "." + header);
	}
	
	public void addIndex(String table, String header) {
		if(!db.containsKey(table)) {
			System.out.println(table + "." + header + " doesn't exist. Operation skipped.");
			return;
		}
		cache.put(table + "." + header, generateCache(db.get(table), header));
	}
	
	public void removeIndex(String table, String header) {
		if(!cache.containsKey(table + "." + header)) {
			System.out.println(table + "." + header + " doesn't exist. Nothing to do.");
			return;
		}
		cache.remove(table + "." + header);
	}

	public AVLtree<IndexCache> generateCache(Table table, String header) {
		AVLtree<IndexCache> tree = new AVLtree<IndexCache>();
		List<String> column = table.getColumnValues(header);

		for(int i = 0; i < column.size(); ++i) {
			IndexCache ic = new IndexCache(column.get(i));
			IndexCache tmp = tree.search(ic);
			if(tmp == null) {
				ic.add(i);
				tree.insert(ic);
			} else {
				ic = tmp;
				ic.add(i);
			}
		}
		
		return tree;
	}
	
	public void writeIndex() throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter("Indexes.txt"));
		for(String key : cache.keySet()) {
			out.write(key + "\n");
		}
		out.close();
	}

	// Write only one table.
	public void write(String table) throws IOException {
		if(!db.containsKey(table)) {
			throw new RuntimeException("Table doesn't exist");
		}
		Path p = Paths.get(dbpath).resolve(table + ".csv");
		CSVWriter writer = new CSVWriter(p.toString());

		writer.writeNext(db.get(table).getHeaders().toArray(new String[0]));
		Table t = db.get(table);
		List<String> headers = t.getHeaders();
		for(int i = 0; i < t.getRecordCount(); ++i) {
			if(isUnused(table, i)) {
				continue;
			}
			String[] record = new String[headers.size()];
			for(int j = 0; j < headers.size(); ++j) {
				record[j] = t.getRecord(i).get(headers.get(j));
			}
			writer.writeNext(record);
		}

		writer.close();
	}

	// Write all tables to disk
	public void writeAll() throws IOException {
		for(String table : db.keySet()) {
			write(table);
		}
	}

	public void addTable(String name, Table t, boolean overwrite) {
		if(db.containsKey(name) && !overwrite) {
			System.out.println("Table exists. Operation skipped.");
		} else {
			db.put(name, t);
			unused.put(name, new ArrayList<Integer>());
		}
	}
	public void addTable(String name, Table t) {
		this.addTable(name, t, false);
	}
	
	public Table getTable(String name) {
		return db.get(name);
	}

	public void setUnused(String table, int index) {
		if(unused.get(table).contains(index)) {
			return;
		}
		unused.get(table).add(index);
		for(String s : cache.keySet()) {
			if(s.startsWith(table + ".")) {
				IndexCache c = cache.get(s).search(new IndexCache(db.get(table).getRecord(index).get(Arrays.asList(s.split("\\.")).get(1))));
				for(int i = 0; i < c.getIndexes().size(); ++i) {
					if(c.getIndexes().get(i).equals(index)) {
						c.getIndexes().remove(i);
					}
				}
//				c.getIndexes().remove((Integer) index);
				if(c.getIndexes().isEmpty()) {
					cache.get(s).delete(c);
				}
			}
		}
	}
	
	public boolean isUnused(String table, int index) {
		return unused.get(table).contains(index);
	}

	public int getUnused(String table) {
		if(unused.get(table).isEmpty()) {
			return db.get(table).getRecordCount();
		} else {
			return unused.get(table).remove(0);
		}
	}
}