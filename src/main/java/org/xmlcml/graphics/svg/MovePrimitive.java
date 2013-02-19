package org.xmlcml.graphics.svg;

import java.awt.geom.GeneralPath;

import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;

public class MovePrimitive extends SVGPathPrimitive {

	public final static String TAG = "M";

	public MovePrimitive(Real2 real2) {
		this.coordArray = new Real2Array();
		coordArray.add(real2);
	}

	public String getTag() {
		return TAG;
	}
	
	@Override
	public void operateOn(GeneralPath path) {
		if (coordArray != null) {	
			Real2 coord = coordArray.elementAt(0);
			path.moveTo(coord.x, coord.y);
		}
	}

	public String toString() {
		return TAG + formatCoords(coordArray.get(0));
	}
	
	@Override
	/**
	 * @return null
	 */
	public Angle getAngle() {
		return null;
	}

}
