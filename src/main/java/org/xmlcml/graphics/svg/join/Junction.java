package org.xmlcml.graphics.svg.join;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGText;

/** a junction between two or more objects.

 * <p>Extends SVGCircle so we can draw it.</p>
 * 
 * @author pm286
 *
 */
public class Junction extends SVGCircle {

	private final static Logger LOG = Logger.getLogger(Junction.class);
	
	private List<Joinable> joinableList;
	private Real2 coordinates;
	private SVGText svgText;
	private JoinPoint joinPoint;
	
	private static double EPS = 2.0; // initial guess
	
	
	public Junction() {
	}

	public Junction(Joinable joinablei, Joinable joinablej, JoinPoint joinPoint) {
		add(joinablei);
		add(joinablej);
		if (coordinates == null) {
			coordinates = joinablei.intersectionWith(joinablej);
		}
		this.joinPoint = joinPoint;
	}

	private void add(Joinable joinable) {
		ensureJoinableList();
		if (!joinableList.contains(joinable)) {
			joinableList.add(joinable);
//			point = joinable.getPoint();
		}
	}

	private void ensureJoinableList() {
		if (joinableList == null) {
			joinableList = new ArrayList<Joinable>();
		}
	}
	
	public boolean containsCommonPoints(Junction labile) {
//		List<Joinable> labileList = labile.joinableList;
//		LOG.debug(this.joinPoint.getId()+" / "+labile.joinPoint.getId());
		double dist = this.joinPoint.getDistanceTo(labile.joinPoint);
		if (dist < EPS) {
			LOG.debug(dist);
			return true;
		}
		return false;
	}

	public void transferDetailsTo(Junction fixed) {
		for (Joinable joinable : joinableList) {
			if (fixed.joinableList.contains(joinable)) {
			} else { 
				fixed.joinableList.add(joinable);
			}
			fixed.coordinates = fixed.coordinates.getMidPoint(this.coordinates);
		}
		LOG.error("NYI");
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("J>>");
		for (Joinable joinable : joinableList) {
			sb.append(" ["+joinable.getSVGElement().getClass().getSimpleName()+": "+joinable.getId()+"] ");
		}
		return sb.toString();
	}

}
