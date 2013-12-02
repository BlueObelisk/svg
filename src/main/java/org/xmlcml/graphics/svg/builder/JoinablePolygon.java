package org.xmlcml.graphics.svg.builder;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGMarker;
import org.xmlcml.graphics.svg.SVGPolygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JoinablePolygon extends JoinableWithBackbone {
	
	private final static Logger LOG = Logger.getLogger(JoinablePolygon.class);

	private static final double POLYGON_PRORITY = 3.0;

	private SVGPolygon svgPolygon;
	private JoinManager joinManager;
	
	private static final double DEFAULT_RELATIVE_DISTANCE_TO_INTERSECTION = 1.5;
	
	private double relativeDistance = DEFAULT_RELATIVE_DISTANCE_TO_INTERSECTION;

	public JoinablePolygon(SVGPolygon svgPolygon) {
		if (svgPolygon.getLineList().size() != 3) {
			throw new IllegalArgumentException();
		}
		this.svgPolygon = svgPolygon;
		createJoinerAndAddPoints();
	}

	public double getPriority() {
		return POLYGON_PRORITY;
	}
	
	public void createJoinerAndAddPoints() {
		joinManager = new JoinManager();
		double shortestLineLength = Double.MAX_VALUE;
		SVGLine shortestLine = null;
		Real2 point = null;
		List<Real2> allPoints = new ArrayList<Real2>();
		for (SVGMarker l : svgPolygon.getPointList()) {
			allPoints.add(((SVGCircle) l.getChild(0)).getXY());
		}
		for (SVGLine l : svgPolygon.getLineList()) {
			if (l.getLength() < shortestLineLength) {
				shortestLineLength = l.getLength();
				shortestLine = l;
				for (Real2 p : allPoints) {
					if (!p.isEqualTo(l.getXY(0), 1e-10) && !p.isEqualTo(l.getXY(1), 1e-10)) {
						point = p;
					}
				}
			}
		}
		joinManager.add(new JoinPoint(this, point));
		joinManager.add(new JoinPoint(this, Real2.getCentroid(Arrays.asList(shortestLine.getXY(0), shortestLine.getXY(1)))));
	}

	public JoinPoint getIntersectionPoint(Joinable joinable) {
		return joinable.getIntersectionPoint(this);
	}

	public JoinPoint getIntersectionPoint(JoinableLine line) {
		return joinManager.getCommonPoint(line);
	}

	public JoinPoint getIntersectionPoint(JoinableText text) {
		return joinManager.getCommonPoint(text);
	}

	public JoinPoint getIntersectionPoint(TramLine tramLine) {
		return joinManager.getCommonPoint(tramLine);
	}

	public JoinPoint getIntersectionPoint(JoinablePolygon polygon) {
		return joinManager.getCommonPoint(polygon);
	}

	public JoinPoint getIntersectionPoint(HatchedPolygon polygon) {
		return joinManager.getCommonPoint(polygon);
	}

	public JoinManager getJoinPointList() {
		return joinManager;
	}

	public String getId() {
		return svgPolygon.getId();
	}

	public SVGElement getSVGElement() {
		return svgPolygon;
	}

	public SVGLine getBackbone() {
		return new SVGLine(joinManager.getJoinPoints().get(0).getPoint(), joinManager.getJoinPoints().get(1).getPoint());
	}

	/** returns null.
	 * 
	 */
	public Real2 getPoint() {
		return null;
	}

	public void addJunction(Junction junction) {
		joinManager.add(junction);
	}
	
	public List<Junction> getJunctionList() {
		return joinManager == null ? new ArrayList<Junction>() : joinManager.getJunctionList();
	}
	
	public String toString() {
		return svgPolygon.toXML()+"\n ... "+joinManager;
	}

	@Override
	public Double getRelativeDistance() {
		return relativeDistance;
	}
}
