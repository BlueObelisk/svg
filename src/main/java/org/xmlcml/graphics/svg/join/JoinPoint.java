package org.xmlcml.graphics.svg.join;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;

/** point where a graphic primitive may overlap or join to another.
 * 
 * @author pm286
 *
 */
public class JoinPoint {

	private final static Logger LOG = Logger.getLogger(JoinPoint.class);
	
	private Joinable joinable;
	private Real2 point;
	private Double radius = 1.5;
	
	public JoinPoint(Joinable joinable, Real2 point) {
		this.point = point;
		this.joinable = joinable;
	}

	public double getDistanceTo(JoinPoint otherPoint) {
		return point.getDistance(otherPoint.getPoint());
	}

	private Real2 getPoint() {
		return point;
	}

	public double getRadius() {
		return radius;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getPriority() {
		return joinable.getPriority();
	}

	public String getId() {
		return joinable.getId();
	}
}
