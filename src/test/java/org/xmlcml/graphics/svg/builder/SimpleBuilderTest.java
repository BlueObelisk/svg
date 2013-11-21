package org.xmlcml.graphics.svg.builder;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.*;

import java.io.File;
import java.util.List;

/** test reading molecules
 * 
 * Reads SVG and uses heuristics to create chemistry.
 * 
 * @author pm286
 *
 */
//@Ignore
public class SimpleBuilderTest {

	private final static Logger LOG = Logger.getLogger(SimpleBuilderTest.class);
	public static final Angle MAX_ANGLE = new Angle(0.12, Units.RADIANS);
	public static final Double MAX_WIDTH = 2.0;

	File IMAGE_2_11_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.11.svg");
	File IMAGE_2_13_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.13.svg");
	File IMAGE_2_15_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.15.svg");
	File IMAGE_2_16_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.16.svg");
	File IMAGE_2_18_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.18.svg");
	File IMAGE_2_23_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.23.svg");
	File IMAGE_2_25_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.25.svg");
	File IMAGE_5_11_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.5.11.svg");
	File IMAGE_5_12_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.5.12.svg");
	File IMAGE_5_13_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.5.13.svg");
	File IMAGE_5_14_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.5.14.svg");
	File IMAGE_02_00100_65_SVG = new File(Fixtures.MOLECULES_DIR, "02.00100.g.6.5.svg");
	File SMALL_TEST_1 = new File(Fixtures.MOLECULES_DIR, "smalltest1.svg");
	File SMALL_TEST_2 = new File(Fixtures.MOLECULES_DIR, "smalltest2.svg");
	
	@Test
	public void testAllLists() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		//Assert.assertEquals("implicit", 6, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//Assert.assertNull("explicit", simpleBuilder.getRawPrimitives().getLineList());
		Assert.assertEquals("explicit", 0, simpleBuilder.getRawPrimitives().getLineList().size());
		//Assert.assertEquals("paths", 6, simpleBuilder.getRawPathList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getRawPrimitives().getLineList().size());
		//List<SVGLine> singleLineList = simpleBuilder.getHigherPrimitives().getSingleLineList();
		//Assert.assertEquals("lines", 6, singleLineList.size());
		Assert.assertEquals("lines", 6, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//Assert.assertEquals("unused paths", 0, simpleBuilder.getRawPrimitives().getLineList().size());
		//simpleBuilder.createJoinableList();
		Assert.assertEquals("joinable", 11, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		//List<Junction> junctionList = simpleBuilder.createRawJunctionList();
		Assert.assertEquals("junction", 8, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
	}

	@Test
	public void testJunction() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		//simpleBuilder.createTramLineList();
		Assert.assertEquals("tram", 1, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//List<Junction> junctionList = simpleBuilder.createMergedJunctions();
		Assert.assertEquals("junctions", 6, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
	}

	@Test
	public void testnonWedgeBondsAndElements() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(IMAGE_2_13_SVG);
		SimpleBuilder simpleBuilder = new SimpleBuilder(svgRoot);
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 13, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
	
	@Test
	public void testnonWedgeBondsAndElements1() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(IMAGE_2_16_SVG);
		SimpleBuilder simpleBuilder = new SimpleBuilder(svgRoot);
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 20, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
	
	@Test
	public void testSubscripts() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(IMAGE_2_11_SVG);
		SimpleBuilder simpleBuilder = new SimpleBuilder(svgRoot);
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 6, simpleBuilder.getDerivedPrimitives().getLineList().size());
		
	}
	
	@Test
	public void testWedges() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_18_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 21, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}

	@Test
	public void testNoRingsOrWedges() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_23_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 24, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
	
	@Test
	@Ignore
	public void testHard() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_25_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 25, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
	
