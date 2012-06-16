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


import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;




/** grouping element
 * 
 * @author pm286
 *
 */
public class SVGG extends SVGElement {
	@SuppressWarnings("unused")
	private static Logger LOG = Logger.getLogger(SVGG.class);

	public final static String TAG ="g";
	/** constructor
	 */
	public SVGG() {
		super(TAG);
	}

	public SVGG(SVGElement element) {
        super((SVGElement) element);
	}
	
	/** constructor
	 */
	public SVGG(Element element) {
        super((SVGElement) element);
	}
	
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
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
	 * 
	 * @param width
	 */
	public void setWidth(double width) {
		this.addAttribute(new Attribute("width", ""+width+"px"));
	}

	/**
	 * 
	 * @param height
	 */
	public void setHeight(double height) {
		this.addAttribute(new Attribute("height", ""+height+"px"));
	}

	/**
	 * 
	 * @param scale
	 */
	public void setScale(double scale) {
		this.addAttribute(new Attribute("transform", "scale("+scale+","+scale+")"));
	}
	
	/** traverse all children recursively
	 * 
	 * @return null by default
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			aggregateBBfromSelfAndDescendants();
		}
		return boundingBox;
	}

//	public static HtmlMenuSystem createHTMLMenuSystem(String dirname, List<SVGGBox> svgBoxList) 
//	throws IOException {
//		HtmlMenuSystem htmlMenuSystem = new HtmlMenuSystem();
//    	htmlMenuSystem.setOutdir(dirname);
//    	int i = 0;
//		for (SVGGBox box : svgBoxList) {
//			String id = box.getId();
//			if (id == null) {
//				id = "svg"+(++i);
//			}
//			File f = new File(dirname, SVGSVG.createFileName(id));
//			if (box.getParent() != null) {
//				box.detach();
//			}
//			SVGSVG.wrapAndWriteAsSVG(box, f);
//			htmlMenuSystem.addHRef(f.getName());
//		}
//		htmlMenuSystem.outputMenuAndBottomAndIndexFrame();
//		return htmlMenuSystem;
//	}
//	
//	public void applyTransform(Transform2 transform) {
//		this.setTransform(transform);
//	}
//	


}
