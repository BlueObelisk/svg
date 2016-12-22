package org.xmlcml.graphics.svg;


import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Range;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.path.ClosePrimitive;
import org.xmlcml.graphics.svg.path.CubicPrimitive;
import org.xmlcml.graphics.svg.path.LinePrimitive;
import org.xmlcml.graphics.svg.path.MovePrimitive;
import org.xmlcml.graphics.svg.path.PathPrimitiveList;

public class SVGPathPrimitiveTest {

	private final static Logger LOG = Logger.getLogger(SVGPathPrimitiveTest.class);

	private static final Angle ANGLE_EPS = new Angle(0.0001, Units.RADIANS);
	
	static String D1 = 
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
	
	static String D2 = 
			"m1 1 M 2 2";
	
	static String D3 = 
			"M1 1 M 2 2";
	
	static String D4 = 
			"M1 1 M 2 2";
	
	static String D5 = 
			"M1 1 m 1 1";
	
	static String D6 = 
			"M1 1 L 2 2";
	
	static String D7 = 
			"M1 1 l 1 1";
	
	@Test
	public void testRelativeMoveCommandAtStart() {
		 PathPrimitiveList primitiveList1 = createPrimitiveList(D2);
		 PathPrimitiveList primitiveList2 = createPrimitiveList(D3);
		 Assert.assertEquals(primitiveList1.toString(), primitiveList2.toString());
	}
	
	@Test
	public void testRelativeMoveCommandNotAtStart() {
		 PathPrimitiveList primitiveList1 = createPrimitiveList(D4);
		 PathPrimitiveList primitiveList2 = createPrimitiveList(D5);
		 Assert.assertEquals(primitiveList1.toString(), primitiveList2.toString());
	}
	
	@Test
	public void testRelativeLineCommand() {
		 PathPrimitiveList primitiveList1 = createPrimitiveList(D6);
		 PathPrimitiveList primitiveList2 = createPrimitiveList(D7);
		 Assert.assertEquals(primitiveList1.toString(), primitiveList2.toString());
	}

	@Test
	public void testString() {
		 PathPrimitiveList primitiveList = createPrimitiveList(D1);
		 Assert.assertEquals("l", 12, primitiveList.size());
		 Assert.assertTrue("m", primitiveList.get(0) instanceof MovePrimitive);
		 Assert.assertTrue("l", primitiveList.get(1) instanceof LinePrimitive);
		 Assert.assertTrue("c", primitiveList.get(10) instanceof CubicPrimitive);
		 Assert.assertTrue("z", primitiveList.get(11) instanceof ClosePrimitive);
	}

	@Test
	public void testZerothCoord1() {
		PathPrimitiveList primitiveList = createPrimitiveList(D1);
		Assert.assertTrue("m", new Real2(110.7, 262.43).isEqualTo(primitiveList.get(0).getZerothCoord(), 0.001));
		Assert.assertTrue("l", new Real2(110.7, 262.44).isEqualTo(primitiveList.get(1).getZerothCoord(), 0.001));
		Assert.assertTrue("c", new Real2(111.24, 263.16).isEqualTo(primitiveList.get(10).getZerothCoord(), 0.001));
		Assert.assertTrue("z", new Real2(110.7, 262.43).isEqualTo(primitiveList.get(11).getZerothCoord(), 0.001));
	}
	

	@Test
	public void testFirstCoord() {
		 PathPrimitiveList primitiveList = createPrimitiveList(D1);
		 Assert.assertTrue("m", new Real2(110.7, 262.44).isEqualTo(primitiveList.get(0).getFirstCoord(), 0.001));
		 Assert.assertTrue("l", new Real2(110.82, 261.839).isEqualTo(primitiveList.get(1).getFirstCoord(), 0.001));
		 Assert.assertTrue("c", new Real2(110.88, 263.1).isEqualTo(primitiveList.get(10).getFirstCoord(), 0.001));
		 Assert.assertNull("z", primitiveList.get(11).getFirstCoord());
	}

	@Test
	public void testLastCoord() {
		PathPrimitiveList primitiveList = createPrimitiveList(D1);
		 Assert.assertTrue("m", new Real2(110.7, 262.44).isEqualTo(primitiveList.get(0).getLastCoord(), 0.001));
		 Assert.assertTrue("l", new Real2(110.82, 261.839).isEqualTo(primitiveList.get(1).getLastCoord(), 0.001));
		 Assert.assertTrue("c", new Real2(110.7, 262.43).isEqualTo(primitiveList.get(10).getLastCoord(), 0.001));
		 Assert.assertNull("z", primitiveList.get(11).getLastCoord());
	}

