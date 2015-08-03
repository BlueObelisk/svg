package org.xmlcml.graphics.svg.text;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGText;
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

	public Real2Range getBoundingBox() {
		Real2Range bbox = null;
		for (SVGWord word : this.getOrCreateWordList()) {
			if (bbox == null) {
				bbox = word.getBoundingBox();
			} else {
				bbox = bbox.plus(word.getBoundingBox());
			}
		}
		return bbox;
	}

	public static SVGPhrase createPhraseFromCharacters(List<SVGText> textList) {
		SVGPhrase phrase = null;
		LOG.trace("phrase: "+textList);
		if (textList != null && textList.size() > 0) {
			phrase = new SVGPhrase();
			SVGWord word = new SVGWord(textList.get(0));
			phrase.addTrailingWord(word);
			for (int i = 1; i < textList.size(); i++) {
				SVGText text = textList.get(i); 
				if (word.canAppend(text)) {
					word.append(text);
				} else {
					word = new SVGWord(text);
					phrase.addTrailingWord(word);
				}
			}
		}
		return phrase;
	}

	public double getInterWordGap() {
		return interWordGap;
	}

	public void setInterWordGap(double interWordGap) {
		this.interWordGap = interWordGap;
	}

	public List<String> getOrCreateStringList() {
		getOrCreateWordList();
		List<String> stringList = new ArrayList<String>();
		for (SVGWord word : wordList) {
			stringList.add(word.getStringValue());
		}
		return stringList;
	}
	
	/** returns an array of all the words as numbers.
	 * useful for scales, graphs, lists, tables, etc.
	 * fails if any word is not numeric
	 * 
	 * @return null if cannot parse values
	 */
	public RealArray getNumericValues() {
		List<String> stringList = getOrCreateStringList();
    	RealArray values = RealArray.createRealArray(stringList);
    	return values;
	}
}