	@Test
	@Ignore
	public void test00100() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_02_00100_65_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines(); // should be 27??
		Assert.assertEquals("lines", 32, simpleBuilder.getDerivedPrimitives().getLineList().size());//should be 28 (this said 34 before)
		//simpleBuilder.createTramLineList();
		Assert.assertEquals("lines", 6, simpleBuilder.getHigherPrimitives().getTramLineList().size());
	}
		
	@Test
	public void testWithElementPNG5_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 46, simpleBuilder.getDerivedPrimitives().getLineList().size());//FIXME should be 47
	}
	@Test
	public void testWithElementPNG5_12() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_12_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 45, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
	@Test
	public void testWithElementPNG5_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_13_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 86, simpleBuilder.getDerivedPrimitives().getLineList().size());//FIXME should be 88; missing lines in SVG
	}		
	@Test
	public void testWithElementPNG5_14() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_14_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 95, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
	
	@Test
	public void testTramLinesG2_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//List<SVGLine> lineList = simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 6, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//TramLineManager tramLineManager = new TramLineManager();
		//List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		//Assert.assertEquals("tramLines", 1, tramLineList.size());
		Assert.assertEquals("tramLines", 1, simpleBuilder.getHigherPrimitives().getTramLineList().size());
	}
	
	@Test
	public void testTramLines() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_13_SVG));
		//Assert.assertNull("singleLines", simpleBuilder.getDerivedPrimitives().getLineList());
		//Assert.assertNull("explicitLines", simpleBuilder.getRawPrimitives().getLineList());
		//Assert.assertNull("implicitLines", simpleBuilder.getDerivedPrimitives().getLineList());
		Assert.assertNull("derivedPrimitives", simpleBuilder.getDerivedPrimitives());
		simpleBuilder.createHigherPrimitives();
		//Assert.assertNull("paths", simpleBuilder.getExplicitPathList());
		//simpleBuilder.createRawAndDerivedLines();
		//Assert.assertEquals("singleLines", 13, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//Assert.assertEquals("explicitLines", null, simpleBuilder.getRawPrimitives().getLineList().size());
		//Assert.assertEquals("implicitLines", 13, simpleBuilder.getDerivedPrimitives().getLineList().size());
		// creating lines has removed paths
		//Assert.assertEquals("paths", 0, simpleBuilder.getCurrentPathList().size());
		//List<TramLine> tramLineList = simpleBuilder.createTramLineList();
		//Assert.assertEquals("implicitLines", 13, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("lLines", 13, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//TramLine creation removes single lines
		//Assert.assertEquals("tramLines", 3, tramLineList.size());
		Assert.assertEquals("tramLines", 3, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("singleLines", 7, simpleBuilder.getHigherPrimitives().getLineList().size());
		//Assert.assertEquals("explicitLines", 0, simpleBuilder.getRawPrimitives().getLineList().size());

	}
	@Test
	public void testTramLines2_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		Assert.assertEquals("lines", 4, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("tramLines", 1, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getRawPrimitives().getLineList().size());
	}
	@Test
	public void testTramLines2_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_13_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		Assert.assertEquals("tramLines", 3, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("singleLines", 13, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
	@Test
	public void testTramLines2_18() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_18_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		//Assert.assertEquals("paths", 1, simpleBuilder.getRawPrimitives().getLineList().size());
		Assert.assertEquals("tramLines", 5, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("singleLines", 21, simpleBuilder.getDerivedPrimitives().getLineList().size());
	}
	
	@Test
	public void testTramLines2_23() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_23_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		Assert.assertEquals("tramLines", 4, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getRawPrimitives().getLineList().size());
		Assert.assertEquals("paths", 16, simpleBuilder.getHigherPrimitives().getLineList().size());
	}
	@Test
	@Ignore
	public void testTramLines2_25() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_25_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		Assert.assertEquals("tramLines", 5, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//Assert.assertEquals("paths", 1, simpleBuilder.getRawPrimitives().getLineList().size());
		Assert.assertEquals("paths", 15, simpleBuilder.getHigherPrimitives().getLineList().size());
	}
	@Test
	public void testTramLines5_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("tramLines", 4, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getRawPrimitives().getLineList().size());
		Assert.assertEquals("paths", 38, simpleBuilder.getHigherPrimitives().getLineList().size());//FIXME should be 39
	}
	@Test
	public void testTramLines5_12() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_12_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("tramLines", 6, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//Assert.assertEquals("paths", 1, simpleBuilder.getRawPrimitives().getLineList().size());
		Assert.assertEquals("paths", 33, simpleBuilder.getHigherPrimitives().getLineList().size());
	}
	@Test
	public void testTramLines5_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_13_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("tramLines", 11, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//Assert.assertEquals("paths", 1, simpleBuilder.getRawPrimitives().getLineList().size());
		Assert.assertEquals("paths", 64, simpleBuilder.getHigherPrimitives().getLineList().size());//FIXME should be 66
	}
	@Test
	public void testTramLines5_14() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_14_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createTramLineList();
		Assert.assertEquals("tramLines", 13, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//Assert.assertEquals("paths", 1, simpleBuilder.getRawPrimitives().getLineList().size());
		Assert.assertEquals("paths", 69, simpleBuilder.getHigherPrimitives().getLineList().size());
	} 
	
	@Test
	@Ignore
	public void testWedgeHash() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_18_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawAndDerivedLines();
		// this contained a rect translated to a line
		//Assert.assertEquals("explicitLines", 1, simpleBuilder.getRawPrimitives().getLineList().size());
		//Assert.assertEquals("implicitLines", 21, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("singleLines", 21, simpleBuilder.getHigherPrimitives().getLineList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getCurrentPathList().size());
		// polygon and 5 circles
		//Assert.assertEquals("shapes", 6, simpleBuilder.getCurrentShapeList().size());
		// creating lines has removed paths
		//Assert.assertEquals("paths", 0, simpleBuilder.getCurrentPathList().size());
		//List<TramLine> tramLineList = simpleBuilder.createTramLineList();
		Assert.assertEquals("implicitLines", 21, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//TramLine creation removes single lines
		Assert.assertEquals("singleLines", 12, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("tramLines", 5, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("explicitLines", 1, simpleBuilder.getRawPrimitives().getLineList().size());

	}
	
	@Test
	public void testJunctionMerging2_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//, 6, 8, 6);    // fails
		Assert.assertEquals("lines", 6, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
		Assert.assertEquals("lines", 4, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("lines", 1, simpleBuilder.getHigherPrimitives().getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_13_SVG));
		simpleBuilder.createHigherPrimitives();
		//, 13, 17, 10);
		Assert.assertEquals("lines", 10, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
		Assert.assertEquals("lines", 7, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("lines", 3, simpleBuilder.getHigherPrimitives().getTramLineList().size());
	}
	
	@Test
	@Ignore//FIXME
	public void testJunctionMerging2_18() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_18_SVG));
		simpleBuilder.createHigherPrimitives();
		//, 21, 23, 14);
		Assert.assertEquals("lines", 15, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
		Assert.assertEquals("lines", 12, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("lines", 5, simpleBuilder.getHigherPrimitives().getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_23() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_23_SVG));
		simpleBuilder.createHigherPrimitives();
		//, 24, 37, 21);
		Assert.assertEquals("lines", 22, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
		Assert.assertEquals("lines", 16, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("lines", 4, simpleBuilder.getHigherPrimitives().getTramLineList().size());
	}
	
	@Test
	@Ignore//FIXME
	public void testJunctionMerging2_25() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_25_SVG));
		simpleBuilder.createHigherPrimitives();
		//no hatches; should be 25, 32, 20; l of Cl not circular enough, =O too near other bonds
		Assert.assertEquals("lines", 22, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());
		Assert.assertEquals("lines", 15, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("lines", 5, simpleBuilder.getHigherPrimitives().getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging5_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//simpleBuilder.createRawJunctionList();
		//hatches and arrow; should be 36, 49, 26
		//simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 46, simpleBuilder.getDerivedPrimitives().getLineList().size());//Should be 39 (38)
		//simpleBuilder.createMergedJunctions();
		//Assert.assertEquals("lines", 38, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("lines", 4, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 23, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());//FIXME should be 26 as not picking up text; 29 with hatches and wedges
	}
	
	/*@Test
	public void testJunctionMergingReduced5_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(new File("src/test/resources/org/xmlcml/graphics/svg/molecules/image.g.5.11reduced2.svg")));
		simpleBuilder.createRawJunctionList();
		drawFromSimpleBuilder(simpleBuilder);
		simpleBuilder.createMergedJunctions();//hatches and arrow; should be 36, 49, 26
		Assert.assertEquals("lines", 1, simpleBuilder.createMergedJunctions().size());//FIXME should be 26 as not picking up text; 29 with hatches and wedges
		Assert.assertEquals("lines", 8, simpleBuilder.getHigherPrimitives().getSingleLineList().size());//Should be 39
		Assert.assertEquals("lines", 0, simpleBuilder.getHigherPrimitives().getTramLineList().size());
	}*/
	
	@Test
	public void testJunctionMerging5_12() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_12_SVG));
		simpleBuilder.createHigherPrimitives();
		Assert.assertEquals("lines", 23, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());//FIXME should be 27 as not picking up text; 29 with hatches and wedges
		Assert.assertEquals("lines", 33, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("lines", 6, simpleBuilder.getHigherPrimitives().getTramLineList().size());
	}
	
	@Test
	@Ignore//FIXME
	public void testJunctionMerging5_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_13_SVG));
		simpleBuilder.createHigherPrimitives();
		//first 37, 48, 26; second 39, 51, 27
		//simpleBuilder.createMergedJunctions();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 42, simpleBuilder.getHigherPrimitives().getMergedJunctionList().size());//FIXME me should be 46; 53 with text; 58 with hatches and wedges
		Assert.assertEquals("lines", 59, simpleBuilder.getDerivedPrimitives().getLineList().size());//74
		Assert.assertEquals("lines", 11, simpleBuilder.getHigherPrimitives().getTramLineList().size());
	}
	
	@Test
	public void testJunctionWithTram() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//List<SVGLine> lineList = simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 6, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//TramLineManager tramLineManager = new TramLineManager();
		//List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		//lineList = tramLineManager.removeUsedTramLinePrimitives(lineList);
		//Assert.assertEquals("tramLines", 1, tramLineList.size());
		Assert.assertEquals("tramLines", 1, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		//List<Joinable> joinableList = JoinManager.makeJoinableList(lineList);
		//Assert.assertEquals("no tram", 4, joinableList.size());
		//joinableList.add(tramLineList.get(0));
		//Assert.assertEquals("joinable", 5, joinableList.size());
		//List<Junction> junctionList = simpleBuilder.createRawJunctionList();
		//Assert.assertEquals("junction", 7, junctionList.size());
		Assert.assertEquals("junction", 8, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
	}

	@Test
	public void testJunctionWithTramAndText() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createHigherPrimitives();
		//List<SVGLine> lineList = simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 6, simpleBuilder.getDerivedPrimitives().getLineList().size());
		//TramLineManager tramLineManager = new TramLineManager();
		//List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		//lineList = tramLineManager.removeUsedTramLinePrimitives(lineList);
		//List<Joinable> joinableList = JoinManager.makeJoinableList(lineList);
		//joinableList.addAll(tramLineList);
		//List<SVGText> textList = simpleBuilder.createRawTextList();
		//for (SVGText svgText : textList) {
			//joinableList.add(new JoinableText(svgText));
		//}
		Assert.assertEquals("text", 11, simpleBuilder.getHigherPrimitives().getJoinableList().size());
		//List<Junction> junctionList = simpleBuilder.createRawJunctionList();
		//for (Junction junction : junctionList) {
		for (Junction junction : simpleBuilder.getHigherPrimitives().getRawJunctionList()) {
			LOG.trace(junction);
		}
		Assert.assertEquals("junction", 8, simpleBuilder.getHigherPrimitives().getRawJunctionList().size());
	}
	
	@Test
	public void testSmall1() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(SMALL_TEST_1));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
	}
	
	@Test
	public void testSmall2() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(SMALL_TEST_2));
		simpleBuilder.createHigherPrimitives();
		drawFromSimpleBuilder(simpleBuilder);
	}
	
	@Test
	public void test215() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_15_SVG));
		simpleBuilder.createHigherPrimitives();
		Assert.assertEquals("lines", 42, simpleBuilder.getDerivedPrimitives().getLineList().size());
		Assert.assertEquals("lines", 28, simpleBuilder.getHigherPrimitives().getLineList().size());
		Assert.assertEquals("tramLines", 7, simpleBuilder.getHigherPrimitives().getTramLineList().size());
		Assert.assertEquals("texts", 24, simpleBuilder.getDerivedPrimitives().getTextList().size());
		Assert.assertEquals("polylines", 2, simpleBuilder.getRawPrimitives().getPolylineList().size());
		Assert.assertEquals("polylines", 2, simpleBuilder.getDerivedPrimitives().getPolylineList().size());
		Assert.assertEquals("polylines", 61, simpleBuilder.getHigherPrimitives().getJoinableList().size());
	}

	// ================= HELPERS ===============

	private void drawFromSimpleBuilder(SimpleBuilder simpleBuilder) {
		SVGG out = new SVGG();
		SVGG circles = new SVGG();
		out.appendChild(circles);
		for (Junction j : simpleBuilder.higherPrimitives.getRawJunctionList()) {
			Real2 coords = (j.getCoordinates() == null ? new Real2(0, 0) : j.getCoordinates());
			SVGCircle c = new SVGCircle(coords, 1.2);
			c.setFill("#FF9999");
			c.setOpacity(0.7);
			c.setStrokeWidth(0.0);
			circles.appendChild(c);
			SVGText t = new SVGText(coords.plus(new Real2(1.5, 0)), j.getId());
			out.appendChild(t);
		}
		/*for (SVGElement l : simpleBuilder.createComplexShapesFromPaths()) {
			SVGShape o = (SVGShape) l.copy();
			o.setStrokeWidth(0.1);
			o.setFill("white");
			out.appendChild(o);
		}*/
		/*for (SVGPath l : simpleBuilder.getExplicitPathList()) {
			SVGPath o = (SVGPath) l.copy();
			o.setStrokeWidth(0.1);
			o.setFill("white");
			out.appendChild(o);
		}*/
		for (SVGText t : simpleBuilder.getDerivedPrimitives().getTextList()) {
			SVGText o = (SVGText) t.copy();
			out.appendChild(o);
		}
		for (SVGLine l : simpleBuilder.getDerivedPrimitives().getLineList()) {
			SVGLine o = (SVGLine) l.copy();
			o.setStrokeWidth(0.4);
			out.appendChild(o);
		}
		SVGSVG.wrapAndWriteAsSVG(out, new File("target/andy.svg"));
	}

}
