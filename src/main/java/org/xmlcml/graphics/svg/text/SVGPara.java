package org.xmlcml.graphics.svg.text;

import nu.xom.Attribute;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.graphics.svg.SVGG;

/** holds a "paragraph".
 * 
 * Currently driven by <p> elements emitted by Tesseract. These in turn hold lines and words.
 * Still exploratory
 * 
 * @author pm286
 *
 */
public class SVGPara extends SVGG {

	
	private static final Logger LOG = Logger.getLogger(SVGPara.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	private static final String PARA = "para";
	
	public SVGPara() {
		super();
		this.setClassName(PARA);
	}


}
