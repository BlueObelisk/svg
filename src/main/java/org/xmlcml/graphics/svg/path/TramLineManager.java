package org.xmlcml.graphics.svg.path;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Line2;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.graphics.svg.SVGLine;

public class TramLineManager {

	private static Logger LOG = Logger.getLogger(TramLineManager.class);
	
	private final static Double EPS = 0.000001; // rounding errors
	private List<TramLine> tramLineList;
	private Angle angleEps = new Angle(0.2, Units.RADIANS);
	private Double tramLineSeparationFactor = 0.2; // this is tricky for very short tramlines
	
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
	}
	
	public TramLine createTramLine(SVGLine linei, SVGLine linej) {
		TramLine tramLine = null;
		if (linei == null || linej == null) return tramLine; 
		if (linei.isParallelOrAntiParallelTo(linej, angleEps)) {
			Double dist = linei.calculateUnsignedDistanceBetweenLines(linej, angleEps);
			if (dist < linei.getLength() * tramLineSeparationFactor ) {
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
			for (int j = i + 1; j < lineList.size(); j++) {
				SVGLine linej = lineList.get(j);
				TramLine tramLine = createTramLine(linei, linej);
				if (tramLine != null) {
					LOG.debug("tram "+i+" "+j );
					tramLineList.add(tramLine);
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


	
}
