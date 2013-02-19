package org.xmlcml.graphics.svg;

import java.awt.geom.GeneralPath;

import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real2;

public class UnknownPrimitive extends SVGPathPrimitive {

	private String TAG = "?";

	public UnknownPrimitive(char cc) {
		this.TAG = ""+cc;
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
