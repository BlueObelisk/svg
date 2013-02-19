package org.xmlcml.graphics.svg;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Range;
import org.xmlcml.euclid.Real2;

public class SVGPathPrimitiveTest {

	static String D1="" +
			"M110.7 262.44 " +
			"L110.82 261.839 " +
			"L111.0 260.459 " +
			"L111.06 259.98 " +
			"C111.12 259.62 111.42 259.38 111.78 259.44 " +
			"C112.14 259.5 112.38 259.8 112.32 260.16 " +
			"L112.26 260.64 " +
			"L112.02 262.019 " +
			"L111.96 262.62 " +
			"C111.9 262.98 111.6 263.22 111.24 263.16 " +
			"C110.88 263.1 110.64 262.8 110.7 262.43 " +
			"Z";

	@Test
	public void testString() {
		 List<SVGPathPrimitive> primitiveList = createPrimitiveList(D1);
		 Assert.assertEquals("l", 12, primitiveList.size());
		 Assert.assertTrue("m", primitiveList.get(0) instanceof MovePrimitive);
		 Assert.assertTrue("l", primitiveList.get(1) instanceof LinePrimitive);
		 Assert.assertTrue("c", primitiveList.get(10) instanceof CubicPrimitive);
		 Assert.assertTrue("z", primitiveList.get(11) instanceof ClosePrimitive);
	}

	@Test
	public void testZerothCoord() {
		 List<SVGPathPrimitive> primitiveList = createPrimitiveList(D1);
		 Assert.assertNull("m", primitiveList.get(0).getZerothCoord());
		 Assert.assertNull("l", primitiveList.get(1).getZerothCoord());
		 Assert.assertNull("c", primitiveList.get(10).getZerothCoord());
		 Assert.assertNull("z", primitiveList.get(11).getZerothCoord());
	}

	@Test
	public void testZerothCoord1() {
		 List<SVGPathPrimitive> primitiveList = createPrimitiveList(D1);
		 SVGPathPrimitive.setFirstPoints(primitiveList);
		 Assert.assertTrue("m", new Real2(110.7, 262.43).isEqualTo(primitiveList.get(0).getZerothCoord(), 0.001));
		 Assert.assertTrue("l", new Real2(110.7, 262.44).isEqualTo(primitiveList.get(1).getZerothCoord(), 0.001));
		 Assert.assertTrue("c", new Real2(111.24, 263.16).isEqualTo(primitiveList.get(10).getZerothCoord(), 0.001));
		 Assert.assertTrue("z", new Real2(110.7, 262.43).isEqualTo(primitiveList.get(11).getZerothCoord(), 0.001));
	}
	

	@Test
	public void testFirstCoord() {
		 List<SVGPathPrimitive> primitiveList = createPrimitiveList(D1);
		 Assert.assertTrue("m", new Real2(110.7, 262.44).isEqualTo(primitiveList.get(0).getFirstCoord(), 0.001));
		 Assert.assertTrue("l", new Real2(110.82, 261.839).isEqualTo(primitiveList.get(1).getFirstCoord(), 0.001));
		 Assert.assertTrue("c", new Real2(110.88, 263.1).isEqualTo(primitiveList.get(10).getFirstCoord(), 0.001));
		 Assert.assertNull("z", primitiveList.get(11).getFirstCoord());
	}

	@Test
	public void testLastCoord() {
		 List<SVGPathPrimitive> primitiveList = createPrimitiveList(D1);
		 Assert.assertTrue("m", new Real2(110.7, 262.44).isEqualTo(primitiveList.get(0).getLastCoord(), 0.001));
		 Assert.assertTrue("l", new Real2(110.82, 261.839).isEqualTo(primitiveList.get(1).getLastCoord(), 0.001));
		 Assert.assertTrue("c", new Real2(110.7, 262.43).isEqualTo(primitiveList.get(10).getLastCoord(), 0.001));
		 Assert.assertNull("z", primitiveList.get(11).getLastCoord());
	}

