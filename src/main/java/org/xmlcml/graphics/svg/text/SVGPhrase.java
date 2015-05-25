package org.xmlcml.graphics.svg.text;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.xml.XMLUtil;

/** holds a set of words which are geometrically joined into a single unit.
 * 
 * Still exploratory
 * 
 * @author pm286
 *
 */
public class SVGPhrase extends SVGG {

	
	private static final Logger LOG = Logger.getLogger(SVGPhrase.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public static final String CLASS = "phrase";
	
	private List<SVGWord> wordList;
	private double interWordGap = 17.0; // empirical gap between Tesseract words in a phrase
	
	public SVGPhrase() {
		super();
		this.setClassName(CLASS);
	}
	
	/** uses child Word elements to populate list.
	 * 
	 * @return
	 */
	public List<SVGWord> getOrCreateWordList() {
		if (this.wordList == null) {
			wordList = new ArrayList<SVGWord>();
			List<Element> elements = XMLUtil.getQueryElements(this, "*[@class='"+SVGWord.CLASS+"']");
			for (Element element : elements) {
				wordList.add((SVGWord)element);
			}
		}
		return wordList;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (SVGWord word : wordList) {
			if (i++ > 0) {
				sb.append(" ");
			}
			sb.append(word.getValue());
		}
		return sb.toString();
	}
	
	private void ensureWordList() {
		if (this.wordList == null) {
			wordList = new ArrayList<SVGWord>();
		}
	}

	public SVGWord getLastWord() {
		getOrCreateWordList();
		return wordList.size() == 0 ? null : wordList.get(wordList.size() - 1);
	}

	public void addTrailingWord(SVGWord svgWord) {
		ensureWordList();
		wordList.add(svgWord);
	}

	public boolean canGeometricallyAdd(SVGWord svgWord) {
		SVGWord lastWord = wordList.get(wordList.size() - 1);
		double gap = svgWord.gapFollowing(lastWord);
		LOG.trace("GAP "+gap+"; "+lastWord.getChildRectBoundingBox()+"; "+svgWord.getChildRectBoundingBox());
		return gap < interWordGap;
	}

}
