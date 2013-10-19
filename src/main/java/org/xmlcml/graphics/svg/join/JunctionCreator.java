package org.xmlcml.graphics.svg.join;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGText;

public class JunctionCreator {

	private final static Logger LOG = Logger.getLogger(JunctionCreator.class);
	
	public static final Angle DEFAULT_MAX_ANGLE = new Angle(0.12, Units.RADIANS);
	public static final Double DEFAULT_MAX_WIDTH = 2.0;

	private List<Junction> rawJunctionList;
	private List<Junction> mergedJunctionList;
	private List<SVGPath> pathList;
	private List<SVGLine> lineList;
	private List<TramLine> tramLineList;
	private List<SVGText> textList;
	private double maxWidth = DEFAULT_MAX_WIDTH;
	private Angle maxAngle = DEFAULT_MAX_ANGLE;

	public List<SVGLine> createLinesFromOutlines(SVGElement svgRoot) {
		pathList = SVGPath.extractPaths(svgRoot);
		SVGG g = new SVGG();
		int i = 0;
		lineList = new ArrayList<SVGLine>();
		LOG.trace("P "+pathList.size()+" "+maxAngle+" "+maxWidth);
		for (SVGPath path : pathList) {
			SVGPath newPath = path.replaceAllUTurnsByButt(maxAngle);
			if (newPath != null) {
				SVGLine line = newPath.createLineFromMLLLL(maxAngle, maxWidth);
				LOG.trace("LL "+line);
				if (line != null) {
					String id = path.getId();
					if (id == null) {
						id = this.createId(i);
						line.setId(id);
					}
					g.appendChild(line);
					lineList.add(line);
				} else {
					g.appendChild(newPath.copy());
				}
			} else {
				g.appendChild(path.copy());
			}
			i++;
		}
		LOG.trace("****************************************************");
		return lineList;
	}

	public List<SVGText> createTextListAndAddIds(SVGElement svg) {
		textList = SVGText.extractSelfAndDescendantTexts(svg);
		for (int i = 0; i < textList.size(); i++){
			SVGText text = textList.get(i);
			String id = text.getId();
			if (id == null) {
				text.setId("text."+i);
			}
		}
		return textList;
	}

	protected List<SVGText> getTextList() {
		return textList;
	}

	public List<TramLine> makeTramLines(SVGElement svgRoot) {
		lineList = createLinesFromOutlines(svgRoot);
		TramLineManager tramLineManager = new TramLineManager();
		tramLineList = tramLineManager.makeTramLineList(lineList);
		return tramLineList;
	}

	protected List<SVGLine> getLineList() {
		return lineList;
	}

	protected List<TramLine> getTramLineList() {
		enableTramListList();
		return tramLineList;
	}

	private void enableTramListList() {
		if (this.tramLineList == null) {
			tramLineList = new ArrayList<TramLine>();
		}
	}

	protected List<SVGPath> getPathList() {
		return pathList;
	}

	public List<Junction> getRawJunctionList() {
		return rawJunctionList;
	}

	public List<Junction> getMergedJunctionList() {
		return mergedJunctionList;
	}

	private List<Junction> mergeJunctions(List<SVGText> textList, List<SVGLine> lineList) {
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.makeTramLineList(lineList);
		lineList = tramLineManager.removeUsedTramLinePrimitives(lineList);
		
		List<Joinable> joinableList = JoinManager.makeJoinableList(lineList);
		joinableList.addAll(tramLineList);
		for (SVGText svgText : textList) {
			joinableList.add(new JoinableText(svgText));
		}
		
		rawJunctionList = this.makeRawJunctionList(joinableList);
		mergedJunctionList = this.mergeJunctions();
		return mergedJunctionList;
	}

	public List<Junction> mergeJunctionsFromExplicitLines(SVGElement svgElement) {
		List<SVGText> textList = createTextListAndAddIds(svgElement);
		List<SVGLine> lineList = SVGLine.extractSelfAndDescendantLines(svgElement);
		mergeJunctions(textList, lineList);
		mergedJunctionList = rawJunctionList;
		return mergedJunctionList;
	}

	public void mergeJunctionsFromOutlines(SVGElement svgRoot) {
		List<SVGText> textList = createTextListAndAddIds(svgRoot);
		List<SVGLine> lineList = createLinesFromOutlines(svgRoot);
		mergeJunctions(textList, lineList);
	}
	
	public void setMaxWidth(double maxWidth) {
		this.maxWidth = maxWidth;
	}

	public void setMaxAngle(Angle maxAngle) {
		this.maxAngle = maxAngle;
	}

	public String createId(int i) {
		return "line."+i;
	}

	public List<Junction> makeRawJunctionList(List<Joinable> joinableList) {
		ensureJunctionList();
		for (int i = 0; i < joinableList.size() - 1; i++) {
			Joinable joinablei = joinableList.get(i);
			for (int j = i + 1; j < joinableList.size(); j++) {
				Joinable joinablej = joinableList.get(j);
				JoinPoint commonPoint = joinablei.getIntersectionPoint(joinablej);
				if (commonPoint != null) {
					Junction junction = new Junction(joinablei, joinablej, commonPoint);
					rawJunctionList.add(junction);
				}
			}
		}
		return rawJunctionList;
	}

	void ensureJunctionList() {
		if (rawJunctionList == null) {
			rawJunctionList = new ArrayList<Junction>();
		}
	}

	public List<Junction> mergeJunctions() {
		for (int i = rawJunctionList.size() - 1; i > 0; i--) {
			Junction labile = rawJunctionList.get(i);
			for (int j = 0; j < i; j++) {
				Junction fixed = rawJunctionList.get(j);
				if (fixed.containsCommonPoints(labile)) {
					labile.transferDetailsTo(fixed);
					rawJunctionList.remove(i);
					break;
				}
			}
		}
		return rawJunctionList;
	}

}
