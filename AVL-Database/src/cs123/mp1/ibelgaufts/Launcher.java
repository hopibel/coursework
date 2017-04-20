package cs123.mp1.ibelgaufts;

import java.io.IOException;
import java.util.Scanner;

public class Launcher {
	public static void main(String[] args) {
		Database db = null;

		// Read database into memory. Database is a directory containing CSV files
		System.out.println("Reading database...");
		try {
			db = new Database("Tables");
		} catch(IOException e) {
			System.err.println("Couldn't open database: " + e.getMessage());
			System.exit(1);
		}

// Even the profiler couldn't decide if the optimized search was 4 times faster or slower.
// I give up. I'm blaming Java
/*while(true){
		long start, end;
//		Interpreter.parse(db, new Scanner("create index hobbits.Surname"));
		start = System.currentTimeMillis();
		for(int i = 0; i < 100; ++i) {
			Interpreter.parse(db, new Scanner("select * from hobbits where Surname > Gamgee"));
		}
		end = System.currentTimeMillis();
		System.out.println("With index: " + (end-start));

		Interpreter.parse(db, new Scanner("drop index hobbits.Surname"));
		start = System.currentTimeMillis();
		for(int i = 0; i < 100; ++i) {
			Interpreter.parse(db, new Scanner("select * from hobbits where Surname > Baggins"));
		}
		end = System.currentTimeMillis();
		System.out.println("No index: " + (end-start));
}
*/

		System.out.print("Ready for input. Type 'help' for available commands\n>");
		Scanner input = new Scanner(System.in);
		while(!input.hasNext("exit")) {
			Interpreter.parse(db, input);
			System.out.print(">");
		}

		input.close();
		try {
			db.writeAll();
		} catch (IOException e) {
			System.out.println("Couldn't write database:" + e.getMessage());
		}
		try {
			db.writeIndex();
		} catch (IOException e) {
			System.out.println("Couldn't write Indexes.txt:" + e.getMessage());
		}
	}
}
