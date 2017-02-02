package org.xmlcml.graphics.svg.objects;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGMarker;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGShape;
import org.xmlcml.graphics.svg.SVGUtil;

public class SVGArrow extends SVGLine {

private static final Logger LOG = Logger.getLogger(SVGArrow.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public static final String ARROW = "arrow";
	public final static SVGMarker ARROWHEAD;
	public final static SVGPath TRIANGLE;
	static {
		ARROWHEAD = new SVGMarker();
		/**
    <marker id="triangle"
      viewBox="0 0 10 10" refX="0" refY="5" 
      markerUnits="strokeWidth"
      markerWidth="4" markerHeight="3"
      orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" />
    </marker>

		 */
		ARROWHEAD.setId("arrowhead");
		ARROWHEAD.setViewBox("0 0 10 10");
		ARROWHEAD.setRefX("0");
		ARROWHEAD.setRefY("5");
		ARROWHEAD.setMarkerUnits(SVGMarker.STROKE_WIDTH);
		ARROWHEAD.setMarkerWidth(4);
		ARROWHEAD.setMarkerHeight(3);
		ARROWHEAD.setOrient(SVGMarker.AUTO);
		TRIANGLE = new SVGPath("M 0 0 L 10 5 L 0 10 z");
		TRIANGLE.setFill("black");
		ARROWHEAD.appendChild(TRIANGLE);
		
	}

	private SVGShape subline;
	/** the serial number of the end of the line that is part of the arrow */
	private int linePoint;
	private SVGTriangle triangle;
	/** the serial number of the edge of the triangle that touches the line */
	private int trianglePoint;
	
	public SVGArrow() {
		super();
		this.setClassName(ARROW);
	}

	public SVGArrow(SVGLine subline, int linePoint, SVGTriangle triangle, int trianglePoint) {
		this();
		this.subline = subline;
		this.linePoint = linePoint;
		this.setXY(subline.getXY(1 - linePoint), 0);
		
		this.triangle = triangle;
		this.trianglePoint = trianglePoint;
		this.setXY(triangle.getLineStartingFrom(trianglePoint).getXY(0), 1);
		
	}

	/** creates arrow if geometry is right.
	 * 
	 * @param subline
	 * @param triangle
	 * @param delta
	 * @return arrow if can create else null
	 */
	public static SVGArrow createArrow(SVGLine subline, SVGTriangle triangle, double delta) {
		SVGArrow arrow = createArrow(subline, 0, triangle, delta / 2);
		if (arrow == null) {
			arrow = createArrow(subline, 1, triangle, delta / 2);
		}
		return arrow;
	}
	
	private static SVGArrow createArrow(SVGLine subline, int lineEnd, SVGTriangle triangle, double delta) {
		SVGArrow arrow = null;
		if (subline != null && triangle != null) {
			SVGPolyline polyline = triangle.getOrCreateClosedPolyline();
			Real2Range bboxLine = subline.getBoundingBox();
			Real2Range bboxTriangle = triangle.getBoundingBox();
			if (!SVGUtil.isNullReal2Range(bboxLine.intersectionWith(bboxTriangle))) {
				int lineSerial = triangle.getLineTouchingPoint(subline.getXY(lineEnd), delta);
				if (lineSerial != -1) {
					int trianglePoint = (lineSerial + 2  ) % 3; // get opposite point
					LOG.trace("line serial "+lineSerial+" / "+trianglePoint);
					arrow = new SVGArrow(subline, lineEnd, triangle, trianglePoint);
				}
			}
		}
		return arrow;
 
	}

	public String toString() {
		String s = subline.toString();
		s += "/"+triangle.toString();
		s += "{"+getXY(1)+","+getXY(0)+"}";
		return s;
	}

}
