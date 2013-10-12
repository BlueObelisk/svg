package org.xmlcml.graphics.svg;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real2;

public class SVGLineTest {

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
}
