package ibelgaufts.convexhull;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

/* I ran out of time, OK? */
public class ConvexHull extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final int PAD = 50;
	private static final int WAIT = 50;
	private static final int POINTS = 100;
	private List<Point> points = new ArrayList<>();
	private List<Point> hull = new ArrayList<>();
	private static final Random rng = new Random();
	private int maxX, maxY;
	private boolean running;

	public ConvexHull() {

		Action command = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (running) return;
				running = true;
				new SwingWorker<Void, Void>() {
					@Override
					public Void doInBackground() {
						generatePoints();
						Point p0 = points.get(0);
						int n = points.size();
		
						// for same angle, keep farthest point
						int m = 1; // modified size
						for (int i = 1; i < n; ++i) {
							// skip i if i+1 has same angle
							while (i < n-1 && PointSort.ccw(p0, points.get(i), points.get(i+1)) == 0) {
								++i;
							}
							
							points.set(m, points.get(i));
							++m;
						}
		
						// need 3 points to start
						hull.add(points.get(0));
						hull.add(points.get(1));
						try {
							Thread.sleep(WAIT);
						} catch (InterruptedException e) {}
						repaint();
		
						hull.add(points.get(2));
						try {
							Thread.sleep(WAIT);
						} catch (InterruptedException e) {}
						repaint();
		
						for (int i = 3; i < m; ++i) {
							// keep removing until ccw
							while (PointSort.ccw(hull.get(hull.size()-2), hull.get(hull.size()-1), points.get(i)) <= 0) {
								hull.add(points.get(i));
								try {
									Thread.sleep(WAIT);
								} catch (InterruptedException e) {}
								repaint();
		
								hull.remove(hull.size()-1);
								try {
									Thread.sleep(WAIT);
								} catch (InterruptedException e) {}
								repaint();
		
								hull.remove(hull.size()-1);
								try {
									Thread.sleep(WAIT);
								} catch (InterruptedException e) {}
								repaint();
							}
							hull.add(points.get(i));
		
							try {
								Thread.sleep(WAIT);
							} catch (InterruptedException e) {}
							repaint();
				}
		
						hull.add(points.get(0));
						return null;
					}
			
					@Override
					public void done() {
						revalidate();
						repaint();
						running = false;
					}
				}.execute();
			}
		};

		//get the "focus is in the window" input map for the center panel
		int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
		InputMap imap = getInputMap(mapName);
		KeyStroke key = KeyStroke.getKeyStroke(' ');
		imap.put(key, "restart");
		//get the action map for the panel
		ActionMap amap = getActionMap();
		amap.put("restart", command);
		requestFocus();
	}

	private int getMaxX() {
		int max = Integer.MIN_VALUE;
		for (Point p : points) {
			if (p.x > max) {
				max = p.x;
			}
		}
		return max;
	}

	private int getMaxY() {
		int max = Integer.MIN_VALUE;
		for (Point p : points) {
			if (p.y > max) {
				max = p.y;
			}
		}
		return max;
	}

	private void generatePoints() {
		points = new ArrayList<Point>();
		hull = new ArrayList<Point>();
		for (int i = 0; i < POINTS; ++i) {
			points.add(new Point(Math.abs(rng.nextInt(10000)), Math.abs(rng.nextInt(10000))));
		}
		PointSort.sort(points);
		maxX = getMaxX();
		maxY = getMaxY();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		((Graphics2D) g).setStroke(new BasicStroke(3));

		int w = getWidth();
		int h = getHeight();
		double scaleX = (double)(w - 2*PAD)/maxX;
		double scaleY = (double)(h - 2*PAD)/maxY;

		if (points.isEmpty()) return;
		for (Point p : points) {
			int x = (int)(PAD + p.x*scaleX);
			int y = (int)(PAD + p.y*scaleY);
			g.drawLine(x, y, x, y);
		}

		if (hull.isEmpty()) return;
		for (int i = 0; i < hull.size()-1; ++i) {
			int startX = (int)(PAD + hull.get(i).x*scaleX);
			int startY = (int)(PAD + hull.get(i).y*scaleY);
			int endX = (int)(PAD + hull.get(i+1).x*scaleX);
			int endY = (int)(PAD + hull.get(i+1).y*scaleY);
			g.drawLine(startX, startY, endX, endY);
		}

		((Graphics2D) g).setStroke(new BasicStroke(1));
		int startX = (int)(PAD + hull.get(hull.size()-1).x*scaleX);
		int startY = (int)(PAD + hull.get(hull.size()-1).y*scaleY);
		int endX = (int)(PAD + points.get(0).x*scaleX);
		int endY = (int)(PAD + points.get(0).y*scaleY);
		g.drawLine(startX, startY, endX, endY);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Press Space to compute new convex hull");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(new ConvexHull());

		//Display the window.
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}

class Point {
	public int x;
	public int y;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
}