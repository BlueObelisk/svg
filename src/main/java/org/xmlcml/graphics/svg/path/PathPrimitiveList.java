package org.xmlcml.graphics.svg.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGPathPrimitive;

/** container and managed for primitives from an SVGPath.
 * 
 * @author pm286
 *
 */
public class PathPrimitiveList implements Iterable<SVGPathPrimitive> {

	private List<SVGPathPrimitive> primitiveList;
	private boolean isClosed;
	
	public PathPrimitiveList() {
	}
	
	public void add(SVGPathPrimitive primitive) {
		ensurePathPrimitiveList();
		primitiveList.add(primitive);
		setFirstPoints();
	}

	public void add(List<SVGPathPrimitive> primitiveList) {
		ensurePathPrimitiveList();
		primitiveList.addAll(primitiveList);
		setFirstPoints();
	}

	private void ensurePathPrimitiveList() {
		if (primitiveList == null) {
			primitiveList = new ArrayList<SVGPathPrimitive>();
		}
	}
	
	/**
	 * sets first points of primitives to last coord of precedingPrimitive
	 * if last primitive (j) is Z, or isClosed,  set firstCoord of primitive(0) to lastCoord of primitive(j-1) 
	 * @param primitiveList
	 */
	void setFirstPoints() {
		ensurePathPrimitiveList();
		int nprim = primitiveList.size();
		for (int i = 1; i < nprim; i++) {
			primitiveList.get(i).setFirstPoint(primitiveList.get(i-1).getLastCoord());
		}
		if (primitiveList.get(nprim-1) instanceof ClosePrimitive) {
			if (nprim > 1) {
				primitiveList.get(0).setFirstPoint(primitiveList.get(nprim-2).getLastCoord());
			}
		} else if (isClosed()) {
			if (nprim > 1) {
				primitiveList.get(0).setFirstPoint(primitiveList.get(nprim-1).getLastCoord());
			}
		}
	}
	
	public Iterator<SVGPathPrimitive> iterator() {
		ensurePathPrimitiveList();
		return primitiveList.iterator();
	}

	public int size() {
		ensurePathPrimitiveList();
		return primitiveList.size();
	}

	public SVGPathPrimitive get(int i) {
		ensurePathPrimitiveList();
		return (i < 0 || i >= primitiveList.size()) ? null : primitiveList.get(i);
	}

	public List<SVGPathPrimitive> getPrimitiveList() {
		return primitiveList;
	}

	public Boolean isTwoQuadrants(int i, Angle angleEps) {
		Integer turn = quadrantValue(i, angleEps) + quadrantValue(i + 1, angleEps);
		return Math.abs(turn) == 2;
	}

	/** get value of a quadrant.
	 * 
	 * @param i index of primitive
	 * @param angleEps max deviation from PI/2.
	 * @return 1 ifPI/2 turn, -1 if -PI/2 turn else 0
	 */
	public int quadrantValue(int i, Angle angleEps) {
		Integer value = 0;
		Angle angle = this.getAngle(i);
		if (angle != null && this.get(i) instanceof CubicPrimitive) {
			double delta = Math.abs( Math.abs(angle.getRadian()) - Math.PI / 2.);
			if (delta < angleEps.getRadian()) {
				value = angle.greaterThan(Math.PI / 4.) ? 1 : -1;
			}
		}
		return value;
	}

