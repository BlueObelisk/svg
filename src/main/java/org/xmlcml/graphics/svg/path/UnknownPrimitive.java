package org.xmlcml.graphics.svg.path;

import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGPathPrimitive;

import java.awt.geom.GeneralPath;

public class UnknownPrimitive extends SVGPathPrimitive {

	private String TAG = "?";

	public UnknownPrimitive(char cc) {
		this.TAG = String.valueOf(cc);
	}

	public String getTag() {
		return TAG;
	}
	
	public String toString() {
		return TAG;
	}

	@Override
	public void operateOn(GeneralPath path) {
		throw new RuntimeException("Cannot create path");
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
