package org.xmlcml.graphics.svg.unplot;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;

public class AnnotatedAxis {

	private static final Logger LOG = Logger.getLogger(AnnotatedAxis.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	
	static double EPS = 0.01;
	private LineDirection direction;
	private RealRange range;
	private RealArray majorTicksPixels; // the position of the minor ticks
	private RealArray minorTicksPixels; // the position of the major ticks
	private RealArray tickValues; // the actual numbers in the scale
	private RealArray tickValuePositions; // the best estimate of the numbers positions
	private String tickSignature;
	

	protected AnnotatedAxis() {
		this.direction = null;
	}
	
	public AnnotatedAxis(LineDirection direction) {
		this.direction = direction;		
	}

	void setRange(RealRange range) {
		this.range = range;
	}

	public RealArray getMajorTicksPixels() {
		return majorTicksPixels;
	}

	public void setMajorTicksPixels(RealArray majorTicksPixels) {
		this.majorTicksPixels = majorTicksPixels;
	}

	public RealArray getMinorTicksPixels() {
		return minorTicksPixels;
	}

	public void setMinorTicksPixels(RealArray minorTicksPixels) {
		this.minorTicksPixels = minorTicksPixels;
	}

	public RealArray getTickValues() {
		return tickValues;
	}

	public void setTickValues(RealArray tickValues) {
		this.tickValues = tickValues;
	}

	public RealArray getTickValuePositions() {
		return tickValuePositions;
	}

	public void setTickValuePositions(RealArray tickValuePositions) {
		this.tickValuePositions = tickValuePositions;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("dir: "+direction+"; ");
		sb.append("range: "+range+"\n");
		sb.append("majorTicks: "+majorTicksPixels+"\n");
		sb.append("minorTicks: "+minorTicksPixels+"\n");
		sb.append("tickValues: "+tickValues+"\n");
		sb.append("tickValuePositions: "+tickValuePositions+"\n");
		return sb.toString();
	}

	public void setTickSignature(String string) {
		this.tickSignature = string;
	}
	
}
