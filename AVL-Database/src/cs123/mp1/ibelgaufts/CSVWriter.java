package cs123.mp1.ibelgaufts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class CSVWriter implements Closeable{
	private BufferedWriter out;

	public CSVWriter(String path) throws IOException {
		File f = new File(path);
		f.createNewFile();
		out = new BufferedWriter(new FileWriter(f.getAbsoluteFile()));
	}
	
	public void writeNext(String[] record) throws IOException {
		out.write(Utils.join(record, ",") + "\n");
	}

	public void close() throws IOException {
		out.close();
	}
}