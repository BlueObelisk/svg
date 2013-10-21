package org.xmlcml.graphics.svg.image;

import org.xmlcml.euclid.Int2;
import org.xmlcml.euclid.IntMatrix;
import org.xmlcml.euclid.Line2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;

public class FloodFill {

	private double xmin;
	private double xmax;
	private int xSteps;
	private double ymin;
	private double ymax;
	private int ySteps;
	private double delta;
	private IntMatrix matrix;

	public FloodFill(Real2Range boundingBox, double delta) {
		this.delta = delta;
		createIntMatrix(boundingBox, delta);
	}

	private void createIntMatrix(Real2Range boundingBox, double delta) {
		xmin = boundingBox.getXMin();
		xmax = boundingBox.getXMax();
		xSteps = (int) ((xmax - xmin)/delta) +1;
		ymin = boundingBox.getYMin();
		ymax = boundingBox.getYMax();
		ySteps = (int) ((ymax - ymin)/delta) +1;
		matrix = new IntMatrix(xSteps, ySteps);
	}

	/** fill points on line 
	 * replace by Bresenham asap
	 * 
	 * @param line2
	 */
	public void fillLine(Line2 line2) {
		double dist = line2.getLength();
		int nsteps = (int)(dist/delta * 2.)+1;  // 2 is a fudgefactor to ensure completeness
		Real2 lineVector = line2.getVector();
		Real2 stepVector = lineVector.multiplyBy(1.0/((double) nsteps));
		for (int i = 1; i < nsteps; i++) {
			Real2 step = stepVector.multiplyBy((double) i);
			Real2 point = line2.getFrom().plus(step);
			Int2 intPoint = getNearestPoint(point);
		}
	}

	private Int2 getNearestPoint(Real2 point) {
		Int2 intPoint = null;
		double dx = point.getX() - xmin;
		double dy = point.getY() - ymin;
		if (dx >= 0.0 && dy >= 0.0) {
			int nx = (int) Math.round(dx/xSteps);
			int ny = (int) Math.round(dy/ySteps);
			intPoint = new Int2(nx, ny);
		}
		return intPoint;
	}
}
