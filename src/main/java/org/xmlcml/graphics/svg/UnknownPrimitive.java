package org.xmlcml.graphics.svg;

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

}
