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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Line2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.xml.XMLUtil;

import nu.xom.Element;
import nu.xom.ParentNode;

/** 
 * Represents a collection of straight lines (not implicitly closed).
 * 
 * @author pm286
 */
public class SVGPolyline extends SVGPoly {

	
	public final static String ALL_POLYLINE_XPATH = ".//svg:polyline";

	private static final String X1 = "x1";
	private static final String X2 = "x2";
	private static final String Y1 = "y1";
	private static final String Y2 = "y2";

	private static Logger LOG = Logger.getLogger(SVGPolyline.class);
	
	public final static String TAG ="polyline";
	
	protected double epsilon = 0.005;

	/** 
	 * Constructor.
	 */
	public SVGPolyline() {
		super(TAG);
		init();
	}
	
	/** 
	 * Constructor.
	 */
	public SVGPolyline(SVGLine line) {
        this();
        XMLUtil.copyAttributesFromTo(line, this);
        XMLUtil.deleteAttribute(this, X1);
        XMLUtil.deleteAttribute(this, Y1);
        XMLUtil.deleteAttribute(this, X2);
        XMLUtil.deleteAttribute(this, Y2);
        this.real2Array = new Real2Array();
        this.real2Array.add(line.getXY(0));
        this.real2Array.add(line.getXY(1));
        this.setReal2Array(real2Array);
	}
	
	/** 
	 * Constructor.
	 */
	public SVGPolyline(SVGElement element) {
        super(element);
	}
	
	/** 
	 * Constructor.
	 */
	public SVGPolyline(Element element) {
        super((SVGElement) element);
	}
	
	/** 
	 * Constructor.
	 * 
	 * @param x1
	 * @param x2
	 */
	public SVGPolyline(Real2Array real2Array) {
		this();
		setReal2Array(real2Array);
		lineList = null;
	}
	
    /**
     * Copies node.
     *
     * @return Element
     */
    public Element copy() {
        return new SVGPolyline(this);
    }
    
	public SVGPolyline(List<SVGLine> lineList) {
		this();
		int npoints = lineList.size();
		Real2Array real2Array0 = new Real2Array(npoints + 1);
		for (int i = 0; i < npoints; i++) {
			real2Array0.setElement(i, lineList.get(i).getXY(0));
		}
		real2Array0.setElement(npoints, lineList.get(npoints - 1).getXY(1));
		setReal2Array(real2Array0);
	}



	/** 
	 * Passes polyline or converts line.
	 * 
	 * @param element
	 * @return
	 */
	public static SVGPoly getOrCreatePolyline(SVGElement element) {
		SVGPoly polyline = null;
		if (element instanceof SVGLine) {
			polyline = new SVGPolyline((SVGLine) element);
			
		} else if (element instanceof SVGPolyline) {
			polyline = (SVGPoly) element;
		}
		return polyline;
	}

	/** 
	 * Gets tag.
	 * 
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	@Override
	protected void drawElement(Graphics2D g2d) {
		super.drawPolylineOrGon(g2d, false);
	}
	
	/** 
	 * Passes polyline or converts line.
	 * 
	 * @param element
	 * @return
	 */
	public static SVGPoly createPolyline(SVGElement element) {
		SVGPoly polyline = null;
		if (element instanceof SVGLine) {
			polyline = new SVGPolyline((SVGLine) element);
		} else if (element instanceof SVGPath) {
			polyline = ((SVGPath) element).createPolyline();
		} else if (element instanceof SVGPolyline) {
			polyline = (SVGPoly) element;
		}
		return polyline;
	}

	/** 
	 * Creates a polyline IFF consists of a M(ove) followed by one or
	 * more L(ines).
	 * 
	 * @param element
	 * @return null if not transformable into a Polyline
	 */
	public static SVGPoly createPolyline(SVGPath element) {
		SVGPoly polyline = null;
		System.err.println("Beware NYI");
		return polyline;
	}

