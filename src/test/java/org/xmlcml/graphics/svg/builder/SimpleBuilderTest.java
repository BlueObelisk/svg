package org.xmlcml.graphics.svg.builder;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGShape;
import org.xmlcml.graphics.svg.SVGText;

/** test reading molecules
 * 
 * Reads SVG and uses heuristics to create chemistry.
 * 
 * @author pm286
 *
 */
@Ignore
public class SimpleBuilderTest {

	private final static Logger LOG = Logger.getLogger(SimpleBuilderTest.class);
	public static final Angle MAX_ANGLE = new Angle(0.12, Units.RADIANS);
	public static final Double MAX_WIDTH = 2.0;

	File IMAGE_2_11_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.11.svg");
	File IMAGE_2_13_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.13.svg");
	File IMAGE_2_16_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.16.svg");
	File IMAGE_2_18_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.18.svg");
	File IMAGE_2_23_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.23.svg");
	File IMAGE_2_25_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.25.svg");
	File IMAGE_5_11_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.5.11.svg");
	File IMAGE_5_12_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.5.12.svg");
	File IMAGE_5_13_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.5.13.svg");
	File IMAGE_5_14_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.5.14.svg");
	
	@Test
	public void testAllLists() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("implicit", 6, simpleBuilder.getDerivedLineList().size());
		//Assert.assertEquals("explicit", 0, simpleBuilder.getRawLineList().size());
		//Assert.assertEquals("paths", 6, simpleBuilder.getRawPathList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getRawLineList().size());
		List<SVGLine> singleLineList = simpleBuilder.getSingleLineList();
		Assert.assertEquals("lines", 6, singleLineList.size());
		//Assert.assertEquals("unused paths", 0, simpleBuilder.getRawLineList().size());
		simpleBuilder.createJoinableList();
		Assert.assertEquals("joinable", 6, simpleBuilder.getJoinableList().size());
		List<Junction> junctionList = simpleBuilder.createRawJunctionList();
		Assert.assertEquals("junction", 7, junctionList.size());
	}

	@Test
	public void testJunction() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createRawAndDerivedLines();
		simpleBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tram", 1, simpleBuilder.getTramLineList().size());
		List<Junction> junctionList = simpleBuilder.createMergedJunctions();
		Assert.assertEquals("junctions", 6, junctionList.size());
	}

	
