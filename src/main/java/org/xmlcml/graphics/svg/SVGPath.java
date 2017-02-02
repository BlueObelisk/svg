/**
 *    Copyright 2011 Peter Murray-Rust et. al.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.xmlcml.graphics.svg;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.euclid.Vector2;
import org.xmlcml.graphics.svg.path.Arc;
import org.xmlcml.graphics.svg.path.ClosePrimitive;
import org.xmlcml.graphics.svg.path.CubicPrimitive;
import org.xmlcml.graphics.svg.path.LinePrimitive;
import org.xmlcml.graphics.svg.path.MovePrimitive;
import org.xmlcml.graphics.svg.path.PathPrimitiveList;
import org.xmlcml.xml.XMLConstants;
import org.xmlcml.xml.XMLUtil;

/** 
 * @author pm286
 */
public class SVGPath extends SVGShape {

	private static Logger LOG = Logger.getLogger(SVGPath.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private static final String MLLLL = "MLLLL";
	private static final String MLLLLZ = "MLLLLZ";
	public final static String CC = "CC";
	public final static String D = "d";
	public final static String TAG ="path";
	private final static double EPS1 = 0.000001;
	private final static double MIN_COORD = .00001;
	public final static String ALL_PATH_XPATH = ".//svg:path";
	public final static String REMOVED = "removed";
	public final static String ROUNDED_CAPS = "roundedCaps";
	private static final Double ANGLE_EPS = 0.01;
	private static final Double MAX_WIDTH = 2.0;
	public final static Pattern REPEATED_ML = Pattern.compile("ML(ML)*");
	private static final double CIRCLE_EPSILON = 0.01;
	
	private GeneralPath path2;
	private boolean isClosed = false;
	private Real2Array coords = null; // for diagnostics
	private SVGPolyline polyline;
	private Real2Array allCoords;
	private PathPrimitiveList primitiveList;
	private Boolean isPolyline;
	private Real2Array firstCoords;
	private String signature;

	/** 
	 * Constructor
	 */
	public SVGPath() {
		super(TAG);
		init();
	}
	
	/** 
	 * Constructor
	 */
	public SVGPath(SVGPath element) {
        super(element);
	}
	
	/** 
	 * Constructor
	 */
	public SVGPath(GeneralPath generalPath) {
        super(TAG);
        String d = SVGPath.constructDString(generalPath);
        setDString(d);
	}
	
	public SVGPath(Shape shape) {
		super(TAG);
		PathIterator pathIterator = shape.getPathIterator(new AffineTransform());
		String pathString = SVGPath.getPathAsDString(pathIterator);
		this.setDString(pathString);
	}

	
	/** constructor
	 */
	public SVGPath(Element element) {
        super((SVGElement) element);
	}
	
	public SVGPath(Real2Array xy) {
		this(createD(xy));
	}
	
	public SVGPath(String d) {
		this();
		setDString(d);
	}
	
	public SVGPath(PathPrimitiveList primitiveList, SVGPath reference) {
		this();
		if (reference != null) {
			XMLUtil.copyAttributes(reference, this);
		}
		setDString(SVGPathPrimitive.createD(primitiveList));
	}
	
	/**
     * Copies node.
     *
     * @return Node
     */
    public Node copy() {
        return new SVGPath(this);
    }

	protected void init() {
		super.setDefaultStyle();
		setDefaultStyle(this);
	}
	
	public boolean isClosed() {
		return isClosed;
	}
	public static void setDefaultStyle(SVGPath path) {
		path.setStroke("black");
		path.setStrokeWidth(0.5);
		path.setFill("none");
	}
	
	/** 
	 * Creates a list of primitives
	 * <p>
	 * At present Move, Line, Curve, Z
	 * @param d
	 * @return
	 */
	public PathPrimitiveList parseDString() {
		String d = getDString();
		return (d == null ? null : SVGPathPrimitive.parseDString(d));
	}
	
    private static String createD(Real2Array xy) {
		String s = XMLConstants.S_EMPTY;
		StringBuilder sb = new StringBuilder();
		if (xy.size() > 0) {
			sb.append("M");
			sb.append(xy.get(0).getX()+S_SPACE);
			sb.append(xy.get(0).getY()+S_SPACE);
		}
		if (xy.size() > 1) {
			for (int i = 1; i < xy.size(); i++ ) {
				sb.append("L");
				sb.append(xy.get(i).getX()+S_SPACE);
				sb.append(xy.get(i).getY()+S_SPACE);
			}
			sb.append("Z");
		}
		s = sb.toString();
		return s;
	}
	
	public void setD(Real2Array r2a) {
		this.setDString(createD(r2a));
	}
	
	public void setDString(String d) {
		if (d != null) {
			this.addAttribute(new Attribute(D, d));
		}
	}
	
	public String getDString() {
		return this.getAttributeValue(D);
	}
	
	
//  <g style="stroke-width:0.2;">
//  <line x1="-1.9021130325903073" y1="0.6180339887498945" x2="-1.175570504584946" y2="-1.618033988749895" stroke="black" style="stroke-width:0.36;"/>
//  <line x1="-1.9021130325903073" y1="0.6180339887498945" x2="-1.175570504584946" y2="-1.618033988749895" stroke="white" style="stroke-width:0.12;"/>
//</g>
	
	protected void drawElement(Graphics2D g2d) {
		saveGraphicsSettingsAndApplyTransform(g2d);
		setAntialiasing(g2d, true);
//		setAntialiasing(g2d, false);
		GeneralPath path = createPath2D();
		path.transform(cumulativeTransform.getAffineTransform());
		drawFill(g2d, path);
		restoreGraphicsSettingsAndTransform(g2d);
	}

	/** 
	 * Extract polyline if path is M followed by Ls
	 * 
	 * @return
	 */
	public void createCoordArray() {
		polyline = null;
		allCoords = new Real2Array();
		firstCoords = new Real2Array();
		ensurePrimitives();
		isPolyline = true;
		for (SVGPathPrimitive primitive : primitiveList) {
			if (primitive instanceof CubicPrimitive) {
				isPolyline = false;
				Real2Array curveCoords = primitive.getCoordArray();
				allCoords.add(curveCoords);
				firstCoords.add(primitive.getFirstCoord());
				//break;
			} else if (primitive instanceof ClosePrimitive) {
				isClosed = true;
			} else {
				Real2 r2 = primitive.getFirstCoord();
				allCoords.add(r2);
				firstCoords.add(r2);
			}
		}
	}
	
	public SVGPoly createPolyline() {
		createCoordArray();
		if (isPolyline && allCoords.size() > 1) {
			polyline = new SVGPolyline(allCoords);
			polyline.setClosed(isClosed);
		}
		return polyline;
	}
	
	public SVGRect createRectangle(double epsilon) {
		createPolyline();
		return polyline == null ? null : polyline.createRect(epsilon);
	}

	public SVGSymbol createSymbol(double maxWidth) {
		createCoordArray();
		SVGSymbol symbol = null;
		Real2Range r2r = getBoundingBox();
		if (Math.abs(r2r.getXRange().getRange()) < maxWidth && Math.abs(r2r.getYRange().getRange()) < maxWidth) {
			symbol = new SVGSymbol();
			SVGPath path = (SVGPath) copy();
			Real2 orig = path.getOrigin();
			path.normalizeOrigin();
			SVGShape line = path.createHorizontalOrVerticalLine(EPS);
			symbol.appendChild(path);
			symbol.setId(path.getId()+".s");
			List<SVGElement> defsNodes = SVGUtil.getQuerySVGElements(this, "/svg:svg/svg:defs");
			defsNodes.get(0).appendChild(symbol);
		}
		return symbol;
	}

	private SVGShape createHorizontalOrVerticalLine(double eps) {
		SVGLine  line = null;
		Real2Array coords = getCoords();
		if (coords.size() == 2) {
			line = new SVGLine(coords.get(0), coords.get(1));
			if (!line.isHorizontal(eps) && !line.isVertical(eps)) {
				line = null;
			}
		}
		return  line;
	}

	/** sometimes polylines represent circles
	 * crude algorithm - assume points are roughly equally spaced
	 * @param maxSize
	 * @param epsilon
	 * @return
	 */
	public SVGCircle createCircle(double epsilon) {
		createCoordArray();
		SVGCircle circle = null;
		String signature = getSignature();
		if (signature.equals("MCCCCZ") || signature.equals("MCCCC") && isClosed) {
			PathPrimitiveList primList = ensurePrimitives();
			Angle angleEps = new Angle(0.05, Units.RADIANS);
			Real2Array centreArray = new Real2Array();
			RealArray radiusArray = new RealArray();
			for (int i = 1; i < 5; i++) {
				Arc arc = primList.getQuadrant(i, angleEps);
				if (arc != null) {
					Real2 centre = arc.getCentre();
					if (centre != null) {
						centreArray.add(centre);
						double radius = arc.getRadius();
						radiusArray.addElement(radius);
					}
				} else {
					LOG.trace("null quadrant");
				}
			}
			Real2 meanCentre = centreArray.getMean();
			Double meanRadius = radiusArray.getMean();
			if (meanCentre != null) {
				circle = new SVGCircle(meanCentre, meanRadius);
			}
		} else if (isClosed && allCoords.size() >= 8) {
			// no longer useful I think
			/*LOG.debug("CIRCLE: "+signature);
			Real2Range r2r = this.getBoundingBox();
			//Is it square?
			if (Real.isEqual(r2r.getXRange().getRange(),  r2r.getYRange().getRange(), 2*epsilon)) {
				Real2 centre = r2r.getCentroid();
				Double sum = 0.0;
				double[] spokeLengths = new double[firstCoords.size()];
				for (int i = 0; i < firstCoords.size(); i++) {
					Double spokeLength = centre.getDistance(firstCoords.get(i));
					spokeLengths[i] = spokeLength;
					sum += spokeLength;
				}
				Double rad = sum / firstCoords.size();
				for (int i = 0; i < spokeLengths.length; i++) {
					if (Math.abs(spokeLengths[i] - rad) > epsilon) {
						return null;
					}
				}
				circle = new SVGCircle(centre, rad);
			}*/
		}
		return circle;
	}

	public PathPrimitiveList ensurePrimitives() {
		isClosed = false;
		if (primitiveList == null) {
			primitiveList = createPathPrimitives();
		}
		if (primitiveList.size() > 1) {
			SVGPathPrimitive primitive0 = primitiveList.get(0);
			SVGPathPrimitive primitiveEnd = primitiveList.get(primitiveList.size() - 1);
			Real2 coord0 = primitive0.getFirstCoord();
			Real2 coordEnd = primitiveEnd.getLastCoord();
			isClosed = coord0.getDistance(coordEnd) < EPS1;
			primitiveList.setClosed(isClosed);
		}
		return primitiveList;
	}

	/**
	 * Do two paths have identical coordinates?
	 * 
	 * @param svgPath 
	 * @param path2
	 * @param epsilon tolerance allowed
	 * @return
	 */
	public boolean hasEqualCoordinates(SVGPath path2, double epsilon) {
		Real2Array r2a = getCoords();
		Real2Array r2a2 = path2.getCoords();
		return r2a.isEqualTo(r2a2, epsilon);
	}
	
	public Real2Array getCoords() {
		//if (coords == null) {
		coords = new Real2Array();
		String ss = getDString().trim()+S_SPACE;
		PathPrimitiveList primitives = createPathPrimitives();
		for (SVGPathPrimitive primitive : primitives) {
			Real2 coord = primitive.getFirstCoord();
			Real2Array coordArray = primitive.getCoordArray();
			if (coord != null) {
				coords.add(coord);
			} else if (coordArray != null) {
				coords.add(coordArray);
			}
		}
		//}
		return coords;
	}
	
	/**
	 * Scale of bounding boxes
	 * <p>
	 * scale = Math.sqrt(xrange2/this.xrange * yrange2/this.yrange)
	 * <p> 
	 * (Can be used to scale vector fonts or other scalable objects)
	 * 
	 * @param path2
	 * @return null if problem (e.g. zero ranges). result may be zero
	 */
	public Double getBoundingBoxScalefactor(SVGPath path2) {
		Double s = null;
		if (path2 != null) {
			Real2Range bb = this.getBoundingBox();
			Real2Range bb2 = path2.getBoundingBox();
			double xr = bb.getXRange().getRange();
			double yr = bb.getYRange().getRange();
			if (xr > EPS && yr > EPS) {
				s = Math.sqrt(bb2.getXRange().getRange()/xr * bb2.getYRange().getRange()/yr);
			}
		}
		return s;
	}
	
	/**
	 * compares paths scaled by bounding boxes and then compares coordinates
	 * @param path2
	 * @return null if paths cannot be scaled else bounding box ratio
	 */
	public Double getScalefactor(SVGPath path2, double epsilon) {
		Double s = this.getBoundingBoxScalefactor(path2);
		if (s != null) {
			SVGPath path = (SVGPath) this.copy();
			path.normalizeOrigin();
			Transform2 t2 = new Transform2(new double[]{s,0.,0.,0.,s,0.,0.,0.,1.});
			path.applyTransform(t2);
			SVGPath path22 = (SVGPath) path2.copy();
			path22.normalizeOrigin();
			if (!path.hasEqualCoordinates(path22, epsilon)) {
				s = null;
			}
		}
		return s;
	}

	private PathPrimitiveList createPathPrimitives() {
		return SVGPathPrimitive.parseDString(getDString());
	}

	/** 
	 * Gets bounding box
	 * <p>
	 * Uses coordinates given and ignores effect of curves
	 */
	@Override
	public Real2Range getBoundingBox() {
		if (boundingBox == null) {
			getCoords();
			boundingBox = coords.getRange2();
		}
		return boundingBox;
	}
	
	/** 
	 * Property of graphic bounding box
	 * <p>
	 * Can be overridden
	 * 
	 * @return default none
	 */
	protected String getBBFill() {
		return "none";
	}

	/** 
	 * Property of graphic bounding box
	 * <p>
	 * Can be overridden
	 * 
	 * @return default blue
	 */
	protected String getBBStroke() {
		return "blue";
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default 0.1
	 */
	protected double getBBStrokeWidth() {
		return 0.1;
	}

	public GeneralPath createPath2D() {
		path2 = new GeneralPath();
		ensurePrimitives();
		for (SVGPathPrimitive pathPrimitive : primitiveList) {
			pathPrimitive.operateOn(path2);
		}
		return path2;
	}
	
	/** get tag.
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	public GeneralPath getPath2() {
		return path2;
	}

	public void setPath2(GeneralPath path2) {
		this.path2 = path2;
	}
	
	public void applyTransform(Transform2 t2) {
		PathPrimitiveList pathPrimitives = this.createPathPrimitives();
		for (SVGPathPrimitive primitive : pathPrimitives) {
			primitive.transformBy(t2);
		}
		setD(pathPrimitives);
	}
	
	public void format(int places) {
		super.format(places);
		String d = getDString();
		d = SVGPathPrimitive.formatDString(d, places);
		this.setDString(d);
	}

	@Override
	public String getSignature() {
		if (signature == null) {
			if (getDString() != null) {
				ensurePrimitives();
				signature = SVGPathPrimitive.createSignature(primitiveList);
			}
		}
		return signature;
	}

//	private PathPrimitiveList getPrimitiveList() {
//		if (primitiveList == null) {
//			primitiveList = new PathPrimitiveList();
//			primitiveList.add(SVGPathPrimitive.parseDString(getDString()).getPrimitiveList());
//		}
//		return primitiveList;
//	}

	public void normalizeOrigin() {
		boundingBox = null; // fprce recalculate
		Real2Range boundingBox = this.getBoundingBox();
		if (boundingBox == null) {
			throw new RuntimeException("NULL BoundingBox");
		}
		RealRange xr = boundingBox.getXRange();
		RealRange yr = boundingBox.getYRange();
		Real2 xymin = new Real2(xr.getMin(), yr.getMin());
		xymin = xymin.multiplyBy(-1.0);
		Transform2 t2 = new Transform2(new Vector2(xymin));
		applyTransformationToPrimitives(t2);
	}

	private void applyTransformationToPrimitives(Transform2 t2) {
		PathPrimitiveList primitives = this.parseDString();
		for (SVGPathPrimitive primitive : primitives) {
			primitive.transformBy(t2);
		}
		this.setD(primitives);
	}

	private void setD(PathPrimitiveList primitives) {
		String d = constructDString(primitives);
		this.addAttribute(new Attribute(D, d));
	}


	public static String constructDString(GeneralPath generalPath) {
		PathIterator pathIterator = generalPath.getPathIterator(new AffineTransform());
		return getPathAsDString(pathIterator);
	}

	public static String getPathAsDString(PathIterator pathIterator) {
		StringBuilder dd = new StringBuilder();
		double[] coords = new double[6];
		while (!pathIterator.isDone()) {
			int segType = pathIterator.currentSegment(coords);
			coords = normalizeSmallCoordsToZero(coords);
			if (PathIterator.SEG_MOVETO == segType) {
				dd.append(" M "+coords[0]+" "+coords[1]);
			} else if (PathIterator.SEG_LINETO == segType) {
				dd.append(" L "+coords[0]+" "+coords[1]);
			} else if (PathIterator.SEG_QUADTO == segType) {
				dd.append(" Q "+coords[0]+" "+coords[1]+" "+coords[2]+" "+coords[3]);
			} else if (PathIterator.SEG_CUBICTO == segType) {
				dd.append(" C "+coords[0]+" "+coords[1]+" "+coords[2]+" "+coords[3]+" "+coords[4]+" "+coords[5]);
			} else if (PathIterator.SEG_CLOSE == segType) {
				dd.append(" Z ");
			} else {
				throw new RuntimeException("UNKNOWN "+segType);
			}
			pathIterator.next();
		}
		return dd.toString();
	}

	private static double[] normalizeSmallCoordsToZero(double[] coords) {
		for (int i = 0; i < coords.length; i++) {
			if (!Double.isNaN(coords[i]) && Math.abs(coords[i]) < MIN_COORD) {
				coords[i] = 0.0;
			}
		}
		return coords;
	}

	public static String constructDString(PathPrimitiveList primitives) {
		StringBuilder dd = new StringBuilder();
		for (SVGPathPrimitive primitive : primitives) {
			dd.append(primitive.toString());
		}
		return dd.toString();
	}

	public Real2 getOrigin() {
		Real2Range r2r = this.getBoundingBox();
		return new Real2(r2r.getXMin(), r2r.getYMin());
	}

	/** opposite corner to origin
	 * 
	 * @return
	 */
	public Real2 getUpperRight() {
		Real2Range r2r = this.getBoundingBox();
		return new Real2(r2r.getXMax(), r2r.getYMax());
	}

	// there are some polylines which contain a small number of curves and may be transformable
	public SVGPoly createHeuristicPolyline(int minL, int maxC, int minPrimitives) {
		SVGPoly polyline = null;
		String signature = this.getSignature();
		if (signature.length() < 3) {
			return null;
		}
		// must start with M
		if (signature.charAt(0) != 'M') {
			return null;
		}
		// can only have one M
		if (signature.substring(1).indexOf("M") != -1) {
			return null;
		}
		signature.replaceAll("[^C]", "").length();
		StringBuilder sb = new StringBuilder();
		if (signature.length() >= minPrimitives) {
			int cCount = signature.replaceAll("[^C]", "").length();
			int lCount = signature.replaceAll("[^L]", "").length();
			if (lCount >= minL && maxC >= cCount) {
				for (SVGPathPrimitive primitive : primitiveList) {
					if (primitive instanceof CubicPrimitive) {
						sb.append("L"+primitive.getLastCoord().toString());
					} else {
						sb.append(primitive.toString());
					}
				}
			}
			SVGPath path = new SVGPath(sb.toString());
			polyline = new SVGPolyline(path);
		}
		return polyline;
	}
	
	public SVGShape createRoundedBox(double roundedBoxEps) {
		return null;
	}

	/** 
	 * Makes a new list composed of the paths in the list
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGPath> extractPaths(List<SVGElement> elements) {
		List<SVGPath> pathList = new ArrayList<SVGPath>();
		for (SVGElement element : elements) {
			if (element instanceof SVGPath) {
				pathList.add((SVGPath) element);
			}
		}
		return pathList;
	}

	@Override
	public String getGeometricHash() {
		return getDString();
	}

	/** 
	 * Convenience method to extract list of svgPaths in element
	 * 
	 * @param svgElement
	 * @return
	 */
	public static List<SVGPath> extractPaths(SVGElement svgElement) {
		return SVGPath.extractPaths(SVGUtil.getQuerySVGElements(svgElement, ALL_PATH_XPATH));
	}

	public static List<SVGPath> extractSelfAndDescendantPaths(SVGElement svgElement) {
		return SVGPath.extractPaths(SVGUtil.getQuerySVGElements(svgElement, ALL_PATH_XPATH));
	}

	/** 
	 * Not finished
	 * 
	 * @param svgPath
	 * @param distEps
	 * @param angleEps
	 * @return
	 * @deprecated Use replaceAllUTurnsByButt(Angle, false).
	 */
	public SVGPath replaceAllUTurnsByButt(Angle angleEps) {
		return replaceAllUTurnsByButt(angleEps, false);
	}

	/** 
	 * Not finished
	 * 
	 * @param svgPath
	 * @param distEps
	 * @param angleEps
	 * @return
	 */
	public SVGPath replaceAllUTurnsByButt(Angle angleEps, boolean extend) {
		SVGPath path = null;
		if (getSignature().contains(CC)) {
			PathPrimitiveList primList = ensurePrimitives();
			List<Integer> quadrantStartList = primList.getUTurnList(angleEps);
			if (quadrantStartList.size() > 0) {
				for (int quad = quadrantStartList.size() - 1; quad >= 0; quad--) {
					primList.replaceUTurnsByButt(quadrantStartList.get(quad), extend/*, maxCapRadius*/);
				}
				path = new SVGPath(primList, this);
				SVGUtil.setSVGXAttribute(path, ROUNDED_CAPS, REMOVED);
			}
		}
		return path;
	}

	/** 
	 * Creates a line from path with signature "MLLLL", "MLLLLZ".
	 * 
	 * creates a line from thin rectangle.

	 * <p>
	 * Uses primitiveList.createLineFromMLLLL().
	 * 
	 * @param angleEps
	 * @param maxWidth above this assumes it's a rectangle
	 * @return null if line has wrong signature or is too wide or not antiParallel.
	 */
	public SVGLine createLineFromMLLLL(Angle angleEps, Double maxWidth) {
		SVGLine line = null;
		String sig = getSignature();
		if (MLLLL.equals(sig) || MLLLLZ.equals(sig)) {
			ensurePrimitives();
			line = primitiveList.createLineFromMLLLL(angleEps, maxWidth);
		}
		return line;
	}
	
	/**
	 * a signature of MLMLML... indicates separate lines (either ladders or dashed lines)
	 * 
	 * @param empty or null, use any MLML... ; or specific MLML
	 * @return list of lines (one for each ML)
	 */
	public List<SVGLine> createSeparatedLinesFromRepeatedML(String refSig) {
		String sig = getSignature();
		List<SVGLine> lineList = new ArrayList<SVGLine>();
		if (isRepeatedML(sig)) {
			if (refSig == null || "".equals(refSig.trim()) ||
			refSig.equals(sig)) {
				ensurePrimitives();
				for (int i = 0; i < primitiveList.size(); i += 2) {
					MovePrimitive movePrimitive = (MovePrimitive) primitiveList.get(i);
					LinePrimitive linePrimitive = (LinePrimitive) primitiveList.get(i + 1);
					SVGLine line = new SVGLine(movePrimitive.getFirstCoord(), linePrimitive.getFirstCoord());
					lineList.add(line);
				}
			}
		}
		return lineList;
	}

	public static boolean isRepeatedML(String sig) {
		return sig.startsWith("ML") && sig.replaceAll("ML", "").length() == 0;
	}

	public static List<SVGLine> createLinesFromPaths(List<SVGPath> pathList) {
		List<SVGLine> allLines = new ArrayList<SVGLine>();
		for (SVGPath path : pathList) {
			SVGLineList lineList = path.createLineListFromRepeatedML(null);
			if (lineList != null) {
				allLines.addAll(lineList.getLineList());
			}
		}
		return allLines;
	}

	private SVGLineList createLineListFromRepeatedML(String repeatedMLSignature) {
		SVGLineList lineList = null;
		List<SVGLine> lines = this.createSeparatedLinesFromRepeatedML(repeatedMLSignature);
		if (lines.size() > 0) {
			lineList = new SVGLineList(lines);
		}
		return lineList;
	}

	public static List<SVGCircle> createCirclesFromPaths(List<SVGPath> pathList) {
		List<SVGCircle> circleList = new ArrayList<SVGCircle>();
		for (SVGPath path : pathList) {
			SVGCircle circle = path.createCircle(CIRCLE_EPSILON);
			if (circle != null) {
				circleList.add(circle);
			} else {
				LOG.trace(path.getSignature());
			}
		}
		return circleList;
	}

	public static List<SVGLineList> createLineListListFromPaths(
			List<SVGPath> pathList, String repeatedMLSignature) {
		List<SVGLineList> lineListList = new ArrayList<SVGLineList>();
		for (SVGPath path : pathList) {
			SVGLineList lineList = path.createLineListFromRepeatedML(repeatedMLSignature);
			if (lineList != null) {
				lineListList.add(lineList);
			}
		}
		return lineListList;
	}

	/** translates "m" into "M" to create an absolute coordinate system.
	 * Also epxands implicit L commands into absolute ones
	 * @return
	 */
	public void makeRelativePathsAbsolute() {
		String d = getDString();
		if (d != null) {
			primitiveList = PathPrimitiveList.createPrimitiveList(d);
			String d1 = primitiveList.getDString();
			this.setDString(d1);
		}
	}

	@Override
	public String toString() {
		return (this.toXML());
	}

	@Override
	public boolean isZeroDimensional() {
		String signature = this.getSignature().toUpperCase();
		return (signature.equals("MZ") || signature.equals("M"));
	}

}