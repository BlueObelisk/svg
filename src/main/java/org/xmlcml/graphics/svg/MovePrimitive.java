package org.xmlcml.graphics.svg;

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
	
	public String toString() {
		return TAG + formatCoords(coordArray.get(0));
	}
	
	

}
