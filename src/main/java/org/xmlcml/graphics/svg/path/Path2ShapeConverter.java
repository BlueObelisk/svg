package org.xmlcml.graphics.svg.path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.ParentNode;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGPathPrimitive;
import org.xmlcml.graphics.svg.SVGPoly;
import org.xmlcml.graphics.svg.SVGPolygon;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGShape;
import org.xmlcml.graphics.svg.SVGUtil;
import org.xmlcml.graphics.svg.StyleBundle;
import org.xmlcml.xml.XMLUtil;

/** converts a SVGPath or list of SVGPaths to SVGShape(s).
 * 
 * <p>
 * Uses a variety of heuristics to split and combine primitives.
 * Customisable through setters.
 * </p>
 * 
 * @author pm286
 *
 */
public class Path2ShapeConverter {

	private final static Logger LOG = Logger.getLogger(Path2ShapeConverter.class);
	
	private static final String MLCCLCC = "MLCCLCC";
	private static final String MLCCLCCZ = "MLCCLCCZ";
	private static final String MLLLL = "MLLLL";
	private static final String MLLL = "MLLL";
	private static final String MCLC = "MCLC";

	private static final double _CIRCLE_EPS = 0.7;
	private static final double MOVE_EPS = 0.001;
	private static final double RECT_EPS = 0.01;
	private static final double _ROUNDED_BOX_EPS = 0.4;
	
	private static final Angle DEFAULT_MAX_ANGLE_FOR_PARALLEL = new Angle(0.12, Units.RADIANS);
	private static final double DEFAULT_MAX_WIDTH_FOR_PARALLEL = 2.0;
	public static final Angle DEFAULT_MAX_ANGLE = new Angle(0.15, Units.RADIANS);
	public static final Double DEFAULT_MAX_WIDTH = 2.0;
	private static final Double DEFAULT_MIN_RECT_THICKNESS = 0.99;
	private static final double DEFAULT_MAX_PATH_WIDTH = 1.0;
	private static final int DEFAULT_LINES_IN_POLYLINE = 8;
	private static final int DEFAULT_DECIMAL_PLACES = 3;
	
	private static final String SVG = "svg";
	private static final Angle ANGLE_EPS = new Angle(0.01);


	private Integer decimalPlaces = DEFAULT_DECIMAL_PLACES;
	private Integer minLinesInPolyline = DEFAULT_LINES_IN_POLYLINE;
	private boolean removeDuplicatePaths = true;
	private boolean removeRedundantMoveCommands = true;
	private boolean splitAtMoveCommands = true;
	private double maxPathWidth = DEFAULT_MAX_PATH_WIDTH;
	private double maxWidth = DEFAULT_MAX_WIDTH;
	private Angle maxAngle = DEFAULT_MAX_ANGLE;
	private Double minRectThickness = DEFAULT_MIN_RECT_THICKNESS;
	
	/** input and output */
	private List<SVGPath> pathListIn;
	private List<SVGShape> shapeListOut;
	private List<SVGPath> splitPathList;
	private SVGPath svgPath;

	private Angle maxAngleForParallel = DEFAULT_MAX_ANGLE_FOR_PARALLEL;
	private double maxWidthForParallel = DEFAULT_MAX_WIDTH_FOR_PARALLEL;

	public Path2ShapeConverter() {
	}

	public Path2ShapeConverter(SVGPath svgPath) {
		this.svgPath = svgPath;
	}
	
	public Path2ShapeConverter(List<SVGPath> pathListIn) {
		this.setPathList(pathListIn);
	}
	
	/** maximum width to be considered for condensing outline paths.
	 * 
	 * @param maxPathWidth
	 */
	public void setMaxPathWidth(double maxPathWidth) {
		this.maxPathWidth = maxPathWidth;
	}

	public void setMaxWidth(double maxWidth) {
		this.maxWidth = maxWidth;
	}

	public void setMaxAngle(Angle maxAngle) {
		this.maxAngle = maxAngle;
	}
	
	
	/** main routine if pathList has been read in.
	 * 
	 * @param pathList
	 */
	public List<SVGShape> convertPathsToShapes() {
		if (pathListIn != null) {
			convertPathsToShapes(pathListIn);
		}
		return shapeListOut;
	}

