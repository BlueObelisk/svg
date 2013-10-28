package org.xmlcml.graphics.svg.builder;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGShape;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.path.Path2ShapeConverter;

/**
 * Builds higher-level primitives from SVGPaths, SVGLines, etc. to create SVG objects 
 * such as TramLine and (later) Arrow.
 * 
 * <p>GeometryBuilder's main function is to:
 * <ul>
 * <li>Read a raw SVG object and make lists of SVGPath and SVGText (and possibly higher levels ones
 * if present.)</li>
 * <li>turn SVGPaths into SVGLines , etc.</li>
 * <li>identify Junctions (line-line, line-text, and probably more)</li>
 * <li>join lines where they meet into higher level objects (TramLines, SVGRect, crosses, arrows, etc.)</li>
 * <li>create topologies (e.g. connection of lines and Junctions)</li>
 * </ul>
 * 
 * GeometryBuilder uses the services of the org.xmlcml.graphics.svg.path package and may later use
 * org.xmlcml.graphics.svg.symbol.
 * </p>
 * 
 * <p>Input may either be explicit SVG primitives (e.g. <svg:rect>, <svg:line>) or 
 * implicit (<svg:path> which can be interpreted to the above. The input may be either or
 * both - we can't control it. The implicit get converted to explicit and then merged with the 
 * explicit:
 * <pre>
 *    paths-> implicitLineList + rawLinelist -> explicitLineList 
 * </pre>
 * </p>
 * 
 * <h3>Strategy</h3>
 * <p>createHigherLevelPrimitives() carries out the complete chain from svgRoot to the final
 * primitives. Each step tests to see whether the result of the previous is null.
 * If so its creates a non-null list and fills it if possible. </p>
 * 
 * UPDATE: 2013-10-23 Renamed to "SimpleGeometryManager" as it doesn't deal with Words (which
 * require TextStructurer.) it's possible the whole higherlevel primitive stuff should be removed to another
 * project.
 * 
 * @author pm286
 *
 */
public class SimpleBuilder {

	private static final String TEXT = "text";

	private final static Logger LOG = Logger.getLogger(SimpleBuilder.class);
	
	public static final Angle DEFAULT_MAX_ANGLE = new Angle(0.12, Units.RADIANS);
	public static final Double DEFAULT_MAX_WIDTH = 2.0;
	private static final Double DEFAULT_MIN_RECT_THICKNESS = 0.99;

	private static final double DEFAULT_BOND_LENGTH_SCALE = 0.1;

	protected SVGElement svgRoot;
	private List<SVGElement> highLevelPrimitives;
	
	private List<SVGPath> explicitPathList;
	private List<SVGShape> explicitShapeList; // does not include Path or Line
	private List<SVGLine> explicitLineList;
	private List<SVGText> explicitTextList;
	
	private List<SVGLine> rawLineList;
	private List<SVGShape> implicitShapeList; // does not include Path or Line 
	private List<SVGLine> implicitLineList;
	private List<Junction> rawJunctionList;
	
	private List<Junction> mergedJunctionList;
	private List<SVGPath> currentPathList;
	private List<SVGShape> currentShapeList;
	private List<SVGLine> singleLineList;
	private List<TramLine> tramLineList;
	private List<Joinable> joinableList;
	
	private double maxWidth = DEFAULT_MAX_WIDTH;
	private Angle maxAngle = DEFAULT_MAX_ANGLE;
	private Double minRectThickness = DEFAULT_MIN_RECT_THICKNESS;

	public SimpleBuilder() {
	}

	public SimpleBuilder(SVGElement svgRoot) {
		this.setSvgRoot(svgRoot);
	}

	public void setSvgRoot(SVGElement svgRoot) {
		this.svgRoot = svgRoot;
	}

	/** complete processing chain for lowlevel SVG into highlevel SVG.
	 * 
	 */
	public void createHigherLevelPrimitives() {
		
	}

	/**
	 * turn SVGPaths into higher level primitives such as SVGLine.
	 * Create collections of primitives.
	 * Join them where possible.
	 * 
	 * @return
	 */
	public List<SVGElement> createHighLevelPrimitivesFromPaths() {
		if (highLevelPrimitives == null) {
			highLevelPrimitives = new ArrayList<SVGElement>();
			createExplicitShapeAndPathLists();
			createImplicitShapesFromPaths();
			createExplicitTextList();
		}
		return highLevelPrimitives;
	}
	
