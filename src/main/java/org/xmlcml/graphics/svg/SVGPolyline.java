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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;

import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Real2Range;

/** draws a straight line.
 * 
 * @author pm286
 *
 */
public class SVGPolyline extends SVGPoly {
	private static Logger LOG = Logger.getLogger(SVGPolyline.class);
	
	public final static String TAG ="polyline";
	public final static String[] SVG_ATTS0 = {
		"points"
	};
	public final static List<String> SVG_ATTS = Arrays.asList(
		new String[] {
			"points"
		}
	);
	
	private List<SVGLine> lineList;
	private List<SVGMarker> pointList;
	private Boolean isClosed = false;
	public List<SVGLine> getLineList() {
		if (lineList == null) {
			createLineList();
		}
		return lineList;
	}

	public List<SVGMarker> getPointList() {
		return pointList;
	}

	public Boolean getIsClosed() {
		return isClosed;
	}

	public Boolean getIsBox() {
		return isBox;
	}

	public Boolean getIsAligned() {
		return isAligned;
	}

	private Boolean isBox;
	private Boolean isAligned = null;

	/** constructor
	 */
	public SVGPolyline() {
		super(TAG);
		init();
	}
	
	/** constructor
	 */
	public SVGPolyline(SVGLine line) {
        this();
        CMLElement.copyAttributesFromTo(line, this);
        CMLElement.deleteAttribute(this, "x1");
        CMLElement.deleteAttribute(this, "y1");
        CMLElement.deleteAttribute(this, "x2");
        CMLElement.deleteAttribute(this, "y2");
        this.real2Array = new Real2Array();
        this.real2Array.add(line.getXY(0));
        this.real2Array.add(line.getXY(1));
        this.setReal2Array(real2Array);
	}
	
	/** constructor
	 */
	public SVGPolyline(SVGElement element) {
        super((SVGElement) element);
	}
	
	/** constructor
	 */
	public SVGPolyline(Element element) {
        super((SVGElement) element);
	}
	
	/** constructor.
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
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGPolyline(this);
    }

	/** pass polyline or convert line.
	 * 
	 * @param element
	 * @return
	 */
	public static SVGPolyline getOrCreatePolyline(SVGElement element) {
		SVGPolyline polyline = null;
		if (element instanceof SVGLine) {
			polyline = new SVGPolyline((SVGLine) element);
			
		} else if (element instanceof SVGPolyline) {
			polyline = (SVGPolyline) element;
		}
		return polyline;
	}

	/** get tag.
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	public Boolean isClosed() {
		return isClosed;
	}
	
	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
	
	/** pass polyline or convert line.
	 * 
	 * @param element
	 * @return
	 */
	public static SVGPolyline createPolyline(SVGElement element) {
		SVGPolyline polyline = null;
		if (element instanceof SVGLine) {
			polyline = new SVGPolyline((SVGLine) element);
		} else if (element instanceof SVGPath) {
			polyline = ((SVGPath) element).createPolyline();
		} else if (element instanceof SVGPolyline) {
			polyline = (SVGPolyline) element;
		}
		return polyline;
	}

	/** creates a polyline IFF consists of a M(ove) followed by one or
	 * more L(ines)
	 * @param element
	 * @return null if not transformable into a Polyline
	 */
	public static SVGPolyline createPolyline(SVGPath element) {
		SVGPolyline polyline = null;
		System.err.println("Beware NYI");
		return polyline;
	}
		

	public static List<SVGPolyline> binaryMergePolylines(List<SVGPolyline> polylineList, double eps) {
		List<SVGPolyline> newList = new ArrayList<SVGPolyline>();
		int size = polylineList.size();
		int niter = size / 2;
		for (int i = 0; i < niter*2; i += 2) {
			SVGPolyline line0 = polylineList.get(i);
			SVGPolyline line1 = polylineList.get(i + 1);
			SVGPolyline newLine = createMergedLine(line0, line1, eps);
			newList.add(newLine);
		}
		if (size %2 != 0) {
			newList.add(polylineList.get(size-1));
		}
		return newList;
	}