	/** main routine?
	 * 
	 * @param pathList
	 */
	public List<SVGShape> convertPathsToShapes(List<SVGPath> pathList) {
		this.setPathList(pathList);
		shapeListOut = new ArrayList<SVGShape>();
		int id = 0;
		for (SVGElement path : pathList) {
			SVGShape shape = convertPathToShape(path);
			shape.setId(shape.getClass().getSimpleName().toLowerCase().substring(SVG.length())+"."+id);
			shapeListOut.add(shape);
			id++;
		}
		return shapeListOut;
	}

	public SVGShape convertPathToShape(SVGElement path) {
		this.setSVGPath((SVGPath) path);
		SVGShape shape = this.convertPathToShape();
		return shape;
	}

	public void setSVGPath(SVGPath path) {
		this.svgPath = path;
	}
	
	public void setDecimalPlaces(int places) {
		this.decimalPlaces = places;
	}
	
	private SVGLine createLineFromMLLLorMLCCLCC(SVGPath path) {
		SVGLine line = null;
		if (path != null) {
			path = removeRoundedCapsFromPossibleLine(path);
			// if signature is now MLLLL continue
			line = path.createLineFromMLLLL(maxAngleForParallel, maxWidthForParallel);
		}
		return line;
	}

	private SVGPath removeRoundedCapsFromPossibleLine(SVGPath path) {
		String signature = path.getSignature();
		if (MLCCLCC.equals(signature) || MLCCLCCZ.equals(signature)) {
			SVGPath newPath = path.replaceAllUTurnsByButt(maxAngleForParallel);
			if (newPath != null) {
				path = newPath;			
			}
		}
		return path;
	}

	/** create best guess at higher SVGElement
	 * try rect, circle, line, polygon, polyline, else original path
	 * 
	 * @param shapeListOut
	 */
	public SVGShape convertPathToShape() {
		if (svgPath == null) return null;
		SVGShape shape = null;
		shape = createRectOrAxialLine(RECT_EPS);
		if (shape == null) {
			shape = svgPath.createRoundedBox(_ROUNDED_BOX_EPS);
		}
		if (shape == null) {
			shape = svgPath.createCircle(_CIRCLE_EPS);
		}
		if (shape == null) {
			SVGPolyline polyline = (SVGPolyline) svgPath.createPolyline();
			// not a polyline, return unchanged path
			if (polyline == null) {
				shape = new SVGPath(svgPath);
			} else {
				// SVG is a polyline, try the variants
					// is it a line?
				shape = polyline.createSingleLine();
				if (shape == null) {
					// or a polygon?
					shape = createPolygonRectOrLine(shape, polyline);
					LOG.trace("polygon "+shape);
				}
				// no, reset to polyline
				if (shape == null || shape instanceof SVGPolygon) {
					shape = createNarrowLine((SVGPolygon) shape);
					if (shape == null) {
						shape = polyline;
					} 
				}
			}
		}
		copyAttributes(svgPath, shape);
		shape.format(decimalPlaces);
		return shape;
	}

	private SVGShape createNarrowLine(SVGPolygon polygon) {
		SVGLine line = null;
		if (polygon != null && (polygon.size() == 4 || polygon.size() == 3)) {
			SVGLine line0 = polygon.getLineList().get(0);
			SVGLine line2 = polygon.getLineList().get(2);
			line = createNarrowLine(line0, line2);
		}
		return line;
	}

	private SVGShape createPolygonRectOrLine(SVGShape shape, SVGPolyline polyline) {
		if (polyline != null) {
			SVGPolygon polygon = (SVGPolygon) polyline.createPolygon(RECT_EPS);
			if (polygon != null) {
				SVGRect rect = polygon.createRect(RECT_EPS);
				SVGLine line = createLineFromRect(rect);
				if (line != null) {
					shape = line;
				} else if (rect != null){
					shape = rect;
				} else {
					shape = polygon;
				}
			}
		}
		return shape;
	}

