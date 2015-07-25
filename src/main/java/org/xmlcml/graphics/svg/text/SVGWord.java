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

	/**
	 * gap between end of last word and start of this.
	 * 
	 * if either component is null, return zero
	 * 
	 * @param lastWord preceding word
	 * @return
	 */
	public double gapFollowing(SVGWord lastWord) {
		Real2Range lastBox = (lastWord == null) ? null : lastWord.getChildRectBoundingBox();
		Real2Range thisBox = this.getChildRectBoundingBox();
		return (lastBox == null || thisBox == null) ? 0.0 : thisBox.getXMin() - lastBox.getXMax();
	}

	
	@Override
	public String toString() {
		return getSVGText() == null ? null : getSVGText().toString();
	}


}
