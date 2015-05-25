package org.xmlcml.graphics.svg.text;

import nu.xom.Attribute;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.xml.XMLUtil;

/** holds a "paragraph".
 * 
 * Currently driven by <p> elements emitted by Tesseract. These in turn hold lines and words.
 * Still exploratory
 * 
 * @author pm286
 *
 */
public class SVGWord extends SVGG {

	
	private static final Logger LOG = Logger.getLogger(SVGWord.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public static final String CLASS = "word";
	
	public SVGWord() {
		super();
		this.setClassName(CLASS);
	}

	public SVGText getSVGText() {
		return (SVGText) XMLUtil.getSingleElement(this, "*[local-name()='"+SVGText.TAG+"']");
	}

	public double gapFollowing(SVGWord lastWord) {
		return this.getChildRectBoundingBox().getXMin() - lastWord.getChildRectBoundingBox().getXMax();
	}



}
