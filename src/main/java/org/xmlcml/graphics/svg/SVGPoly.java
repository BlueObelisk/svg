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


import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.ArrayBase;
import org.xmlcml.euclid.Axis.Axis2;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealArray.Monotonicity;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.graphics.svg.path.PathPrimitiveList;

import nu.xom.Attribute;
import nu.xom.Element;

/** 
 * Represents a collection of straight lines.
 * 
 * @author pm286
 *
 */
public abstract class SVGPoly extends SVGShape {
	
	private static final String X1 = "x1";
	private static final String X2 = "x2";
	private static final String Y1 = "y1";
	private static final String Y2 = "y2";

	@SuppressWarnings("unused")
	private static Logger LOG = Logger.getLogger(SVGPoly.class);
	
	public final static String MONOTONIC = "monotonic";
	public final static String POINTS = "points";
	public final static String[] SVG_ATTS0 = {
		POINTS
	};
	public final static List<String> SVG_ATTS = Arrays.asList(POINTS);
	
	protected Real2Array real2Array;
	protected List<SVGLine> lineList;
	protected List<SVGMarker> markerList;

	protected Boolean isClosed = false;
	protected Boolean isBox;
	protected Boolean isAligned = null;

	/** 
	 * Constructor
	 */
	public SVGPoly(String name) {
		super(name);
	}
	
	/** 
	 * Constructor
	 */
	public SVGPoly(SVGElement element) {
        super(element);
	}
	
	/** 
	 * Constructor
	 */
	public SVGPoly(Element element) {
        super((SVGElement) element);
	}
	
	protected void init() {
		super.setDefaultStyle();
		setDefaultStyle(this);
	}
	
	public static void setDefaultStyle(SVGElement line) {
		line.setStroke("black");
		line.setStrokeWidth(1.0);
	}
	
    /**
     * Copies node.
     *
     * @return Element
     */
    public Element copy() {
        return new SVGPolyline(this);
    }

	/**
	 * @param xy coordinates
	 */
	public void setReal2Array(Real2Array r2a) {
		if (r2a == null) {
			System.err.println("null real2Array in polyline: ");
		} else {
			String points = r2a.getStringArray();
			addAttribute(new Attribute(POINTS, points));
			// copy unless same object
			if (real2Array != r2a) {
				real2Array = new Real2Array(r2a);
			}
		}
	}
	
	public Real2Array getReal2Array() {
		if (real2Array == null) {
			String pointsValue = getAttributeValue("points");
			real2Array = Real2Array.createFromPairs(pointsValue, ArrayBase.ARRAY_REGEX);
		}
		return real2Array;
	}
	
	
	/*<g style="stroke-width:0.2;">
	  <line x1="-1.9021130325903073" y1="0.6180339887498945" x2="-1.175570504584946" y2="-1.618033988749895" stroke="black" style="stroke-width:0.36;"/>
	  <line x1="-1.9021130325903073" y1="0.6180339887498945" x2="-1.175570504584946" y2="-1.618033988749895" stroke="white" style="stroke-width:0.12;"/>
	</g>*/

	protected abstract void drawElement(Graphics2D g2d);

