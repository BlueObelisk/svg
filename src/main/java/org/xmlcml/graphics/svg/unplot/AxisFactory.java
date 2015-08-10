package org.xmlcml.graphics.svg.unplot;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.text.SVGPhrase;
import org.xmlcml.graphics.svg.text.SVGWord;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/** creates axes from ticks, scales, titles.
 * 
 * @author pm286
 *
 */
public class AxisFactory {

	private static final String MINOR_CHAR = "i";
	private static final String MAJOR_CHAR = "I";
	private static final Logger LOG = Logger.getLogger(AxisFactory.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	
	private List<SVGPath> pathList;
	private List<SVGText> textList;
	private LineDirection direction;
	private List<SVGLine> tickLines;
	private List<SVGLine> lineList;
	private List<SVGLine> horizontalLines;
	private List<SVGLine> verticalLines;
	private AnnotatedAxis axis;
	private SVGPhrase scalesPhrase;

	public AxisFactory() {
		direction = null;
	}
	
	private void processPaths() {
		turnPathsIntoAxisComponents();
		if (direction != null) {
			createAxisAndRanges();
		}
	}

	public void setTextsAndPaths(List<SVGText> textList, List<SVGPath> pathList) {
		axis = new AnnotatedAxis();
		this.pathList = pathList;
		this.textList = textList;
		processPaths();
		processTexts();
	}

	private void processTexts() {
		// assume sorted
		processScales();
		processTitle();
	}

	private void processTitle() {
		LOG.debug("title NYI");
	}

	private SVGPhrase processScales() {
		scalesPhrase = null;
		if (textList.size() > 0) {
			if (LineDirection.HORIZONTAL.equals(direction)) {
				scalesPhrase = SVGPhrase.createPhraseFromCharacters(textList);
				axis.setTickValues(scalesPhrase.getNumericValues());
			} else {
				List<SVGWord> wordList = new ArrayList<SVGWord>();
				SVGWord word = new SVGWord(textList.get(0));
				wordList.add(word);
				for (int i = 1; i < textList.size(); i++) {
					SVGText text = textList.get(i);
					if (word.canAppend(text)) {
						word.append(text);
					} else {
						word = new SVGWord(textList.get(i));
						wordList.add(word);
					}
				}
				double[] values = new double[wordList.size()];
				for (int i = 0; i < wordList.size(); i++) {
					SVGWord word0 = wordList.get(i);
					String ss = word0.getStringValue();
					LOG.trace("ss "+ss);
					values[i] = new Double(ss);
				}
				RealArray realArray = new RealArray(values);
				axis.setTickValues(realArray);
			}
		}
		return scalesPhrase;
	}


	private void createAxisAndRanges() {
		RealRange range = axis.getSingleLine().getBoundingBox().getXRange();
		// assume sorted - we'll need to add sort later
		Real2Range tick2Range = SVGLine.getReal2Range(tickLines);
		RealRange tickRange = LineDirection.HORIZONTAL.equals(direction) ? tick2Range.getXRange() : tick2Range.getYRange();
		if (RealRange.isEqual(range, tickRange, AnnotatedAxis.EPS)) {
			axis.setRange(range);
		}
	}

	private void turnPathsIntoAxisComponents() {
		lineList = SVGPath.createLinesFromPaths(pathList);
		horizontalLines = SVGLine.findHorizontalOrVerticalLines(lineList, LineDirection.HORIZONTAL, AnnotatedAxis.EPS);
		verticalLines = SVGLine.findHorizontalOrVerticalLines(lineList, LineDirection.VERTICAL, AnnotatedAxis.EPS);
		// we'll assume one horizontal and many vertical lines 
		if (horizontalLines.size() == 1 && verticalLines.size() > 1) {
			createMainAndTickLines(LineDirection.HORIZONTAL, horizontalLines.get(0), verticalLines);
		} else if (verticalLines.size() == 1 && horizontalLines.size() > 1) {
			createMainAndTickLines(LineDirection.VERTICAL, verticalLines.get(0), horizontalLines);
		} else {
			LOG.debug("Cannot extract axis");
		}
	}

	private void createMainAndTickLines(LineDirection direction, SVGLine singleLine, List<SVGLine> tickLines) {
		this.direction = direction;
		axis.setSingleLine(singleLine);
		axis.setDirection(direction);
		this.tickLines = tickLines;
		Multiset<Double> tickLengths = HashMultiset.create();
		for (SVGLine tickLine : tickLines) {
			tickLengths.add((Double)Real.normalize(tickLine.getLength(), 2));
		}
		LOG.trace(tickLengths);
		if (tickLengths.elementSet().size() == 1) {
			axis.setMajorTickLength(tickLengths.elementSet().iterator().next());
		} else if (tickLengths.elementSet().size() == 2) {
			analyzeMajorAndMinorTickLengths(tickLengths);
			getTickLinesAndSignature();
		} else {
			LOG.error("cannot process ticks: "+tickLengths);
		}
	}

	private void getTickLinesAndSignature() {
		StringBuilder sb = new StringBuilder();
		List<SVGLine> majorTickLines = new ArrayList<SVGLine>();
		List<SVGLine> minorTickLines = new ArrayList<SVGLine>();
		for (SVGLine tickLine : tickLines) {
			Double l = tickLine.getLength();
			String ss = null;
			if (Real.isEqual(l,  axis.getMajorTickLength(), AnnotatedAxis.EPS)) {
				ss = MAJOR_CHAR;
				majorTickLines.add(tickLine);
			} else {
				ss = MINOR_CHAR;
				minorTickLines.add(tickLine);
			}
			sb.append(ss);
		}
		axis.setMajorTickLines(majorTickLines);
		axis.setMinorTickLines(minorTickLines);
		axis.setMajorTicksPixels(getPixelCoordinatesForTickLines(majorTickLines));
		axis.setMinorTicksPixels(getPixelCoordinatesForTickLines(minorTickLines));
		axis.setTickSignature(sb.toString());
	}

	private RealArray getPixelCoordinatesForTickLines(List<SVGLine> tickLines) {
		double[] coord = new double[tickLines.size()];
		for (int i = 0; i < tickLines.size(); i++) {
			SVGLine tickLine = tickLines.get(i);
			Real2 xy = tickLine.getXY(0);
			coord[i] = (LineDirection.HORIZONTAL.equals(direction)) ? xy.getX() : xy.getY();
		}
		RealArray array = new RealArray(coord);
		return array;
	}

	private void analyzeMajorAndMinorTickLengths(Multiset<Double> tickLengths) {
		Double majorTickLength = null;
		Double minorTickLength = null;
		for (Double d : tickLengths.elementSet()) {
			if (majorTickLength == null) {
				majorTickLength = d;
			} else {
				if (d < majorTickLength) {
					minorTickLength = d;
				} else {
					minorTickLength = majorTickLength;
					majorTickLength = d;
				}
			}
		}
		axis.setMajorTickLength(majorTickLength);
		axis.setMinorTickLength(minorTickLength);
	}

	public AnnotatedAxis getAxis() {
		axis.mapTicksToTickValues();
		return axis;
	}

}
