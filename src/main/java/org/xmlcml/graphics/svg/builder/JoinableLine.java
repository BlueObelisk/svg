package org.xmlcml.graphics.svg.builder;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;

import java.util.ArrayList;
import java.util.List;

public class JoinableLine implements Joinable {
	
	private final static Logger LOG = Logger.getLogger(JoinableLine.class);

	private static final double LINE_PRORITY = 1.0;

	private SVGLine svgLine;
	private JoinManager joinManager;

	private static final double DEFAULT_RELATIVE_DISTANCE_TO_INTERSECTION = 1.5;
	
	private double relativeDistance = DEFAULT_RELATIVE_DISTANCE_TO_INTERSECTION;

	public JoinableLine(SVGLine svgLine) {
		this.svgLine = svgLine;
		createJoinerAndAddPoints();
	}

	public double getPriority() {
		return LINE_PRORITY;
	}
	
	public void createJoinerAndAddPoints() {
		joinManager = new JoinManager();
		joinManager.add(new JoinPoint(this, svgLine.getXY(0)));
		joinManager.add(new JoinPoint(this, svgLine.getXY(1)));
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
	
	public JoinManager getJoinPointList() {
		return joinManager;
	}

	public String getId() {
		return svgLine.getId();
	}

	public SVGElement getSVGElement() {
		return svgLine;
	}

	public SVGLine getBackbone() {
		return svgLine;
	}

	/** returns null.
	 * 
	 */
	public Real2 getPoint() {
		return null;
	}

	public Real2 intersectionWith(Joinable otherJoinable) {
		Real2 intersectionPoint = null;
		if (otherJoinable != null) {
			SVGLine otherBackbone = otherJoinable.getBackbone();
			Real2 otherPoint = otherJoinable.getPoint();
			if (getBackbone() != null && otherBackbone != null) {
				intersectionPoint = getBackbone().getIntersection(otherBackbone);
				if (Double.isNaN(intersectionPoint.getX()) || Double.isNaN(intersectionPoint.getY())) {
					if (getBackbone().isParallelTo(otherBackbone, new Angle(1, Units.RADIANS))) {
						double dist1 = getBackbone().getEuclidLine().getXY(0).getDistance(otherBackbone.getEuclidLine().getXY(1));
						double dist2 = getBackbone().getEuclidLine().getXY(1).getDistance(otherBackbone.getEuclidLine().getXY(0));
						if (dist1 < dist2) {
							intersectionPoint = getBackbone().getEuclidLine().getXY(0).getMidPoint(otherBackbone.getEuclidLine().getXY(1));
						} else {
							intersectionPoint = getBackbone().getEuclidLine().getXY(1).getMidPoint(otherBackbone.getEuclidLine().getXY(0));
						}
					} else {
						double dist1 = getBackbone().getEuclidLine().getXY(0).getDistance(otherBackbone.getEuclidLine().getXY(0));
						double dist2 = getBackbone().getEuclidLine().getXY(1).getDistance(otherBackbone.getEuclidLine().getXY(1));
						if (dist1 < dist2) {
							intersectionPoint = getBackbone().getEuclidLine().getXY(0).getMidPoint(otherBackbone.getEuclidLine().getXY(0));
						} else {
							intersectionPoint = getBackbone().getEuclidLine().getXY(1).getMidPoint(otherBackbone.getEuclidLine().getXY(1));
						}
					}
				}
				if (getBackbone().getEuclidLine().getXY(0).getDistance(intersectionPoint) > getBackbone().getLength() * relativeDistance && getBackbone().getEuclidLine().getXY(1).getDistance(intersectionPoint) > getBackbone().getLength() * relativeDistance) {
					return null;
				}
				if (otherBackbone.getEuclidLine().getXY(0).getDistance(intersectionPoint) > otherBackbone.getLength() * relativeDistance && otherBackbone.getEuclidLine().getXY(1).getDistance(intersectionPoint) > otherBackbone.getLength() * relativeDistance) {
					return null;
				}
			} else if (getPoint() != null) {
				intersectionPoint = (otherPoint == null) ? 
						getPoint() : getPoint().getMidPoint(otherPoint);
			} else {
				intersectionPoint = otherPoint;
			}
		}
		return intersectionPoint;
	}

	public void addJunction(Junction junction) {
		joinManager.add(junction);
	}
	
	public List<Junction> getJunctionList() {
		return joinManager == null ? new ArrayList<Junction>() : joinManager.getJunctionList();
	}
	
	public String toString() {
		return svgLine.toXML()+"\n ... "+joinManager;
	}
}
