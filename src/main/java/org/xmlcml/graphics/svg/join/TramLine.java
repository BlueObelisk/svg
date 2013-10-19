package org.xmlcml.graphics.svg.join;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;

/** two or more parallel lines with overlap.
 * 
 * <p>Used for double bonds, grid lines, etc.</p>
 * 
 * extends SVGG so it can be used in place of SVGLines when needed
 * 
 * @author pm286
 *
 */
public class TramLine extends SVGG implements Joinable {

	@SuppressWarnings("unused")
	private final static Logger LOG = Logger.getLogger(TramLine.class);

	// large enough to cover any known tramlines
	private final static Angle ANGLE_EPS = new Angle(0.3, Units.RADIANS);

	private static final double TRAM_LINE_PRORITY = 5.0;

	private List<SVGLine> lineList;
	private JoinManager joinManager;
	private SVGLine backbone;
	

	public TramLine(SVGLine linei, SVGLine linej) {
		this.add(linei);
		this.add(linej);
		createJoinerAndAddJoinPoints();
	}


	private void createJoinerAndAddJoinPoints() {
		Angle EPS = new Angle(0.1, Units.RADIANS); // we already know they are aligned
		joinManager = new JoinManager();
		SVGLine line0 = lineList.get(0);
		SVGLine line1 = lineList.get(1);
		Real2 point00 = line0.getXY(0);
		Real2 point01 = line0.getXY(1);
		Real2 point10 = line1.getXY(0);
		Real2 point11 = line1.getXY(1);
		Real2 join0 = null;
		Real2 join1 = null;
		if (line0.isAntiParallelTo(line1, EPS)) {
			join0 = point00.getMidPoint(point11);
			join1 = point01.getMidPoint(point10);
		} else {
			join0 = point00.getMidPoint(point10);
			join1 = point01.getMidPoint(point11);
		}
		joinManager.add(new JoinPoint(this, join0));
		joinManager.add(new JoinPoint(this, join1));
	}
	
	public double getPriority() {
		return TRAM_LINE_PRORITY;
	}
	public void add(SVGLine line) {
//		this.appendChild(line);
		ensureLineList();
		lineList.add(line);
	}
	public SVGLine getLine(int i) {
		ensureLineList();
		return (i < 0 || i >= lineList.size()) ? null : lineList.get(i);
	}

	private void ensureLineList() {
		if (lineList == null) {
			this.lineList = SVGLine.extractSelfAndDescendantLines(this);
		}
	}

	public List<JoinPoint> getJoinPoints() {
		return null;
	}

//	public boolean canBeJoinedTo(Joinable otherJoinable) {
//		boolean joinsTo = false;
//		JoinPointList otherJoiner = otherJoinable.getJoinPointList();
//		if (otherJoinable instanceof JoinableText) {
//			joinsTo = true;
////			joinsTo = joiner.canBeJoinedTo(otherJoiner);
//		} else if (otherJoinable instanceof JoinableLine) {
//			joinsTo = true;
////			joinsTo = joiner.canBeJoinedTo(otherJoiner);
//		} else if (otherJoinable instanceof TramLine) {
//			//no-op
//		}
//		return joinsTo;
//	}

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

	public JoinManager getJoinPointList() {
		return joinManager;
	}

	public void setFillAll(String fill) {
		ensureLineList();
		for (SVGLine line :lineList) {
			line.setFill(fill);
		}
	}

	public void setStrokeWidthAll(double d) {
		ensureLineList();
		for (SVGLine line :lineList) {
			line.setStrokeWidth(d);
		}
	}

	public SVGElement getSVGElement() {
		return this;
	}
	
	public SVGLine getBackbone() {
		if (backbone == null) {
			if (lineList != null && lineList.size() == 2) {
				backbone = lineList.get(0).getMeanLine(lineList.get(1), ANGLE_EPS);
			}
		}
		return backbone;
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
	
	/** returns intersection of backbones.
	 * 
	 * @return null if joinable has no backbone (e.g. text);
	 */
	public Real2 intersectionWith(Joinable joinable) {
		return getBackbone().getIntersection(joinable.getBackbone());
	}

}