	/** appends poly1 to poly0.
	 * does not duplicate common element
	 * copy semantics
	 * @param poly0 not changed
	 * @param poly1 not changed
	 * @param eps
	 * @return
	 */
	public static SVGPolyline createMergedLine(SVGPolyline poly0, SVGPolyline poly1, double eps) {
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

	public List<SVGLine> createLineList() {
		return createLineList(false);
	}
	
	public List<SVGLine> createLineList(boolean clear) {
		if (clear) {
			lineList = null;
		}
		if (lineList == null) {
			String id = this.getId();
			lineList = new ArrayList<SVGLine>();
			pointList = new ArrayList<SVGMarker>();
			SVGMarker lastPoint = new SVGMarker(real2Array.get(0));
			pointList.add(lastPoint);
			SVGLine line;
			for (int i = 1; i < real2Array.size(); i++) {
				line = new SVGLine(real2Array.elementAt(i-1), real2Array.elementAt(i));
				copyNonSVGAttributes(this, line);
				SVGMarker point = new SVGMarker(real2Array.get(i));
				pointList.add(point);
				lastPoint.addLine(line);
				point.addLine(line);
				if (line.getEuclidLine().getLength() < 0.0000001) {
					LOG.trace("ZERO LINE");
				}
				lineList.add(line);
				lastPoint = point;
			}
		}
		return lineList;
	}
	
	private void copyNonSVGAttributes(SVGPolyline svgPolyline, SVGLine line) {
		for (int i = 0; i < svgPolyline.getAttributeCount(); i++) {
			Attribute attribute = svgPolyline.getAttribute(i);
			if (!SVG_ATTS.contains(attribute.getLocalName())) {
				line.addAttribute((Attribute)attribute.copy());
			}
		}
	}

	public SVGLine createSingleLine() {
		createLineList();
		return lineList.size() == 1 ? lineList.get(0) : null;
	}
	
	public List<SVGMarker> createPointList() {
		createLineList();
		return pointList;
	}
	
	/** is polyline aligned with axes?
	 * 
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
	
	/** calculates whether 4 lines form a rectangle aligned with the axes
	 * 
	 * @param epsilon tolerance in coords
	 * @return is rectangle
	 */
	public Boolean isBox(double epsilon) {
		if (isBox == null) {
			createLineList();
			if (lineList.size() == 4) {
				SVGLine line0 = lineList.get(0);
				SVGLine line2 = lineList.get(2);
				Real2 point0 = line0.getXY(0);
				Real2 point1 = line0.getXY(1);
				Real2 point2 = line2.getXY(0);
				Real2 point3 = line2.getXY(1);
				// vertical
				isBox = Real.isEqual(point0.getX(), point1.getX(), epsilon) &&
					Real.isEqual(point2.getX(), point3.getX(), epsilon) &&
					Real.isEqual(point1.getY(), point2.getY(), epsilon) &&
					Real.isEqual(point3.getY(), point0.getY(), epsilon);
				if (!isBox) {
					isBox = Real.isEqual(point0.getY(), point1.getY(), epsilon) &&
							Real.isEqual(point2.getY(), point3.getY(), epsilon) &&
							Real.isEqual(point1.getX(), point2.getX(), epsilon) &&
							Real.isEqual(point3.getX(), point0.getX(), epsilon);
				}
			} else {
				isBox = false;
			}
		}
		return isBox;
	}

	public void removeLastLine() {
		createLineList();
		if (lineList.size() > 0) {
			lineList.remove(lineList.size()-1);
			pointList.remove(pointList.size()-1);
			if (pointList.size() == 1) {
				pointList.remove(0);
			}
		}
	}
	
	public void removeFirstLine() {
		createLineList();
		if (lineList.size() > 0) {
			lineList.remove(0);
			pointList.remove(0);
			if (pointList.size() == 1) {
				pointList.remove(0);
			}
		}
	}
	
	public void add(SVGLine line) {
		ensureLineList();
		ensurePointList();
		if (pointList.size() == 0) {
			addNewMarker(line);
		}
		lineList.add(line);
		addNewMarker(line);
	}

	private void addNewMarker(SVGLine line) {
		SVGMarker marker = new SVGMarker();
		marker.addLine(line);
		pointList.add(marker);
	}
	
	public List<SVGPolyline> createLinesSplitAtPoint(int split) {
		createLineList();
		List<SVGPolyline> polylines = null;
		if (split > 0 && split <= lineList.size() ) {
			polylines = new ArrayList<SVGPolyline>();
			SVGPolyline polyline0 = new SVGPolyline();
			polylines.add(polyline0);
			for (int i = 0; i < split; i++) {
				polyline0.add(new SVGLine(lineList.get(i)));
			}
			SVGPolyline polyline1 = new SVGPolyline();
			polylines.add(polyline1);
			for (int i = split; i < lineList.size(); i++) {
				polyline1.add(new SVGLine(lineList.get(i)));
			}
		}
		return polylines;
	}
	
	public SVGPolyline createJoinedLines(List<SVGPolyline> polylineList) {
		SVGPolyline newPolyline = new SVGPolyline();
		for (SVGPolyline polyline : polylineList) {
			List<SVGLine> lineList = polyline.createLineList();
			for (SVGLine line : lineList) {
				newPolyline.add(line);
			}
		}
		return newPolyline;
	}
	
	private List<SVGLine> ensureLineList() {
		if (lineList == null) {
			lineList = new ArrayList<SVGLine>();
		}
		return lineList;
	}

	private List<SVGMarker> ensurePointList() {
		if (pointList == null) {
			pointList = new ArrayList<SVGMarker>();
		}
		return pointList;
	}

	public SVGPolygon createPolygon(double eps) {
		createLineList();
		SVGPolygon polygon = null;
		if (real2Array == null) {
			throw new RuntimeException("null real2Array");
		}
		if (real2Array.size() > 2) {
			if (real2Array.get(0).isEqualTo(real2Array.get(real2Array.size()-1), eps)) {
				Real2Array real2Array1 = new Real2Array(real2Array);
				real2Array1.deleteElement(real2Array.size()-1);
				polygon = new SVGPolygon(real2Array1);
			}
		}
		return polygon;
	}

	public SVGRect createRect(double epsilon) {
		SVGRect rect = null;
		if (this != null && isBox(epsilon)) {
			Real2Range r2r = this.getBoundingBox();
			rect = new SVGRect(r2r.getCorners()[0], r2r.getCorners()[1]);
			rect.setFill("none");
		}
		return rect;
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

	/** makes a new list composed of the polylines in the list
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
}