	private SVGShape createRectOrAxialLine(double eps) {
		SVGShape shape;
		SVGRect rect = svgPath.createRectangle(eps);
		SVGLine line = null;
		if (rect == null) {
			line = createLineFromMLLLorMLCCLCC(svgPath);
		} else {
			line = createLineFromRect(rect); 
		}
		shape = (line != null) ? null : rect;
		return shape;
	}

	/**
	 * Replaces any paths by their converted equivalents.
	 * 
	 * @param shapeList SVG equivalents from pathList
	 * @param pathList original paths
	 */
	private static void replacePathsByShapes(List<SVGShape> shapeList, List<SVGPath> pathList) {
		if (shapeList.size() != pathList.size()){
			throw new RuntimeException("converted paths ("+shapeList.size()+") != old paths ("+pathList.size()+")");
		}
		for (int i = 0; i < pathList.size(); i++) {
			SVGPath path = pathList.get(i);
			SVGShape shape = shapeList.get(i);
			ParentNode parent = path.getParent();
			LOG.trace("Parent "+parent);
			if (parent != null) {
				LOG.trace("CONV "+shape.toXML());
				if (shape instanceof SVGPath) {
					// no need to replace as no conversion done
				} else {
					parent.replaceChild(path, shape);
				}
			}
		}
	}

	
	/** its seems many paths are drawn twice
	 * if their paths are equal, remove the later one(s)
	 */
	public void removeDuplicatePaths() {
		if (this.removeDuplicatePaths) {
			shapeListOut = removeDuplicateShapes(shapeListOut);
		}
	}
	/** modifies the paths
	 * 
	 * @param shapeListOut
	 */
	public void removeRedundantMoveCommands() {
		if (this.removeRedundantMoveCommands) {
			for (SVGPath path : pathListIn) {
				removeRedundantMoveCommands(path, MOVE_EPS);
			}
		}
	}
	/** runs components having set true/false flags if required
	 * 
	 * Not currently used ... change it
	 */
	public void runAnalyses(List<SVGPath> pathList) {
		this.pathListIn = pathList;
		this.removeDuplicatePaths();
		this.removeRedundantMoveCommands();
		this.splitAtMoveCommands();
		List<SVGShape> shapeList = this.convertPathsToShapes(pathListIn);
		this.splitPolylinesToLines(shapeList);
	}
	
	public void setPathList(List<SVGPath> pathListIn) {
		this.pathListIn = pathListIn;
	}

	/** some paths have redundant M commands which can be removed.
	 * 
	 *  e.g. M x1 y1 L x2 y2 M x2 y2 L x3 y3 can be converted to 
	 *       M x1 y1 L x2 y2 L x3 y3  
	 *  
	 * modifies the path
	 * 
	 * @param path
	 * @param eps
	 */
	private void removeRedundantMoveCommands(SVGPath path, double eps) {
		if (removeRedundantMoveCommands) {
			String d = path.getDString();
			if (d != null) {
				PathPrimitiveList newPrimitives = new PathPrimitiveList();
				PathPrimitiveList primitives = SVGPathPrimitive.parseDString(d);
				int primitiveCount = primitives.size();
				SVGPathPrimitive lastPrimitive = null;
				for (int i = 0; i < primitives.size(); i++) {
					SVGPathPrimitive currentPrimitive = primitives.get(i);
					boolean skip = false;
					if (currentPrimitive instanceof MovePrimitive) {
						if (i == primitives.size() -1) { // final primitive
							skip = true;
						} else if (lastPrimitive != null) {
							// move is to end of last primitive
							Real2 lastLastCoord = lastPrimitive.getLastCoord();
							Real2 currentFirstCoord = currentPrimitive.getFirstCoord();
							skip = (lastLastCoord != null) && lastLastCoord.isEqualTo(currentFirstCoord, eps);
						}
						if (!skip && lastPrimitive != null) {
							SVGPathPrimitive nextPrimitive = primitives.get(i+1);
							Real2 currentLastCoord = currentPrimitive.getLastCoord();
							Real2 nextFirstCoord = nextPrimitive.getFirstCoord();
							skip = (nextFirstCoord != null) && currentLastCoord.isEqualTo(nextFirstCoord, eps);
						}
					}
					if (!skip) {
						newPrimitives.add(currentPrimitive);
					} else {
						LOG.trace("skipped "+lastPrimitive+ "== "+currentPrimitive);
					}
					lastPrimitive = currentPrimitive;
				}
				createNewPathIfModified(path, d, newPrimitives, primitiveCount);
			}
		}
	}

