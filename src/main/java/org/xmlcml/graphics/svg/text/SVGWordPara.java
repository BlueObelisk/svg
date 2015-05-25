package org.xmlcml.graphics.svg.text;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.xml.XMLUtil;

/** holds a "paragraph".
 * 
 * Currently driven by <p> elements emitted by Tesseract. These in turn hold lines and words.
 * Still exploratory
 * 
 * @author pm286
 *
 */
public class SVGWordPara extends SVGG {

	
	private static final Logger LOG = Logger.getLogger(SVGWordPara.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public static final String CLASS = "para";
	
	public SVGWordPara() {
		super();
		this.setClassName(CLASS);
	}

	public List<SVGWordLine> getSVGLineList() {
		List<Element> elements = XMLUtil.getQueryElements(this, "*[@class='"+SVGWordLine.CLASS+"']");
		List<SVGWordLine> wordLineList = new ArrayList<SVGWordLine>();
		for (Element element : elements) {
			wordLineList.add((SVGWordLine) element);
		}
		return wordLineList;
	}


}
