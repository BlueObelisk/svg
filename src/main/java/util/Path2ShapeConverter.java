package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.ParentNode;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.graphics.svg.MovePrimitive;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGPathPrimitive;
import org.xmlcml.graphics.svg.SVGPolyline;
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
	
	private static final double _CIRCLE_EPS = 0.7;
	private static final double MOVE_EPS = 0.001;
	private static final double RECT_EPS = 0.01;
	private static final String SVG = "svg";
	private static final Integer DEFAULT_DECIMAL_PLACES = 3;

	private Integer decimalPlaces = DEFAULT_DECIMAL_PLACES;
	private Integer minLinesInPolyline = 8;
	private boolean removeDuplicatePaths = true;
	private boolean removeRedundantMoveCommands = true;
	private boolean splitAtMoveCommands = true;
	
	private List<SVGPath> pathListIn;
	private List<SVGShape> shapeListOut;
	private List<SVGPath> splitPathList;

	public Path2ShapeConverter() {
	}
	
	public Path2ShapeConverter(List<SVGPath> pathListIn) {
		this.setPathList(pathListIn);
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
			SVGShape  shape = this.convertPathToShape((SVGPath)path);
			shape.setId(shape.getClass().getSimpleName().toLowerCase().substring(SVG.length())+"."+id);
			shapeListOut.add(shape);
			id++;
		}
		return shapeListOut;
	}

	/** create best guess at higher SVGElement
	 * try rect, circle, line, polygon, polyline, else original path
	 * 
	 * @param shapeListOut
	 */
	public SVGShape convertPathToShape(SVGPath path) {
		SVGShape newSvg = null;
		newSvg = path.createRectangle(RECT_EPS);
		if (newSvg == null) {
			newSvg = path.createCircle(_CIRCLE_EPS);
		}
		if (newSvg == null) {
			SVGPolyline polyline = path.createPolyline();
			// not a polyline, return unchanged path
			if (polyline == null) {
				newSvg = new SVGPath(path);
			} else {
				// SVG is a polyline, try the variants
					// is it a line?
				newSvg = polyline.createSingleLine();
				if (newSvg == null) {
					// or a polygon?
					newSvg = polyline.createPolygon(RECT_EPS);
				}
				// no, reset to polyline
				if (newSvg == null) {
					newSvg = polyline;
				}
			}
		}
		copyAttributes(path, newSvg);
		newSvg.format(decimalPlaces);
		return newSvg;
	}
	
//	/** replaces old paths with new SVGElements where they have been converted
//	 * 
//	 */
//	public void replacePathsWithShapes() {
//		convertPathsToShapes();
//		List<SVGShape> shapeList = shapeContainer.getShapeList();
//		List<SVGPath> pathList = getShapeList();
//		ShapeAnalyzer.replacePathsByShapes(shapeList, pathList);
//	}
	
//	public static void replacePathsByShapes(List<SVGPath> pathList) {
//		List<SVGShape> shapeList = Path2ShapeConverter.convertPathsToShapes(pathList);
//		ShapeAnalyzer.replacePathsByShapes(shapeList, pathList);
//	}

//	public void convertPathsToShapes() {
////		ensurePath2ShapeConverter();
//		List<SVGPath> pathList = getPathList();
//		List<SVGShape> shapeList = Path2ShapeConverter.convertPathsToShapes(pathList);
//		shapeContainer.setShapeList(shapeList);
//	}

//	private void ensurePath2ShapeConverter() {
//		if (path2SVGInterpreter == null) {
//			this.path2SVGInterpreter = new Path2ShapeConverter(shapeContainer.getShapeList());
//		}
//	}

//	public Path2ShapeConverter getPath2SVGInterpreter() {
//		return path2SVGInterpreter;
//	}


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
				List<SVGPathPrimitive> newPrimitives = new ArrayList<SVGPathPrimitive>();
				List<SVGPathPrimitive> primitives = SVGPathPrimitive.parseDString(d);
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
			List<SVGPathPrimitive> newPrimitives, int primitiveCount) {
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
				 splitAtMoveCommands(path);
			 }
		}
	}

	private List<SVGPath> splitAtMoveCommands(SVGPath path) {
		 splitPathList = new ArrayList<SVGPath>();
		 String d = path.getDString();
		 List<String> newDStringList = splitAtMoveCommandsAndCreateNewDStrings(d);
		 if (newDStringList.size() == 1) {
			 splitPathList.add(path);
		 } else {
			 ParentNode parent = path.getParent();
			 int index = parent.indexOf(path);
			 for (String newDString : newDStringList) {
				 SVGPath newPath = new SVGPath();
				 XMLUtil.copyAttributesFromTo(path, newPath);
				 newPath.setDString(newDString);
				 parent.insertChild(newPath, ++index);
				 splitPathList.add(newPath);
			 }
			 path.detach();
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
				SVGPolyline polyline = (SVGPolyline) shape;
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

	private void annotateLinesAndAddToParentAndList(
			List<SVGLine> totalSplitLineList, SVGPolyline polyline, List<SVGLine> linesToAdd) {
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



}
