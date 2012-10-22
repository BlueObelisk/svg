package org.xmlcml.graphics.svg;

import java.awt.geom.GeneralPath;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.EuclidTestUtils;
import org.xmlcml.euclid.Real2Array;

public class SVGPathTest {

	@Test
	public void testCreatePolyline() {
		String d = "M379.558 218.898 L380.967 212.146 L380.134 212.146 L378.725 218.898 L379.558 218.898";
		SVGPath path = new SVGPath(d);
		SVGPolyline polyline = path.createPolyline();
		Assert.assertNotNull(polyline);
		Real2Array r2a = polyline.getReal2Array();
		String errmsg = EuclidTestUtils.testEquals("xarray", new double[] {379.558,380.967,380.134,378.725,379.558}, r2a.getXArray().getArray(), 0.001);
		Assert.assertNull(errmsg);
		errmsg = EuclidTestUtils.testEquals("xarray", new double[] {218.898,212.146,212.146,218.898,218.898}, r2a.getYArray().getArray(), 0.001);
		Assert.assertNull(errmsg);
	}
	
	@Test
	public void testBBScalefactor() {
		SVGPath path1 = new SVGPath("M1 2 L3 4 L1 2");
		SVGPath path2 = new SVGPath("M2 4 L2 8 L6 4");
		Double d = path1.getBoundingBoxScalefactor(path2);
		Assert.assertEquals("scale", 2.0, d, 0.00001);
	}
	
	@Test
	public void testScalefactor1() {
		SVGPath path1 = new SVGPath("M1 2 L3 4 L1 2");
		SVGPath path2 = new SVGPath("M2 4 L2 8 L6 4");
		Double d = path1.getScalefactor(path2, 0.00001);
		Assert.assertNull("cannot get scalefactor", d);
	}
	
	@Test
	public void testScalefactor2() {
		SVGPath path1 = new SVGPath("M1 2 L3 4 L1 2");
		SVGPath path2 = new SVGPath("M2 4 L6 8 L2 4");
		Double d = path1.getScalefactor(path2, 0.00001);
		Assert.assertEquals("scale", 2.0, d, 0.00001);
	}
	
	@Test
	@Ignore
	public void testCircle() {
		SVGPath path1 = new SVGPath(
				"M408.95 493.497 C408.95 492.438 407.805 491.779 406.889 492.308 C405.971 492.839 405.972 494.161 406.89 494.69 C407.807 495.217 408.95 494.557 408.95 493.497");
		SVGCircle circle = path1.createCircle(0.5);
		Assert. assertNotNull(circle);
		Assert.assertEquals("rad", 1.675, circle.getRad(), 0.1);
		Assert.assertEquals("cx", 407.4, circle.getCX(), 0.2);
		Assert.assertEquals("cx", 493.5, circle.getCY(), 0.2);
		
	}

	@Test
	public void testGeneralPath() {
		GeneralPath generalPath = new GeneralPath();
		generalPath.moveTo(1.0d, 2.0d);
		generalPath.lineTo(3.0d, 4.0d);
		generalPath.quadTo(5.0d, 6.0d, 7.0d, 8.0d);
		generalPath.curveTo(9.0d, 10.0d, 11.0d, 12.0d, 13.0d, 14.0d);
		generalPath.closePath();
		SVGPath path = new SVGPath(generalPath);
		String d = path.getDString();
		Assert.assertNotNull("d", d);
		Assert.assertEquals("d", "M 1.0 2.0 L 3.0 4.0 Q 5.0 6.0 7.0 8.0 C 9.0 10.0 11.0 12.0 13.0 14.0 Z", d.trim());
	}

	@Test
	public void testFormat() {
		String d = "M 1.1234 2.12345 L 3.1234567 4.12 C 5.123456 6.1 7.123456789 8.12345 9.12345 10.12345 Z";
		SVGPath path = new SVGPath(d);
		path.format(3);
		Assert.assertEquals("format", "M1.123 2.123 L3.123 4.12 C5.123 6.1 7.123 8.122 9.123 10.123 Z", path.getDString().trim());
	}

	@Test
	public void testFormat1() {
		String d = "M 219.75799560546875 604.5350341796875 L 229.24200439453125 604.5350341796875";
		SVGPath path = new SVGPath(d);
		path.format(3);
		Assert.assertEquals("format", "M219.758 604.535 L229.242 604.535", path.getDString().trim());
	}


}