	private void createNewPathIfModified(SVGPath path, String d,
			PathPrimitiveList newPrimitives, int primitiveCount) {
		int newPrimitiveCount = newPrimitives.size();
		if (newPrimitiveCount != primitiveCount) {
			LOG.trace("Deleted "+(primitiveCount - newPrimitiveCount)+" redundant moves");
			String newD = SVGPath.constructDString(newPrimitives);
			SVGPath newPath = new SVGPath(newD);
			XMLUtil.copyAttributesFromTo(path,  newPath);
			newPath.setDString(newD);
			path.getParent().replaceChild(path,  newPath);
			LOG.trace(">>>"+d+"\n>>>"+newD);
		}
	}
	
	public void splitAtMoveCommands() {
		if (this.splitAtMoveCommands ) {
			 for (SVGPath path : pathListIn) {
				 splitAtMoveCommandsX();
			 }
		}
	}

	private List<SVGPath> splitAtMoveCommandsX() {
		 splitPathList = new ArrayList<SVGPath>();
		 String d = svgPath.getDString();
		 List<String> newDStringList = splitAtMoveCommandsAndCreateNewDStrings(d);
		 if (newDStringList.size() == 1) {
			 splitPathList.add(svgPath);
		 } else {
			 ParentNode parent = svgPath.getParent();
			 int index = parent.indexOf(svgPath);
			 for (String newDString : newDStringList) {
				 SVGPath newPath = new SVGPath();
				 XMLUtil.copyAttributesFromTo(svgPath, newPath);
				 newPath.setDString(newDString);
				 parent.insertChild(newPath, ++index);
				 splitPathList.add(newPath);
			 }
			 svgPath.detach();
		 }
		 return splitPathList;
	}
	
	private List<String> splitAtMoveCommandsAndCreateNewDStrings(String d) {
		List<String> strings = new ArrayList<String>();
		int current = -1;
		while (true) {
			int i = d.indexOf(SVGPathPrimitive.ABS_MOVE, current+1);
			if (i == -1 && current >= 0) {
				strings.add(d.substring(current));
				break;
			}
			if (i > current+1) {
				strings.add(d.substring(current, i));
			}
			current = i;
		}
		return strings;
	}
	
	/** iterates over allShapes and extracts all splitLines.
	 * 
	 * @param shapeList
	 * @return
	 */
	public List<SVGLine> splitPolylinesToLines(List<SVGShape> shapeList) {
		LOG.trace("minLines: "+minLinesInPolyline);
		List<SVGLine> totalSplitLineList = new ArrayList<SVGLine>();
		for (SVGShape shape : shapeList) {
			if (shape instanceof SVGPolyline) {
				SVGPoly polyline = (SVGPoly) shape;
				List<SVGLine> lines = polyline.createLineList();
				if (lines.size() < minLinesInPolyline) {
					annotateLinesAndAddToParentAndList(totalSplitLineList, polyline, lines);
				} else {
					LOG.trace("not split: "+lines.size());
				}
			}
		}
		return totalSplitLineList;
	}

	private void annotateLinesAndAddToParentAndList (
			List<SVGLine> totalSplitLineList, SVGPoly polyline, List<SVGLine> linesToAdd) {
		ParentNode parent = polyline.getParent();
		for (int i = 0; i < linesToAdd.size(); i++) {
			SVGLine line = linesToAdd.get(i);
			parent.appendChild(line);
			line.setId(line.getId()+"."+i);
			totalSplitLineList.add(line);
		}
		polyline.detach();
		LOG.trace("split: "+linesToAdd.size());
	}

