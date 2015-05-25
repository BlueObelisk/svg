package org.xmlcml.graphics.svg.text;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
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
public class SVGWordLine extends SVGG {

	
	private static final Logger LOG = Logger.getLogger(SVGWordLine.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	public static final String CLASS = "line";
	private List<SVGPhrase> phraseList;
	private List<SVGWord> wordList;
	
	public SVGWordLine() {
		super();
		this.setClassName(CLASS);
	}

	public List<SVGPhrase> makePhrasesFromWords() {
		phraseList = new ArrayList<SVGPhrase>();
		getOrCreateSVGWordList();
		checkWordsAreSorted();
		if (wordList.size() > 0) {
			for (int i = 0; i < wordList.size(); i++) {
				addWordToPhraseList(wordList.get(i));
			}
		}
		return phraseList;
	}
	
	private void checkWordsAreSorted() {
		for (int i = 1; i < wordList.size(); i++) {
			if (!isOrdered(wordList.get(i - 1), wordList.get(i))) {
				throw new RuntimeException ("Unordered wordlist from Tesseract");
			}
		}
	}

	private void addWordToPhraseList(SVGWord svgWord) {
		ensurePhraseList();
		if (phraseList.size() == 0) {
			createNewPhraseAndAddWord(svgWord);
		} else {
			SVGPhrase lastPhrase = phraseList.get(phraseList.size() - 1);
			if (lastPhrase.canGeometricallyAdd(svgWord)) {
				lastPhrase.addTrailingWord(svgWord);
			} else {
				createNewPhraseAndAddWord(svgWord);
			}
		}
	}

	private void createNewPhraseAndAddWord(SVGWord svgWord) {
		SVGPhrase phrase = new SVGPhrase();
		phraseList.add(phrase);
		phrase.addTrailingWord(svgWord);
	}

	private void ensurePhraseList() {
		if (phraseList == null) {
			phraseList = new ArrayList<SVGPhrase>();
		}
	}

	private boolean isOrdered(SVGWord svgWord0, SVGWord svgWord1) {
		Real2 xy0 = svgWord0.getChildRectBoundingBox().getCorners()[0];
		Real2 xy1 = svgWord1.getChildRectBoundingBox().getCorners()[0];
		return xy0.getX() < xy1.getX();
	}

	public List<SVGPhrase> getOrCreateSVGPhraseList() {
		if (phraseList == null) {
			List<Element> elements = XMLUtil.getQueryElements(this, "*[@class='"+SVGPhrase.CLASS+"']");
			phraseList = new ArrayList<SVGPhrase>();
			for (Element element : elements) {
				phraseList.add((SVGPhrase) element);
			}
		}
		return phraseList;
	}
	
	

	public List<SVGWord> getOrCreateSVGWordList() {
		if (wordList == null) {
			List<Element> elements = XMLUtil.getQueryElements(this, ".//*[@class='"+SVGWord.CLASS+"']");
			wordList = new ArrayList<SVGWord>();
			for (Element element : elements) {
				wordList.add((SVGWord) element);
			}
		}
		return wordList;
	}

	/** returns value if the is exactly one Phrase.
	 * 
	 * @return
	 */
	public String getSinglePhraseValue() {
		return (phraseList == null || phraseList.size() != 1) ? null : phraseList.get(0).toString();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (SVGPhrase phrase : phraseList) {
			sb.append("[["+phrase.toString()+"]]");
		}
		return sb.toString();
	}


}
