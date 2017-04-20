package ibelgaufts.sungka;

import javax.swing.SwingUtilities;

public class Main {
	public static void main(String[] args) {
		// Apparently methods that modify the GUI should be run on
		// Swing's event dispatch thread.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new SungkaGUI();
			}
		});
	}
}
