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

import org.xmlcml.euclid.Real2Array;

/** draws a straight line.
 * 
 * @author pm286
 *
 */
public class SVGPolygon extends SVGPoly {

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
        super((SVGElement) element);
	}
	
	/** constructor
	 */
	public SVGPolygon(Element element) {
        super((SVGElement) element);
	}
	
	/** constructor.
	 * 
	 * @param x1
	 * @param x2
	 */
	public SVGPolygon(Real2Array real2Array) {
		this();
		setReal2Array(real2Array);
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
		return real2Array.size();
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
}
