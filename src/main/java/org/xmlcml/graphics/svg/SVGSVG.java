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
import java.io.File;
import java.io.FileOutputStream;

import nu.xom.Attribute;
import nu.xom.Node;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.xml.XMLConstants;

/** container for SVG
 * "svg"
 * @author pm286
 *
 */
public class SVGSVG extends SVGElement {

	public final static Logger LOG = Logger.getLogger(SVGSVG.class);
	public final static String TAG = "svg";
	private static String svgSuffix = "svg";
	private Double begin = null;
	private Double dur = null;
	
	/** constructor.
	 * 
	 */
	public SVGSVG() {
		super(TAG);
	}
	
	/** constructor
	 */
	public SVGSVG(SVGSVG element) {
        super(element);
	}
	
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGSVG(this);
    }

	/**
	 * @return tag
	 */

	public String getTag() {
		return TAG;
	}

	protected void drawElement(Graphics2D g2d) {
		super.drawElement(g2d);
	}
	
	public void setId(String id) {
		this.addAttribute(new Attribute("id", id));
	}
	
	public String getId() {
		return this.getAttributeValue("id");
	}

	/** defaults to heigh=800 width=700.
	 * 
	 * */
	public static SVGSVG wrapAndWriteAsSVG(SVGElement svgg, File file) {
		return wrapAndWriteAsSVG(svgg, file, 800.0, 700.0);
	}
	
	/**	creates an SVGSVG wrapper for any element and outputs to file.
	 * 
	 *   <p>mainly for debugging.</p>
	 *   
	 * @param svgg
	 * @param file
	 * @param height
	 * @param width
	 * @return
	 */
	public static SVGSVG wrapAndWriteAsSVG(SVGElement svgg, File file, double height, double width) {
		SVGSVG svgsvg = wrapAsSVG(svgg);
		svgsvg.setHeight(height);
		svgsvg.setWidth(width);
		try {
			LOG.trace("Writing SVG "+file.getAbsolutePath());
			FileOutputStream fos = new FileOutputStream(file);
			SVGUtil.debug(svgsvg, fos, 1);
			fos.close();
		} catch (Exception e) {
			throw new RuntimeException("cannot write svg to "+file, e);
		}
		return svgsvg;
	}

	public static SVGSVG wrapAsSVG(SVGElement svgg) {
		if (svgg.getParent() != null) {
			svgg.detach();
		}
		SVGSVG svgsvg = new SVGSVG();
		svgsvg.appendChild(svgg);
		return svgsvg;
	}

	public static String createFileName(String id) {
		return id + XMLConstants.S_PERIOD+svgSuffix ;
	}

	public void setDur(Double d) {
		this.dur  = d;
	}

	public void setBegin(Double d) {
		this.begin = d;
	}
	
	/** traverse all children recursively
	 * @return bbox
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			aggregateBBfromSelfAndDescendants();
		}
		return boundingBox;
	}


	/**
	 * adds a new svg:g between element and its children
	 * this can be used to set scales, rendering, etc.
	 * @param element to amend (is changed)
	 */
	public static SVGG interposeGBetweenChildren(SVGElement element) {
		SVGG g = new SVGG();
		element.appendChild(g);
		while (element.getChildCount() > 1) {
			Node child = element.getChild(0);
			child.detach();
			g.appendChild(child);
		}
		return g;
	}

}
