package org.xmlcml.graphics.svg;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real2;

public class SVGLineTest {

	private final static Double FP_EPS = 0.000001;
	private final static Angle ANGLE_EPS = new Angle(0.000001, Angle.Units.RADIANS);
	
	@Test
	public void testPerpendicular() {
		SVGLine line0 = new SVGLine(new Real2(0,0), new Real2(0,1));
		SVGLine line1 = new SVGLine(new Real2(1,0), new Real2(0,0));
		Assert.assertTrue("perp", line0.isPerpendicularTo(line1, 0.00001));
	}

	@Test
	public void testPerpendicular1() {
		SVGLine line0 = new SVGLine(new Real2(0,0), new Real2(0,-1));
		SVGLine line1 = new SVGLine(new Real2(1,0), new Real2(0,0));
		Assert.assertTrue("perp", line0.isPerpendicularTo(line1, 0.00001));
	}
	
	@Test
	public void testPerpendicular2() {
		SVGLine line0 = new SVGLine(new Real2(0,0), new Real2(0,-1));
		SVGLine line1 = new SVGLine(new Real2(1,0.1), new Real2(0,0));
		Assert.assertFalse("perp", line0.isPerpendicularTo(line1, 0.00001));
	}
	
	@Test
	public void testParallel0() {
		SVGLine line0 = new SVGLine(new Real2(0,0), new Real2(0,1));
		SVGLine line1 = new SVGLine(new Real2(0,1), new Real2(0,2));
		Assert.assertTrue("para", line0.isParallelTo(line1, ANGLE_EPS));
	}

	@Test
	public void testParallel() {
		SVGLine line0 = new SVGLine(new Real2(0,0), new Real2(0,1));
		SVGLine line1 = new SVGLine(new Real2(1,0), new Real2(1,1));
		Assert.assertTrue("para", line0.isParallelTo(line1, ANGLE_EPS));
	}

	@Test
	public void testParallel1() {
		SVGLine line0 = new SVGLine(new Real2(0,0), new Real2(0,-1));
		SVGLine line1 = new SVGLine(new Real2(1,0), new Real2(1,-1));
		Assert.assertTrue("para", line0.isParallelTo(line1, ANGLE_EPS));
	}
	
	@Test
	public void testParallel2() {
		SVGLine line0 = new SVGLine(new Real2(0,0), new Real2(0,-1));
		SVGLine line1 = new SVGLine(new Real2(1,0.1), new Real2(0,0));
		Assert.assertFalse("perp", line0.isParallelTo(line1, ANGLE_EPS));
	}
	
	@Test
	public void testParallel3() {
		SVGLine line0 = new SVGLine(new Real2(0,0), new Real2(0,-1));
		SVGLine line1 = new SVGLine(new Real2(0,0), new Real2(0,1));
		Assert.assertFalse("perp", line0.isParallelTo(line1, ANGLE_EPS));
	}
	
	@Test
	public void testOverlap() {
		SVGLine line0 = new SVGLine(new Real2(0,0), new Real2(0,1));
		SVGLine line1 = new SVGLine(new Real2(0.5, 0.5), new Real2(0.5,1.5));
		Assert.assertTrue(line0.overlapsWithLine(line1, FP_EPS));
		Assert.assertTrue(line1.overlapsWithLine(line0, FP_EPS));
		SVGLine line2 = new SVGLine(new Real2(0.5, 0.25), new Real2(0.5, 0.75));
		Assert.assertFalse(line0.overlapsWithLine(line2, FP_EPS));
		Assert.assertTrue(line2.overlapsWithLine(line0, FP_EPS));
		SVGLine line3 = new SVGLine(new Real2(0.5, 1.25), new Real2(0.5, 2.0));
		Assert.assertFalse(line0.overlapsWithLine(line3, FP_EPS));
		Assert.assertFalse(line3.overlapsWithLine(line0, FP_EPS));
		
	}
}
