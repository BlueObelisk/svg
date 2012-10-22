package org.xmlcml.graphics.svg;

import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;

public class CubicPrimitive extends SVGPathPrimitive {

	public final static String TAG = "C";

	public CubicPrimitive(Real2Array coordArray) {
		if (coordArray == null || coordArray.size() != 3) {
			throw new RuntimeException("Bad coordArray: "+coordArray);
		}
		this.coordArray = coordArray;
	}

	public String getTag() {
		return TAG;
	}
	
	public String toString() {
		String s = TAG;
		for (int i = 0; i < coordArray.size(); i++) {
			Real2 coord = coordArray.get(i);
			s += formatCoords(coord);
		}
		return s;
	}
}
