package ibelgaufts.sungka;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * part of sample code by Simon Lucas used during testing (see TreeView)
 * <br>
 * Again, do not grade because this isn't mine.
 */
public class JEasyFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	public Component comp;
    public JEasyFrame(Component comp, String title) {
        super(title);
        this.comp = comp;
        getContentPane().add(BorderLayout.CENTER, comp);
        pack();
        this.setVisible(true);
//        setDefaultCloseOperation(EXIT_ON_CLOSE);
        repaint();
    }
}

