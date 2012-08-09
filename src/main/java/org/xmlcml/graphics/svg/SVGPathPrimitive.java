package org.xmlcml.graphics.svg;

import java.util.ArrayList;
import java.util.List;

import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Transform2;

/**
 * parts of path (M, L, C, Z) currently not LHSQTA
 * @author pm286
 *
 */
public abstract class SVGPathPrimitive {

	public static final char ABS_LINE = 'L';
	public static final char ABS_MOVE = 'M';
	public static final char REL_LINE = 'l';
	public static final char REL_MOVE = 'm';
	
	protected Real2 coords;
	protected Real2Array coordArray;
	
	public SVGPathPrimitive() {
		
	}
	
	public abstract String getTag();
	
	public static List<SVGPathPrimitive> parseD(String d) {
		if (d == null) {
			return null;
		}
		List<SVGPathPrimitive> primitiveList = new ArrayList<SVGPathPrimitive>();
		d = d.trim();
		int ii = 0;
		StringBuilder dd = new StringBuilder(d);
		Real2 lastXY = null;
		while (ii < dd.length()) {
			char cc = dd.charAt(ii);
			Real2String r2s = null;
			Real2 r2 = null;
			SVGPathPrimitive pathPrimitive = null;
			if (cc == 'M' || cc == 'm' || cc == 'L' || cc == 'l') {
				ii++;
				r2s = new Real2String(dd.substring(ii));
				r2 = r2s.getReal2();
				ii += r2s.idx;
				if (cc == ABS_MOVE) {
					pathPrimitive = new MovePrimitive(r2);
					lastXY = r2;
				} else if (cc == REL_MOVE) {
					lastXY = (lastXY == null) ? r2 : lastXY.plus(r2);
					pathPrimitive = new MovePrimitive(lastXY);
				} else if (cc == ABS_LINE) {
					pathPrimitive = new LinePrimitive(r2);
					lastXY = r2;
				} else if (cc == REL_LINE) {
					lastXY = (lastXY == null) ? r2 : lastXY.plus(r2);
					pathPrimitive = new LinePrimitive(lastXY);
				}

			} else if (cc == 'z' || cc == 'Z') {
				ii++;
				pathPrimitive = new ClosePrimitive();
			} else if (cc == 'c' || cc == 'C') {
				ii++;
				Real2Array r2a = new Real2Array();
				while (ii < dd.length()) {
					char c = dd.charAt(ii);
					if (Character.isLetter(c)) {
						break;
					} else {
						r2s = new Real2String(dd.substring(ii));
						r2 = r2s.getReal2();
						r2a.add(r2);
						ii += r2s.idx;
						if (cc == 'C') {
							lastXY = r2;
						} else {
							lastXY = (lastXY == null) ? r2 : lastXY.plus(r2);
						}
					}
				}
				pathPrimitive = new CurvePrimitive(r2a);
			} else if (cc == 'H' || cc == 'l' ||
					cc == 'V' || cc == 'v' ||
					cc == 'S' || cc == 's' ||
					cc == 'Q' || cc == 'q' ||
					cc == 'T' || cc == 't' ||
					cc == 'A' || cc == 'a') {
				throw new RuntimeException("command ("+cc+") not yet implemented");
			} else {
				pathPrimitive = new UnknownPrimitive(cc);
			}
			primitiveList.add(pathPrimitive);
		}
		return primitiveList;
	}
	
	public static String createSignature(List<SVGPathPrimitive> primitiveList) {
		StringBuilder sig = new StringBuilder();
		for (SVGPathPrimitive primitive : primitiveList) {
			sig.append(primitive.getTag());
		}
		return sig.toString();
	}

	public void transformBy(Transform2 t2) {
		
		if (coords != null) {
			coords.transformBy(t2);
		}
		if (coordArray != null) {
			coordArray.transformBy(t2);
		}
	}
	
	public Real2 getCoords() {
		return coords;
	}

	public Real2Array getCoordArray() {
		return coordArray;
	}

	public String toString() {
		throw new RuntimeException("Must override toString() in SVGPathPrimitives");
	}
	
	protected String formatCoords(Real2 coords) {
		return coords == null ? null : ((int)(1000*coords.getX()))/1000.+" "+(int)(1000*coords.getY())/1000.+" ";
	}

	public Real2 getFirstCoord() {
		Real2 coord = getCoords();
		if (coord != null) {
			return coord;
		}
		Real2Array coordArray = getCoordArray();
		return (coordArray) == null ? null : coordArray.get(0);
	}
	
	public Real2 getLastCoord() {
		Real2 coord = getCoords();
		if (coord != null) {
			return coord;
		}
		Real2Array coordArray = getCoordArray();
		return (coordArray) == null ? null : coordArray.getLastElement();
	}
}
