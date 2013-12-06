package org.xmlcml.graphics.svg.builder;

import nu.xom.Attribute;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.*;
import org.xmlcml.graphics.svg.path.Path2ShapeConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.UnionFind;

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

	private static final double HATCH_DETECTION_DEFAULT_MAXIMUM_ANGLE_FOR_PARALLEL = 0.15;
	private static final double HATCH_DETECTION_DEFAULT_MAXIMUM_LINE_LENGTH = 4;
	private static final double HATCH_DETECTION_DEFAULT_MAXIMUM_SPACING = 4;
	private static final double POLYGON_ABSTRACTION_DEFAULT_RELATIVE_AREA_THRESHOLD = 0.92;
	private static final double POLYGON_ABSTRACTION_DEFAULT_RELATIVE_DISTANCE_FROM_LINE_THRESHOLD = 0.06;
	private static final double HATCH_DETECTION_DEFAULT_LINE_OVERLAP_EPSILON = 1e-8;
	private static final double DEFAULT_POINT_EQUIVALENCE_EPSILON = 1e-8;

	private final static Logger LOG = Logger.getLogger(SimpleBuilder.class);
	
	protected SVGElement svgRoot;
	
	protected SVGPrimitives derivedPrimitives;
	protected SVGPrimitives rawPrimitives;
	protected HigherPrimitives higherPrimitives;

	private double relativeDistanceFromLineThreshold = POLYGON_ABSTRACTION_DEFAULT_RELATIVE_DISTANCE_FROM_LINE_THRESHOLD;
	private double relativeAreaThreshold = POLYGON_ABSTRACTION_DEFAULT_RELATIVE_AREA_THRESHOLD;
	private double hatchLineMaximumLength = HATCH_DETECTION_DEFAULT_MAXIMUM_LINE_LENGTH;
	private double hatchLinesMaximumSpacing = HATCH_DETECTION_DEFAULT_MAXIMUM_SPACING;
	private double maximumAngleForParallel = HATCH_DETECTION_DEFAULT_MAXIMUM_ANGLE_FOR_PARALLEL;
	private double lineOverlapEpsilon = HATCH_DETECTION_DEFAULT_LINE_OVERLAP_EPSILON;
	private double pointEquivalenceEpsilon = DEFAULT_POINT_EQUIVALENCE_EPSILON;
	
	public SimpleBuilder() {
		
	}

	public SimpleBuilder(SVGElement svgRoot) {
		setSvgRoot(svgRoot);
	}

	public void setSvgRoot(SVGElement svgRoot) {
		this.svgRoot = svgRoot;
	}
	
	public void fillRawPrimitivesLists() {
		if (rawPrimitives == null) {
			rawPrimitives = new SVGPrimitives();
			List<SVGShape> shapeList = SVGShape.extractSelfAndDescendantShapes(svgRoot);
			rawPrimitives.addShapesToSubclassedLists(shapeList);
			rawPrimitives.addTexts(SVGText.extractSelfAndDescendantTexts(svgRoot));
		}
	}

	/**
	 * Turns SVGPaths into higher-level primitives such as SVGLine.
	 * 
	 */
	public void createDerivedPrimitives() {
		if (derivedPrimitives == null) {
			fillRawPrimitivesLists();
			derivedPrimitives = new SVGPrimitives(rawPrimitives);
			convertPathsToShapes();
			convertPolylinesToPolygons();
			removeHiddenLines();
			abstractPolygons();
		}
	}

	private void removeHiddenLines() {
		for (SVGPolygon p : derivedPrimitives.getPolygonList()) {
			Iterator<SVGLine> i = derivedPrimitives.getLineList().iterator();
			while (i.hasNext()) {
				SVGLine l = i.next();
				if (p.includes(l)) {
					i.remove();
				}
			}
		}
		for (SVGRect p : derivedPrimitives.getRectList()) {
			Iterator<SVGLine> i = derivedPrimitives.getLineList().iterator();
			while (i.hasNext()) {
				SVGLine l = i.next();
				if (p.includes(l)) {
					i.remove();
				}
			}
		}
	}

	private void convertPolylinesToPolygons() {
		Iterator<SVGPolyline> i = derivedPrimitives.getPolylineList().iterator();
		while (i.hasNext()) {
			SVGPolyline polyline = i.next();
			SVGPolygon result = polyline.createPolygon(pointEquivalenceEpsilon);
			if (result != null) {
				LOG.trace("polygon "+result.getClass().getSimpleName());
				//addId(i, shape, path);
				derivedPrimitives.addShapeToSubclassedLists(result);
				//LOG.trace("Lines " + (derivedPrimitives.getLineList() == null ? 0 : derivedPrimitives.getLineList().size()));
				i.remove();
			}
		}
	}

	/**
	 * Complete processing chain for low-level SVG into high-level SVG and non-SVG primitives such as tramlines.
	 * Handles junctions.
	 * Runs createDerivedPrimitives.
	 * 
	 */
	public void createHigherPrimitives() {
		if (higherPrimitives == null) {
			createDerivedPrimitives();
			higherPrimitives = new HigherPrimitives();
			higherPrimitives.addSingleLines(derivedPrimitives.getLineList());
			combineHatchLines();
			createTramLineList();
			createMergedJunctions();
		}
	}

	private void combineHatchLines() {
		List<HatchedTriangle> hatchList = new ArrayList<HatchedTriangle>();
		higherPrimitives.setHatchList(hatchList);
		List<SVGLine> smallLines = new ArrayList<SVGLine>();
		for (SVGLine l : derivedPrimitives.getLineList()) {
			if (l.getLength() < hatchLineMaximumLength) {
				smallLines.add(l);
			}
		}
		if (smallLines.size() == 0) {
			return;
		}
		UnionFind<SVGLine> disjointSets = UnionFind.create(smallLines);
		for (SVGLine i : smallLines) {
			for (SVGLine j : smallLines) {
				Double dist = i.calculateUnsignedDistanceBetweenLines(j, new Angle(maximumAngleForParallel, Units.RADIANS));
				if (dist != null && dist < hatchLinesMaximumSpacing && (i.overlapsWithLine(j, lineOverlapEpsilon) || j.overlapsWithLine(i, lineOverlapEpsilon))) {
					disjointSets.union(i, j);
				}
			}
		}
		makeHatchedPolygons(disjointSets);
	}

	private void makeHatchedPolygons(UnionFind<SVGLine> disjointSets) {
		List<HatchedTriangle> hatchList = higherPrimitives.getHatchList();
		for (Set<SVGLine> set : disjointSets.snapshot()) {
			ArrayList<SVGLine> lines1 = new ArrayList<SVGLine>(set);
			ArrayList<SVGLine> lines2 = new ArrayList<SVGLine>(set);
			Collections.sort(lines1, new Comparator<SVGLine>(){
				public int compare(SVGLine i, SVGLine j) {
					return (Real.isEqual(i.getMidPoint().getX(), j.getMidPoint().getX(), 0.1) ? Double.compare(i.getMidPoint().getY(), j.getMidPoint().getY()) : Double.compare(i.getMidPoint().getX(), j.getMidPoint().getX()));
				}});
			Collections.sort(lines2, new Comparator<SVGLine>(){
				public int compare(SVGLine i, SVGLine j) {
					return (Real.isEqual(i.getMidPoint().getY(), j.getMidPoint().getY(), 0.1) ? Double.compare(i.getMidPoint().getX(), j.getMidPoint().getX()) : Double.compare(i.getMidPoint().getY(), j.getMidPoint().getY()));
				}});
			List<SVGLine> lines3 = (ArrayList<SVGLine>) lines1.clone();
			Collections.reverse(lines3);
			if (lines1.equals(lines2) || lines3.equals(lines2)) {
				try {
					double direction = Math.signum(lines1.get(1).getLength() - lines1.get(0).getLength());
					double firstLength = lines1.get(0).getLength();
					for (int i = 2; i < lines1.size(); i++) {
						if (Math.signum(lines1.get(i).getLength() - firstLength) != direction) {
							return;
						}
					}
				} catch (IndexOutOfBoundsException e) {
					
				}
				hatchList.add(new HatchedTriangle(lines1));
				higherPrimitives.getLineList().removeAll(lines1);
			}
		}
	}

	private void convertPathsToShapes() {
		Path2ShapeConverter path2ShapeConverter = new Path2ShapeConverter();
		Iterator<SVGPath> i = derivedPrimitives.getPathList().iterator();
		while (i.hasNext()) {
			SVGPath path = i.next();
			SVGShape shape = path2ShapeConverter.convertPathToShape(path);
			if (shape != null && !(shape instanceof SVGPath)) {
				LOG.trace("shape "+shape.getClass().getSimpleName());
				//addId(i, shape, path);
				derivedPrimitives.addShapeToSubclassedLists(shape);
				//LOG.trace("Lines " + (derivedPrimitives.getLineList() == null ? 0 : derivedPrimitives.getLineList().size()));
				i.remove();
			}
		}
	}

	private void abstractPolygons() {
		Iterator<SVGPolygon> i = derivedPrimitives.getPolygonList().iterator();
		while (i.hasNext()) {
			double longestLine = 0.0;
			ArrayList<ArrayList<Real2>> sections = new ArrayList<ArrayList<Real2>>();
			SVGPolygon polygon = i.next();
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
				if (l.getNearestPointOnLine(previousPoint).getDistance(previousPoint) < relativeDistanceFromLineThreshold * longestLine) {
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
			if (line.getNearestPointOnLine(previousPoint).getDistance(previousPoint) < relativeDistanceFromLineThreshold * longestLine) {
				currentSection.add(previousPoint);
				sections.get(0).addAll(currentSection);
				sections.remove(currentSection);
			}
			double area = polygon.getBoundingBox().calculateArea();
			for (ArrayList<Real2> section : sections) {
				int position = 0;
				for (int k = points.size() - 1; k >= 0; k--) {
					for (Real2 l : section) {
						if (points.get(k).isEqualTo(l, pointEquivalenceEpsilon)) {
							points.deleteElement(k);
							position = k;
							break;
						}
					}
				}
				polygon.setBoundingBoxCached(false);
				if (polygon.getBoundingBox().calculateArea() < relativeAreaThreshold * area) {
					Real2Array pointsNew = points.createSubArray(0, position - 1);
					pointsNew.add(section.get(section.size() / 2));
					pointsNew.add(points.createSubArray(position, points.size() - 1));
					points = pointsNew;
				}
				polygon.setReal2Array(points);
			}
		}
	}
	
	private void createMergedJunctions() {
		createRawJunctionList();
		List<Junction> junctionList = new ArrayList<Junction>(higherPrimitives.getRawJunctionList());
		for (int i = junctionList.size() - 1; i > 0; i--) {
			Junction labile = junctionList.get(i);
			for (int j = 0; j < i; j++) {
				Junction fixed = junctionList.get(j);
				if (fixed.containsCommonPoints(labile)) {
					labile.transferDetailsTo(fixed);
					junctionList.remove(i);
					break;
				}
			}
		}
		higherPrimitives.setMergedJunctionList(junctionList);
	}

	protected void createJoinableList() {
		List<Joinable> joinableList = JoinManager.makeJoinableList(higherPrimitives.getLineList());
		joinableList.addAll(JoinManager.makeJoinableList(derivedPrimitives.getPolygonList()));
		joinableList.addAll(higherPrimitives.getTramLineList());
		joinableList.addAll(higherPrimitives.getHatchList());
		for (SVGText svgText : derivedPrimitives.getTextList()) {
			joinableList.add(new JoinableText(svgText));
		}
		higherPrimitives.addJoinableList(joinableList);
	}

	private void createTramLineList() {
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.createTramLineList(higherPrimitives.getLineList());
		tramLineManager.removeUsedTramLinePrimitives(higherPrimitives.getLineList());
		higherPrimitives.setTramLineList(tramLineList);
	}

	private void createRawJunctionList() {
		createJoinableList();
		List<Joinable> joinableList = higherPrimitives.getJoinableList();
		List<Junction> rawJunctionList = new ArrayList<Junction>();
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
					LOG.debug("junct: "+junction.getId()+" between " + joinablei.getClass() + " and " + joinablej.getClass() + " with coords "+junction.getCoordinates()+" "+commonPoint.getPoint());
				}
			}
		}
		higherPrimitives.setRawJunctionList(rawJunctionList);
	}

	/*private void addId(int i, SVGElement element, SVGElement reference) {
		String id = (reference == null) ? null : reference.getId();
		if (id == null) {
			id = element.getLocalName()+"."+i;
			element.setId(id);
		}
		
	}
	
	private void ensureIds(List<? extends SVGElement> elementList) {
		for (int i = 0; i < elementList.size(); i++){
			SVGElement element = elementList.get(i);
			addId(i, element, null);
		}
	}*/

	public SVGElement getSVGRoot() {
		return svgRoot;
	}

	/*public List<SVGLine> getSingleLineList() {
		return higherPrimitives.getSingleLineList();
	}

	public List<SVGPath> getDerivedPathList() {
		return derivedPrimitives.getPathList();
	}

	public List<SVGPath> getCurrentPathList() {
		return derivedPrimitives.getPathList();
	}

	public List<SVGShape> getCurrentShapeList() {
		return derivedPrimitives.getShapeList();
	}*/

	/*protected void ensureRawContainer() {
		if (rawPrimitives == null) {
			rawPrimitives = new SVGPrimitives();
		}
	}

	protected void ensureDerivedContainer() {
		if (derivedPrimitives == null) {
			derivedPrimitives = new SVGPrimitives();
		}
	}*/

	public SVGPrimitives getDerivedPrimitives() {
		return derivedPrimitives;
	}


	public SVGPrimitives getRawPrimitives() {
		return rawPrimitives;
	}

	public HigherPrimitives getHigherPrimitives() {
		return higherPrimitives;
	}
	
	/*public List<SVGText> getRawTextList() {
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
	}*/

	/*public void extractPlotComponents() {
		ensureRawContainer();
		ensureDerivedContainer();
		List<SVGPath> pathList = SVGPath.extractPaths(getSVGRoot());
		Path2ShapeConverter path2ShapeConverter = new Path2ShapeConverter();
		derivedPrimitives.addShapesToSubclassedLists(path2ShapeConverter.convertPathsToShapes(pathList));
	}*/
	
}
