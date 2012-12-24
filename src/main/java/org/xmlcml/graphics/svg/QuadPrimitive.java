package org.xmlcml.graphics.svg;

import java.awt.geom.GeneralPath;

import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;

public class QuadPrimitive extends SVGPathPrimitive {

	public final static String TAG = "Q";

	public QuadPrimitive(Real2Array coordArray) {
		if (coordArray == null || coordArray.size() != 2) {
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
	
	@Override
	public void operateOn(GeneralPath path) {
		if (coordArray != null) {	
			Real2 coord0 = coordArray.elementAt(0);
			Real2 coord1 = coordArray.elementAt(1);
			path.quadTo(coord0.x, coord0.y, coord1.x, coord1.y);
		}
	}

}
