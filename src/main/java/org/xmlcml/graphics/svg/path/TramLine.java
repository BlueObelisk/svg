package org.xmlcml.graphics.svg.path;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGLine;

/** two or more parallel lines with overlap.
 * 
 * <p>Used for double bonds, grid lines, etc.</p>
 * 
 * extends SVGLine so it can be used in place of SVGLines when needed
 * 
 * @author pm286
 *
 */
public class TramLine extends SVGLine {

	private final static Logger LOG = Logger.getLogger(TramLine.class);
	
	private SVGLine line0;
	private SVGLine line1;

	public TramLine(SVGLine linei, SVGLine linej) {
		this.line0 = linei;
		this.line1 = linej;
	}

	public SVGLine getLine0() {
		return line0;
	}

	public SVGLine getLine1() {
		return line1;
	}

	public void setFillx(String fill) {
		line0.setFill(fill);
		line1.setFill(fill);
	}

	public void setStrokeWidthx(double d) {
		line0.setStrokeWidth(d);
		line1.setStrokeWidth(d);
	}

}
