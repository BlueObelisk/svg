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
	private SVGLine singleLine;
	private List<SVGLine> tickLines;
	private List<SVGLine> lineList;
	private List<SVGLine> horizontalLines;
	private List<SVGLine> verticalLines;
	private AnnotatedAxis axis;
	private SVGPhrase scalesPhrase;
	private RealArray scalesValues;
	private Double majorTickLength;
	private Double minorTickLength;
	private String tickSignature;

	public AxisFactory() {
		direction = null;
		singleLine = null;
		tickLines = null;
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
		LOG.debug("scales: "+scalesPhrase+"; "+scalesValues);
		processTitle();
	}

	private void processTitle() {
		LOG.debug("title NYI");
	}

	private SVGPhrase processScales() {
		scalesPhrase = null;
		if (textList.size() > 0) {
			scalesPhrase = SVGPhrase.createPhraseFromCharacters(textList);
			scalesValues = scalesPhrase.getNumericValues();
			axis.setTickValues(scalesValues);
		}
		return scalesPhrase;
	}


	private void createAxisAndRanges() {
		RealRange range = singleLine.getBoundingBox().getXRange();
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
		this.singleLine = singleLine;
		this.tickLines = tickLines;
		Multiset<Double> tickLengths = HashMultiset.create();
		for (SVGLine tickLine : tickLines) {
			tickLengths.add((Double)Real.normalize(tickLine.getLength(), 2));
		}
		LOG.debug(tickLengths);
		if (tickLengths.elementSet().size() == 1) {
			majorTickLength = tickLengths.elementSet().iterator().next();
		} else if (tickLengths.elementSet().size() == 2) {
			getMajorAndMinorTickLengths(tickLengths);
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
			if (Real.isEqual(l,  majorTickLength, AnnotatedAxis.EPS)) {
				ss = MAJOR_CHAR;
				majorTickLines.add(tickLine);
			} else {
				ss = MINOR_CHAR;
				minorTickLines.add(tickLine);
			}
			sb.append(ss);
		}
		axis.setMajorTicksPixels(getPixels(majorTickLines));
		axis.setMinorTicksPixels(getPixels(minorTickLines));
		axis.setTickSignature(sb.toString());
	}

	private RealArray getPixels(List<SVGLine> tickLines) {
		double[] coord = new double[tickLines.size()];
		for (int i = 0; i < tickLines.size(); i++) {
			SVGLine tickLine = tickLines.get(i);
			Real2 xy = tickLine.getXY(0);
			coord[i] = (LineDirection.HORIZONTAL.equals(direction)) ? xy.getX() : xy.getY();
		}
		RealArray array = new RealArray(coord);
		LOG.debug("A "+array+"; "+array.calculateDifferences());
		return array;
	}

	private void getMajorAndMinorTickLengths(Multiset<Double> tickLengths) {
		majorTickLength = null;
		minorTickLength = null;
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
	}

	public AnnotatedAxis getAxis() {
		return axis;
	}


}
