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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import nu.xom.Node;

import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.euclid.Util;

/** draws a straight line.
 * 
 * @author pm286
 *
 */
public class SVGRect extends SVGElement {

	final public static String TAG ="rect";

	/** constructor
	 */
	public SVGRect() {
		super(TAG);
		init();
	}
	
	/** constructor
	 */
	public SVGRect(SVGElement element) {
        super((SVGElement) element);
	}
	
	/** constructor
	 */
	public SVGRect(Element element) {
        super((SVGElement) element);
	}
	
	protected void init() {
		super.setDefaultStyle();
		setDefaultStyle(this);
	}
	public static void setDefaultStyle(SVGElement rect) {
		rect.setStroke("black");
		rect.setStrokeWidth(1.0);
		rect.setFill("none");
	}
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGRect(this);
    }

	/** constructor.
	 * 
	 * @param x1
	 * @param x2
	 */
	public SVGRect(double x, double y, double w, double h) {
		this();
		setX(x);
		setY(y);
		setWidth(w);
		setHeight(h);
	}

	/** create from bounding box
	 * 
	 * @param r2r
	 * @return null if r2r is null
	 */
	public static SVGRect createFromReal2Range(Real2Range r2r) {
		SVGRect rect = null;
		if (r2r != null) {
			Real2[] corners = r2r.getCorners();
			if (corners != null && corners.length == 2) {
				rect = new SVGRect(corners[0], corners[1]);
			}
		}
		return rect;
	}
	
	/** constructor.
	 * 
	 * @param x1 "lower left"
	 * @param x2 "upper right"
	 */
	public SVGRect(Real2 x1, Real2 x2) {
		this(x1.getX(), x1.getY(), x2.getX() - x1.getX(), x2.getY() - x1.getY());
	}
//  <g style="stroke-width:0.2;">
//  <line x1="-1.9021130325903073" y1="0.6180339887498945" x2="-1.175570504584946" y2="-1.618033988749895" stroke="black" style="stroke-width:0.36;"/>
//  <line x1="-1.9021130325903073" y1="0.6180339887498945" x2="-1.175570504584946" y2="-1.618033988749895" stroke="white" style="stroke-width:0.12;"/>
//</g>
	
	@Deprecated //"use createFromReal2Range which deals with nulls"
	public SVGRect(Real2Range bbox) {
		this(bbox.getXMin(), bbox.getYMin(), bbox.getXRange().getRange(), bbox.getYRange().getRange());
	}
	
//	public static SVGRect createSVGRect(Real2Range bbox) {
//		SVGRect rect = null;
//		if (bbox != null) {
//			RealRange xRange = bbox.getXRange();
//			RealRange yRange = bbox.getYRange();
//			if (xRange != null && yRange != null) {
//				rect = new SVGRect(xRange.getMin(), yRange.getMin(), xRange.getRange(), yRange.getRange());
//			}
//		}
//		return rect;
//	}

	protected void drawElement(Graphics2D g2d) {
		saveGraphicsSettingsAndApplyTransform(g2d);
		ensureCumulativeTransform();
		double x1 = this.getDouble("x");
		double y1 = this.getDouble("y");
		Real2 xy1 = new Real2(x1, y1);
		xy1 = transform(xy1, cumulativeTransform);
		double w = this.getDouble("width");
		double h = this.getDouble("height");
		Real2 xy2 = new Real2(x1+w, y1+h);
		xy2 = transform(xy2, cumulativeTransform);
		
		Rectangle2D rect = new Rectangle2D.Double(xy1.x, xy1.y, xy2.x-xy1.x, xy2.y-xy1.y);
		fill(g2d, rect);
		draw(g2d, rect);
		restoreGraphicsSettingsAndTransform(g2d);
	}

	
	public void applyTransform(Transform2 t2) {
		//assume scale and translation only
		Real2 xy = getXY();
		xy.transformBy(t2);
		this.setXY(xy);
		Real2 xxyy = new Real2(xy.getX()+getWidth(), xy.getY()+getHeight());
		xxyy.transformBy(t2);
		setHeight(xxyy.getY() - xy.getY());
		setWidth(xxyy.getX() - xy.getX());
	}
	
    /** round to decimal places.
     * 
     * @param places
     * @return this
     */
    public void format(int places) {
    	setXY(getXY().format(places));
    	setHeight(Util.format(getHeight(), places));
    	setWidth(Util.format(getWidth(), places));
    }
	
	/** extent of rect
	 * 
	 * @return
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			boundingBox = new Real2Range();
			Real2 origin = getXY();
			boundingBox.add(origin);
			boundingBox.add(origin.plus(new Real2(getWidth(), getHeight())));
		}
		return boundingBox;
	}
	
	/** get tag.
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	public void setBounds(Real2Range r2r) {
		if (r2r != null) {
			RealRange xr = r2r.getXRange();
			RealRange yr = r2r.getYRange();
			this.setXY(new Real2(xr.getMin(), yr.getMin()));
			this.setWidth(xr.getRange());
			this.setHeight(yr.getRange());
		}
	}
	
	/** makes a new list composed of the rects in the list
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGRect> extractRects(List<SVGElement> elements) {
		List<SVGRect> rectList = new ArrayList<SVGRect>();
		for (SVGElement element : elements) {
			if (element instanceof SVGRect) {
				rectList.add((SVGRect) element);
			}
		}
		return rectList;
	}
}
