package edu.real.external;

import android.graphics.Point;

public class Intersections {

	public Intersections()
	{
		// TODO Auto-generated constructor stub
	}

	// http://www.jeffreythompson.org/collision-detection/line-rect.php
	static public Point lineline(float x1, float y1, float x2, float y2,
			float x3, float y3, float x4, float y4)
	{

		// calculate the direction of the lines
		float uA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))
				/ ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
		float uB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3))
				/ ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));

		// if uA and uB are between 0-1, lines are colliding
		if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
			// optionally, draw a circle where the lines meet
			float intersectionX = x1 + (uA * (x2 - x1));
			float intersectionY = y1 + (uA * (y2 - y1));

			return new Point((int) intersectionX, (int) intersectionY);
		} else {
			return null;
		}
	}
}
