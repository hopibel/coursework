package ibelgaufts.convexhull;

import java.util.Collections;
import java.util.List;

public class PointSort {
	private PointSort() {}

	// ccw > 0
	// colinear == 0
	// cw < 0
	public static int ccw(Point p1, Point p2, Point p3) {
		return (p2.x - p1.x)*(p3.y - p1.y) - (p2.y - p1.y)*(p3.x - p1.x);
	}

	public static void sort(List<Point> points) {
		// Get lowest and leftmost point and move to front
		int min = 0;
		for (int i = 0; i < points.size(); ++i) {
			Point p = points.get(i);
			if (p.y < points.get(min).y) {
				min = i;
			} else if (p.y == points.get(min).y) {
				min = p.x < points.get(min).x ? i : min;
			}
		}
		Collections.swap(points, 0, min);

		// Sort according to angle from min
		quickSort_rec(points, 1, points.size()-1);
	}

	private static void quickSort_rec(List<Point> points, int start, int end) {
		if (end - start > 0) {
			int p = partition(points, start, end);
			quickSort_rec(points, start, p-1);
			quickSort_rec(points, p+1, end);
		}
	}

	private static int partition(List<Point> points, int start, int end) {
		int pivot = end;
		int wall = start;
		for (int i = wall; i < pivot; ++i) {
			if (compare(points.get(0), points.get(i), points.get(pivot)) < 0) {
				Collections.swap(points, i, wall);
				++wall;
			}
		}
		Collections.swap(points, wall, pivot);
		return wall;
	}

	// p0 - lowest and leftmost point
	// -1 means smaller angle wrt p0
	private static int compare(Point p0, Point p1, Point p2) {
		// Find orientation
		int o = ccw(p0, p1, p2);
		if (o == 0) {
			return (distSq(p0, p2) >= distSq(p0, p1))? -1 : 1;
		}

		return (o > 0)? -1: 1;
	}
	
	private static int distSq(Point p1, Point p2) {
		return (p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y);
	}
}