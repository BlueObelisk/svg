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

import nu.xom.Element;
import nu.xom.Node;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.Transform2;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** a virtual point. May not correspond completely with SVG <marker>
 * creates a "point", drawn by an arbitrary symbol
 * @author pm286
 *
 */
public class SVGMarker extends SVGElement {

	final public static String TAG ="marker";
	// an addition
	private List<SVGLine> lineList; 
	private static double size = 2;

	/** constructor
	 */
	public SVGMarker() {
		super(TAG);
		init();
	}
	
	/** constructor
	 */
	public SVGMarker(SVGElement element) {
		super(element);
	}
	
	/** constructor
	 */
	public SVGMarker(Element element) {
        super((SVGElement) element);
	}
	
	protected void init() {
	}

    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGMarker(this);
    }


    public void setSymbol(SVGElement symbol) {
    	SVGElement symb = this.getSymbol();
    	if (symb != null) {
    		symb.detach();
    	}
    	this.appendChild(symbol);
    }
	/** constructor.
	 * 
	 * @param x1
	 * @param x2
	 */
	public SVGMarker(double x, double y) {
		this();
//		setX(x);
//		setY(y);
	}

	
	public SVGMarker(Real2 xy) {
		this();
//		SVGElement symbol = new SVGCircle(xy.plus(new Real2(-size, -size)), size);
		SVGElement symbol = new SVGCircle(xy, size);
		this.appendChild(symbol);
	}

	protected void drawElement(Graphics2D g2d) {
		saveGraphicsSettingsAndApplyTransform(g2d);
//		double x1 = this.getDouble("x");
//		double y1 = this.getDouble("y");
//		Real2 xy1 = new Real2(x1, y1);
//		xy1 = transform(xy1, cumulativeTransform);
//		double w = this.getDouble("width");
//		double h = this.getDouble("height");
//		Real2 xy2 = new Real2(x1+w, y1+h);
//		xy2 = transform(xy2, cumulativeTransform);
//		float width = 1.0f;
//		String style = this.getAttributeValue("style");
//		if (style.startsWith("stroke-width:")) {
//			style = style.substring("stroke-width:".length());
//			style = style.substring(0, (style+S_SEMICOLON).indexOf(S_SEMICOLON));
//			width = (float) new Double(style).doubleValue();
//			width *= 15.f;
//		}
//		
//		Stroke s = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
//		g2d.setStroke(s);
//		
//		String colorS = "black";
//		String stroke = this.getAttributeValue("stroke");
//		if (stroke != null) {
//			colorS = stroke;
//		}
//		Color color = colorMap.get(colorS);
//		g2d.setColor(color);
//		Line2D line = new Line2D.Double(xy1.x, xy1.y, xy2.x, xy2.y);
//		g2d.draw(line);
		restoreGraphicsSettingsAndTransform(g2d);
	}
	
	public void applyTransform(Transform2 t2) {
		//assume scale and translation only
//		Real2 xy = getXY();
//		xy.transformBy(t2);
//		this.setXY(xy);
//		Real2 xxyy = new Real2(xy.getX()+getWidth(), xy.getY()+getHeight());
//		xxyy.transformBy(t2);
	}
	
    /** round to decimal places.
     * 
     * @param places
     * @return this
     */
    public void format(int places) {
//    	setXY(getXY().format(places));
//    	setHeight(Util.format(getHeight(), places));
//    	setWidth(Util.format(getWidth(), places));
    }
	
	/** extent 
	 * 
	 * @return
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			SVGElement element = getSymbol();
			boundingBox = (element == null) ? null : element.getBoundingBox();
		}
		return boundingBox;
	}
	
	public SVGElement getSymbol() {
		if (this.getChildElements().size()  == 1) {
			Element element = this.getChildElements().get(0);
			return (element instanceof SVGElement) ? (SVGElement) element : null;
		}
		return null;
	}

	/** get tag.
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	public void addLine(SVGLine line) {
		ensureLineList();
		lineList.add(line);
	}

	private void ensureLineList() {
		if (lineList == null) {
			lineList = new ArrayList<SVGLine>();
		}
	}

}
