package org.xmlcml.graphics.svg.builder;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.graphics.svg.SVGLine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TramLineManager {

	private static Logger LOG = Logger.getLogger(TramLineManager.class);
	
	private final static Double EPS = 0.000001; // rounding errors
	private List<TramLine> tramLineList;
	private Angle angleEps = new Angle(0.3, Units.RADIANS);
	private Double maxTramLineSeparationFactor = 0.35; // this is tricky for very short tramlines
	private Double minTramLineSeparationFactor = 0.1;
	private Double minRelativeLineLength = 0.5;
	
	private Set<SVGLine> usedLineSet;

	public TramLineManager() {
		
	}
	
	public void add(TramLine line) {
		ensureTramLineListAndUsedLineSet();
		tramLineList.add(line);
	}

	private void ensureTramLineListAndUsedLineSet() {
		if (tramLineList == null) {
			tramLineList = new ArrayList<TramLine>();
	 		if (usedLineSet == null) {
		 		usedLineSet = new HashSet<SVGLine>();
		 	}

		}
	}
	
	public TramLine createTramLine(SVGLine linei, SVGLine linej) {
		TramLine tramLine = null;
		Double length1 = linei.getLength();
		Double length2 = linej.getLength();
		Double longer = (length1 > length2 ? length1 : length2);
		Double shorter = (length1 > length2 ? length2 : length1);
		if (linei == null || linej == null) return tramLine;
		if (shorter / longer > minRelativeLineLength) {
			if (linei.isParallelOrAntiParallelTo(linej, angleEps)) {
				Double dist = linei.calculateUnsignedDistanceBetweenLines(linej, angleEps);
				if (dist < longer * maxTramLineSeparationFactor && dist > longer * minTramLineSeparationFactor) {
					if (linei.overlapsWithLine(linej, EPS) || linej.overlapsWithLine(linei, EPS)) {
						tramLine = new TramLine(linei, linej);
					}
				}
			}
		}
		return tramLine;
	}


	public List<TramLine> createTramLineList(List<SVGLine> lineList) {
		if (tramLineList == null) {
			ensureTramLineListAndUsedLineSet();
			for (int i = 0; i < lineList.size() - 1; i++) {
				SVGLine linei = lineList.get(i);
				for (int j = i + 1; j < lineList.size(); j++) {
					SVGLine linej = lineList.get(j);
					TramLine tramLine = createTramLine(linei, linej);
					if (tramLine != null) {
						LOG.trace("tram "+linei.getId()+" "+linej.getId());
	 					tramLine.setId("tram."+linei.getId()+"."+linej.getId());
	 					tramLineList.add(tramLine);
	 					usedLineSet.add(linei);
	 					usedLineSet.add(linej);
					}
				}
			}
		}
		return tramLineList;
	}
	
	public Angle getAngleEps() {
		return angleEps;
	}

	public void setAngleEps(Angle angleEps) {
		this.angleEps = angleEps;
	}

	public Double getMaxTramLineSeparationFactor() {
		return maxTramLineSeparationFactor;
	}

	public void setMaxTramLineSeparationFactor(Double maxTramLineSeparationFactor) {
		this.maxTramLineSeparationFactor = maxTramLineSeparationFactor;
	}
	
	public Double getMinTramLineSeparationFactor() {
		return minTramLineSeparationFactor;
	}

	public void setMinTramLineSeparationFactor(Double minTramLineSeparationFactor) {
		this.minTramLineSeparationFactor = minTramLineSeparationFactor;
	}

	public Double getMinRelativeLineLength() {
		return minRelativeLineLength;
	}

	public void setMinRelativeLineLength(Double minRelativeLineLength) {
		this.minRelativeLineLength = minRelativeLineLength;
	}

	public List<TramLine> getTramLineList() {
		return tramLineList;
	}

	public List<SVGLine> removeUsedTramLinePrimitives(List<SVGLine> lineList) {
 		for (int i = lineList.size() - 1; i >= 0; i--) {
 			if (usedLineSet.contains(lineList.get(i))) {
 				lineList.remove(i);
 			}
 		}
 		return lineList;
 	}
	
}
