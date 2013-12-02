package org.xmlcml.graphics.svg.builder;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGText;

import java.util.ArrayList;
import java.util.List;

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

	/** gets the first and only SVG Joinable.
	 * 
	 * @return
	 */
	public SVGText getSvgText() {
		if (svgText == null) {
			ensureJoinableList();
			for (Joinable joinable : joinableList) {
				if (joinable instanceof JoinableText) {
					svgText = (SVGText) joinable.getSVGElement();
					break;
				}
			}
		}
		return svgText;
	}

	/** gets Id of the SVGText.
	 * 
	 */
	public String getId() {
//		SVGText svgText = getSvgText();
//		return svgText == null ? null: svgText.getId();
		return super.getId();
	}
	
	private void add(Joinable joinable) {
		ensureJoinableList();
		if (!joinableList.contains(joinable)) {
			joinableList.add(joinable);
		}
	}

	private void ensureJoinableList() {
		if (joinableList == null) {
			joinableList = new ArrayList<Joinable>();
		}
	}
	
	public boolean containsCommonPoints(Junction labile) {
		double dist = joinPoint.getDistanceTo(labile.joinPoint);
		if (dist < EPS) {
			LOG.trace(dist);
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
			if (fixed.coordinates != null) {
				fixed.coordinates = fixed.coordinates.getMidPoint(coordinates);
			}
		}
	}
	
	public List<Joinable> getJoinableList() {
		return joinableList;
	}
	
	public JoinPoint getJoinPoint() {
		return joinPoint;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("J: ");
		for (Joinable joinable : joinableList) {
			sb.append(" ["+joinable.getSVGElement().getClass().getSimpleName()+": "+joinable.getId()+"] ");
		}
		return sb.toString();
	}

	public Real2 getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Real2 coords) {
		this.coordinates = coords;
	}

	public String getSvgTextValue() {
		SVGText svgText = getSvgText();
		String name = (svgText == null ? "" : svgText.getValue());
		return name;
	}

}
