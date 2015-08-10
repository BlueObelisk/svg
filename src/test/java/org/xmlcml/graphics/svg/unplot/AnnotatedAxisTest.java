package org.xmlcml.graphics.svg.unplot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGText;
import org.xmlcml.graphics.svg.SVGUtil;

public class AnnotatedAxisTest {

	private static final Logger LOG = Logger.getLogger(AnnotatedAxisTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	@Test
	public void testHorizontalAxis() throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.FIGURE_DIR, "horizontalAxis.svg")));
		List<SVGPath> pathList = SVGPath.extractPaths(svgElement);
		List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svgElement);
		AxisFactory axisFactory = new AxisFactory();
		axisFactory.setTextsAndPaths(textList, pathList);
		AnnotatedAxis axis = axisFactory.getAxis();
		LOG.debug("axis: "+axis.toString());
	}
	
	@Test
	public void testVerticalAxis() throws FileNotFoundException {
		SVGElement svgElement = SVGUtil.parseToSVGElement(new FileInputStream(new File(Fixtures.FIGURE_DIR, "verticalAxis.svg")));
		List<SVGPath> pathList = SVGPath.extractPaths(svgElement);
		List<SVGText> textList = SVGText.extractSelfAndDescendantTexts(svgElement);
		AxisFactory axisFactory = new AxisFactory();
		axisFactory.setTextsAndPaths(textList, pathList);
		AnnotatedAxis axis = axisFactory.getAxis();
		LOG.debug("axis: "+axis.toString());
	}
}
