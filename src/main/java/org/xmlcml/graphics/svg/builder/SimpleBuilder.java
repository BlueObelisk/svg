package org.xmlcml.graphics.svg.builder;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
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
 * <p>SimpleBuilder's main function is to:
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
 * If so it creates a non-null list and fills it if possible. </p>
 * 
 * UPDATE: 2013-10-23 Renamed to "SimpleGeometryManager" as it doesn't deal with Words (which
 * require TextStructurer). It's possible the whole higher-level primitive stuff should be removed to another
 * project.
 * 
 * @author pm286
 *
 */
public class SimpleBuilder {

	private static final String TEXT = "text";

	private final static Logger LOG = Logger.getLogger(SimpleBuilder.class);
	
	protected SVGElement svgRoot;
	private List<SVGElement> highLevelPrimitives;
	
	protected SVGPrimitives derivedPrimitives;
	protected SVGPrimitives rawPrimitives;
	protected HigherPrimitives higherPrimitives;
	
//	protected List<SVGPath> currentPathList;

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
			createRawShapeAndPathLists();
			createDerivedShapesFromPaths();
			abstractPolygons();
			createRawTextList();
		}
		return highLevelPrimitives;
	}
	
	private void createRawShapeAndPathLists() {
		if (rawPrimitives == null) {
			rawPrimitives = new SVGPrimitives();
			List<SVGShape> shapeList = SVGShape.extractSelfAndDescendantShapes(svgRoot);
			rawPrimitives.addShapesToSubclassedLists(shapeList);
		}
	}

	
	/**
	 * Try to create high-level primitives from paths.
	 * 
	 * <p>Uses Path2ShapeConverter.
	 * Where successful the path is removed from currentPathlist
	 * and added to implicitShapeList.
	 * </p>
	 * 
	 * @return List of newly created SVGShapes
	 */
	
	public void createDerivedShapesFromPaths() {
		createRawShapeAndPathLists();
		if (derivedPrimitives == null) {
			derivedPrimitives = new SVGPrimitives();
			List<SVGShape> shapeList = SVGShape.extractSelfAndDescendantShapes(svgRoot);
			derivedPrimitives.addShapesToSubclassedLists(shapeList);
			Path2ShapeConverter path2ShapeConverter = new Path2ShapeConverter();
			List<SVGPath> currentPathList = derivedPrimitives.getPathList();
			for (int i = currentPathList.size() - 1; i >= 0; i--) {
				SVGPath path = currentPathList.get(i);
				SVGShape shape = path2ShapeConverter.convertPathToShape(path);
				if (shape != null) {
					LOG.trace("shape "+shape.getClass().getSimpleName());
					addId(i, shape, path);
					derivedPrimitives.addShapeToSubclassedLists(shape);
					LOG.trace("Lines "+derivedPrimitives.getLineList().size());
					currentPathList.remove(i);
				}
			}
		}
	}

	public List<SVGLine> createRawAndDerivedLines() {
		if (rawPrimitives  == null) {
			createDerivedShapesFromPaths();
			ensureHigherPrimitives();
			ensureRawContainer();
			ensureDerivedContainer();
			// 
			List<SVGLine> rawLines = rawPrimitives.getLineList();
			higherPrimitives.addSingleLines(rawLines);
			List<SVGLine> derivedLines = derivedPrimitives.getLineList();
			higherPrimitives.addSingleLines(derivedLines);
		}
		return higherPrimitives.getSingleLineList();
	}
	
	public void abstractPolygons() {
		for (int i = rawPrimitives.getPathList().size() - 1; i >= 0; i--) {
			SVGShape s = rawPrimitives.getPathList().get(i);
			if (s instanceof SVGPolygon) {
				double longestLine = 0.0;
				ArrayList<ArrayList<Real2>> sections = new ArrayList<ArrayList<Real2>>();
				SVGPolygon polygon = (SVGPolygon) s;
				Real2Array points = polygon.getReal2Array(); 
				Real2 previousPoint = points.get(points.size() - 1);
				for (int j = 0; j < points.size(); j++) {
					Real2 p = points.get(j);
					if (p.getDistance(previousPoint) > longestLine) {
						longestLine = p.getDistance(previousPoint);
					}
					previousPoint = p;
				}
				Real2 previousPreviousPoint = points.get(0);
				previousPoint = points.get(points.size() - 1);
				ArrayList<Real2> currentSection = new ArrayList<Real2>();
				sections.add(currentSection);
				for (int j = points.size() - 2; j >= 0; j--) {
					SVGLine l = new SVGLine(points.get(j), previousPreviousPoint);
					if (l.getNearestPointOnLine(previousPoint).getDistance(previousPoint) < 0.08 * longestLine) {
						currentSection.add(previousPoint);
					} else {
						currentSection = new ArrayList<Real2>();
						sections.add(currentSection);
					}
					previousPreviousPoint = previousPoint;
					previousPoint = points.get(j);
				}
				int j = points.size() - 1;
				SVGLine line = new SVGLine(points.get(j), previousPreviousPoint);
				if (line.getNearestPointOnLine(previousPoint).getDistance(previousPoint) < 0.08 * longestLine) {
					currentSection.add(previousPoint);
					sections.get(0).addAll(currentSection);
					sections.remove(currentSection);
				}
				double area = polygon.getBoundingBox().getDimension().getHeight() * polygon.getBoundingBox().getDimension().getWidth();
				for (ArrayList<Real2> section : sections) {
					int position = 0;
					for (int k = points.size() - 1; k >= 0; k--) {
						for (Real2 l : section) {
							if (points.get(k).isEqualTo(l, 1.0E-10)) {
								points.deleteElement(k);
								position = k;
								break;
							}
						}
					}
					polygon.setBoundingBoxCached(false);
					if (polygon.getBoundingBox().getDimension().getHeight() * polygon.getBoundingBox().getDimension().getWidth() < 0.92 * area) {
						Real2Array pointsNew = points.createSubArray(0, position - 1);
						pointsNew.add(section.get(section.size() / 2));
						pointsNew.add(points.createSubArray(position, points.size() - 1));
						points = pointsNew;
					}
					polygon.setReal2Array(points);
				}
				highLevelPrimitives.add(polygon);
				rawPrimitives.getPathList().remove(i);
			}
		}
	}

	private void ensureHigherPrimitives() {
		if (higherPrimitives == null) {
			higherPrimitives = new HigherPrimitives();
		}
	}

	public List<SVGText> createRawTextList() {
		derivedPrimitives.addTexts(SVGText.extractSelfAndDescendantTexts(svgRoot));
		return derivedPrimitives.getTextList();
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
			createRawAndDerivedLines();
			abstractPolygons();
			List<SVGElement> toJoin = new ArrayList<SVGElement>();
			toJoin.addAll(higherPrimitives.getSingleLineList());
			toJoin.addAll(highLevelPrimitives);
			joinableList = JoinManager.makeJoinableList(toJoin);
			higherPrimitives.addJoinableList(joinableList);
		}
		return joinableList;
	}

	public List<TramLine> createTramLineListAndRemoveUsedLines() {
		List<TramLine> tramLineList = higherPrimitives.getTramLineList();
		if (tramLineList == null) {
			createRawAndDerivedLines();
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
			abstractPolygons();
			List<SVGElement> toJoin = new ArrayList<SVGElement>();
			toJoin.addAll(higherPrimitives.getSingleLineList());
			toJoin.addAll(highLevelPrimitives);
			List<Joinable> joinableList = JoinManager.makeJoinableList(toJoin);
			joinableList.addAll(higherPrimitives.getTramLineList());
			createRawTextList();
			for (SVGText svgText : derivedPrimitives.getTextList()) {
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
						LOG.debug("junct: "+junction.getId()+" coords "+junction.getCoordinates()+" "+commonPoint.getPoint());
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

	public List<SVGLine> getSingleLineList() {
		return higherPrimitives == null ? null : higherPrimitives.getSingleLineList();
	}

	public List<SVGPath> getDerivedPathList() {
		return derivedPrimitives == null ? null : derivedPrimitives.getPathList();
	}

	public List<SVGPath> getCurrentPathList() {
		return derivedPrimitives == null ? null : derivedPrimitives.getPathList();
	}

	public List<SVGShape> getCurrentShapeList() {
		return derivedPrimitives == null ? null : derivedPrimitives.getShapeList();
	}

	protected void ensureRawContainer() {
		if (rawPrimitives == null) {
			rawPrimitives = new SVGPrimitives();
		}
	}

	protected void ensureDerivedContainer() {
		if (derivedPrimitives == null) {
			derivedPrimitives = new SVGPrimitives();
		}
	}

	public List<SVGText> getRawTextList() {
		return derivedPrimitives  == null ? null : derivedPrimitives.getTextList();
	}

	public List<SVGLine> getRawLineList() {
		return (rawPrimitives == null) ? null : rawPrimitives.getLineList();
	}

	public List<SVGLine> getDerivedLineList() {
		return (derivedPrimitives == null) ? null : derivedPrimitives.getLineList();
	}

	public List<Joinable> getJoinableList() {
		return (higherPrimitives == null) ? null : higherPrimitives.getJoinableList();
	}

	public List<TramLine> getTramLineList() {
		return (higherPrimitives == null) ? null : higherPrimitives.getTramLineList();
	}

	public void extractPlotComponents() {
		ensureRawContainer();
		ensureDerivedContainer();
		List<SVGPath> pathList = SVGPath.extractPaths(getSVGRoot());
		Path2ShapeConverter path2ShapeConverter = new Path2ShapeConverter();
		derivedPrimitives.addShapesToSubclassedLists(path2ShapeConverter.convertPathsToShapes(pathList));
	}

	
}
