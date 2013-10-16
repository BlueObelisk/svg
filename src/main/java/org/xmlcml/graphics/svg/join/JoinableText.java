package org.xmlcml.graphics.svg.join;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGText;

public class JoinableText implements Joinable {

	private final static Logger LOG = Logger.getLogger(JoinableText.class);

	private static final double TEXT_PRIORITY = 10.0;

	private JoinPointList joinPointList;
	private SVGText svgText;

	public JoinableText(SVGText svgText) {
		this.svgText = svgText;
		createJoinerAndAddJoinPoints();
	}

	private void createJoinerAndAddJoinPoints() {
		joinPointList = new JoinPointList();
		Real2 coord = svgText.getCentrePointOfFirstCharacter();
		if (coord != null) {
			JoinPoint joinPoint = new JoinPoint(this, coord);
			joinPoint.setRadius(svgText.getRadiusOfFirstCharacter());
			joinPointList.add(joinPoint);
		}
	}

	public boolean canBeJoinedTo(Joinable joinable) {
		boolean joinsTo = false;
		if (joinable instanceof JoinableText) {
			// no-op for text-> text
		} else if (joinable instanceof JoinableLine) {
			joinsTo = true;
		} else if (joinable instanceof TramLine) {
			joinsTo = true;
		}
		return joinsTo;
	}

	public JoinPoint getIntersectionPoint(Joinable joinable) {
//		return joinable.getJoinPointList().getCommonPoint(this);
		return joinable.getIntersectionPoint(this);
	}

	public JoinPoint getIntersectionPoint(JoinableLine line) {
		return joinPointList.getCommonPoint(line);
	}

	public JoinPoint getIntersectionPoint(JoinableText text) {
//		return joinPointList.getCommonPoint(text);
		// text cannnot join to itself
		return null;
	}

	public JoinPoint getIntersectionPoint(TramLine tramLine) {
		return joinPointList.getCommonPoint(tramLine);
	}

	public JoinPointList getJoinPointList() {
		return joinPointList;
	}

	public String getId() {
		return svgText.getId();
	}
	
	public SVGElement getSVGElement() {
		return svgText;
	}

	/** 
	 * @return null (might change later)
	 */
	public SVGLine getBackbone() {
		return null;
	}

	/** get centre of first character.
	 * 
	 */
	public Real2 getPoint() {
		return (svgText == null) ? null : svgText.getCentrePointOfFirstCharacter();
	}

	/** always return this getPoint().
	 * 
	 */
	public Real2 intersectionWith(Joinable joinable) {
		return this.getPoint();
	}

	public double getPriority() {
		return TEXT_PRIORITY;
	}

}
