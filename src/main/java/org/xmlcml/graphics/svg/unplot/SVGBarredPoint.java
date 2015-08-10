package org.xmlcml.graphics.svg.unplot;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Point2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.SVGConstants;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLineList;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGShape;
import org.xmlcml.graphics.svg.unplot.SVGErrorBar.BarDirection;

/** holds a point with ErrorBars.
 * 
 * @author pm286
 *
 */
public class SVGBarredPoint extends SVGG {

	private static Logger LOG = Logger.getLogger(SVGBarredPoint.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private SVGShape shape;
	private List<SVGErrorBar> errorBars;
	private Real2 centroid;
	private Real2Range errorBox;

	protected SVGBarredPoint() {
		super();
		ensureErrorBars();
	}
	
	private void ensureErrorBars() {
		if (errorBars == null) {
			errorBars = new ArrayList<SVGErrorBar>();
		}
 	}

	public static SVGBarredPoint createPoint(SVGShape shape) {
		SVGBarredPoint point = null;
		if (shape != null) {
			point = new SVGBarredPoint();
			point.shape = shape;
			point.getOrCreateCentroid();
		}
		return point;
	}
	
	/** creates errorBar from lline and point.
	 * 
	 * If line is horizontal or vertical and point lies on line and line end
	 * is close to point , then create error bar.
	 * 
	 * @param line
	 * @param maxDist maximum distance of line end from point
	 * @param eps
	 * @return
	 */
	public SVGErrorBar createErrorBar(SVGLine line, double maxDist, double eps) {
		SVGErrorBar errorBar = null;
		if (line != null) {
			getOrCreateCentroid();
			if (centroid != null) {
				Point2 point = new Point2(centroid.getXY());
				if (line.getEuclidLine().contains(point, eps, true)) {
					// swap line so XY(0) is nearest point
					SVGLine newLine = new SVGLine(line);
					Real2 xy0 = line.getXY(0);
					Real2 xy1 = line.getXY(1);
					if (xy1.getDistance(point) < xy0.getDistance(point)) {
						newLine.setXY(xy1, 0);
						newLine.setXY(xy0, 1);
					}
					if (newLine.getXY(0).getDistance(point) < maxDist) {
						int serial = -1;
						if (line.isHorizontal(eps)) {
							serial = (xy0.getX() < xy1.getX()) ? 1 : 3;
						} else if (line.isVertical(eps)) {
							serial = (xy0.getY() < xy1.getY()) ? 0 : 2;
						}
						BarDirection barDirection = serial == -1 ? null : BarDirection.values()[serial];
						errorBar = new SVGErrorBar();
						errorBar.setBarDirection(barDirection);
						errorBar.setLine(newLine);
					}
				}
			}
		}
		return errorBar;
	}

	Real2 getOrCreateCentroid() {
		if (centroid == null && shape != null) {
			centroid = shape.getBoundingBox().getCentroid();
		}
		return centroid;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(""+shape+"; "+centroid.format(3)+"; bars: "+errorBars);
		return sb.toString();
	}

	/** creates error bars from lines pointing at 'this'.
	 * 
	 * @param lineList lines to analyze
	 * @param maxDist to nearest end of line to be considered as error bar
	 * @param eps max distance of this centroid from extended line
	 * @return null if lines are not pointing within eps
	 */
	public List<SVGErrorBar> createErrorBarList(SVGLineList lineList, double maxDist, double eps) {
		List <SVGErrorBar> errorBarList = new ArrayList<SVGErrorBar>();
		for (SVGLine line : lineList) {
			SVGErrorBar errorBar = this.createErrorBar(line, maxDist, eps);
			if (errorBar == null) {
				return null;
			} else {
				errorBarList.add(errorBar);
			}
		}
		return errorBarList;
	}

	public void add(SVGErrorBar errorBar) {
		ensureErrorBars();
		errorBars.add(errorBar);
	}
	
	public List<SVGErrorBar> getErrorBarList() {
		ensureErrorBars();
		return errorBars;
	}

	public SVGShape getErrorShape() {
		SVGShape shape = null;
		Real2Range errorBox = getOrCreateErrorBox();
		if (errorBox.getXRange().getRange() < SVGConstants.EPS ||
			errorBox.getYRange().getRange() < SVGConstants.EPS) {
			Real2[] corners = errorBox.getCorners();
			shape = new SVGLine(corners[0], corners[1]);
		} else {
			shape = SVGRect.createFromReal2Range(errorBox);
		}
		shape.setStrokeWidth(SVGConstants.EPS);
		return shape;
	}

	private Real2Range getOrCreateErrorBox() {
		if (errorBox == null) {
			errorBox = new Real2Range();
			ensureErrorBars();
			for (SVGErrorBar errorBar : errorBars) {
				if (errorBar != null) {
					Real2 xy = errorBar.getLine().getXY(1);
					errorBox.add(xy);
				}
			}
		}
		return errorBox;
	}
}