	/** with help from
	http://stackoverflow.com/questions/4958161/determine-the-centre-center-of-a-circle-using-multiple-points
		 * @param p1
		 * @param p2
		 * @param p3
		 * @return
		 */
	public static SVGCircle findCircleFrom3Points(Real2 p1, Real2 p2, Real2 p3, Double eps) {
		SVGCircle circle = null;
		if (p1 != null && p2 != null && p3 != null) {
			Double d2 = p2.x * p2.x + p2.y * p2.y;
			Double bc = (p1.x * p1.x + p1.y * p1.y - d2) / 2;
			Double cd = (d2 - p3.x * p3.x - p3.y * p3.y) / 2;
			Double det = (p1.x - p2.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p2.y);
			if (Math.abs(det) > eps) {
				Real2 center = new Real2(
						(bc * (p2.y - p3.y) - cd * (p1.y - p2.y)) / det,
						((p1.x - p2.x) * cd - (p2.x - p3.x) * bc) / det);
				Double rad = center.getDistance(p1);
				circle = new SVGCircle(center, rad);
			}
		}
		return circle;
	}


	public static SVGCircle findCircleFromPoints(Real2Array r2a, double eps) {
		SVGCircle circle = null;
		if (r2a == null || r2a.size() < 3) {
			//
		} else if (r2a.size() == 3) {
			circle = findCircleFrom3Points(r2a.get(0), r2a.get(1), r2a.get(2), eps);
		} else {
			RealArray x2y2Array = new RealArray();
			RealArray xArray = new RealArray();
			RealArray yArray = new RealArray();
			for (int i = 0; i < r2a.size(); i++) {
				Real2 point = r2a.get(i);
				double x = point.x;
				double y = point.y;
				x2y2Array.addElement(x * x + y * y);
				xArray.addElement(x);
				yArray.addElement(y);
			}
			Real2Range bbox =r2a.getRange2();
			// check if scatter in both directions
			if (bbox.getXRange().getRange() > eps && bbox.getYRange().getRange() > eps) {
				// don't lnow the distribution and can't afford to find all triplets
				// so find the extreme points
				Real2 minXPoint = r2a.getPointWithMinimumX();
				Real2 maxXPoint = r2a.getPointWithMaximumX();
				Real2 minYPoint = r2a.getPointWithMinimumY();
				Real2 maxYPoint = r2a.getPointWithMaximumY();
			}
		}
		return circle;
	}


	public static List<SVGShape> removeDuplicateShapes(List<SVGShape> shapeList) {
		if (shapeList != null) {
			Set<String> dStringSet = new HashSet<String>();
			int count = 0;
			List<SVGShape> newPathList = new ArrayList<SVGShape>();
			for (SVGShape shape : shapeList) {
				String dString = shape.getGeometricHash();
				if (dStringSet.contains(dString)) {
					LOG.trace("detached a duplicate path "+dString);
					shape.detach();
					count++;
				} else {
					dStringSet.add(dString);
					newPathList.add(shape);
				}
			}
			if (count > 0) {
				LOG.trace("detached "+count+" duplicate paths");
				shapeList = newPathList;
			}
		}
		return shapeList;
	}


	public static void copyAttributes(SVGPath path, SVGElement result) {
		for (String attName : new String[]{
				StyleBundle.FILL, 
				StyleBundle.OPACITY, 
				StyleBundle.STROKE, 
				StyleBundle.STROKE_WIDTH, 
				}) {
			String val = path.getAttributeValue(attName);
			if (val != null) {
				result.addAttribute(new Attribute(attName, val));
			}
		}
		String zvalue = SVGUtil.getSVGXAttribute(path, "z");
		if (zvalue != null) {
			SVGUtil.setSVGXAttribute(result, "z", zvalue);
		}
	}
	
