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

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;

import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.Transform2;

/** draws a straight line.
 * NOT TESTED
 * @author pm286
 *
 */
public class SVGEllipse extends SVGElement {

	public final static String TAG ="ellipse";

	/** constructor
	 */
	public SVGEllipse() {
		super(TAG);
		init();
	}
	
	/** constructor
	 */
	public SVGEllipse(SVGElement element) {
        super((SVGElement) element);
	}
	
	/** constructor
	 */
	public SVGEllipse(Element element) {
        super((SVGElement) element);
	}
	
	protected void init() {
		super.setDefaultStyle();
		setDefaultStyle(this);
	}
	public static void setDefaultStyle(SVGElement ellipse) {
		ellipse.setStroke("black");
		ellipse.setStrokeWidth(0.5);
		ellipse.setFill("#aaffff");
	}
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGEllipse(this);
    }

	/** constructor.
	 * 
	 * @param x1
	 * @param rad
	 */
	public SVGEllipse(double cx, double cy, double rx, double ry) {
		this();
		this.setCXY(new Real2(cx, cy));
		this.setRXY(new Real2(rx, ry));
	}
	
	protected void drawElement(Graphics2D g2d) {
		ensureCumulativeTransform();
		Real2 xy0 = getCXY();
		Real2 rxy = getRXY();
		xy0 = transform(xy0, cumulativeTransform);
		double rrx = rxy.getX() * cumulativeTransform.getMatrixAsArray()[0] * 0.5;
		double rry = rxy.getY() * cumulativeTransform.getMatrixAsArray()[0] * 0.5;
		
		Ellipse2D ellipse = new Ellipse2D.Double(xy0.x-rrx, xy0.y-rry, rrx+rrx, rry+rry);
		Color color = this.getColor("fill");
		g2d.setColor(color);
		g2d.fill(ellipse);
	}
	
	public Real2 getRXY() {
		return new Real2(this.getRX(), this.getRY());
	}
	
	public double getRX() {
		return this.getCoordinateValueDefaultZero("rx");
	}
	
	public double getRY() {
		return this.getCoordinateValueDefaultZero("ry");
	}
	
	public void applyTransform(Transform2 transform) {
		Real2 xy = this.getCXY();
		setCXY(xy.getTransformed(transform));
		Real2 rxy = this.getRXY();
		setRXY(rxy.getTransformed(transform));
	}

	public void format(int places) {
		setCXY(getCXY().format(places));
		setRXY(getRXY().format(places));
	}
	
	public void setRXY(Real2 rxy) {
		this.setRX(rxy.getX());
		this.setRY(rxy.getY());
	}

	public void setRX(double x) {
		this.addAttribute(new Attribute("rx", ""+x));
	}

	public void setRY(double y) {
		this.addAttribute(new Attribute("ry", ""+y));
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

	/** extent of ellipse
	 * 
	 * @return
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			boundingBox = new Real2Range();
			Real2 center = getCXY();
			Real2 rad = getRXY();
			boundingBox.add(center.subtract(rad));
			boundingBox.add(center.plus(rad));
		}
		return boundingBox;
	}
	
	
}