	@Test
	public void testGetDistance() {
		 List<SVGPathPrimitive> primitiveList = createPrimitiveList(D1);
		 SVGPathPrimitive.setFirstPoints(primitiveList);
		 Real2 vector = primitiveList.get(0).getTranslation();
		 Assert.assertTrue("m"+vector, new Real2(0.0, 0.01).isEqualTo(vector, 0.001));
		 vector = primitiveList.get(1).getTranslation();
		 
		 Assert.assertTrue("l"+vector, new Real2(0.12, -0.6).isEqualTo(vector, 0.001));
		 vector = primitiveList.get(2).getTranslation();
		 Assert.assertTrue("l"+vector, new Real2(0.18, -1.38).isEqualTo(vector, 0.001));
		 vector = primitiveList.get(3).getTranslation();
		 Assert.assertTrue("l"+vector, new Real2(0.06, -0.479).isEqualTo(vector, 0.001));
		 
		 vector = primitiveList.get(4).getTranslation();
		 Assert.assertTrue("c"+vector, new Real2(0.72, -0.54).isEqualTo(vector, 0.001));
		 vector = primitiveList.get(5).getTranslation();
		 Assert.assertTrue("c"+vector, new Real2(0.54, 0.72).isEqualTo(vector, 0.001));

		 vector = primitiveList.get(6).getTranslation();   // 6 == -3
		 Assert.assertTrue("l"+vector, new Real2(-0.06, 0.48).isEqualTo(vector, 0.001));
		 vector = primitiveList.get(7).getTranslation();  // 7 ~~ -2
		 Assert.assertTrue("l"+vector, new Real2(-0.24, 1.38).isEqualTo(vector, 0.001));
		 vector = primitiveList.get(8).getTranslation();  // 8 ~~ -1
		 Assert.assertTrue("l"+vector, new Real2(-0.06, 0.60).isEqualTo(vector, 0.001));
		 
		 vector = primitiveList.get(9).getTranslation();
		 Assert.assertTrue("c"+vector, new Real2(-0.72, 0.54).isEqualTo(vector, 0.001));
		 vector = primitiveList.get(10).getTranslation();
		 Assert.assertTrue("c"+vector, new Real2(-0.54, -0.73).isEqualTo(vector, 0.001));
		 vector = primitiveList.get(9).getZerothCoord().subtract(primitiveList.get(10).getLastCoord());
		 Assert.assertTrue("9-10 "+vector, new Real2(1.26, .19).isEqualTo(vector, 0.001));
		 Assert.assertNull("z", primitiveList.get(11).getTranslation());
	}

	@Test
	public void testGetAngle() {
		Angle pi2 = new Angle(Math.PI/2.);
		pi2.setRange(Range.UNSIGNED);
		 List<SVGPathPrimitive> primitiveList = createPrimitiveList(D1);
		 SVGPathPrimitive.setFirstPoints(primitiveList);
		 Angle angle = primitiveList.get(0).getAngle();
		 Assert.assertNull("m", angle);
		 angle = primitiveList.get(1).getAngle();
		 Assert.assertTrue("l"+angle, angle.isEqualTo(0.0, 0.001));
		 
		 angle = primitiveList.get(4).getAngle();
		 angle.setRange(Range.UNSIGNED);
		 Assert.assertTrue("c"+angle, angle.isEqualTo(Math.PI/2.0, 0.001));
		 angle = primitiveList.get(5).getAngle();
		 angle.setRange(Range.UNSIGNED);
		 Assert.assertTrue("c"+angle, angle.isEqualTo(Math.PI/2.0, 0.001));
		 
		 angle = primitiveList.get(9).getAngle();
		 angle.setRange(Range.UNSIGNED);
		 Assert.assertTrue("c"+angle, angle.isEqualTo(Math.PI/2.0, 0.001));
		 angle = primitiveList.get(10).getAngle();
		 angle.setRange(Range.UNSIGNED);
		 // this one is not quite PI/2 - about 0.3% out
		 Assert.assertTrue("c"+angle,angle.isEqualTo(Math.PI/2.0, 0.006));
		 Assert.assertNull("z", primitiveList.get(11).getAngle());
	}

	private List<SVGPathPrimitive> createPrimitiveList(String d) {
		SVGPath path = new SVGPath(d);
		List<SVGPathPrimitive> primitiveList = path.ensurePrimitives();
		return primitiveList;
	}
	
}
