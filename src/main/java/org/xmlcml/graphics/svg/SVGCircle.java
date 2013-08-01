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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;

import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.euclid.Util;

/** draws a straight line.
 * 
 * @author pm286
 *
 */
public class SVGCircle extends SVGElement {

	public final static String TAG ="circle";
	private Ellipse2D.Double circle2;

	public Ellipse2D.Double getCircle2() {
		return circle2;
	}

	public void setCircle2(Ellipse2D.Double circle2) {
		this.circle2 = circle2;
	}

	/** constructor
	 */
	public SVGCircle() {
		super(TAG);
		init();
	}
	
	protected void init() {
		super.setDefaultStyle();
		setDefaultStyle(this);
	}
	
	/** constructor
	 */
	public SVGCircle(SVGElement element) {
        super((SVGElement) element);
	}
	
	/** constructor
	 */
	public SVGCircle(Element element) {
        super((SVGElement) element);
	}
	
	/**
	 * 
	 * @param circle
	 */
	public static void setDefaultStyle(SVGElement circle) {
		circle.setStroke("black");
		circle.setStrokeWidth(0.5);
		circle.setFill("#aaffff");
	}
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGCircle(this);
    }

	
	/** constructor.
	 * 
	 * @param x1
	 * @param rad
	 */
	public SVGCircle(Real2 x1, double rad) {
		this();
		setXY(x1);
		setRad(rad);
		circle2 = new Ellipse2D.Double(x1.getX(), x1.getY(), rad, rad);
	}
	
	protected void drawElement(Graphics2D g2d) {
		ensureCumulativeTransform();
		double x = this.getDouble("cx");
		double y = this.getDouble("cy");
		double r = this.getDouble("r");
		Real2 xy0 = new Real2(x, y);
		xy0 = transform(xy0, cumulativeTransform);
		double rad = r * cumulativeTransform.getMatrixAsArray()[0] * 0.5;
		
		Ellipse2D ellipse = new Ellipse2D.Double(xy0.x-rad, xy0.y-rad, rad+rad, rad+rad);
		Color color = this.getColor("fill");
		g2d.setColor(color);
		g2d.fill(ellipse);
	}
	
	/**
	 * @param x1 the x1 to set
	 */
	public void setXY(Real2 x1) {
		this.addAttribute(new Attribute("cx", ""+x1.getX()));
		this.addAttribute(new Attribute("cy", ""+x1.getY()));
	}

	/**
	 * @param x1 the x1 to set
	 */
	public Real2 getXY() {
		return new Real2(
				getCX(),
				getCY()
			);
	}
	
	public void applyTransform(Transform2 transform) {
		Real2 xy = this.getXY();
		setXY(xy.getTransformed(transform));
		Real2 rxy = new Real2(this.getRad(), 0);
		setRad(rxy.getX());
	}

	public void format(int places) {
		setXY(getXY().format(places));
		setRad(Util.format(getRad(), places));
	}

	/**
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	/**
	 * @param rad the rad to set
	 */
	public void setRad(double rad) {
		this.addAttribute(new Attribute("r", ""+rad));
	}
	
	/** get radius
	 * 
	 * @return Double.NaN if not set
	 */
	public double getRad() {
		String r = this.getAttributeValue("r");
		Double d = new Double(r);
		return (d == null) ? Double.NaN : d.doubleValue();
	}

	public Ellipse2D.Double createAndSetCircle2D() {
		ensureCumulativeTransform();
		double rad = this.getDouble("r");
		double x1 = this.getDouble("cx");
		double y1 = this.getDouble("cx");
		Real2 xy1 = new Real2(x1, y1);
		xy1 = transform(xy1, cumulativeTransform);
		float width = 5.0f;
		String style = this.getAttributeValue("style");
		if (style.startsWith("stroke-width:")) {
			style = style.substring("stroke-width:".length());
			style = style.substring(0, (style+S_SEMICOLON).indexOf(S_SEMICOLON));
			width = (float) new Double(style).doubleValue();
			width *= 15.f;
		}
		circle2 = new Ellipse2D.Double(xy1.x - rad, xy1.y - rad, rad+rad, rad+rad);
		return circle2;
	}
	
	/** extent of circle
	 * 
	 * @return
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			boundingBox = new Real2Range();
			Real2 center = getCXY();
			double rad = getRad();
			boundingBox.add(new Real2(center.getX() - rad, center.getY() - rad));
			boundingBox.add(new Real2(center.getX() + rad, center.getY() + rad));
		}
		return boundingBox;
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
	 * @return default green
	 */
	protected String getBBStroke() {
		return "green";
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default 0.5
	 */
	protected double getBBStrokeWidth() {
		return 0.5;
	}
	
	public boolean includes(Real2 point) {
		Real2 center = this.getCXY();
		return point != null && point.getDistance(center) < getRad();
	}

	/** tests whether element is geometricallyContained within this
	 * @param element
	 * @return
	 * @Override
	 */
	public boolean includes(SVGElement element) {
		Real2Range thisBbox = this.getBoundingBox();
		Real2Range elementBox = (element == null) ? null : element.getBoundingBox();
		if (thisBbox == null) {
			return false;
		}
		Real2[] corners = elementBox.getCorners();
		if (!this.includes(corners[0]) || !this.includes(corners[1])) {
			return false;
		}
		// generate and test other corners
		if (!this.includes(new Real2(corners[0].x, corners[1].y))) {
			return false;
		}
		if (!this.includes(new Real2(corners[1].x, corners[0].y))) {
			return false;
		}
		return true;
	}
	
	/** makes a new list composed of the circles in the list
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGCircle> extractCircles(List<SVGElement> elements) {
		List<SVGCircle> circleList = new ArrayList<SVGCircle>();
		for (SVGElement element : elements) {
			if (element instanceof SVGCircle) {
				circleList.add((SVGCircle) element);
			}
		}
		return circleList;
	}
}
