package org.xmlcml.graphics.svg;

import org.xmlcml.euclid.Real2;

public class MovePrimitive extends SVGPathPrimitive {

	public final static String TAG = "M";

	public MovePrimitive(Real2 real2) {
		this.coords = real2;
	}

	public String getTag() {
		return TAG;
	}
	
	public String toString() {
		return TAG + formatCoords(coords);
	}
	
	

}