	/** runs through a list of lines joining where possible to create (smaller) list.
	 * 
	 * Crude algorithm. 
	 *   1 start = 0;
	 *   2 size = number of lines
	 *   3 iline = start ... size-1
	 *   4 test (iline, iline+1) for join at either end
	 *      if join, replace lines with merged line
	 *      start = iline
	 *      goto 2
	 *   5 exit if no change
	 * 
	 * iterates from 
	 * 
	 * @param polylineList
	 * @param eps
	 * @return
	 */
	public static List<SVGPolyline> quadraticMergePolylines(List<SVGPolyline> polylineList, double eps) {
		List<SVGPolyline> polylineListNew = new ArrayList<SVGPolyline>(polylineList);
		boolean change = true;
		int startLine = 0;
		while (change) {
			change = false;
			int size = polylineListNew.size();
			for (int iline = startLine; iline < size - 1; iline++) {
				SVGPolyline line0 = polylineListNew.get(iline);
				SVGPolyline line1 = polylineListNew.get(iline + 1);
				SVGPolyline newLine = createMergedLine(line0, line1, eps);
				newLine = (newLine != null) ? newLine : createMergedLine(line1, line0, eps);
				if (newLine != null) {
					startLine = iline;
					replaceLineAndCloseUp(iline, newLine, polylineListNew);
					break;
				}
			}
		}
		return polylineListNew;
	}

	private static void replaceLineAndCloseUp(int iline, SVGPolyline newLine, List<SVGPolyline> polylineListNew) {
		polylineListNew.set(iline, newLine);
		polylineListNew.remove(iline + 1);
	}

	/** merges lines where endA = startB.
	 * 
	 * I think it relies on lines being ordered
	 * 
	 * Not fully tested
	 * 
	 * @param polylineList
	 * @param eps
	 * @return
	 */
	public static List<SVGPolyline> binaryMergePolylines(List<SVGPolyline> polylineList, double eps) {
		List<SVGPolyline> newList = new ArrayList<SVGPolyline>();
		int size = polylineList.size();
		int niter = size / 2;
		for (int i = 0; i < niter * 2; i += 2) {
			SVGPoly line0 = polylineList.get(i);
			SVGPoly line1 = polylineList.get(i + 1);
			if (line0 == null || line1 == null) {
				continue;
			}
			SVGPolyline newLine = createMergedLine(line0, line1, eps);
			if (newLine != null) {
				newList.add(newLine);
//				polylineList.remove(i);
			}
		}
		if (size %2 != 0) {
			newList.add(polylineList.get(size - 1));
		}
		return newList;
	}

	/** 
	 * Appends poly1 to poly0.
	 * <p>
	 * Does not duplicate common element.
	 * <p>
	 * Copy semantics.
	 * 
	 * @param poly0 not changed
	 * @param poly1 not changed
	 * @param eps
	 * @return
	 */
	public static SVGPolyline createMergedLine(SVGPoly poly0, SVGPoly poly1, double eps) {
		SVGPolyline newPoly = null;
		Real2 last0 = poly0.getLast();
		Real2 first1 = poly1.getFirst();
		if (last0.isEqualTo(first1, eps)) {
			newPoly = new SVGPolyline(poly0);
			Real2Array r20 = newPoly.getReal2Array();
			Real2Array r21 = poly1.getReal2Array();
			for (int i = 1; i < r21.size(); i++) {
				r20.add(new Real2(r21.get(i)));
			}
			newPoly.setReal2Array(r20);
		}
		return newPoly;
	}

	public SVGLine createSingleLine() {
		SVGLine line = createVerticalOrHorizontalLine(epsilon);
		if (line == null) {
			createLineList();
			if (lineList.size() == 1) {
				line = lineList.get(0);
			}
		}
		return line;
	}
	
	/** 
	 * Is polyline aligned with axes?
	 */
	public Boolean isAlignedWithAxes(double epsilon) {
		if (isAligned  == null) {
			createLineList();
			isAligned = true;
			for (SVGLine line : lineList) {
				if (!line.isHorizontal(epsilon) && !line.isVertical(epsilon)) {
					isAligned = false;
					break;
				}
			}
		}
		return isAligned;
	}
	
