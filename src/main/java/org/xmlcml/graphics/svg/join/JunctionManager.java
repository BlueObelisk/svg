package org.xmlcml.graphics.svg.join;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;

public class JunctionManager {

	private final static Logger LOG = Logger.getLogger(JunctionManager.class);
	
	private List<Junction> junctionList;

	public JunctionManager() {
		
	}

	public List<Junction> makeJunctionList(List<Joinable> joinableList) {
		ensureJunctionList();
		for (int i  = 0; i < joinableList.size() - 1; i++) {
			Joinable joinablei = joinableList.get(i);
			JoinPointList jpli = joinablei.getJoinPointList();
			for (int j  = i + 1; j < joinableList.size(); j++) {
				Joinable joinablej = joinableList.get(j);
				JoinPoint commonPoint = joinablei.getIntersectionPoint(joinablej);
				if (commonPoint != null) {
					LOG.debug("made junction: "+commonPoint.getPriority());
					Junction junction = new Junction(joinablei, joinablej, commonPoint);
					junctionList.add(junction);
				}
			}
		}
		return junctionList;
	}

//	private void getCommonPoint(JoinPointList jpli, Joinable joinablej) {
//		JoinPointList jplj = joinablej.getJoinPointList();
//		List<JoinPoint> commonJoinPoints = jpli.getCommonJoinPointList(jplj);
//	}

	private void ensureJunctionList() {
		if (junctionList == null) {
			junctionList = new ArrayList<Junction>();
		}
	}

	public List<Junction> mergeJunctions() {
		for (int i = junctionList.size() - 1; i > 0; i--) {
			Junction labile = junctionList.get(i);
			for (int j = 0; j < i; j++) {
				Junction fixed = junctionList.get(j);
				if (fixed.containsCommonPoints(labile)) {
					labile.transferDetailsTo(fixed);
					LOG.debug("removing "+junctionList.get(i));
					junctionList.remove(i);
				}
			}
		}
		return junctionList;
	}
}
