package org.xmlcml.graphics.svg;

import java.awt.geom.GeneralPath;

import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;

public class LinePrimitive extends SVGPathPrimitive {

	public final static String TAG = "L";

	public LinePrimitive(Real2 real2) {
		this.coordArray = new Real2Array();
		coordArray.add(real2);
	}

	public String getTag() {
		return TAG;
	}
	
	public String toString() {
		return TAG + formatCoords(coordArray.get(0));
	}

	@Override
	public void operateOn(GeneralPath path) {
		if (coordArray != null) {	
			Real2 coord = coordArray.elementAt(0);
			path.lineTo(coord.x, coord.y);
		}
	}
}