	@Test
	public void testGetDistance() {
		 PathPrimitiveList primitiveList = createPrimitiveList(D1);
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
		PathPrimitiveList primitiveList = createPrimitiveList(D1);
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
	
	/*<svg xmlns="http://www.w3.org/2000/svg">
	 <g>
	  <path stroke="black" fill="#000000" stroke-width="0.0" 
	  d="M172.14 512.58 
	  L172.14 504.3 
	  C172.14 504.18 172.26 504.06 172.38 504.06 
	  C172.5 504.06 172.62 504.18 172.62 504.3 
	  L172.62 512.58 
	  C172.62 512.76 172.5 512.88 172.38 512.88 
	  C172.26 512.88 172.14 512.76 172.14 512.58 "
	  svgx:z="1737"/>
	 </g>
	</svg>*/
	@Test
	public void checkAngleForClosedCurve() {
		SVGPath ROUNDED_LINE_SVG = (SVGPath) SVGElement.readAndCreateSVG(Fixtures.ROUNDED_LINE_SVG_FILE)
				.getChildElements().get(0).getChildElements().get(0);
	    PathPrimitiveList primitiveList = ROUNDED_LINE_SVG.ensurePrimitives();
		Assert.assertEquals("MLCCLCC", ROUNDED_LINE_SVG.getSignature());
		Assert.assertTrue("closed", primitiveList.isClosed());
		Assert.assertNull("angle0", primitiveList.getAngle(0)); //MOVE
		Assert.assertEquals("angle1", 0.0, primitiveList.getAngle(1).getRadian(), ANGLE_EPS.getRadian());
		Assert.assertEquals("angle2", Math.PI / 2., primitiveList.getAngle(2).getRadian(), ANGLE_EPS.getRadian());
		Assert.assertEquals("angle3", Math.PI / 2., primitiveList.getAngle(3).getRadian(), ANGLE_EPS.getRadian());
		Assert.assertEquals("angle4", 0.0, primitiveList.getAngle(4).getRadian(), ANGLE_EPS.getRadian());
		Assert.assertEquals("angle5", Math.PI / 2., primitiveList.getAngle(5).getRadian(), ANGLE_EPS.getRadian());
		Assert.assertEquals("angle6", Math.PI / 2., primitiveList.getAngle(6).getRadian(), ANGLE_EPS.getRadian());
	}

	@Test
	public void testQuadrantValue() {
		SVGPath ROUNDED_LINE_SVG = (SVGPath) SVGElement.readAndCreateSVG(Fixtures.ROUNDED_LINE_SVG_FILE)
				.getChildElements().get(0).getChildElements().get(0);
		PathPrimitiveList primitiveList = ROUNDED_LINE_SVG.ensurePrimitives();
		Assert.assertEquals("MLCCLCC", ROUNDED_LINE_SVG.getSignature());
		Assert.assertEquals("q0", 0, primitiveList.quadrantValue(0, ANGLE_EPS));
		Assert.assertEquals("q1", 0, primitiveList.quadrantValue(1, ANGLE_EPS));
		Assert.assertEquals("q2", 1, primitiveList.quadrantValue(2, ANGLE_EPS));
		Assert.assertEquals("q3", 1, primitiveList.quadrantValue(3, ANGLE_EPS));
		Assert.assertEquals("q4", 0, primitiveList.quadrantValue(4, ANGLE_EPS));
		Assert.assertEquals("q5", 1, primitiveList.quadrantValue(5, ANGLE_EPS));
		Assert.assertEquals("q6", 1, primitiveList.quadrantValue(6, ANGLE_EPS));
	}

	@Test
	public void testTwoQuadrantList() {
		SVGPath ROUNDED_LINE_SVG = (SVGPath) SVGElement.readAndCreateSVG(Fixtures.ROUNDED_LINE_SVG_FILE)
				.getChildElements().get(0).getChildElements().get(0);
	PathPrimitiveList primitiveList = ROUNDED_LINE_SVG.ensurePrimitives();
		List<Integer> quadStartList = primitiveList.getUTurnList(ANGLE_EPS);
		Assert.assertEquals("quads", 2, quadStartList.size());
		Assert.assertEquals("quads1", 2, (int) quadStartList.get(0));
		Assert.assertEquals("quads2", 5, (int) quadStartList.get(1));
	}
	
	@Test
	public void testFindSemiCircles() {
		SVGPath ROUNDED_LINE_SVG = (SVGPath) SVGElement.readAndCreateSVG(Fixtures.ROUNDED_LINE_SVG_FILE)
				.getChildElements().get(0).getChildElements().get(0);
		PathPrimitiveList primitiveList = ROUNDED_LINE_SVG.ensurePrimitives();
		Assert.assertEquals("MLCCLCC", ROUNDED_LINE_SVG.getSignature());
		Assert.assertTrue(primitiveList.isUTurn(2, ANGLE_EPS));
	}
	
	@Test
	public void testTwoQuadrantListMolecule() {
		List<SVGPath> pathList = SVGPath.extractPaths(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_13_SVG));
		Assert.assertEquals("paths", 13, pathList.size());
		SVGPath path = pathList.get(3);
		SVGSVG.wrapAndWriteAsSVG(path, new File("target/badPath.svg"));
		LOG.trace(path.toXML());
		PathPrimitiveList primitiveList = path.ensurePrimitives();
		List<Integer> quadStartList = primitiveList.getUTurnList(new Angle(0.1, Units.RADIANS));
		Assert.assertEquals("quads", 2, quadStartList.size());
		Assert.assertEquals("quads1", 2, (int) quadStartList.get(0));
		Assert.assertEquals("quads2", 5, (int) quadStartList.get(1));
	}

	// ==================================================================
	
	private PathPrimitiveList createPrimitiveList(String d) {
		SVGPath path = new SVGPath(d);
		PathPrimitiveList primitiveList = path.ensurePrimitives();
		return primitiveList;
	}
	
}
