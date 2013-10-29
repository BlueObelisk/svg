package org.xmlcml.graphics.svg.builder;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGPolygon;
import org.xmlcml.graphics.svg.SVGPolyline;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGSVG;
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
	

	private static final double DEFAULT_BOND_LENGTH_SCALE = 0.1;

	protected SVGElement svgRoot;
	private List<SVGElement> highLevelPrimitives;
	
	protected SVGPrimitives explicitContainer;
	protected SVGPrimitives implicitContainer;
	protected HigherPrimitives higherPrimitives;
	
	protected List<SVGPath> currentPathList;

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
		if (explicitContainer == null) {
			explicitContainer = new SVGPrimitives();
			List<SVGShape> shapeList = SVGShape.extractSelfAndDescendantShapes(svgRoot);
			explicitContainer.addShapes(shapeList);
			currentPathList = explicitContainer.getPathList();
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
	
	public void createImplicitShapesFromPaths() {
		if (implicitContainer == null) {
			createExplicitShapeAndPathLists();
			implicitContainer = new SVGPrimitives();
			List<SVGShape> shapeList = SVGShape.extractSelfAndDescendantShapes(svgRoot);
			implicitContainer.addShapes(shapeList);
			Path2ShapeConverter path2ShapeConverter = new Path2ShapeConverter();
			for (int i = currentPathList.size() - 1; i >= 0; i--) {
				SVGPath path = currentPathList.get(i);
				SVGShape shape = path2ShapeConverter.convertPathToShape(path);
				if (shape != null) {
					addId(i, shape, path);
					implicitContainer.add(shape);
					currentPathList.remove(i);
				}
			}
		}
	}

//	public List<SVGPath> createExplicitPathList() {
//		if (explicitPathList == null) {
//			if (svgRoot != null) {
//				explicitPathList = SVGPath.extractPaths(svgRoot);
//				currentPathList = new ArrayList<SVGPath>(explicitPathList);
//			}
//		}
//		return explicitPathList;
//	}
	

	public List<SVGLine> createExplicitAndImplicitLines() {
		createImplicitShapesFromPaths();
		ensureHigherPrimitives();
		higherPrimitives.addSingleLines(implicitContainer.getLineList());
		higherPrimitives.addSingleLines(explicitContainer.getLineList());
		return higherPrimitives.getSingleLineList();
	}

	private void ensureHigherPrimitives() {
		if (higherPrimitives == null) {
			higherPrimitives = new HigherPrimitives();
		}
	}

	public List<SVGText> createExplicitTextList() {
		explicitContainer.addTexts(SVGText.extractSelfAndDescendantTexts(svgRoot));
		return explicitContainer.getTextList();
	}

	private void ensureIds(List<? extends SVGElement> elementList) {
		for (int i = 0; i < elementList.size(); i++){
			SVGElement element = elementList.get(i);
			addId(i, element, null);
		}
	}
	
	private List<Junction> mergeJunctions() {
		List<Junction> rawJunctionList = createRawJunctionList();
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

	public List<Joinable> createJoinableList() {
		List<Joinable> joinableList = higherPrimitives.getJoinableList();
		if (joinableList == null) {
			createExplicitAndImplicitLines();
			joinableList = JoinManager.makeJoinableList(higherPrimitives.getSingleLineList());
		}
		return joinableList;
	}

	public List<TramLine> createTramLineListAndRemoveUsedLines() {
		List<TramLine> tramLineList = higherPrimitives.getTramLineList();
		if (tramLineList == null) {
			createExplicitAndImplicitLines();
			TramLineManager tramLineManager = new TramLineManager();
			tramLineList = tramLineManager.createTramLineList(higherPrimitives.getSingleLineList());
			tramLineManager.removeUsedTramLinePrimitives(higherPrimitives.getSingleLineList());
		}
		return tramLineList;
	}

	public List<Junction> createMergedJunctions() {
		List<Junction> junctionList = higherPrimitives.getMergedJunctionList();
		if (junctionList == null) {
			createTramLineListAndRemoveUsedLines();
			List<Joinable> joinableList = JoinManager.makeJoinableList(higherPrimitives.getSingleLineList());
			joinableList.addAll(higherPrimitives.getTramLineList());
			createExplicitTextList();
			for (SVGText svgText : explicitContainer.getTextList()) {
				joinableList.add(new JoinableText(svgText));
			}
			junctionList = this.mergeJunctions();
		}
		return junctionList;
		
	}

	public List<Junction> createRawJunctionList() {
		List<Junction> rawJunctionList = higherPrimitives.getRawJunctionList();
		if (rawJunctionList == null) {
			List<Joinable> joinableList = createJoinableList();
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

	public List<SVGLine> getImplicitLineList() {
		return (implicitContainer == null) ? null : implicitContainer.getLineList();
	}

	public List<SVGLine> getExplicitLineList() {
		return (explicitContainer == null) ? null : explicitContainer.getLineList();
	}

	public List<Joinable> getJoinableList() {
		return (higherPrimitives == null) ? null : higherPrimitives.getJoinableList();
	}

	public List<TramLine> getTramLineList() {
		return (higherPrimitives == null) ? null : higherPrimitives.getTramLineList();
	}

	public List<SVGLine> getSingleLineList() {
		return higherPrimitives == null ? null : higherPrimitives.getSingleLineList();
	}

	public List<SVGPath> getExplicitPathList() {
		return explicitContainer == null ? null : explicitContainer.getPathList();
	}

	public List<SVGShape> getCurrentPathList() {
		return implicitContainer == null ? null : implicitContainer.getShapeList();
	}

	protected void ensureImplicitContainer() {
		if (implicitContainer == null) {
			implicitContainer = new SVGPrimitives();
		}
	}

	protected void ensureExplicitContainer() {
		if (explicitContainer == null) {
			explicitContainer = new SVGPrimitives();
		}
	}

	public List<SVGText> getRawTextList() {
		return explicitContainer  == null ? null : explicitContainer.getTextList();
	}

	public void extractPlotComponents() {
		ensureImplicitContainer();
		ensureExplicitContainer();
		List<SVGPath> pathList = SVGPath.extractPaths(getSVGRoot());
		Path2ShapeConverter path2ShapeConverter = new Path2ShapeConverter();
		explicitContainer.addShapes(path2ShapeConverter.convertPathsToShapes(pathList));
	}

	public List<SVGShape> getCurrentShapeList() {
		return implicitContainer == null ? null : implicitContainer.getShapeList();
	}

	
}
