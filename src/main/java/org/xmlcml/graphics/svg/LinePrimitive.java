package org.xmlcml.graphics.svg;

import org.xmlcml.euclid.Real2;

public class LinePrimitive extends SVGPathPrimitive {

	public final static String TAG = "L";

	public LinePrimitive(Real2 real2) {
		this.coords = real2;
	}

	public String getTag() {
		return TAG;
	}
	
	public String toString() {
		return TAG + formatCoords(coords);
	}

}
