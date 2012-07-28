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

import java.util.ArrayList;
import java.util.List;

import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.Transform2;

import nu.xom.Element;
import nu.xom.Node;

/** supports defs
 * 
 * @author pm286
 *
 */
public class SVGImage extends SVGElement {

	public final static String TAG ="image";
	/** constructor
	 */
	public SVGImage() {
		super(TAG);
		init();
	}
	
	protected void init() {
	}
	
	/** constructor
	 */
	public SVGImage(SVGElement element) {
        super((SVGElement) element);
	}
	
	/** constructor
	 */
	public SVGImage(Element element) {
        super((SVGElement) element);
	}
	
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGImage(this);
    }

	

	/**
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	/** extent of text
	 * defined as the point origin (i.e. does not include font)
	 * @return
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			Real2 xy = this.getXY();
			Double width = getWidth();
			Double height = getHeight();
			boundingBox = new Real2Range(xy, xy.plus(new Real2(width, height)));
		}
		return boundingBox;
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default none
	 */
	protected String getBBFill() {
		return "pink";
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default blue
	 */
	protected String getBBStroke() {
		return "blue";
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default 0.5
	 */
	protected double getBBStrokeWidth() {
		return 0.5;
	}

	/**
   <image x="0" y="0" 
     transform="matrix(0.3605,0,0,0.3592,505.824,65.944)" 
     width="158" xlink:href="data:image/png;base64,iVBORw0KGgbGgjc... ...kJggg=="
     style="clip-path:url(#clipPath18);" 
     height="199" 
     preserveAspectRatio="none" xmlns:xlink="http://www.w3.org/1999/xlink"/>
	 */
	public void applyTransform(Transform2 t2) {
		Real2 xy = getXY();
		xy.transformBy(t2);
		setXY(xy);
		Real2 wh = new Real2(getWidth(), getHeight());
		Transform2 rotScale = t2.removeTranslations();
		wh.transformBy(rotScale);
		this.setWidth(wh.getX());
		this.setHeight(wh.getY());
	}
	
	/** makes a new list composed of the images in the list
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGImage> extractImages(List<SVGElement> elements) {
		List<SVGImage> imageList = new ArrayList<SVGImage>();
		for (SVGElement element : elements) {
			if (element instanceof SVGImage) {
				imageList.add((SVGImage) element);
			}
		}
		return imageList;
	}
	
}