	private void createExplicitShapeAndPathLists() {
		if (explicitShapeList == null) {
			explicitShapeList = new ArrayList<SVGShape>();
			explicitLineList = new ArrayList<SVGLine>();
			explicitPathList = new ArrayList<SVGPath>();
			if (svgRoot != null) {
				List<SVGShape> shapeList = SVGShape.extractSelfAndDescendantShapes(svgRoot);
				for (SVGShape shape : shapeList) {
					if (shape instanceof SVGPath) {
						explicitPathList.add((SVGPath)shape);
					} else if (shape instanceof SVGRect) {
						addAsLineOrRect(shape, explicitShapeList, explicitLineList);
					} else if (shape instanceof SVGLine) {
						explicitLineList.add((SVGLine)shape);
					} else {
						explicitShapeList.add(shape);
					}
				}
				currentPathList = new ArrayList<SVGPath>(explicitPathList);
				currentShapeList = new ArrayList<SVGShape>(explicitShapeList);
			}
		}
	}
	
	/**
	 * Try to create highLevel primitives from paths.
	 * 
	 * <p>Uses Path2ShapeConverter.
	 * Where successful the path is removed from currentPathlist
	 * and added to implicitShapeList.
	 * </p>
	 * 
	 * @return List of newly created SVGShapes
	 */
	
	public List<SVGShape> createImplicitShapesFromPaths() {
		if (implicitShapeList == null) {
			createExplicitShapeAndPathLists();
			implicitShapeList = new ArrayList<SVGShape>();
			implicitLineList = new ArrayList<SVGLine>();
			Path2ShapeConverter path2ShapeConverter = new Path2ShapeConverter();
			for (int i = currentPathList.size() - 1; i >= 0; i--) {
				SVGPath path = currentPathList.get(i);
				SVGShape shape = path2ShapeConverter.convertPathToShape(path);
				if (shape != null) {
					addId(i, shape, path);
					if (shape instanceof SVGLine) {
						implicitLineList.add((SVGLine)shape);
					} else if (shape instanceof SVGRect) {
						addAsLineOrRect(shape, implicitShapeList, implicitLineList);
					} else {
						implicitShapeList.add(path);
					}
					currentPathList.remove(i);
				}
			}
		}
		return implicitShapeList;
	}

	private void addAsLineOrRect(SVGShape shape, List<SVGShape> shapeList, List<SVGLine> lineList) {
		SVGLine line = makeLineFromRect(shape);
		if (line == null) {
			shapeList.add(shape);
		} else {
			lineList.add(line);
		}
	}

	private SVGLine makeLineFromRect(SVGShape shape) {
		SVGRect rect = (SVGRect) shape;
		Real2 origin = rect.getXY();
		double width = rect.getWidth();
		double height = rect.getHeight();
		SVGLine line = null;
		if (width < minRectThickness ) {
			line = new SVGLine(origin, origin.plus(new Real2(0.0, height)));
		} else if (height < minRectThickness ) {
			line = new SVGLine(origin, origin.plus(new Real2(width, 0.0)));
		}
		return line;
	}
	
	public List<SVGPath> createExplicitPathList() {
		if (explicitPathList == null) {
			if (svgRoot != null) {
				explicitPathList = SVGPath.extractPaths(svgRoot);
				currentPathList = new ArrayList<SVGPath>(explicitPathList);
			}
		}
		return explicitPathList;
	}
	

	public List<SVGLine> createExplicitAndImplicitLines() {
		createImplicitShapesFromPaths();
		singleLineList = new ArrayList<SVGLine>();
		singleLineList.addAll(implicitLineList);
		singleLineList.addAll(explicitLineList);
		return singleLineList;
	}

	public List<SVGText> createExplicitTextList() {
		if (explicitTextList == null) {
			explicitTextList = SVGText.extractSelfAndDescendantTexts(svgRoot);
			ensureIds(explicitTextList);
		}
		return explicitTextList;
	}

	private void ensureIds(List<? extends SVGElement> elementList) {
		for (int i = 0; i < elementList.size(); i++){
			SVGElement element = elementList.get(i);
			addId(i, element, null);
		}
	}
	
	private List<Junction> mergeJunctions() {
		createRawJunctionList();
		for (int i = rawJunctionList.size() - 1; i > 0; i--) {
			Junction labile = rawJunctionList.get(i);
			for (int j = 0; j < i; j++) {
				Junction fixed = rawJunctionList.get(j);
				if (fixed.containsCommonPoints(labile)) {
					labile.transferDetailsTo(fixed);
					rawJunctionList.remove(i);
					break;
				}
			}
		}
		return rawJunctionList;
	}

	public void createJoinableList() {
		if (joinableList == null) {
			createExplicitAndImplicitLines();
			joinableList = JoinManager.makeJoinableList(singleLineList);
		}
	}

	public List<TramLine> createTramLineListAndRemoveUsedLines() {
		if (tramLineList == null) {
			createExplicitAndImplicitLines();
			TramLineManager tramLineManager = new TramLineManager();
			tramLineList = tramLineManager.createTramLineList(singleLineList);
			tramLineManager.removeUsedTramLinePrimitives(singleLineList);
		}
		return tramLineList;
	}

