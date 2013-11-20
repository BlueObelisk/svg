package org.xmlcml.graphics.svg.builder;

import org.xmlcml.graphics.svg.SVGLine;

import java.util.ArrayList;
import java.util.List;

/** holds primitives that are being built.
 * 
 * @author pm286
 *
 */
public class HigherPrimitives {

	private List<SVGLine> lineList;
	private List<TramLine> tramLineList;
	private List<Junction> mergedJunctionList;
	private List<Joinable> joinableList;
	private List<Junction> rawJunctionList;

	private void ensureLineList() {
		if (lineList == null) {
			lineList = new ArrayList<SVGLine>();
		}
	}

	public void addSingleLines(List<SVGLine> lineList) {
		ensureLineList();
		if (lineList != null) {
			this.lineList.addAll(lineList);
		}
	}

	public List<TramLine> getTramLineList() {
		return tramLineList;
	}

	public List<Junction> getMergedJunctionList() {
		return mergedJunctionList;
	}

	public List<Joinable> getJoinableList() {
		return joinableList;
	}

	public List<SVGLine> getLineList() {
		return lineList;
	}

	public List<Junction> getRawJunctionList() {
		return rawJunctionList;
	}

	public void setRawJunctionList(List<Junction> junctionList) {
		this.rawJunctionList = junctionList;
	}

	public void addJoinableList(List<Joinable> joinableList) {
		ensureJoinableList();
		this.joinableList.addAll(joinableList);
	}

	private void ensureJoinableList() {
		if (joinableList == null) {
			joinableList = new ArrayList<Joinable>();
		}
	}
	
	public void setTramLineList(List<TramLine> tramLineList) {
		this.tramLineList = tramLineList;
	}
	
	public void setMergedJunctionList(List<Junction> mergedJunctionList) {
		this.mergedJunctionList = mergedJunctionList;
	}

}
