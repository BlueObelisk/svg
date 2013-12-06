package org.xmlcml.graphics.svg.builder;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.euclid.Vector2;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGMarker;
import org.xmlcml.graphics.svg.SVGPolygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HatchedTriangle extends JoinableWithBackbone implements Joinable {
	
	private final static Logger LOG = Logger.getLogger(HatchedTriangle.class);

	private static final double HATCHED_TRIANGLE_PRORITY = 2.9;

	private List<SVGLine> lineList;
	private JoinManager joinManager;
	
	private static final double DEFAULT_RELATIVE_DISTANCE_TO_INTERSECTION = 0.5;
	private static final double DEFAULT_RELATIVE_DISTANCE_FROM_SINGLE_LINE = 1;
	
	private double relativeDistance = DEFAULT_RELATIVE_DISTANCE_TO_INTERSECTION;
	private double relativeDistanceFromSingleLine = DEFAULT_RELATIVE_DISTANCE_FROM_SINGLE_LINE;

	public HatchedTriangle(List<SVGLine> lines) {
		this.lineList = lines;
		createJoinerAndAddPoints();
	}

	public double getPriority() {
		return HATCHED_TRIANGLE_PRORITY;
	}
	
	public void createJoinerAndAddPoints() {
		joinManager = new JoinManager();
		SVGLine lastLine = lineList.get(lineList.size() - 1);
		SVGLine shortestLine;
		SVGLine longestLine;
		if (lineList.get(0).getLength() > lastLine.getLength()) {
			longestLine = lineList.get(0);
			shortestLine = lastLine;
		} else {
			longestLine = lastLine;
			shortestLine = lineList.get(0);
		}
		try {
			double scaleFactor = (longestLine.getMidPoint().getDistance(shortestLine.getMidPoint()) + lineList.get(0).getMidPoint().getDistance(lineList.get(1).getMidPoint())) / longestLine.getMidPoint().getDistance(shortestLine.getMidPoint());
			Real2 newPoint = new Real2(longestLine.getMidPoint().getX() + (shortestLine.getMidPoint().getX() - longestLine.getMidPoint().getX()) * scaleFactor, longestLine.getMidPoint().getY() + (shortestLine.getMidPoint().getY() - longestLine.getMidPoint().getY()) * scaleFactor);
			joinManager.add(new JoinPoint(this, newPoint));
			joinManager.add(new JoinPoint(this, longestLine.getMidPoint()));
		} catch (IndexOutOfBoundsException e) {
			Real2 perpendicularVector = longestLine.getEuclidLine().getVector().getTransformed(new Transform2(new Angle(90, Units.DEGREES))).getUnitVector();
			joinManager.add(new JoinPoint(this, longestLine.getMidPoint().plus(perpendicularVector.multiplyBy(longestLine.getLength() * relativeDistanceFromSingleLine))));
			joinManager.add(new JoinPoint(this, longestLine.getMidPoint().subtract(perpendicularVector.multiplyBy(longestLine.getLength() * relativeDistanceFromSingleLine))));
		}
		//Vector2 perp = new Vector2(-smallestLine.getEuclidLine().getVector().getUnitVector().y, smallestLine.getEuclidLine().getVector().getUnitVector().x);
		//perp = (Vector2) perp.multiplyBy(lines.get(0).getMidPoint().getDistance(lines.get(1).getMidPoint()));
		//new SVGLine(longestLine.getMidPoint(), shortestLine.getMidPoint());
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

	public JoinPoint getIntersectionPoint(JoinableTriangle polygon) {
		return joinManager.getCommonPoint(polygon);
	}

	public JoinPoint getIntersectionPoint(HatchedTriangle polygon) {
		return joinManager.getCommonPoint(polygon);
	}

	public JoinManager getJoinPointList() {
		return joinManager;
	}

	public SVGElement getSVGElement() {
		return null;
		/*SVGG g = new SVGG();
		for (SVGLine l : lineList) {
			g.appendChild(l);
		}
		return g;*/
	}

	public SVGLine getBackbone() {
		try {
			return new SVGLine(joinManager.getJoinPoints().get(0).getPoint(), joinManager.getJoinPoints().get(1).getPoint());
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public Real2 getPoint() {
		if (joinManager.getJoinPoints().size() == 1){
			return joinManager.getJoinPoints().get(0).getPoint();
		} else {
			return null;
		}
	}

	public void addJunction(Junction junction) {
		joinManager.add(junction);
	}
	
	public List<Junction> getJunctionList() {
		return joinManager == null ? new ArrayList<Junction>() : joinManager.getJunctionList();
	}

	@Override
	public Double getRelativeDistance() {
		return relativeDistance;
	}
}
