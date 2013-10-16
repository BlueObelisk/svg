package org.xmlcml.graphics.svg.join;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;

public class JoinableLine implements Joinable {
	
	private final static Logger LOG = Logger.getLogger(JoinableLine.class);

	private static final double LINE_PRORITY = 1.0;

	private SVGLine svgLine;
	private JoinPointList joinPointList;

	public JoinableLine(SVGLine svgLine) {
		this.svgLine = svgLine;
		createJoinerAndAddPoints();
	}

	public double getPriority() {
		return LINE_PRORITY;
	}
	public void createJoinerAndAddPoints() {
		joinPointList = new JoinPointList();
		joinPointList.add(new JoinPoint(this, svgLine.getXY(0)));
		joinPointList.add(new JoinPoint(this, svgLine.getXY(1)));
	}

	public JoinPoint getIntersectionPoint(Joinable joinable) {
		return joinable.getIntersectionPoint(this);
	}

	public JoinPoint getIntersectionPoint(JoinableLine line) {
		return joinPointList.getCommonPoint(line);
	}

	public JoinPoint getIntersectionPoint(JoinableText text) {
		return joinPointList.getCommonPoint(text);
	}

	public JoinPoint getIntersectionPoint(TramLine tramLine) {
		return joinPointList.getCommonPoint(tramLine);
	}

	public JoinPointList getJoinPointList() {
		return joinPointList;
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


}
