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
import java.awt.geom.Line2D;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;

import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.euclid.Axis.Axis2;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealArray.Monotonicity;
import org.xmlcml.euclid.Transform2;

/** draws a straight line.
 * 
 * @author pm286
 *
 */
public abstract class SVGPoly extends SVGElement {
	@SuppressWarnings("unused")
	private static Logger LOG = Logger.getLogger(SVGPoly.class);
	
	public final static String MONOTONIC = "monotonic";
	protected Real2Array real2Array;
	
	/** constructor
	 */
	public SVGPoly(String name) {
		super(name);
	}
	
	/** constructor
	 */
	public SVGPoly(SVGElement element) {
        super((SVGElement) element);
	}
	
	/** constructor
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
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
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
			this.addAttribute(new Attribute("points", points));
			// copy unless same object
			if (this.real2Array != r2a) {
				this.real2Array = new Real2Array(r2a);
			}
		}
	}
	
	public Real2Array getReal2Array() {
		if (real2Array == null) {
			real2Array = Real2Array.createFromPairs(
					this.getAttributeValue("points"), CMLConstants.S_COMMA+S_PIPE+S_SPACE);
		}
		return real2Array;
	}
	
	
//  <g style="stroke-width:0.2;">
//  <line x1="-1.9021130325903073" y1="0.6180339887498945" x2="-1.175570504584946" y2="-1.618033988749895" stroke="black" style="stroke-width:0.36;"/>
//  <line x1="-1.9021130325903073" y1="0.6180339887498945" x2="-1.175570504584946" y2="-1.618033988749895" stroke="white" style="stroke-width:0.12;"/>
//</g>
	
	protected void drawElement(Graphics2D g2d) {
		Line2D path = createAndSetLine2D();
		applyAttributes(g2d);
		g2d.draw(path);
	}

	public void applyAttributes(Graphics2D g2d) {
		if (g2d != null) {
			float width = (float) this.getStrokeWidth();
			Stroke s = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
			g2d.setStroke(s);
			super.applyAttributes(g2d);
		}
	}

	public Line2D.Double createAndSetLine2D() {
		double x1 = this.getDouble("x1");
		double y1 = this.getDouble("y1");
		Real2 xy1 = new Real2(x1, y1);
		xy1 = transform(xy1, cumulativeTransform);
		double x2 = this.getDouble("x2");
		double y2 = this.getDouble("y2");
		Real2 xy2 = new Real2(x2, y2);
		xy2 = transform(xy2, cumulativeTransform);
		float width = 5.0f;
		String style = this.getAttributeValue("style");
		if (style.startsWith("stroke-width:")) {
			style = style.substring("stroke-width:".length());
			style = style.substring(0, (style+S_SEMICOLON).indexOf(S_SEMICOLON));
			width = (float) new Double(style).doubleValue();
			width *= 15.f;
		}
		Line2D.Double path2 = new Line2D.Double(xy1.x, xy1.y, xy2.x, xy2.y);
		return path2;
	}
	
//	public Line2D.Double getPath2() {
//		return path2;
//	}
//
//	public void setPath2(Line2D.Double path2) {
//		this.path2 = path2;
//	}
	
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
    	real2Array.format(places);
    	setReal2Array(real2Array);
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
     * inspects if all values along axis increase or decrease monotonically
     * @param axis (null returns null)
     * @return {@link Monotonicity} null if not monotonic or only one value
     */
    public Monotonicity getMonotonicity(Axis2 axis) {
    	Monotonicity monotonicity = null;
    	if (axis != null) {
	    	Real2Array real2Array = getReal2Array(); 
	    	RealArray realArray = (axis.equals(Axis2.X)) ? real2Array.getXArray() : real2Array.getYArray();
	    	monotonicity = realArray.getMonotonicity();
    	}
    	return monotonicity;
    }
    
    public void clearMonotonicities() {
    	this.removeAttribute(this.getAttribute(MONOTONIC+Axis2.X));
    	this.removeAttribute(this.getAttribute(MONOTONIC+Axis2.Y));
    }
    
    public void addMonotonicityAttributes() {
		this.addMonotonicity(Axis2.X);
		this.addMonotonicity(Axis2.Y);
    }
	/**
	 * @param polyline
	 * @param axis
	 */
	private void addMonotonicity(Axis2 axis) {
		Monotonicity mono = this.getMonotonicity(axis);
		if (mono != null) {
			this.addAttribute(new Attribute(MONOTONIC+axis, ""+mono));
		}
	}
	
	/** property of graphic bounding box
	 * can be overridden
	 * @return default none
	 */
	protected String getBBFill() {
		return "none";
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default red
	 */
	protected String getBBStroke() {
		return "red";
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default 0.5
	 */
	protected double getBBStrokeWidth() {
		return 0.3;
	}

}