	public void setRemoveDuplicatePaths(boolean removeDuplicatePaths) {
		this.removeDuplicatePaths = removeDuplicatePaths;
	}
	public void setRemoveRedundantMoveCommands(boolean removeRedundantMoveCommands) {
		this.removeRedundantMoveCommands = removeRedundantMoveCommands;
	}
	public void setMinLinesInPolyline(Integer minLinesInPolyline) {
		this.minLinesInPolyline = minLinesInPolyline;
	}
	public void setSplitAtMoveCommands(boolean splitAtMoveCommands) {
		this.splitAtMoveCommands = splitAtMoveCommands;
	}
	public boolean isRemoveDuplicatePaths() {
		return removeDuplicatePaths;
	}
	public boolean isRemoveRedundantMoveCommands() {
		return removeRedundantMoveCommands;
	}
	public Integer getMinLinesInPolyline() {
		return minLinesInPolyline;
	}
	public boolean isSplitAtMoveCommands() {
		return splitAtMoveCommands;
	}
	public Integer getDecimalPlaces() {
		return decimalPlaces;
	}
	public void setDecimalPlaces(Integer decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	/** converts paths in svgElement and replaces originals.
	 * 
	 * @param gChunk
	 */
	public void convertPathsToShapes(SVGElement svgElement) {
		List<SVGPath> pathList = SVGPath.extractPaths(svgElement);
		List<SVGShape> shapeList = convertPathsToShapes(pathList);
		Path2ShapeConverter.replacePathsByShapes(shapeList, pathList);
	}

	public List<SVGShape> getShapeListOut() {
		return shapeListOut;
	}

	public SVGLine createNarrowLine() {
		maxPathWidth = 1.0;
		if (svgPath == null) return null;
		SVGLine line = null;
		String signature = svgPath.getSignature();
		if (MLLL.equals(signature) || MLLLL.equals(signature)) {
			PathPrimitiveList primList = svgPath.ensurePrimitives();
			SVGLine line0 = primList.getLine(1);
			SVGLine line1 = primList.getLine(3);
			line = createNarrowLine(line0, line1);
		}
		return line;
	}

	private SVGLine createNarrowLine(SVGLine line0, SVGLine line1) {
		SVGLine line = null;
		if (line0.isParallelOrAntiParallelTo(line1, maxAngle)) {
			double dist = line0.calculateUnsignedDistanceBetweenLines(line1, maxAngle);
			if (dist < maxPathWidth) {
				Real2 end0Parallel = line0.getXY(0).getMidPoint(line1.getXY(0));
				Real2 end1Parallel = line0.getXY(1).getMidPoint(line1.getXY(1));
				Real2 end0AntiParallel = line0.getXY(0).getMidPoint(line1.getXY(1));
				Real2 end1AntiParallel = line0.getXY(1).getMidPoint(line1.getXY(0));
				SVGLine lineParallel = new SVGLine(end0Parallel, end1Parallel);
				SVGLine lineAntiParallel = new SVGLine(end0AntiParallel, end1AntiParallel);
				line = (lineParallel.getLength() > lineAntiParallel.getLength() ? lineParallel : lineAntiParallel);
				LOG.trace("line: "+line);
			}
		}
		return line;
	}
	
	public SVGPath createNarrowQuadrant() {
		SVGPath newPath = null;
		String signature = svgPath.getSignature();
		if (MCLC.equals(signature)) {
			PathPrimitiveList primList = svgPath.ensurePrimitives();
			Arc quadrant0 = primList.getQuadrant(1, ANGLE_EPS);
			Arc quadrant2 = primList.getQuadrant(3, ANGLE_EPS);
		}
		return newPath;
	}

	public SVGCircle convertToCircle(SVGPolygon polygon) {
		Real2Range bbox = polygon.getBoundingBox();
		SVGCircle circle = null;
		double eps = 10. * RECT_EPS; // why not?
		if (Math.abs(bbox.getXRange().getRange() - bbox.getYRange().getRange()) < eps) {
			Real2 centre = bbox.getCentroid();
			RealArray radArray = new RealArray();
			for (Real2 point : polygon.getReal2Array()) {
				radArray.addElement(centre.getDistance(point));
			}
			circle = new SVGCircle();
			circle.copyAttributesFrom(polygon);
			circle.setRad(radArray.getMean());
			circle.setCXY(centre);
		}
		return circle;
	}
	
	public SVGLine createLineFromRect(SVGRect rect) {
		SVGLine line = null;
		if (rect != null) {
			Real2 origin = rect.getXY();
			double width = rect.getWidth();
			double height = rect.getHeight();
			if (width < minRectThickness) {
				line = new SVGLine(origin.plus(new Real2(width / 2, 0.0)), origin.plus(new Real2(width / 2, height)));
			} else if (height < minRectThickness) {
				line = new SVGLine(origin.plus(new Real2(0.0, height / 2)), origin.plus(new Real2(width, height / 2)));
			}
		}
		return line;
	}
	

}
