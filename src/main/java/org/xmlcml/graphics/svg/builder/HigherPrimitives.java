package org.xmlcml.graphics.svg.builder;

import java.util.ArrayList;
import java.util.List;

import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;

/** holds primitives that are being built.
 * 
 * @author pm286
 *
 */
public class HigherPrimitives {

	private List<SVGLine> singleLineList;
	private List<TramLine> tramLineList;
	private List<Junction> mergedJunctionList;
	private List<Joinable> joinableList;
	private List<Junction> rawJunctionList;

	
	public void addSingleLines(List<SVGLine> lineList) {
		if (lineList != null) {
			ensureSingleLineList();
			singleLineList.addAll(lineList);
		}
	}

	private void ensureSingleLineList() {
		if (singleLineList == null) {
			this.singleLineList = new ArrayList<SVGLine>();
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

	public List<SVGLine> getSingleLineList() {
		return singleLineList;
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