/*	@Test
	public void testnonWedgeBondsAndElements() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(IMAGE_2_13_SVG);
		SimpleBuilder simpleBuilder = new SimpleBuilder(svgRoot);
		simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 13, simpleBuilder.getSingleLineList().size());
	}*/

	
	@Test
	public void testnonWedgeBondsAndElements1() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(IMAGE_2_16_SVG);
		SimpleBuilder simpleBuilder = new SimpleBuilder(svgRoot);
		simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 20, simpleBuilder.getSingleLineList().size());
	}
	
	@Test
	public void testSubscripts() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(IMAGE_2_11_SVG);
		SimpleBuilder simpleBuilder = new SimpleBuilder(svgRoot);
		simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 6, simpleBuilder.getSingleLineList().size());
		
	}
	
	@Test
	public void testWedges() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_18_SVG));
		simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 22, simpleBuilder.getSingleLineList().size());
	}

	@Test
	public void testNoRingsOrWedges() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_23_SVG));
		simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 24, simpleBuilder.getSingleLineList().size());
	}
	
	@Test
	public void testHard() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_25_SVG));
		simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 25, simpleBuilder.getSingleLineList().size());
	}
	
	/*@Test
	public void test00100() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_02_00100_65_SVG));
		simpleBuilder.createRawAndDerivedLines(); // should be 27??
		Assert.assertEquals("lines", 32, simpleBuilder.getSingleLineList().size()); // should be 34
		simpleBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("lines", 6, simpleBuilder.getTramLineList().size());
	}*/
		
	@Test
	public void testWithElementPNG5_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_11_SVG));
		simpleBuilder.createRawAndDerivedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 46, simpleBuilder.getSingleLineList().size());//FIXME should be 47
	}
	@Test
	public void testWithElementPNG5_12() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_12_SVG));
		simpleBuilder.createRawAndDerivedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 46, simpleBuilder.getSingleLineList().size());//FIXME should be 47
	}		
	@Test
	public void testWithElementPNG5_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_13_SVG));
		simpleBuilder.createRawAndDerivedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 87, simpleBuilder.getSingleLineList().size());//FIXME should be 88; missing lines in SVG
	}		
	@Test
	public void testWithElementPNG5_14() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_14_SVG));
		simpleBuilder.createRawAndDerivedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("lines", 95, simpleBuilder.getSingleLineList().size());
	}
	
	@Test
	public void testTramLinesG2_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		List<SVGLine> lineList = simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 6, simpleBuilder.getSingleLineList().size());
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		Assert.assertEquals("tramLines", 1, tramLineList.size());
	}
	
	@Test
	public void testTramLines() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_13_SVG));
		Assert.assertNull("singleLines", simpleBuilder.getSingleLineList());
		Assert.assertNull("explicitLines", simpleBuilder.getRawLineList());
		Assert.assertNull("implicitLines", simpleBuilder.getDerivedLineList());
		//Assert.assertNull("paths", simpleBuilder.getExplicitPathList());
		simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("singleLines", 13, simpleBuilder.getSingleLineList().size());
		//Assert.assertEquals("explicitLines", null, simpleBuilder.getRawLineList().size());
		Assert.assertEquals("implicitLines", 13, simpleBuilder.getDerivedLineList().size());
		// creating lines has removed paths
		Assert.assertEquals("paths", 0, simpleBuilder.getCurrentPathList().size());
		List<TramLine> tramLineList = simpleBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("implicitLines", 13, simpleBuilder.getDerivedLineList().size());
		//TramLine creation removes single lines
		Assert.assertEquals("tramLines", 3, tramLineList.size());
		Assert.assertEquals("singleLines", 7, simpleBuilder.getSingleLineList().size());
		//Assert.assertEquals("explicitLines", 0, simpleBuilder.getRawLineList().size());

	}
	@Test
	public void testTramLines2_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		simpleBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("lines", 4, simpleBuilder.getSingleLineList().size());
		Assert.assertEquals("tramLines", 1, simpleBuilder.getTramLineList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getRawLineList().size());
	}
	@Test
	public void testTramLines2_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_13_SVG));
		simpleBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 3, simpleBuilder.getTramLineList().size());
		Assert.assertEquals("singleLines", 13, simpleBuilder.getDerivedLineList().size());
	}
	@Test
	public void testTramLines2_18() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_18_SVG));
		simpleBuilder.createTramLineListAndRemoveUsedLines();
		//Assert.assertEquals("paths", 1, simpleBuilder.getRawLineList().size());
		Assert.assertEquals("tramLines", 5, simpleBuilder.getTramLineList().size());
		Assert.assertEquals("singleLines", 21, simpleBuilder.getDerivedLineList().size());
	}
	
	@Test
	public void testTramLines2_23() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_23_SVG));
		simpleBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 4, simpleBuilder.getTramLineList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getRawLineList().size());
		Assert.assertEquals("paths", 16, simpleBuilder.getSingleLineList().size());
	}
	@Test
	public void testTramLines2_25() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_25_SVG));
		simpleBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 5, simpleBuilder.getTramLineList().size());
		//Assert.assertEquals("paths", 1, simpleBuilder.getRawLineList().size());
		Assert.assertEquals("paths", 15, simpleBuilder.getSingleLineList().size());
	}
	@Test
	public void testTramLines5_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_11_SVG));
		simpleBuilder.createTramLineListAndRemoveUsedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("tramLines", 4, simpleBuilder.getTramLineList().size());
		//Assert.assertEquals("paths", 0, simpleBuilder.getRawLineList().size());
		Assert.assertEquals("paths", 38, simpleBuilder.getSingleLineList().size());//FIXME should be 39
	}
	@Test
	public void testTramLines5_12() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_12_SVG));
		simpleBuilder.createTramLineListAndRemoveUsedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("tramLines", 6, simpleBuilder.getTramLineList().size());
		//Assert.assertEquals("paths", 1, simpleBuilder.getRawLineList().size());
		Assert.assertEquals("paths", 34, simpleBuilder.getSingleLineList().size());//FIXME should be 35, above 0
	}
	@Test
	public void testTramLines5_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_13_SVG));
		simpleBuilder.createTramLineListAndRemoveUsedLines();
		drawFromSimpleBuilder(simpleBuilder);
		Assert.assertEquals("tramLines", 11, simpleBuilder.getTramLineList().size());
		//Assert.assertEquals("paths", 1, simpleBuilder.getRawLineList().size());
		Assert.assertEquals("paths", 65, simpleBuilder.getSingleLineList().size());//FIXME should be 66, above 0
	}
	@Test
	public void testTramLines5_14() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_14_SVG));
		simpleBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 13, simpleBuilder.getTramLineList().size());
		//Assert.assertEquals("paths", 1, simpleBuilder.getRawLineList().size());
		Assert.assertEquals("paths", 69, simpleBuilder.getSingleLineList().size());
	} 
	
	@Test
	public void testWedgeHash() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_18_SVG));
		simpleBuilder.createRawAndDerivedLines();
		// this contained a rect translated to a line
		//Assert.assertEquals("explicitLines", 1, simpleBuilder.getRawLineList().size());
		Assert.assertEquals("implicitLines", 21, simpleBuilder.getDerivedLineList().size());
		Assert.assertEquals("singleLines", 22, simpleBuilder.getSingleLineList().size());
		Assert.assertEquals("paths", 0, simpleBuilder.getCurrentPathList().size());
		// polygon and 5 circles
		Assert.assertEquals("shapes", 6, simpleBuilder.getCurrentShapeList().size());
		// creating lines has removed paths
		Assert.assertEquals("paths", 0, simpleBuilder.getCurrentPathList().size());
		List<TramLine> tramLineList = simpleBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("implicitLines", 21, simpleBuilder.getDerivedLineList().size());
		//TramLine creation removes single lines
		Assert.assertEquals("singleLines", 12, simpleBuilder.getSingleLineList().size());
		Assert.assertEquals("tramLines", 5, tramLineList.size());
		Assert.assertEquals("explicitLines", 1, simpleBuilder.getRawLineList().size());

	}
	
	
	@Test
	public void testJunctionMerging2_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		//, 6, 8, 6);    // fails
		Assert.assertEquals("lines", 6, simpleBuilder.createMergedJunctions().size());
		Assert.assertEquals("lines", 4, simpleBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 1, simpleBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_13_SVG));
		//, 13, 17, 10);
		Assert.assertEquals("lines", 10, simpleBuilder.createMergedJunctions().size());
		Assert.assertEquals("lines", 7, simpleBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 3, simpleBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_18() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_18_SVG));
		//, 21, 23, 14);
		Assert.assertEquals("lines", 15, simpleBuilder.createMergedJunctions().size());
		Assert.assertEquals("lines", 12, simpleBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 5, simpleBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_23() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_23_SVG));
		//, 24, 37, 21);
		Assert.assertEquals("lines", 22, simpleBuilder.createMergedJunctions().size());
		Assert.assertEquals("lines", 16, simpleBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 4, simpleBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_25() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_25_SVG));
		//no hatches; should be 25, 32, 20; l of Cl not circular enough, =O too near other bonds
		Assert.assertEquals("lines", 22, simpleBuilder.createMergedJunctions().size());
		Assert.assertEquals("lines", 15, simpleBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 5, simpleBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging5_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_11_SVG));
		simpleBuilder.createRawJunctionList();
		drawFromSimpleBuilder(simpleBuilder);
		//hatches and arrow; should be 36, 49, 26
		Assert.assertEquals("lines", 24, simpleBuilder.createMergedJunctions().size());//FIXME should be 26 as not picking up text; 29 with hatches and wedges
		Assert.assertEquals("lines", 38, simpleBuilder.getSingleLineList().size());//Should be 39
		Assert.assertEquals("lines", 4, simpleBuilder.getTramLineList().size());
	}
	
	/*@Test
	public void testJunctionMergingReduced5_11() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(new File("src/test/resources/org/xmlcml/graphics/svg/molecules/image.g.5.11reduced2.svg")));
		simpleBuilder.createRawJunctionList();
		drawFromSimpleBuilder(simpleBuilder);
		simpleBuilder.createMergedJunctions();//hatches and arrow; should be 36, 49, 26
		Assert.assertEquals("lines", 1, simpleBuilder.createMergedJunctions().size());//FIXME should be 26 as not picking up text; 29 with hatches and wedges
		Assert.assertEquals("lines", 8, simpleBuilder.getSingleLineList().size());//Should be 39
		Assert.assertEquals("lines", 0, simpleBuilder.getTramLineList().size());
	}*/
	
	@Test
	public void testJunctionMerging5_12() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_12_SVG));
		Assert.assertEquals("lines", 23, simpleBuilder.createMergedJunctions().size());
		Assert.assertEquals("lines", 31, simpleBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 6, simpleBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging5_13() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_5_13_SVG));
		//first 37, 48, 26; second 39, 51, 27
		Assert.assertEquals("lines", 42, simpleBuilder.createMergedJunctions().size());
		Assert.assertEquals("lines", 59, simpleBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 11, simpleBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionWithTram() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		List<SVGLine> lineList = simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 6, simpleBuilder.getSingleLineList().size());
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		lineList = tramLineManager.removeUsedTramLinePrimitives(lineList);
		Assert.assertEquals("tramLines", 1, tramLineList.size());
		List<Joinable> joinableList = JoinManager.makeJoinableList(lineList);
		Assert.assertEquals("no tram", 4, joinableList.size());
		joinableList.add(tramLineList.get(0));
		Assert.assertEquals("joinable", 5, joinableList.size());
		List<Junction> junctionList = simpleBuilder.createRawJunctionList();
		Assert.assertEquals("junction", 7, junctionList.size());
	}

	@Test
	public void testJunctionWithTramAndText() {
		SimpleBuilder simpleBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(IMAGE_2_11_SVG));
		List<SVGLine> lineList = simpleBuilder.createRawAndDerivedLines();
		Assert.assertEquals("lines", 6, simpleBuilder.getSingleLineList().size());
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		lineList = tramLineManager.removeUsedTramLinePrimitives(lineList);
		List<Joinable> joinableList = JoinManager.makeJoinableList(lineList);
		joinableList.addAll(tramLineList);
		List<SVGText> textList = simpleBuilder.createRawTextList();
		for (SVGText svgText : textList) {
			joinableList.add(new JoinableText(svgText));
		}
		Assert.assertEquals("text", 11, joinableList.size());
		List<Junction> junctionList = simpleBuilder.createRawJunctionList();
		for (Junction junction : junctionList) {
			LOG.trace(junction);
		}
		Assert.assertEquals("junction", 7, junctionList.size());
	}

	// ================= HELPERS ===============

	private void drawFromSimpleBuilder(SimpleBuilder simpleBuilder) {
		SVGG out = new SVGG();
		try {
			for (Junction j : simpleBuilder.higherPrimitives.getRawJunctionList()) {
				SVGCircle c = new SVGCircle(j.getCoordinates(), 1.2);
				c.setFill("#FF9999");
				c.setOpacity(0.7);
				c.setStrokeWidth(0.0);
				out.appendChild(c);
				SVGText t = new SVGText(j.getCoordinates().plus(new Real2(1.5, 0)), j.getId());
				out.appendChild(t);
			}
		} catch (Exception e) {
			
		}
		for (SVGElement l : simpleBuilder.createComplexShapesFromPaths()) {
			SVGShape o = (SVGShape) l.copy();
			o.setStrokeWidth(0.1);
			o.setFill("white");
			out.appendChild(o);
		}
		/*for (SVGPath l : simpleBuilder.getExplicitPathList()) {
			SVGPath o = (SVGPath) l.copy();
			o.setStrokeWidth(0.1);
			o.setFill("white");
			out.appendChild(o);
		}*/
		try {
			for (SVGLine l : simpleBuilder.getSingleLineList()) {
				SVGLine o = (SVGLine) l.copy();
				o.setStrokeWidth(0.4);
				out.appendChild(o);
			}
		} catch (Exception e) {
			
		}
		SVGSVG.wrapAndWriteAsSVG(out, new File("target/andy.svg"));
	}

}
