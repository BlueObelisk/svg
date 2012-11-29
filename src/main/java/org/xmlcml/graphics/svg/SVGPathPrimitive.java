package org.xmlcml.graphics.svg;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.Transform2;

/**
 * parts of path (M, L, C, Z) currently not LHSQTA
 * @author pm286
 *
 */
public abstract class SVGPathPrimitive {
	private static Logger LOG = Logger.getLogger(SVGPathPrimitive.class);

	public static final char ABS_LINE = 'L';
	public static final char ABS_MOVE = 'M';
	public static final char ABS_CUBIC = 'C';
	public static final char ABS_QUAD = 'Q';
	public static final char ABS_CLOSE = 'Z';
	public static final char REL_LINE = 'l';
	public static final char REL_MOVE = 'm';
	public static final char REL_CUBIC = 'c';
	public static final char REL_QUAD = 'q';
	public static final char REL_CLOSE = 'z';
	
	protected Real2Array coordArray;
	
	public SVGPathPrimitive() {
		
	}
	
	public abstract String getTag();
	
	public static List<SVGPathPrimitive> parseDString(String d) {
		List<SVGPathPrimitive> primitiveList = new ArrayList<SVGPathPrimitive>();
		List<String> tokenList = extractTokenList(d);
		int itok = 0;
		Real2 lastXY = null;
		while (itok <tokenList.size()) {
			String token = tokenList.get(itok);
			if (token.length() != 1) {
				throw new RuntimeException("Bad token, expected single char: "+token);
			}
			char t = token.charAt(0);
			itok++;
			if (ABS_MOVE == t) {
				double[] dd = readDoubles(tokenList, 2, itok);
				itok += 2;
				Real2 r2 = new Real2(dd[0], dd[1]);
				SVGPathPrimitive pp = new MovePrimitive(r2);
				primitiveList.add(pp);
				lastXY = r2;
			} else if (REL_MOVE == t) {
				double[] dd = readDoubles(tokenList, 2, itok);
				itok += 2;
				Real2 r2 = new Real2(dd[0], dd[1]);
				r2 = r2.plus(lastXY);
				SVGPathPrimitive pp = new MovePrimitive(r2);
				primitiveList.add(pp);
				lastXY = r2;
			} else if (ABS_LINE == t) {
				double[] dd = readDoubles(tokenList, 2, itok);
				itok += 2;
				Real2 r2 = new Real2(dd[0], dd[1]);
				SVGPathPrimitive pp = new LinePrimitive(r2);
				primitiveList.add(pp);
				lastXY = r2;
			} else if (REL_LINE == t) {
				double[] dd = readDoubles(tokenList, 2, itok);
				itok += 2;
				Real2 r2 = new Real2(dd[0], dd[1]);
				r2 = r2.plus(lastXY);
				SVGPathPrimitive pp = new LinePrimitive(r2);
				primitiveList.add(pp);
				lastXY = r2;
			} else if (ABS_CUBIC == t) {
				Real2Array r2a = readReal2Array(tokenList, 6, itok);
				itok += 6;
				SVGPathPrimitive pp = new CubicPrimitive(r2a);
				primitiveList.add(pp);
				lastXY = null;
			} else if (REL_CUBIC == t) {
				throw new RuntimeException("relative cubic not suported");
			} else if (ABS_QUAD == t) {
				Real2Array r2a = readReal2Array(tokenList, 4, itok);
				itok += 4;
				SVGPathPrimitive pp = new QuadPrimitive(r2a);
				primitiveList.add(pp);
				lastXY = null;
			} else if (REL_QUAD == t) {
				throw new RuntimeException("relative quadratic not suported");
			} else if (ABS_CLOSE == t || REL_CLOSE == t) {
				SVGPathPrimitive pp = new ClosePrimitive();
				primitiveList.add(pp);
			}
		}
		return primitiveList;
	}

	private static Real2Array readReal2Array(List<String> tokenList, int ntoread, int itok) {
		double[] dd = readDoubles(tokenList, ntoread, itok);
		return Real2Array.createFromPairs(new RealArray(dd));
	}

