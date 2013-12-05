package org.xmlcml.graphics.svg.builder;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;

import java.util.ArrayList;
import java.util.List;

public class JoinableLine extends JoinableWithBackbone {
	
	private final static Logger LOG = Logger.getLogger(JoinableLine.class);

	private static final double LINE_PRORITY = 1.0;

	private SVGLine svgLine;
	private JoinManager joinManager;

	private static final double DEFAULT_RELATIVE_DISTANCE_TO_INTERSECTION = 0.5;
	
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

	public JoinPoint getIntersectionPoint(HatchedPolygon polygon) {
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

	public void addJunction(Junction junction) {
		joinManager.add(junction);
	}
	
	public List<Junction> getJunctionList() {
		return joinManager == null ? new ArrayList<Junction>() : joinManager.getJunctionList();
	}
	
	public String toString() {
		return svgLine.toXML()+"\n ... "+joinManager;
	}

	@Override
	public Double getRelativeDistance() {
		return relativeDistance;
	}
}