	public Angle getAngle(int i) {
		Angle angle = null;
		if (i >= 0 && i < primitiveList.size()) {
			angle = primitiveList.get(i).getAngle();
			if (angle != null) {
				angle.normalizeToPlusMinusPI();
			}
		}
		return angle;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
	
	public boolean isClosed() {
		return isClosed;
	}

	public List<Integer> getTwoQuadrantList(Angle angleEps) {
		List<Integer> quadStartList = new ArrayList<Integer>();
		for (int i = 0; i < primitiveList.size() - 1; i++) {
			SVGPathPrimitive primitive0 = primitiveList.get(i);
			SVGPathPrimitive primitive1 = primitiveList.get(i + 1);
			if (primitive0 instanceof CubicPrimitive &&
				primitive1 instanceof CubicPrimitive) {
				if (isTwoQuadrants(i, angleEps)) {
					quadStartList.add(i);
				}
			}
		}
		return quadStartList;
	}

	public void replace2QuadrantsByButt(int quad, double maxCapRadius) {
//		CubicPrimitive cubic0 = (CubicPrimitive) this.primitiveList.get(quad);
		CubicPrimitive cubic1 = (CubicPrimitive) this.primitiveList.get(quad + 1);
//		Real2 point0 = cubic0.getFirstCoord();
		Real2 point1 = cubic1.getLastCoord();
		LinePrimitive linePrimitive = new LinePrimitive(point1);
		primitiveList.remove(quad +1);
		primitiveList.remove(quad);
		primitiveList.add(quad, linePrimitive);
		setFirstPoints();
	}

	/** interpret primitive as line if possible.
	 * 
	 * @param i must be > 0
	 * @return line else null if i == 0
	 */
	public SVGLine getLine(int i) {
		SVGLine line = null;
		if (i > 0) {
			SVGPathPrimitive primitive = this.get(i);
			if (primitive instanceof LinePrimitive) {
				Real2 point0 = this.get(i - 1).getLastCoord();
				Real2 point1 = primitive.getFirstCoord();
				line = new SVGLine(point0, point1);
			}
		}
		return line;
	}

	public Arc getQuadrant(int i, Angle angleEps) {
		Arc quadrant = null;
		SVGPathPrimitive prim = primitiveList.get(i);
		if (prim instanceof CubicPrimitive) {
			Angle angle = prim.getAngle();
		}
		return quadrant;
	}

	/** replaces coordinate array in given primitive.
	 * 
	 * if indexed primitive is of wrong type, no operation.
	 * 
	 * @param cubicPrimitive
	 * @param i
	 */
	public void replaceCoordinateArray(CubicPrimitive cubicPrimitive, int i) {
		if (primitiveList != null) {
			if (this.get(i) instanceof CubicPrimitive) {
				CubicPrimitive thisCubicPrimitive = (CubicPrimitive) this.get(i);
				thisCubicPrimitive.setCoordArray(cubicPrimitive.getCoordArray());
			}
		}
	}
//
//	/** replaces coordinate array in given primitive.
//	 * 
//	 * if indexed primitive is of wrong type, no operation.
//	 * 
//	 * @param cubicPrimitive
//	 * @param i
//	 */
//	public void replaceCoordinateArray(LinePrimitive linePrimitive, int i) {
//		if (primitiveList != null) {
//			if (this.get(i) instanceof LinePrimitive) {
//				this.get(i).setCoordArray(linePrimitive.getCoordArray());
//			}
//		}
//	}

	public String getDString() {
		return SVGPath.constructDString(this);
	}

	public void replaceCoordinateArray(Real2Array coordArray, int i) {
		if (primitiveList != null) {
			if (this.get(i) instanceof LinePrimitive) {
				this.get(i).setCoordArray(coordArray);
			}
		}
	}

	private LinePrimitive calculateMeanLine(int i, int j) {
		LinePrimitive linei = this.getLinePrimitive(i);
		LinePrimitive linej = this.getLinePrimitive(j);
		return (linei == null || linej == null) ? null : linei.calculateMeanLine(linej);
	}

	public LinePrimitive getLinePrimitive(int i) {
		SVGPathPrimitive primitive = this.get(i);
		return (primitive == null || !(primitive instanceof LinePrimitive)) ?
			null : (LinePrimitive) primitive;
	}

	public LinePrimitive createMeanLine(int i, int j) {
		LinePrimitive line = calculateMeanLine(i, j);
		replaceCoordinateArray(line.getCoordArray(), i);
		replaceCoordinateArray(line.getReverseCoordArray(), j);
		return line;
	}

	public CubicPrimitive getCubicPrimitive(int i) {
		SVGPathPrimitive primitive = this.get(i);
		return (primitive == null || !(primitive instanceof CubicPrimitive)) ?
			null : (CubicPrimitive) primitive;
	}

	public Arc createMeanCubic(int i, int j) {
		CubicPrimitive cubic0 = getCubicPrimitive(i);
		CubicPrimitive cubic1 = getCubicPrimitive(j);
		Arc arc0 = new Arc(cubic0);
		Arc arc1 = new Arc(cubic1);
		Arc meanArc = arc0.calculateMeanArc(arc1);
		replaceCoordinateArray(meanArc.getCubicPrimitive(), i);
		replaceCoordinateArray(meanArc.getReverseCubicPrimitive(), j);
		return meanArc;
	}

	public void remove(int i) {
		SVGPathPrimitive primitive = this.get(i);
		if (primitive != null) {
			primitiveList.remove(i);
		}
	}
	
}
