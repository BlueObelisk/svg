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
import java.util.List;

import nu.xom.Element;
import nu.xom.Node;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Line2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Line2AndReal2Calculator;
import org.xmlcml.euclid.Real;

/** draws a straight line.
 * 
 * @author pm286
 *
 */
public class SVGPolygon extends SVGPoly {
	
	private static Logger LOG = Logger.getLogger(SVGPolygon.class);

	public final static String ALL_POLYGON_XPATH = ".//svg:polygon";

	public final static String TAG ="polygon";
	
	/** constructor
	 */
	public SVGPolygon() {
		super(TAG);
		init();
	}
	
	/** constructor
	 */
	public SVGPolygon(SVGElement element) {
        super(element);
        init();
	}
	
	/** constructor
	 */
	public SVGPolygon(Element element) {
        super(element);
        init();
	}
	
	/** constructor.
	 * 
	 * @param x1
	 * @param x2
	 */
	public SVGPolygon(Real2Array real2Array) {
		this();
		init();
		setReal2Array(real2Array);
	}
	
	public SVGPolygon(List<SVGLine> lineList) {
		this();
		int npoints = lineList.size();
		Real2Array real2Array0 = new Real2Array(npoints);
		for (int i = 0; i < npoints; i++) {
			real2Array0.setElement(i, lineList.get(i).getXY(0));
		}
		setReal2Array(real2Array0);
	}

	protected void init() {
		super.init();
		isClosed = true;
	}
	
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGPolygon(this);
    }
		
	/** get tag.
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	
	public int size() {
		getReal2Array();
		return real2Array == null ? 0 : real2Array.size();
	}

	@Override
	protected void drawElement(Graphics2D g2d) {
		super.drawPolylineOrGon(g2d, true);
	}
	
	/** makes a new list composed of the polygons in the list
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGPolygon> extractPolygons(List<SVGElement> elements) {
		List<SVGPolygon> polygonList = new ArrayList<SVGPolygon>();
		for (SVGElement element : elements) {
			if (element instanceof SVGPolygon) {
				polygonList.add((SVGPolygon) element);
			}
		}
		return polygonList;
	}

	public static List<SVGPolygon> extractSelfAndDescendantPolygons(SVGG g) {
		return SVGPolygon.extractPolygons(SVGUtil.getQuerySVGElements(g, ALL_POLYGON_XPATH));
	}
	
	public List<SVGLine> createLineList(boolean clear) {
		List<SVGLine> polyList = super.createLineList(clear);
		SVGLine line = new SVGLine(real2Array.elementAt(real2Array.size() - 1), real2Array.elementAt(0));
		copyNonSVGAttributes(this, line);
		SVGMarker point = new SVGMarker(real2Array.get(0));
		markerList.get(0).addLine(line);
		markerList.get(markerList.size() - 1).addLine(line);
		if (line.getEuclidLine().getLength() < 0.0000001) {
			LOG.trace("ZERO LINE");
		}
		lineList.add(line);
		return lineList;
	}

	public boolean containsPoint(Real2 xy, double nearDuplicateRemovalDistance) {
		Line2 line = new Line2(xy, xy.plus(new Real2(1, 10000000000000d)));
		Real2 lastPoint = getReal2Array().getLastElement();
		int intersections = 0;
		boolean near = false;
		for (Real2 point : getReal2Array()) {
			Line2 edge = new Line2(lastPoint, point);
			Line2AndReal2Calculator calc = new Line2AndReal2Calculator(edge, xy);
			if (calc.minimumDistance < nearDuplicateRemovalDistance) {
				near = true;
				break;
			}
			Real2 intersection = edge.getIntersection(line);
			if (Double.isNaN(intersection.getX()) || Double.isNaN(intersection.getY())) {
				continue;
			}
			double lambda1 = edge.getLambda(intersection);
			double lambda2 = line.getLambda(intersection);
			if (lambda1 >= 0 && lambda1 <= 1 && lambda2 >= 0 && lambda2 <= 1) {
				intersections++;
			}
			lastPoint = point;
		}
		return (near || intersections % 2 == 1);
	}

	public boolean hasMirror(int startPoint, double eps) {
		boolean hasMirror = false;
		hasMirror = hasMirrorAboutMidLine(startPoint, eps);
		hasMirror &= hasMirroredSideLengths(startPoint, eps);
		
		return hasMirror;
	}

	private boolean hasMirrorAboutMidLine(int stIndex, double eps) {
		boolean hasMirror = true;
		int npoints = real2Array.size();
		int npoints2 = npoints / 2;
		int startIndex = (stIndex + 1) % npoints;
		int endIndex = stIndex + npoints2;
		LOG.debug(startIndex + "/" + endIndex);
		Real2 startPoint = real2Array.get(startIndex);
		Real2 endPoint = (npoints % 2 == 0) ? real2Array.get(endIndex) :
			real2Array.get(endIndex).getMidPoint(real2Array.get(endIndex + 1));
		LOG.debug(startPoint+"/"+endPoint);
		Line2 line = new Line2(startPoint, endPoint);
		for (int i = startIndex; i < endIndex; i++) {
			Real2 ppi = real2Array.get(i % npoints);
			Real2 ppn = real2Array.get((npoints - i) % npoints);
			double di = line.getUnsignedDistanceFromPoint(ppi);
			double dn = line.getUnsignedDistanceFromPoint(ppn);
			if (!Real.isEqual(di,  dn, eps)) {
				hasMirror = false;
				break;
			}
		}
		return hasMirror;
	}

	private boolean hasMirroredSideLengths(int stIndex, double eps) {
		boolean hasMirror = true;
		int npoints = real2Array.size();
		int npoints2 = npoints / 2;
		int startIndex = stIndex + 1;
		int endIndex = stIndex + npoints2;
		for (int i = startIndex; i < endIndex; i++) {
			// are opposite sides equal?
			if (i > startIndex) {
				double sidei = real2Array.get(i % npoints).getDistance(real2Array.get((i - 1) % npoints));
				double siden = real2Array.get((npoints - 2) % npoints).getDistance(real2Array.get((npoints - 1) % npoints));
				if (!Real.isEqual(sidei, siden, eps)) {
					hasMirror = false;
					break;
				}
			}
		}
		return hasMirror;
	}

}