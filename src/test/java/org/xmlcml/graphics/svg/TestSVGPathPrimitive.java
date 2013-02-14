package org.xmlcml.graphics.svg;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.xmlcml.euclid.Real2Array;

public class TestSVGPathPrimitive {

	public final static String dString = 
			"M327.397 218.898 L328.024 215.899 L329.074 215.899 C329.433 215.899 329.693 215.936 329.854 216.008" +
			" C330.012 216.08 330.168 216.232 330.322 216.461 C330.546 216.793 330.745 217.186 330.92 217.64" +
			" L331.404 218.898 L332.413 218.898 L331.898 217.626 C331.725 217.202 331.497 216.793 331.215 216.397" +
			" C331.089 216.222 330.903 216.044 330.657 215.863 C331.46 215.755 332.039 215.52 332.4 215.158" +
			" C332.759 214.796 332.938 214.341 332.938 213.791 C332.938 213.397 332.856 213.072 332.692 212.814" +
			" C332.527 212.557 332.301 212.381 332.013 212.287 C331.724 212.192 331.3 212.146 330.741 212.146" +
			" L327.908 212.146 L326.494 218.898 L327.397 218.898 ZM328.654 212.888 L330.857 212.888" +
			" C331.203 212.888 331.448 212.914 331.593 212.967 C331.737 213.018 331.855 213.117 331.942 213.264" +
			" C332.032 213.41 332.075 213.58 332.075 213.776 C332.075 214.011 332.017 214.228 331.898 214.431" +
			" C331.779 214.634 331.609 214.794 331.39 214.914 C331.171 215.034 330.893 215.111 330.552 215.145" +
			" C330.376 215.16 330 215.168 329.424 215.168 L328.176 215.168 L328.654 212.888";

//	@Test
//	public void testParseD() {
//		List<SVGPathPrimitive> primitives = SVGPathPrimitive.parseD(dString);
//		Assert.assertEquals("primitives", 31, primitives.size());
//	}

	@Test
	public void testParseDString() {
		List<SVGPathPrimitive> primitives = SVGPathPrimitive.parseDString(dString);
		Assert.assertEquals("primitives", 31, primitives.size());
	}

//	@Test
//	public void testParseD1() {
//		List<SVGPathPrimitive> primitiveList = SVGPathPrimitive.parseD(dString);
//		String sig = SVGPathPrimitive.createSignature(primitiveList);
//		Assert.assertEquals("signature", "MLLCCCLLLCCCCCCCLLLZMLCCCCCCCLL", sig);
//	}

	@Test
	public void testParseDString1() {
		List<SVGPathPrimitive> primitiveList = SVGPathPrimitive.parseDString(dString);
		String sig = SVGPathPrimitive.createSignature(primitiveList);
		Assert.assertEquals("signature", "MLLCCCLLLCCCCCCCLLLZMLCCCCCCCLL", sig);
	}

	@Test
	public void testNormalize() {
		String d = "M368.744 213.091 L368.943 212.146 L368.113 212.146 L367.915 213.091 L368.744 213.091 ZM367.532 218.898 L368.556 214.008 L367.722 214.008 L366.7 218.898 L367.532 218.898";
		SVGPath path = new SVGPath();
		path.setDString(d);
//		List<SVGPathPrimitive> primitives = SVGPathPrimitive.parseD(d);
		List<SVGPathPrimitive> primitives = SVGPathPrimitive.parseDString(d);
		Real2Array r2a = path.getCoords();
		path.normalizeOrigin();
		Assert.assertEquals("normalized path", 
		    "M2.044 0.945 L2.242 0.0 L1.413 0.0 L1.215 0.945 L2.044 0.945 ZM0.831 6.752 L1.855 1.862 L1.021 1.862 L0.0 6.752 L0.831 6.752",
//
//			"M2.043 0.945 L2.242 0.0 L1.413 0.0 L1.215 0.945 L2.043 0.945 ZM0.832 6.751 L1.855 1.861 L1.021 1.861 L0.0 6.751 L0.832 6.751",
			path.getDString().trim());
	}
}
