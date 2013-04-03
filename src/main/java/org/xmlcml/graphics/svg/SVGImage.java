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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;

import org.apache.log4j.Logger;
import org.apache.xerces.impl.dv.util.Base64;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.Transform2;

/** supports defs
 * 
 * @author pm286
 *
 */
public class SVGImage extends SVGElement {

	private final static Logger LOG = Logger.getLogger(SVGImage.class);
	
	private static final String DATA = "data";
	private static final String BASE64 = "base64";
	public static final String IMAGE_PNG = "image/png";
	public static final String PNG = "PNG";
	public static final String IMAGE_BMP = "image/bmp";
	public static final String BMP = "BMP";
	
	private static final String XLINK_PREF = "xlink";
	private static final String HREF = "href";
	private static final String XLINK_NS = "http://www.w3.org/1999/xlink";
	public final static String TAG ="image";
	
	private static Map<String, String> mimeType2ImageTypeMap;
	static {
		mimeType2ImageTypeMap = new HashMap<String, String>();
		mimeType2ImageTypeMap.put(IMAGE_PNG, PNG);
	}
	
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

	/**
	 * we have to apply transformations HERE as the actual display image is transformed
	 * by the viewer.  
	 * @return
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			Real2 xy = this.getXY();
			Double width = getWidth();
			Double height = getHeight();
			boundingBox = new Real2Range(xy, xy.plus(new Real2(width, height)));
			LOG.trace("BB0 "+boundingBox);
			Transform2 t2 = this.getTransform2FromAttribute();
			LOG.trace("T "+t2);
			boundingBox = boundingBox.getTranformedRange(t2);
			LOG.trace("BB1 "+boundingBox);
			this.setBoundingBoxAttribute(3);
		}
		return boundingBox;
	}
	
	public void setBoundingBoxAttribute(Integer decimalPlaces) {
		if (boundingBox != null) {
			if (decimalPlaces != null) {
				boundingBox.format(decimalPlaces);
			}
			CMLElement.addCMLXAttribute(this, BOUNDING_BOX, boundingBox.toString());
		}
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

	/**
	<image x="0" y="0" transform="matrix(0.144,0,0,0.1439,251.521,271.844)" 
			clip-path="url(#clipPath2)" width="1797" xlink:href="data:image/png;
			base64,iVBORw0KGgoAAAANSUhEUgAABwUAAAV4CAMAAAB2DvLsAAADAFBMVEX////+/v56 
			enpWVlZbW1taWlpZWVnHx8eRkZFVVVWMjIysrKxXV1dYWFhqamr5+fnMzMxeXl7c 
			3NyUlJR/f3+3t7cAAACGhob29vYpKSliYmJPT083Nzf8/PyBgYENDQ3s7OwwMDD1 
			    ...
			    RERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERE 
			RPQP/R8CiIK+y8Q6KQAAAABJRU5ErkJggg==" height="1400"
			 preserveAspectRatio="none" stroke-width="0" xmlns:xlink="http://www.w3.org/1999/xlink"/>
    */
	
	public void readImageData(BufferedImage bufferedImage, String mimeType) {
		String type = mimeType2ImageTypeMap.get(mimeType);
		if (type == null) {
			throw new RuntimeException("Cannot convert mimeType: "+mimeType);
		}
		double x = bufferedImage.getMinX();
		double y = bufferedImage.getMinY();
		double height = bufferedImage.getHeight();
		double width = bufferedImage.getWidth();
		this.setX(x);
		this.setY(y);
		this.setWidth(width);
		this.setHeight(height);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(bufferedImage, type, baos);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read image", e);
		}
		byte[] byteArray = baos.toByteArray();
		String base64 = Base64.encode(byteArray);
		String attValue = DATA+":"+mimeType+";"+BASE64+","+base64;
		this.addAttribute(new Attribute(XLINK_PREF+":"+HREF, XLINK_NS, attValue));
	}
	
	public String getImageValue() {
		return this.getAttributeValue(XLINK_PREF+":"+HREF, XLINK_NS);
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