	public void applyAttributes(Graphics2D g2d) {
		if (g2d != null) {
			double width = getStrokeWidth();
			Stroke s = new BasicStroke((float) width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
			g2d.setStroke(s);
			super.applyAttributes(g2d);
		}
	}

	public List<SVGMarker> createMarkerList() {
		createLineList();
		return markerList;
	}
	
	public Line2D.Double createAndSetLine2D() {
		ensureCumulativeTransform();
		double x1 = this.getDouble(X1);
		double y1 = this.getDouble("y1");
		Real2 xy1 = new Real2(x1, y1);
		xy1 = transform(xy1, cumulativeTransform);
		double x2 = this.getDouble("x2");
		double y2 = this.getDouble("y2");
		Real2 xy2 = new Real2(x2, y2);
		xy2 = transform(xy2, cumulativeTransform);
		float width = 5.0f;
		String style = this.getAttributeValue("style");
		// does this work???
		if (style != null && style.startsWith("stroke-width:")) {
			style = style.substring("stroke-width:".length());
			style = style.substring(0, (style+S_SEMICOLON).indexOf(S_SEMICOLON));
			width = (float) Double.parseDouble(style);
			width *= 15.f;
		}
		Line2D.Double path2 = new Line2D.Double(xy1.x, xy1.y, xy2.x, xy2.y);
		return path2;
	}
	
	public void applyTransform(Transform2 t2) {
		Real2Array xy = this.getReal2Array();
		xy.transformBy(t2);
		setReal2Array(xy);
	}
	
    /** round to decimal places.
     * 
     * @param places
     * @return this
     */
    public void format(int places) {
    	getReal2Array();
    	if (real2Array != null) {
	    	real2Array.format(places);
	    	setReal2Array(real2Array);
    	}
    }
    
    public Real2 getLast() {
		Real2Array r2a = this.getReal2Array();
		return r2a.get(r2a.size()-1);
    }
 	
    public Real2 getFirst() {
		Real2Array r2a = this.getReal2Array();
		return r2a.get(0);
    }
    
    public Real2Range getBoundingBox() {
    	if (boundingBoxNeedsUpdating()) {
	    	boundingBox = new Real2Range();
	    	getReal2Array();
	    	for (int i = 0; i < real2Array.size(); i++) {
	    		boundingBox.add(real2Array.get(i));
	    	}
    	}
    	return boundingBox;
    }
 	
    /**
     * Inspects if all values along axis increase or decrease monotonically
     * 
     * @param axis (null returns null)
     * @return {@link Monotonicity} null if not monotonic or only one value
     */
    public Monotonicity getMonotonicity(Axis2 axis) {
    	Monotonicity monotonicity = null;
    	if (axis != null) {
	    	Real2Array real2Array = getReal2Array(); 
	    	RealArray realArray = (axis.equals(Axis2.X) ? real2Array.getXArray() : real2Array.getYArray());
	    	monotonicity = realArray.getMonotonicity();
    	}
    	return monotonicity;
    }
    
    public void clearMonotonicities() {
    	removeAttribute(getAttribute(MONOTONIC+Axis2.X));
    	removeAttribute(getAttribute(MONOTONIC+Axis2.Y));
    }
    
    public void addMonotonicityAttributes() {
		addMonotonicity(Axis2.X);
		addMonotonicity(Axis2.Y);
    }
    
	/**
	 * @param polyline
	 * @param axis
	 */
	private void addMonotonicity(Axis2 axis) {
		Monotonicity mono = this.getMonotonicity(axis);
		if (mono != null) {
			addAttribute(new Attribute(MONOTONIC+axis, String.valueOf(mono)));
		}
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
	 * @return default red
	 */
	protected String getBBStroke() {
		return "red";
	}

	/** 
	 * Property of graphic bounding box
	 * <p>
	 * Can be overridden
	 * 
	 * @return default 0.3
	 */
	protected double getBBStrokeWidth() {
		return 0.3;
	}

	public List<SVGLine> getLineList() {
		if (lineList == null) {
			createLineList();
		}
		return lineList;
	}

	public List<SVGMarker> getPointList() {
		if (markerList == null) {
			createMarkerList();
		}
		return markerList;
	}

	public List<SVGLine> createLineList() {
		return createLineList(false);
	}
	
	public List<SVGLine> createLineList(boolean clear) {
		Attribute pointsAtt = getAttribute(POINTS);
		if (clear) {
			lineList = null;
			if (pointsAtt != null) {
				pointsAtt.detach();
			}
		}
		if (lineList == null) {
			if (pointsAtt != null) {
				real2Array = Real2Array.createFromPairs(pointsAtt.getValue(), ArrayBase.ARRAY_REGEX);
			}
			String id = getId();
			lineList = new ArrayList<SVGLine>();
			markerList = new ArrayList<SVGMarker>();
			SVGMarker lastPoint = new SVGMarker(real2Array.get(0));
			markerList.add(lastPoint);
			SVGLine line;
			int npoints = real2Array.size();
			if (npoints == 1) {
				// meaningless?
			} else if (npoints == 2) {
				line = new SVGLine(real2Array.get(0), real2Array.get(1));
				lineList.add(line);
			} else {
				for (int i = 1; i < real2Array.size(); i++) {
					line = new SVGLine(real2Array.elementAt(i - 1), real2Array.elementAt(i));
					copyNonSVGAttributes(this, line);
					SVGMarker point = new SVGMarker(real2Array.get(i));
					markerList.add(point);
					lastPoint.addLine(line);
					point.addLine(line);
					if (line.getEuclidLine().getLength() < 0.0000001) {
						LOG.trace("ZERO LINE");
					}
					lineList.add(line);
					lastPoint = point;
				}
				if (isClosed()) {
					line = new SVGLine(real2Array.elementAt(npoints - 1), real2Array.elementAt(0));
					lineList.add(line);
				}
			}
			if (npoints > 2 && lineList.size() < npoints) {
				LOG.trace("unclosed Polyline");
			}
			setReal2Array(real2Array);
			LOG.trace("npoints: "+npoints+"; "+lineList.size());
		}
		ensureLineList();
		return lineList;
	}
	
	/** 
	 * Iterates over all polys and extracts lines.
	 * <p>
	 * Uses poly.createLineList().
	 * 
	 * @param polyList list of polys
	 * @return
	 */
	public static List<SVGLine> splitPolylinesToLines(List<? extends SVGPoly> polyList) {
		List<SVGLine> lineList = new ArrayList<SVGLine>();
		for (SVGPoly poly : polyList) {
			List<SVGLine> lines = poly.createLineList();
			lineList.addAll(lines);
		}
		return lineList;
	}
	
	protected void copyNonSVGAttributesFrom(Element element) {
		for (int i = 0; i < element.getAttributeCount(); i++) {
			Attribute attribute = element.getAttribute(i);
			if (!SVG_ATTS.contains(attribute.getLocalName())) {
				addAttribute((Attribute) attribute.copy());
			}
		}
	}
	
	protected void copyNonSVGAttributes(SVGPoly svgPoly, SVGLine line) {
		for (int i = 0; i < svgPoly.getAttributeCount(); i++) {
			Attribute attribute = svgPoly.getAttribute(i);
			if (!SVG_ATTS.contains(attribute.getLocalName())) {
				line.addAttribute((Attribute) attribute.copy());
			}
		}
	}

	protected List<SVGLine> ensureLineList() {
		if (lineList == null) {
			lineList = new ArrayList<SVGLine>();
		}
		return lineList;
	}

	protected List<SVGMarker> ensurePointList() {
		if (markerList == null) {
			markerList = new ArrayList<SVGMarker>();
		}
		return markerList;
	}

	@Override
	public String getGeometricHash() {
		return String.valueOf(real2Array);
	}

	protected void drawPolylineOrGon(Graphics2D g2d, boolean closed) {
		saveGraphicsSettingsAndApplyTransform(g2d);
		getReal2Array();
		GeneralPath poly = 
		        new GeneralPath(GeneralPath.WIND_EVEN_ODD, real2Array.size());
		Real2 xy0 = real2Array.elementAt(0);
		xy0 = transform(xy0, cumulativeTransform);
		poly.moveTo(xy0.getX(), xy0.getY());
		for (int i = 1; i < real2Array.size(); i++) {
			Real2 xy = real2Array.elementAt(i);
			xy = transform(xy, cumulativeTransform);
		    poly.lineTo(xy.getX(), xy.getY());
		}
        if (closed) {
			poly.closePath();
		}
		fill(g2d, poly);
		draw(g2d, poly);
		restoreGraphicsSettingsAndTransform(g2d);
	}

	public SVGRect createRect(double epsilon) {
		SVGRect rect = null;
		if (isBox(epsilon)) {
			Real2Range r2r = getBoundingBox();
			rect = new SVGRect(r2r.getCorners()[0], r2r.getCorners()[1]);
			rect.setFill("none");
		}
		return rect;
	}

	public Boolean isClosed() {
		return isClosed;
	}

	public Boolean isBox() {
		return isBox;
	}

	@Deprecated
	public Boolean getIsBox() {
		return isBox;
	}
	
	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	/** calculates whether 4 lines form a rectangle aligned with the axes
	 * 
	 * @param epsilon tolerance in coords
	 * @return is rectangle
	 */
	public boolean isBox(double epsilon) {
		if (isBox == null) {
			isBox = false;
			createLineList();
			
			if (lineList == null) {
				return isBox;
			}
			/** error earlier in logic - duplicate last line - catch it anyway */
			if (lineList.size() == 5) {
				Real2 point30 = lineList.get(3).getXY(0);
				Real2 point31 = lineList.get(3).getXY(1);
				Real2 point40 = lineList.get(4).getXY(0);
				Real2 point41 = lineList.get(4).getXY(1);
				if (point30.isEqualTo(point40, epsilon) && point31.isEqualTo(point41, epsilon)) {
					lineList.remove(4);
				}
			}
				
			if (lineList.size() == 4 || (lineList.size() == 3 && isClosed)) {
				SVGLine line0 = lineList.get(0);
				SVGLine line2 = lineList.get(2);
				Real2 point0 = line0.getXY(0);
				Real2 point1 = line0.getXY(1);
				Real2 point2 = line2.getXY(0);
				Real2 point3 = line2.getXY(1);
				// vertical
				// so we can debug!
				double point0x = point0.getX();
				double point1x = point1.getX();
				double point2x = point2.getX();
				double point3x = point3.getX();
				double point0y = point0.getY();
				double point1y = point1.getY();
				double point2y = point2.getY();
				double point3y = point3.getY();
				
				isBox = 
					Real.isEqual(point0x, point1x, epsilon) &&
					Real.isEqual(point2x, point3x, epsilon) &&
					Real.isEqual(point1y, point2y, epsilon) &&
					Real.isEqual(point3y, point0y, epsilon);
				if (!isBox) {
					isBox = Real.isEqual(point0y, point1y, epsilon) &&
							Real.isEqual(point2y, point3y, epsilon) &&
							Real.isEqual(point1x, point2x, epsilon) &&
							Real.isEqual(point3x, point0x, epsilon);
				}
			} else {
				LOG.trace("not a box "+lineList.size());
			}
			
		}
		return isBox;
	}

	public Boolean isAligned() {
		return isAligned;
	}

	@Deprecated
	public Boolean getIsAligned() {
		return isAligned;
	}

	public static void replacePolyLinesBySplitLines(SVGElement svgElement) {
		List<SVGPolyline> polylineList = SVGPolyline.extractSelfAndDescendantPolylines(svgElement);
		for (SVGPoly polyline : polylineList) {
			SVGPolyline.replacePolyLineBySplitLines(polyline);
		}		
	}
	
	public void add(Real2 point) {
		ensureReal2Array();
		real2Array.add(point);
	}

	private void ensureReal2Array() {
		if (real2Array == null) {
			real2Array = new Real2Array();
		}
	}

	public static SVGPoly createSVGPoly(SVGPath path) {
		PathPrimitiveList primList = path.ensurePrimitives();
		Real2 xyLast = null;
		List<SVGLine> lineList = new ArrayList<SVGLine>();
		for (int i = 0; i < primList.size(); i++) {
			SVGPathPrimitive primitive = primList.get(i);
			Real2 xy = primitive.getLastCoord();
			if (xyLast != null) {
				SVGLine line = new SVGLine(xyLast, xy);
				lineList.add(line);
			}
			xyLast = xy;
			LOG.trace("Path primitive "+primitive);
		}
		SVGPoly poly = null;
		if (path.isClosed()) {
			poly = new SVGPolygon(lineList);
		} else {
			poly = new SVGPolyline(lineList);
		}
		return poly;
	}
	
	@Override
	public String toString() {
		return (String.valueOf(real2Array));
	}
	

}