package org.xmlcml.graphics.svg.objects;

import java.util.List;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;

public class SVGPlot extends SVGDiagram {
	
	
	static final Logger LOG = Logger.getLogger(SVGPlot.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	private SVGRect mainRect;
	private List<SVGLine> horizontalList;
	private List<SVGLine> verticalList;

	public SVGPlot(SVGElement diagram) {
		this.rawDiagram = diagram;
	}

	public void createPlot() {
		createPathsTextAndShapes();
		this.createAxisBox(eps);
		
	}

	// FIXME
	private void createAxisBox(double delta) {
		SVGLine.normalizeAndMergeAxialLines(lineList, delta);
		horizontalList = SVGLine.extractAndRemoveHorizontalVerticalLines(
				lineList, eps, LineDirection.HORIZONTAL);
		verticalList = SVGLine.extractAndRemoveHorizontalVerticalLines(
				lineList, eps, LineDirection.VERTICAL);
		if (rectList.size() == 1) {
			mainRect = rectList.get(0);
//			addTickMarks(mainRect);
		}
	}

//	private List<SVGAxis> addTickMarks(SVGRect rect, List<SVGLine> horizontalList, List<SVGLine> verticalList) {
//		SVGAxis leftAxis = addTickMarks(rect, horizontalList, );
//		addTickMarks(rect, verticalList);
//	}

}
