package ibelgaufts.sungka;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Swing GUI for the SungkaGame class. Acts as a Player and can observe games between 2 AIs
 */
public class SungkaGUI extends JPanel implements Player {
	private static final long serialVersionUID = 1L;
	private final JFrame frame;
	private SungkaGame game;
	private BufferedImage background;

	private static final String BOARDPANEL = "board";
	private static final String MENUPANEL = "menu";
	private final JSlider AIslider1;
	private final JSlider AIslider2;
	private boolean animate;

	private static final String HUMAN = "Human";
	private static final String AI = "Computer";

	private JLabel turnLabel;
	private List<JButton> cups = new ArrayList<JButton>(Collections.<JButton>nCopies(16, null));
	private int currentMove;
	private String Player1;
	private String Player2;

	/**
	 * Create new SungkaGUI inside a JFrame.
	 */
	public SungkaGUI() {
		//Create and set up the window.

		try {
			background = ImageIO.read(new File("resources/wood.jpg"));
		} catch (IOException e) {
			System.out.println("Failed to load wood.jpg.");
		}
		frame = new JFrame("Sungka");
		frame.setContentPane(this);

		frame.setMinimumSize(new Dimension(800, 500));

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				confirmAndExit();
			}
		});

		AIslider1 = new JSlider();
		AIslider2 = new JSlider();

		Player1 = Player2 = HUMAN;

		//Create the panel that contains the "cards".