	public void removeLastLine() {
		createLineList();
		if (lineList.size() > 0) {
			lineList.remove(lineList.size()-1);
			markerList.remove(markerList.size()-1);
			if (markerList.size() == 1) {
				markerList.remove(0);
			}
		}
	}
	
	public void removeFirstLine() {
		createLineList();
		if (lineList.size() > 0) {
			lineList.remove(0);
			markerList.remove(0);
			if (markerList.size() == 1) {
				markerList.remove(0);
			}
		}
	}
	
	public void add(SVGLine line) {
		ensureLineList();
		ensurePointList();
		if (markerList.size() == 0) {
			addNewMarker(line);
		}
		lineList.add(line);
		addNewMarker(line);
	}

	private void addNewMarker(SVGLine line) {
		SVGMarker marker = new SVGMarker();
		marker.addLine(line);
		markerList.add(marker);
	}

	/** 
	 * Split polyline at given position.
	 * 
	 * @param splitPosition
	 * @return
	 */
	public List<SVGPolyline> createLinesSplitAtPoint(int splitPosition) {
		createLineList();
		List<SVGPolyline> polylines = null;
		if (splitPosition > 0 && splitPosition <= lineList.size() ) {
			polylines = new ArrayList<SVGPolyline>();
			SVGPolyline polyline0 = new SVGPolyline();
			polylines.add(polyline0);
			for (int i = 0; i < splitPosition; i++) {
				polyline0.add(new SVGLine(lineList.get(i)));
			}
			SVGPolyline polyline1 = new SVGPolyline();
			polylines.add(polyline1);
			for (int i = splitPosition; i < lineList.size(); i++) {
				polyline1.add(new SVGLine(lineList.get(i)));
			}
		}
		return polylines;
	}
	
	public SVGPoly createJoinedLines(List<SVGPolyline> polylineList) {
		SVGPolyline newPolyline = new SVGPolyline();
		for (SVGPoly polyline : polylineList) {
			List<SVGLine> lineList = polyline.createLineList();
			for (SVGLine line : lineList) {
				newPolyline.add(line);
			}
		}
		return newPolyline;
	}
	
	/** 
	 * Alters direction of line. MODIFIES THIS.
	 */
	public void reverse() {
		Real2Array r2a = this.getReal2Array();
		r2a.reverse();
		this.setReal2Array(r2a);
	}
	
	@Override
	public void setReal2Array(Real2Array r2a) {
		super.setReal2Array(r2a);
		//lineList = null;
		//pointList = null;
	}
	
	public SVGPolygon createPolygon(double eps) {
		createLineList();
		SVGPolygon polygon = null;
		if (real2Array == null) {
			throw new RuntimeException("null real2Array");
		}
		if (real2Array.size() > 2) {
			if (real2Array.get(0).isEqualTo(real2Array.get(real2Array.size() - 1), eps)) {
				Real2Array real2Array1 = new Real2Array(real2Array);
				real2Array1.deleteElement(real2Array.size() - 1);
				polygon = new SVGPolygon(real2Array1);
				polygon.copyNonSVGAttributesFrom(this);
			} else if (isClosed()) {
				Real2Array real2Array1 = new Real2Array(real2Array);
				polygon = new SVGPolygon(real2Array1);
				polygon.copyNonSVGAttributesFrom(this);
			}
		}
		return polygon;
	}

	public boolean removeDuplicateLines() {
		boolean duplicate = false;
		List<SVGLine> lineList = createLineList();
		List<SVGLine> lineList1 = new ArrayList<SVGLine>();
		Set<String> xmlSet = new HashSet<String>();
		int count = 0;
		for (SVGLine line : lineList) {
			String xmlS = line.toXML();
			if (xmlSet.contains(xmlS)) {
				LOG.trace("duplicate line in polyline "+xmlS);
				count++;
			} else {
				xmlSet.add(xmlS);
				lineList1.add(line);
			}
		}
		if (count > 0) {
			duplicate = true;
			Real2Array r2a = new Real2Array();
			r2a.add(lineList.get(0).getXY(0));
			for (SVGLine line : lineList1) {
				r2a.add(line.getXY(1));
			}
			this.setReal2Array(r2a);
			this.createLineList(true);
			LOG.trace("removed "+count+" duplicate lines ");
		}
		return duplicate;
	}

