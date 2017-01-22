package org.xmlcml.graphics.svg.path;

import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Array;
import org.xmlcml.graphics.svg.SVGPathPrimitive;

import java.awt.geom.GeneralPath;

/**
 * supports 'Z' command
 * @author pm286
 *
 */
public class ClosePrimitive extends SVGPathPrimitive {

	public final static String TAG = "Z";

	public ClosePrimitive() {
	}
	
	public ClosePrimitive(Real2 real2) {
		this.coordArray = new Real2Array();
		if (real2 != null) {
			coordArray.add(real2);
		} else {
			coordArray = null;
		}
	}

	public String getTag() {
		return TAG;
	}
	
	@Override
	public void operateOn(GeneralPath path) {
		path.closePath();
	}
	
	public String toString() {
		return TAG;
	}
	
	@Override
	/**
	 * @return null
	 */
	public Angle getAngle() {
		return null;
	}

	@Override
	/**
	 * @return null
	 *
	 */
	public Real2 getTranslation() {
		return null;
	}


}
