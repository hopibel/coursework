package cs123.mp1.ibelgaufts;

import java.io.File;
import java.io.IOException;

public class DBImporter {
	public static Database loadDatabase(String path) throws IOException {
		/* Get list of CSV files in path */
		File dir = new File(path);
		if(!dir.exists()) {
			throw new IOException(dir.getAbsolutePath() + " does not exist.");
		}

		File[] files = dir.listFiles();
		Database db = new Database(path);
		
		for(File f : files) {
			if(f.isFile() && f.getName().toLowerCase().endsWith(".csv")) {
				db.addTable(f.getName().replaceFirst("[.][^.]+$", ""), new Table(new CSVReader(f.getAbsolutePath())));
			}
		}
		
		return db;
	}
}