	private static double[] readDoubles(List<String> tokenList, int ntoread, int itok) {
		if (itok + ntoread > tokenList.size()) {
			throw new RuntimeException("Ran out of tokens at "+itok+" wanted "+ntoread);
		}
		double[] dd = new double[ntoread];
		for (int i = 0; i < ntoread; i++) {
			Double d = null;
			String token = null;
			try {
				token = tokenList.get(itok + i);
				dd[i] = new Double(token);
			} catch (Exception e) {
				throw new RuntimeException("Cannot parse as double ("+token+") at : "+itok+i);
			}
		}
		return dd;
	}

	private static List<String> extractTokenList(String d) {
		List<String> tokenList = new ArrayList<String>();
		int numberStart = -1;
		for (int i = 0; i < d.length(); i++) {
			char c = d.charAt(i);
			if (Character.isWhitespace(c) ||c == ',') {
				addCurrentNumber(d, tokenList, numberStart, i);
				numberStart = -1;
			} else if (Character.isDigit(c) || c == '+' || c == '-' || c == '.') {
				if (numberStart == -1) {
					numberStart = i;
				}
			} else if ("mMcClLzZ".indexOf(c) != -1) {
				addCurrentNumber(d, tokenList, numberStart, i);
				tokenList.add(""+c);
				numberStart = -1;
			} else {
				throw new RuntimeException("Unknown character in dString: "+c+" path: "+d);
			}
		}
		addCurrentNumber(d, tokenList, numberStart, d.length());
		return tokenList;
	}

	private static void addCurrentNumber(String d, List<String> tokenList,
			int numberStart, int i) {
		if (numberStart != -1) {
			// add current number
			tokenList.add(d.substring(numberStart, i));
		}
	}
		
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
			if (cc == ABS_MOVE || cc == REL_MOVE || cc == ABS_LINE || cc == REL_LINE ) {
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
				pathPrimitive = new CubicPrimitive(r2a);
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
	
	public static String formatDString(String d, int places) {
		List<SVGPathPrimitive> primitiveList = null;
		try {
			primitiveList = SVGPathPrimitive.parseDString(d);
		} catch (RuntimeException e) {
			LOG.debug("Cannot parse: "+d);
			throw e;
		}
		for (SVGPathPrimitive primitive : primitiveList) {
			primitive.format(places);
		}
		d = createD(primitiveList);
		return d;
	}
	
	public static String formatD(String d, int places) {
		List<SVGPathPrimitive> primitiveList = SVGPathPrimitive.parseD(d);
		for (SVGPathPrimitive primitive : primitiveList) {
			primitive.format(places);
		}
		d = createD(primitiveList);
		return d;
	}
	
	public static String createD(List<SVGPathPrimitive> primitiveList) {
		StringBuilder sb = new StringBuilder();
		for (SVGPathPrimitive primitive : primitiveList) {
			sb.append(primitive.toString());
		}
		return sb.toString();
	}
	
	public static String createSignature(List<SVGPathPrimitive> primitiveList) {
		StringBuilder sig = new StringBuilder();
		for (SVGPathPrimitive primitive : primitiveList) {
			sig.append(primitive.getTag());
		}
		return sig.toString();
	}

	public void transformBy(Transform2 t2) {
		
		if (coordArray != null) {
			coordArray.transformBy(t2);
		}
	}
	
	public Real2 getCoords() {
		return coordArray == null ? null : coordArray.get(0);
	}

	public Real2Array getCoordArray() {
		return coordArray;
	}

	public String toString() {
		throw new RuntimeException("Must override toString() in SVGPathPrimitive");
	}
	
	protected String formatCoords(Real2 coords) {
		return coords == null ? null : ((int)(1000*coords.getX()))/1000.+" "+(int)(1000*coords.getY())/1000.+" ";
	}
	
	public void format(int places) {
		// skip for Z etc
		if (coordArray != null) {
			coordArray.format(places);
		}
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
