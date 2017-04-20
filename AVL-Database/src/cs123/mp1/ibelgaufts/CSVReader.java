package cs123.mp1.ibelgaufts;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSVReader implements Closeable {
	private BufferedReader in;

	public CSVReader(String path) throws FileNotFoundException {
		in = new BufferedReader(new FileReader(path));
	}
	
	public String[] readNext() throws IOException {
		String[] row = null;
		String line = null;
		if((line = in.readLine()) == null) {
			return row;
		}

		row = line.split(",");
		for(int i = 0; i < row.length; ++i) {
			row[i] = row[i].trim();
		}
		return row;
	}
	
	public void close() throws IOException {
		in.close();
	}
}