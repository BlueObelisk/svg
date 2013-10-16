package org.xmlcml.graphics.svg.join;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.graphics.svg.SVGLine;

public class TramLineManager {

	private static Logger LOG = Logger.getLogger(TramLineManager.class);
	
	private final static Double EPS = 0.000001; // rounding errors
	private List<TramLine> tramLineList;
	private Angle angleEps = new Angle(0.2, Units.RADIANS);
	private Double tramLineSeparationFactor = 0.2; // this is tricky for very short tramlines

	private Set<SVGLine> usedLineSet;
	
	public TramLineManager() {
		
	}
	
	public void add(TramLine line) {
		ensureTramLineList();
		tramLineList.add(line);
	}

	private void ensureTramLineList() {
		if (tramLineList == null) {
			tramLineList = new ArrayList<TramLine>();
		}
		if (usedLineSet == null) {
			usedLineSet = new HashSet<SVGLine>();
		}
	}
	
	public TramLine createTramLine(SVGLine linei, SVGLine linej) {
		TramLine tramLine = null;
		if (linei == null || linej == null) return tramLine; 
		if (linei.isParallelOrAntiParallelTo(linej, angleEps)) {
			Double dist = linei.calculateUnsignedDistanceBetweenLines(linej, angleEps);
			Double maxDist = linei.getLength() * tramLineSeparationFactor;
			LOG.trace(linei.getId()+" "+linej.getId()+" "+maxDist+" "+dist);
			if (dist < maxDist) {
				if (linei.overlapsWithLine(linej, EPS) || linej.overlapsWithLine(linei, EPS)) {
					tramLine = new TramLine(linei, linej);
				}
			}
		}
		return tramLine;
	}


	public List<TramLine> makeTramLineList(List<SVGLine> lineList) {
		ensureTramLineList();
		for (int i = 0; i < lineList.size() - 1; i++) {
			SVGLine linei = lineList.get(i);
			linei.format(3);
			LOG.trace("line "+linei.toXML());
			for (int j = i + 1; j < lineList.size(); j++) {
				SVGLine linej = lineList.get(j);
				LOG.trace("line: "+linei.getId()+" "+linej.getId());
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
		return tramLineList;
	}
	
	public Angle getAngleEps() {
		return angleEps;
	}

	public void setAngleEps(Angle angleEps) {
		this.angleEps = angleEps;
	}

	public Double getTramLineSeparationFactor() {
		return tramLineSeparationFactor;
	}

	public void setTramLineSeparationFactor(Double tramLineSeparationFactor) {
		this.tramLineSeparationFactor = tramLineSeparationFactor;
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
