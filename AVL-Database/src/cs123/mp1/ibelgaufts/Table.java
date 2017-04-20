package cs123.mp1.ibelgaufts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Table {
	private List<String> headers;
	private List<Map<String, String>> table;

	public Table(CSVReader reader) throws IOException {
		headers = new ArrayList<String>();
		table = new ArrayList<Map<String, String>>();
		String[] row = null;

		if((row = reader.readNext()) != null) {
			headers = Arrays.asList(row);
		} else {
			return; // No sense continuing if the file is empty
		}

		while((row = reader.readNext()) != null) {
			//parse lines
			Map<String, String> record = new HashMap<String, String>();

			if(row.length > headers.size()) {
				continue;
			}

			int i;
			for(i = 0; i < row.length; ++i) {
				record.put(headers.get(i), row[i]);
			}
			for(; i < headers.size(); ++i) {
				record.put(headers.get(i), ""); // Because split discards trailing blank fields
			}

			table.add(record);
		}

		reader.close();
	}

	// Construct empty table with given headers
	// Copies headers to prevent modification
	public Table(List<String> headers) {
		this.headers = new ArrayList<String>(headers);
		table = new ArrayList<Map<String, String>>();
	}

	public Map<String, String> getRecord(int index) {
		return table.get(index);
	}

	public int getRecordCount() {
		return table.size();
	}
	
	// Return a copy of the headers (so user can't modify our copy)
	public List<String> getHeaders() {
		return new ArrayList<String>(headers);
	}

	public void add(Map<String, String> record) {
		for(String h : headers) {
			if(!record.containsKey(h)) {
				record.put(h, "");
			}
		}
		table.add(record);
	}

	public void add(int index, Map<String, String> record) {
		for(String h : headers) {
			if(!record.containsKey(h)) {
				record.put(h, "");
			}
		}
		table.add(index, record);
	}

	public void remove(int index) {
		table.remove(index);
	}

	
	public void set(int index, String field, String value) {
		table.get(index).put(field, value);
	}

	public List<String> getRowValues(int index) {
		List<String> row = new ArrayList<String>();
		// Return values in order dictated by headers
		for(String header : headers) {
			row.add(table.get(index).get(header));
		}
		return row;
	}

	public List<String> getColumnValues(String header) {
		List<String> column = new ArrayList<String>();
		//Return values corresponding to header 
		for(Map<String, String> row : table) {
			column.add(row.get(header));
		}
		return column;
	}

	public Iterator<Map<String, String>> iterator() {
		return table.iterator();
	}
}
