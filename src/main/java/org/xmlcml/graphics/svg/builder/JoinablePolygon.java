package org.xmlcml.graphics.svg.builder;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPolygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JoinablePolygon implements Joinable {
	
	private final static Logger LOG = Logger.getLogger(JoinablePolygon.class);

	private static final double POLYGON_PRORITY = 3.0;

	private SVGPolygon svgPolygon;
	private JoinManager joinManager;

	public JoinablePolygon(SVGPolygon svgPolygon) {
		this.svgPolygon = svgPolygon;
		createJoinerAndAddPoints();
	}

	public double getPriority() {
		return POLYGON_PRORITY;
	}
	public void createJoinerAndAddPoints() {
		joinManager = new JoinManager();
		for (SVGLine l : svgPolygon.getLineList()) {
			joinManager.add(new JoinPoint(this, l.getXY(0)));
			joinManager.add(new JoinPoint(this, Real2.getCentroid(Arrays.asList(l.getXY(0), l.getXY(1)))));
		}
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
		return svgPolygon.getId();
	}

	public SVGElement getSVGElement() {
		return svgPolygon;
	}

	public SVGLine getBackbone() {
		throw new UnsupportedOperationException("Polygons have no backbone");//TODO
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
			if (this.getBackbone() != null && otherBackbone != null) {
				intersectionPoint = this.getBackbone().getIntersection(otherBackbone);
			} else if (this.getPoint() != null) {
				intersectionPoint = (otherPoint == null) ? 
						this.getPoint() : this.getPoint().getMidPoint(otherPoint);
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
		return svgPolygon.toXML()+"\n ... "+joinManager;
	}
}