	/** 
	 * Makes a new list composed of the polylines in the list.
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGPolyline> extractPolylines(List<SVGElement> elements) {
		List<SVGPolyline> polylineList = new ArrayList<SVGPolyline>();
		for (SVGElement element : elements) {
			if (element instanceof SVGPolyline) {
				polylineList.add((SVGPolyline) element);
			}
		}
		return polylineList;
	}
	
	/** 
	 * Convenience method to extract list of svgTexts in element.
	 * 
	 * @param svgElement
	 * @return
	 */
	public static List<SVGPolyline> extractSelfAndDescendantPolylines(SVGElement svgElement) {
		return SVGPolyline.extractPolylines(SVGUtil.getQuerySVGElements(svgElement, ALL_POLYLINE_XPATH));
	}


	/** 
	 * Appends points in polyline into this.
	 * 
	 * @param polyline
	 * @param start point in polyline to start at; allows for skipping common point(s)
	 */
	public void appendIntoSingleLine(SVGPoly polyline, int start) {
		Real2Array r2a = polyline.real2Array;
		if (start != 0) {
			r2a = r2a.createSubArray(start);
		}
		this.real2Array.add(r2a);
		this.setReal2Array(real2Array);
		createLineList(true);
		createMarkerList();
	}

	/** 
	 * Replaces polyline by its split SVGLines.
	 * <p>
	 * Inserts new lines at old position of polyline.
	 * 
	 * @param polyline
	 */
	public static void replacePolyLineBySplitLines(SVGPoly polyline) {
		List<SVGLine> lines = polyline.createLineList();
		ParentNode parent = polyline.getParent();
		if (parent != null) {
			int index = parent.indexOf(polyline);
			for (int i = lines.size() - 1; i >= 0; i--) {
				parent.insertChild(lines.get(i), index);
			}
			polyline.detach();
		}
	}

	/** 
	 * Number of lines.
	 * 
	 * 
	 * @return
	 */
	public int size() {
		getReal2Array();
		return real2Array.size() - 1;
	}

	public Angle getSignedAngleOfDeviation() {
		Angle angle = new Angle(0.0);
		if (size() > 1) {
			List<SVGLine> lineList = getLineList();
			Line2 line0 = lineList.get(0).getEuclidLine();
			Line2 line1 = lineList.get(lineList.size() - 1).getEuclidLine();
			angle = line0.getAngleMadeWith(line1);
		}
		angle.setRange(Angle.Range.SIGNED);
		return angle;
	}

	/** checks equality assuming points are in same order.
	 * 
	 * for cyclic polylines also assumes same starting point.
	 * 
	 * @param poly
	 * @param delta
	 * @return
	 */
	public boolean hasEqualCoordinates(SVGPolyline polyline, double delta) {
		
		if (this.size() != polyline.size()) {
			return false;
		}
		this.getReal2Array();
		Real2Array polyArray = polyline.getReal2Array();
		for (int i = 0; i < this.size(); i++) {
			Real2 thisp = this.real2Array.elementAt(i);
			Real2 polyp = polyArray.elementAt(i);
			if (thisp.getDistance(polyp) > delta) {
				return false;
			}
		}
		return true;
	}

	/**
	 * create a line if the polyline is aligned with either axis
	 * 
	 * @param delta tolerance from axis
	 * @return null if no line
	 */
	public SVGLine createVerticalOrHorizontalLine(double delta) {
		SVGLine line = null;
		Real2Range bbox = getBoundingBox();
		double xDelta = bbox.getXRange().getRange();
		double yDelta = bbox.getYRange().getRange();
		if (xDelta < delta || yDelta < delta) {
			line = new SVGLine();
			XMLUtil.copyAttributes(this, line);
			line.setXY(this.getFirst(), 0);
			line.setXY(this.getLast(), 1);
		}
		return line;
	}

}