//		cards = new JPanel(new CardLayout());
		this.setLayout(new CardLayout());

		this.add(createMenu(), MENUPANEL);
		this.add(createBoard(), BOARDPANEL);

		//Display the window.
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
	}

	private void confirmAndExit() {
		if (JOptionPane.showConfirmDialog(
			frame, "Are you sure you want to quit?", "Quit",
			JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION
		) {
			System.exit(0);
		}
	}

	private JPanel createMenu() {
		JPanel card = new JPanel(new GridBagLayout());
		JPanel menu = new JPanel(new GridBagLayout());

		JLabel title = new JLabel("SUNGKA");
		title.setFont(new Font("Arial", Font.PLAIN, 80));

		String[] comboBoxItems = {
			"Human",
			"Computer"
		};
		JComboBox<String> cb1 = new JComboBox<>(comboBoxItems);
		cb1.setPrototypeDisplayValue("Must be this wide");
		cb1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if(evt.getItem().equals(AI)) {
					AIslider1.setVisible(true);
					Player1 = AI;
				} else {
					AIslider1.setVisible(false);
					Player1 = HUMAN;
				}
			}
		});

		JComboBox<String> cb2 = new JComboBox<>(comboBoxItems);
		cb2.setPrototypeDisplayValue("Must be this wide");
		cb2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				if(evt.getItem().equals(AI)) {
					AIslider2.setVisible(true);
					Player2 = AI;
				} else {
					AIslider2.setVisible(false);
					Player2 = HUMAN;
				}
			}
		});

		JLabel label1 = new JLabel("Player 1:");
		JLabel label2 = new JLabel("Player 2:");

		JButton start = new JButton("Start");
		start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				currentMove = -1;

				game = new SungkaGame();
				if(Player1 == AI && Player2 == AI) {
					game.setObserver(SungkaGUI.this); // Spectate AI vs AI
				}

				game.setAnimate(animate);

				if(Player1 == AI) {
					SungkaAI ai1 = new SungkaAI(game, AIslider1.getValue() * 1000);
					game.setPlayer(SungkaGame.Turn.PLAYER1, ai1);
				} else {
					game.setPlayer(SungkaGame.Turn.PLAYER1, SungkaGUI.this);
				}
				if(Player2 == AI) {
					SungkaAI ai2 = new SungkaAI(game, AIslider2.getValue() * 1000);
					game.setPlayer(SungkaGame.Turn.PLAYER2, ai2);
				} else {
					game.setPlayer(SungkaGame.Turn.PLAYER2, SungkaGUI.this);
				}

				CardLayout cl = (CardLayout) SungkaGUI.this.getLayout();
				cl.show(SungkaGUI.this, BOARDPANEL);
				new Thread(game).start();
			}
		});

		JButton quit = new JButton("Quit");
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				confirmAndExit();
			}
		});
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 10));
		buttonPanel.add(start);
		buttonPanel.add(quit);

		AIslider1.setMaximum(10);
		AIslider1.setMinimum(1);
		AIslider1.setMajorTickSpacing(1);
		AIslider1.setSnapToTicks(true);
		AIslider1.setPaintLabels(true);
		AIslider1.setValue(5);
		AIslider1.setOpaque(false);
		AIslider1.setVisible(false);

		AIslider2.setMaximum(10);
		AIslider2.setMinimum(1);
		AIslider2.setMajorTickSpacing(1);
		AIslider2.setSnapToTicks(true);
		AIslider2.setPaintLabels(true);
		AIslider2.setValue(5);
		AIslider2.setOpaque(false);
		AIslider2.setVisible(false);
		
		JCheckBox animateOption = new JCheckBox("Animate turns");
		animateOption.setSelected(false);
		animateOption.setOpaque(false);
		animateOption.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent evt) {
				if(evt.getStateChange() == ItemEvent.SELECTED) {
					animate = true;
				} else {
					animate = false;
				}
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		Insets insets = new Insets(2, 2, 2, 2);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		menu.add(title, gbc);
		
		JLabel subtitle = new JLabel("By Hope Ibelgaufts");
		gbc.gridy++;
		insets.bottom = 20;
		gbc.insets = insets;
		menu.add(subtitle, gbc);

		gbc.gridy++;
		gbc.gridwidth = 1;
		insets.bottom = 2;
		insets.right = 20;
		gbc.insets = insets;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		menu.add(label1, gbc);

		insets.right = 2;
		gbc.insets = insets;
		gbc.gridx++;
		menu.add(cb1, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		menu.add(AIslider1, gbc);

		insets.right = 20;
		gbc.insets = insets;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy++;
		menu.add(label2, gbc);

		insets.right = 2;
		gbc.insets = insets;
		gbc.gridx++;
		menu.add(cb2, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
//		gbc.fill = GridBagConstraints.HORIZONTAL;
		menu.add(AIslider2, gbc);
		
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		menu.add(animateOption, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(20, 2, 2, 2);
		menu.add(buttonPanel, gbc);

		menu.setBorder(new CompoundBorder(
			new LineBorder(Color.BLACK),
			new EmptyBorder(30, 30, 30, 30)
		));
		menu.setBackground(new Color(245, 241, 222));

		card.add(menu);
		card.setOpaque(false);

		return card;
	}

	private JPanel createBoard() {
		JPanel card = new JPanel(new BorderLayout());

		// Display current turn at the top
		turnLabel = new JLabel("Connecting to game...", JLabel.CENTER);
		turnLabel.setFont(new Font("Arial", Font.PLAIN, 20));
//		turnLabel.setBorder(new LineBorder(Color.BLACK, 2));
		turnLabel.setOpaque(true);
		turnLabel.setBackground(new Color(245, 241, 222));

		JPanel turnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		turnPanel.add(turnLabel);
		turnPanel.setOpaque(false);

		JButton toMenu = new JButton("Main Menu");
		toMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (JOptionPane.showConfirmDialog(
					frame, "Stop current game and return to menu?", "Return to menu",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION
				) {
					CardLayout cl = (CardLayout) SungkaGUI.this.getLayout();
					cl.show(SungkaGUI.this, MENUPANEL);
				}
			}
		});
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(toMenu);
		buttonPanel.setOpaque(false);
//		menuPanel.setOpaque(false);
		JPanel topPanel = new JPanel(new GridLayout());
		topPanel.add(turnPanel);
		topPanel.add(buttonPanel);
		topPanel.setOpaque(false);

		card.add(topPanel, BorderLayout.NORTH);

		JPanel board = new JPanel(new GridLayout(3, 9)) {
			private static final long serialVersionUID = 1L;

			@Override
			public final Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				Dimension prefSize = null;
				Component c = getParent();
				if (c == null) {
					prefSize = new Dimension(
							(int)d.getWidth(),(int)d.getHeight());
				} else if (c!=null &&
						c.getWidth()>d.getWidth() &&
						c.getHeight()>d.getHeight()) {
					prefSize = c.getSize();
				} else {
					prefSize = d;
				}
				double w = prefSize.getWidth();
				double h = prefSize.getHeight();
				double aspectratio = 9/3;
				w = w > h * aspectratio ? h * aspectratio : w;
				h = w / aspectratio;
				return new Dimension((int) w, (int) h);
			}
		};

		board.add(new JLabel());
		addButton(14, board);
		addButton(13, board);
		addButton(12, board);
		addButton(11, board);
		addButton(10, board);
		addButton(9, board);
		addButton(8, board);
		board.add(new JLabel());
		addButton(15, board);
		board.add(new JLabel());
		board.add(new JLabel());
		board.add(new JLabel());
		board.add(new JLabel());
		board.add(new JLabel());
		board.add(new JLabel());
		board.add(new JLabel());
		addButton(7, board);
		board.add(new JLabel());
		addButton(0, board);
		addButton(1, board);
		addButton(2, board);
		addButton(3, board);
		addButton(4, board);
		addButton(5, board);
		addButton(6, board);
		board.add(new JLabel());

		board.setOpaque(false);

		JPanel container = new JPanel(new GridBagLayout());
//		GridBagConstraints gbc = new GridBagConstraints();
//		gbc.fill = GridBagConstraints.BOTH;
		container.add(board);
		container.setOpaque(false);

		card.add(container);
		card.setBorder(new EmptyBorder(10, 10, 10, 10));
		card.setOpaque(false);

		return card;
	}

	private void addButton(int i, JPanel board) {
		JButton button = new JButton();
		button.setFont(new Font("Arial", Font.PLAIN, 30));
		button.addActionListener(new cupListener(i));

		button.setBackground(new Color(245, 241, 222));
		button.setFocusPainted(false);

		cups.set(i, button);
		board.add(button);
	}

	private class cupListener implements ActionListener {
		int index;

		public cupListener(int i) {
			index = i;
		}

		public void actionPerformed(ActionEvent event) {
			if(index == 7 || index == 15) {return;}
			currentMove = index;
		}
	
	}

	/* (non-Javadoc)
	 * @see ibelgaufts.sungka.Player#getMove()
	 */
	public int getMove() {
		int move = currentMove;
		currentMove = -1;
		return move;
	}

	/* (non-Javadoc)
	 * @see ibelgaufts.sungka.Player#redraw()
	 */
	public void redraw() {
		for(int i = 0; i < cups.size(); ++i) {
			cups.get(i).setText(Integer.toString(game.getStones(i)));
		}
		
		switch(game.getState()) {
		case PLAYER1:
			turnLabel.setText("Waiting for Player 1");
			break;
		case PLAYER2:
			turnLabel.setText("Waiting for Player 2");
			break;
		case PLAYER1_WIN:
			turnLabel.setText("Player 1 wins!");
			break;
		case PLAYER2_WIN:
			turnLabel.setText("Player 2 wins!");
			break;
		case DRAW:
			turnLabel.setText("The game is a draw");
			break;
		}
	}

	/* (non-Javadoc)
	 * @see ibelgaufts.sungka.Player#redrawCell(int)
	 */
	public void redrawCell(int i) {
		turnLabel.setText("Processing turn");
		cups.get(i).setText(Integer.toString(game.getStones(i)));
	}
}