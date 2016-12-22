package org.xmlcml.graphics.svg.plot;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Vector2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGLine.LineDirection;

public class SVGErrorBar extends SVGG {

	private static Logger LOG = Logger.getLogger(SVGErrorBar.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public enum BarDirection {
		TOP("TOP", new Vector2(0, 1)),
		RIGHT("RIGHT", new Vector2(1, 0)),
		BOTTOM("BOTTOM", new Vector2(0, -1)),
		LEFT("LEFT", new Vector2(-1, 0));
		private final String label;
		private final Vector2 vector;
		private BarDirection(String label, Vector2 vector) {
			this.label = label;
			this.vector = vector;
		};
		public BarDirection getBarDirection(int serial) {
			for (int j = 0; j < values().length; j++) {
				if (j == serial) return values()[j];
			}
			return null;
		}
		public BarDirection getBarDirection(String label) {
			for (int j = 0; j < values().length; j++) {
				if (values()[j].label.equals(label)) return values()[j];
			}
			return null;
		}
		public Vector2 getVector(int serial) {
			BarDirection direction = getBarDirection(serial);
			return direction == null ? null : direction.vector;
		}
	}

	public final static double ERROR_EPS = 0.5; // pixels
	private SVGLine line;
	private BarDirection barDirection;
	
	SVGErrorBar() {
	}

	public SVGLine getLine() {
		return line;
	}

	public void setLine(SVGLine line) {
		this.line = line;
	}

	public BarDirection getBarDirection() {
		return barDirection;
	}

	public void setBarDirection(BarDirection barDirection) {
		this.barDirection = barDirection;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(""+barDirection+"; "+line);
		return sb.toString();
	}
}