	public List<Junction> createMergedJunctions() {
		if (mergedJunctionList == null) {
			createTramLineListAndRemoveUsedLines();
			joinableList = JoinManager.makeJoinableList(singleLineList);
			joinableList.addAll(tramLineList);
			createExplicitTextList();
			for (SVGText svgText : explicitTextList) {
				joinableList.add(new JoinableText(svgText));
			}
			mergedJunctionList = this.mergeJunctions();
		}
		return mergedJunctionList;
		
	}

	public List<Junction> createRawJunctionList() {
		if (rawJunctionList == null) {
			createJoinableList();
			rawJunctionList = new ArrayList<Junction>();
			for (int i = 0; i < joinableList.size() - 1; i++) {
				Joinable joinablei = joinableList.get(i);
				for (int j = i + 1; j < joinableList.size(); j++) {
					Joinable joinablej = joinableList.get(j);
					JoinPoint commonPoint = joinablei.getIntersectionPoint(joinablej);
					if (commonPoint != null) {
						Junction junction = new Junction(joinablei, joinablej, commonPoint);
						rawJunctionList.add(junction);
						String junctAttVal = "junct"+"."+rawJunctionList.size();
						junction.addAttribute(new Attribute(SVGElement.ID, junctAttVal));
						if (junction.getCoordinates() == null && commonPoint.getPoint() != null) {
							junction.setCoordinates(commonPoint.getPoint());
						}
						LOG.trace("junct: "+junction.getId()+" coords "+junction.getCoordinates()+" "+commonPoint.getPoint());
					}
				}
			}
		}
		return rawJunctionList;
	}

	// =========== Getters, Setters and Ensurers ============================

	private void ________________________________________________() {}
	
	public List<SVGLine> getImplicitLineList() {
		return implicitLineList;
	}

	private void ensureImplicitLineList() {
		if (implicitLineList == null) {
			implicitLineList = new ArrayList<SVGLine>();
		}
	}

	public List<SVGLine> getExplicitLineList() {
		return explicitLineList;
	}

	private void ensureExplicitLineList() {
		if (explicitLineList == null) {
			explicitLineList = new ArrayList<SVGLine>();
		}
	}

	public List<SVGPath> getExplicitPathList() {
		return explicitPathList;
	}

	public List<SVGPath> getCurrentPathList() {
		return currentPathList;
	}

	private void ensureExplicitPathList() {
		if (explicitPathList == null) {
			explicitPathList = new ArrayList<SVGPath>();
		}
	}

	public List<Joinable> getJoinableList() {
		return joinableList;
	}

	private void ensureJoinableList() {
		if (joinableList == null) {
			joinableList = new ArrayList<Joinable>();
		}
	}

	public List<Junction> getMergedJunctionList() {
		ensureMergedJunctionList();
		return mergedJunctionList;
	}

	private void ensureMergedJunctionList() {
		if (mergedJunctionList == null) {
			createMergedJunctions();
		}
	}

	public List<Junction> getRawJunctionList() {
		return rawJunctionList;
	}

	void ensureRawJunctionList() {
		if (rawJunctionList == null) {
			createRawJunctionList();
		}
	}

	public List<SVGLine> getRawLineList() {
		return rawLineList;
	}

	private void ensureRawLineList() {
		if (rawLineList == null) {
			rawLineList = new ArrayList<SVGLine>();
		}
	}

	public List<SVGShape> getCurrentShapeList() {
		return currentShapeList;
	}
	
	public List<SVGLine> getSingleLineList() {
		return singleLineList;
	}

	private void ensureSingleLineList() {
		if (singleLineList == null) {
			singleLineList = new ArrayList<SVGLine>();
		}
	}
	public List<SVGText> getRawTextList() {
		return explicitTextList;
	}

	private void ensureRawTextList() {
		if (explicitTextList == null) {
			explicitTextList = new ArrayList<SVGText>();
		}
	}
	
	public List<TramLine> getTramLineList() {
		return tramLineList;
	}

	private void ensureTramLineList() {
		if (this.tramLineList == null) {
			tramLineList = new ArrayList<TramLine>();
		}
	}

	// =========================================================================
	
	private void _______________________________________________() {}
	
	public void setMaxWidth(double maxWidth) {
		this.maxWidth = maxWidth;
	}

	public void setMaxAngle(Angle maxAngle) {
		this.maxAngle = maxAngle;
	}
	
	private void addId(int i, SVGElement element, SVGElement reference) {
		String id = (reference == null) ? null : reference.getId();
		if (id == null) {
			id = element.getLocalName()+"."+i;
			element.setId(id);
		}
	}

	public SVGElement getSVGRoot() {
		return svgRoot;
	}


	
}
