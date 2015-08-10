package org.xmlcml.graphics.svg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class SVGLineList extends SVGG implements Iterable<SVGLine> {
	
	private static Logger LOG = Logger.getLogger(SVGLineList.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	private ArrayList<SVGLine> lines;

	public SVGLineList() {
		super();
	}
	
	public SVGLineList(List<SVGLine> lines) {
		this.lines = new ArrayList<SVGLine>(lines);
	}

	public List<SVGLine> getLineList() {
		ensureLines();
		return lines;
	}

	public Iterator<SVGLine> iterator() {
		ensureLines();
		return lines.iterator();
	}

	private void ensureLines() {
		if (lines == null) {
			lines = new ArrayList<SVGLine>();
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		ensureLines();
		sb.append(lines.toString());
		return sb.toString();
	}

	public SVGLine get(int i) {
		ensureLines();
		return (i < 0 || i >= lines.size()) ? null : lines.get(i);
	}

	public int size() {
		ensureLines();
		return lines.size();
	}

	public SVGLine remove(int i) {
		ensureLines();
		if (get(i) != null) {
			return lines.remove(i);
		}
		return null;
	}

}
