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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.xml.XMLUtil;

import nu.xom.Attribute;
import nu.xom.Element;

/** 
 * Grouping element
 * 
 * @author pm286
 */
public class SVGG extends SVGElement {
	
	private static final String STRING_VALUE = "string-value";

	@SuppressWarnings("unused")
	private static Logger LOG = Logger.getLogger(SVGG.class);

	public final static String TAG ="g";
	public final static String ALL_G_XPATH = ".//svg:g";
	
	/** 
	 * Constructor
	 */
	public SVGG() {
		super(TAG);
	}

	public SVGG(SVGElement element) {
        super(element);
	}
	
	/** 
	 * Constructor
	 */
	public SVGG(Element element) {
        super((SVGElement) element);
	}
	
    /**
     * Copy node
     *
     * @return Element
     */
    public Element copy() {
        return new SVGG(this);
    }

	protected void copyAttributes(SVGElement element) {
		for (int i = 0; i < element.getAttributeCount(); i++) {
			this.addAttribute(new Attribute(element.getAttribute(i)));
		}
	}
	
	/**
	 * @return tag
	 */
	
	public String getTag() {
		return TAG;
	}

	/**
	 * @param width
	 */
	public void setWidth(double width) {
		addAttribute(new Attribute("width", String.valueOf(width)+"px"));
	}

	/**
	 * @param height
	 */
	public void setHeight(double height) {
		addAttribute(new Attribute("height", String.valueOf(height)+"px"));
	}

	/**
	 * @param scale
	 */
	public void setScale(double scale) {
		addAttribute(new Attribute("transform", "scale("+scale+","+scale+")"));
	}
	
	/** 
	 * Traverse all children recursively
	 * 
	 * @return null by default
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			aggregateBBfromSelfAndDescendants();
		}
		return boundingBox;
	}

	/** 
	 * Makes a new list composed of the SVGGs in the list
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGG> extractGs(List<SVGElement> elements) {
		List<SVGG> gList = new ArrayList<SVGG>();
		for (SVGElement element : elements) {
			if (element instanceof SVGG) {
				gList.add((SVGG) element);
			}
		}
		return gList;
	}
	
	/** 
	 * Convenience method to extract list of SVGGs in element
	 * 
	 * @param svgElement
	 * @return
	 */
	public static List<SVGG> extractSelfAndDescendantGs(SVGElement svgElement) {
		return SVGG.extractGs(SVGUtil.getQuerySVGElements(svgElement, ALL_G_XPATH));
	}

	public void copyElementsFrom(List<? extends SVGElement> elementList) {
		if (elementList != null) {
			for (SVGElement element : elementList) {
				this.appendChild(SVGElement.readAndCreateSVG(element));
			}
		}
	}

	/** 
	 * Convenience method to return the SVGG (&lt;g&gt;) indicated by the path
	 * 
	 * @param svgFile
	 * @param xPath (returns a list)
	 * @param index in list (Java counting from 0, not XPath)
	 * @return null if not found
	 */
	public final static SVGG createSVGGChunk(File svgFile, String xPath, int index) {
		SVGElement svgElement = SVGElement.readAndCreateSVG(svgFile);
		List<SVGElement> elementList = SVGG.generateElementList(svgElement, xPath);
		SVGG graphic = (elementList.size() == 0) ? null : (SVGG) elementList.get(index);
		return graphic;
	}
	
	/** returns bounding box for explicit child SVGRect.
	 * 
	 * some systems (e.g. HOCR+SVG) add an explicit child bounding box to an SVGG.
	 * 
	 * @return
	 */
	public Real2Range getChildRectBoundingBox() {
		SVGRect rect = (SVGRect) XMLUtil.getSingleElement(this, "*[local-name()='"+SVGRect.TAG+"']");
		return rect == null ? null : rect.getBoundingBox();
	}

	protected void setStringValueAttribute(String value) {
		this.addAttribute(new Attribute(STRING_VALUE, value));
	}

	public String getStringValueAttribute() {
		String value = this.getAttributeValue(STRING_VALUE);
		if (value == null) {
			value = this.getStringValue();
			this.setStringValueAttribute(value);
		}
		return value;
	}
	
	/** normally overridden.
	 * 
	 * @return null
	 */
	public String getStringValue() {
		return null;
	}

}
