package org.xmlcml.graphics.svg.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGShape;
import org.xmlcml.graphics.svg.SVGText;

/** test reading molecules
 * 
 * Reads SVG and uses heuristics to create chemistry.
 * 
 * @author pm286
 *
 */
public class GeometryBuilderTest {

	private final static Logger LOG = Logger.getLogger(GeometryBuilderTest.class);
	public static final Angle MAX_ANGLE = new Angle(0.12, Units.RADIANS);
	public static final Double MAX_WIDTH = 2.0;

	
	@Test
	public void testAllLists() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("implicit", 6, geometryBuilder.getImplicitLineList().size());
		Assert.assertEquals("explicit", 0, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 6, geometryBuilder.getExplicitPathList().size());
		Assert.assertEquals("paths", 0, geometryBuilder.getExplicitLineList().size());
		List<SVGLine> singleLineList = geometryBuilder.getSingleLineList();
		Assert.assertEquals("lines", 6, singleLineList.size());
		Assert.assertEquals("unused paths", 0, geometryBuilder.getExplicitLineList().size());
		geometryBuilder.createJoinableList();
		Assert.assertEquals("joinable", 6, geometryBuilder.getJoinableList().size());
		List<Junction> junctionList = geometryBuilder.createRawJunctionList();
		Assert.assertEquals("junction", 7, junctionList.size());
	}

	@Test
	public void testJunction() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tram", 1, geometryBuilder.getTramLineList().size());
		geometryBuilder.createMergedJunctions();
		List<Junction> junctionList = geometryBuilder.getMergedJunctionList();
		Assert.assertEquals("junctions", 6, junctionList.size());
	}

	
	@Test
	public void testnonWedgeBondsAndElements() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_13_SVG);
		SimpleBuilder geometryBuilder = new SimpleBuilder(svgRoot);
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 13, geometryBuilder.getSingleLineList().size());
	}

	
	@Test
	public void testnonWedgeBondsAndElements1() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_16_SVG);
		SimpleBuilder geometryBuilder = new SimpleBuilder(svgRoot);
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 20, geometryBuilder.getSingleLineList().size());
	}
	
	@Test
	public void testSubscripts() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG);
		SimpleBuilder geometryBuilder = new SimpleBuilder(svgRoot);
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 6, geometryBuilder.getSingleLineList().size());
		
	}
	
	@Test
	public void testWedges() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_18_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 22, geometryBuilder.getSingleLineList().size());
	}

	@Test
	public void testNoRingsOrWedges() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_23_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 24, geometryBuilder.getSingleLineList().size());
	}
	
	@Test
	public void testHard() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_25_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 25, geometryBuilder.getSingleLineList().size());
	}
	
	@Test
	public void test00100() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_02_00100_65_SVG));
		geometryBuilder.createExplicitAndImplicitLines(); // should be 27??
		Assert.assertEquals("lines", 32, geometryBuilder.getSingleLineList().size()); // should be 34
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("lines", 6, geometryBuilder.getTramLineList().size());
	}
	
	
	@Test
	public void testWithElementPNG5_11() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_11_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 40, geometryBuilder.getSingleLineList().size()); // FIXME should be 48
	}		
	@Test
	public void testWithElementPNG5_12() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_12_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
// FIXME		Assert.assertEquals("lines", 51, geometryBuilder.getLineList().size());
	}		
	@Test
	public void testWithElementPNG5_13() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_13_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 81, geometryBuilder.getSingleLineList().size());  
	}		
	@Test
	public void testWithElementPNG5_14() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_14_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 91, geometryBuilder.getSingleLineList().size());
	}
	
	@Test
	public void testTramLinesG2_11() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		List<SVGLine> lineList = geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 6, geometryBuilder.getSingleLineList().size());
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		Assert.assertEquals("tramLines", 1, tramLineList.size());
	}
	
	@Test
	public void testTramLines() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_13_SVG));
		Assert.assertNull("singleLines", geometryBuilder.getSingleLineList());
		Assert.assertNull("explicitLines", geometryBuilder.getExplicitLineList());
		Assert.assertNull("implicitLines", geometryBuilder.getImplicitLineList());
		Assert.assertNull("paths", geometryBuilder.getExplicitPathList());
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("singleLines", 13, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("explicitLines", 0, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("implicitLines", 13, geometryBuilder.getImplicitLineList().size());
		// creating lines has removed paths
		Assert.assertEquals("paths", 0, geometryBuilder.getCurrentPathList().size());
		List<TramLine> tramLineList = geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("implicitLines", 13, geometryBuilder.getImplicitLineList().size());
		//TramLine creation removes single lines
		Assert.assertEquals("tramLines", 3, tramLineList.size());
		Assert.assertEquals("singleLines", 7, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("explicitLines", 0, geometryBuilder.getExplicitLineList().size());

	}
	@Test
	public void testTramLines2_11() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("lines", 4, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("tramLines", 1, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 0, geometryBuilder.getExplicitLineList().size());
	}
	@Test
	public void testTramLines2_13() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_13_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 3, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("singleLines", 13, geometryBuilder.getImplicitLineList().size());
	}
	@Test
	public void testTramLines2_18() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_18_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("paths", 1, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("tramLines", 5, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("singleLines", 21, geometryBuilder.getImplicitLineList().size());
	}
	
	@Test
	public void testTramLines2_23() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_23_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 4, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 0, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 16, geometryBuilder.getSingleLineList().size());
	}
	@Test
	public void testTramLines2_25() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_25_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 5, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 1, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 15, geometryBuilder.getSingleLineList().size());
	}
	@Test
	public void testTramLines5_11() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_11_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 4, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 0, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 32, geometryBuilder.getSingleLineList().size());
	}
	@Test
	public void testTramLines5_12() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_12_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 6, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 1, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 31, geometryBuilder.getSingleLineList().size());
	}
	@Test
	public void testTramLines5_13() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_13_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 11, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 1, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 59, geometryBuilder.getSingleLineList().size());
	}
	@Test
	public void testTramLines5_14() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_14_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 12, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 1, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 67, geometryBuilder.getSingleLineList().size());
	}
	
	@Test
	public void testWedgeHash() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_18_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		// this contained a rect translated to a line
		Assert.assertEquals("explicitLines", 1, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("implicitLines", 21, geometryBuilder.getImplicitLineList().size());
		Assert.assertEquals("singleLines", 22, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("paths", 0, geometryBuilder.getCurrentPathList().size());
		// polygon and 5 circles
		Assert.assertEquals("shapes", 6, geometryBuilder.getCurrentShapeList().size());
		// creating lines has removed paths
		Assert.assertEquals("paths", 0, geometryBuilder.getCurrentPathList().size());
		List<TramLine> tramLineList = geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("implicitLines", 21, geometryBuilder.getImplicitLineList().size());
		//TramLine creation removes single lines
		Assert.assertEquals("singleLines", 12, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("tramLines", 5, tramLineList.size());
		Assert.assertEquals("explicitLines", 1, geometryBuilder.getExplicitLineList().size());

	}
	
	
	@Test
	public void testJunctionMerging2_11() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		geometryBuilder.createMergedJunctions(); //, 6, 8, 6);    // fails
		Assert.assertEquals("lines", 4, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 6, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 1, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_13() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_13_SVG));
		geometryBuilder.createMergedJunctions(); //, 13, 17, 10);
		Assert.assertEquals("lines", 10, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 7, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 3, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_18() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_18_SVG));
		geometryBuilder.createMergedJunctions(); //, 21, 23, 14);
		Assert.assertEquals("lines", 15, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 12, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 5, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_23() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_23_SVG));
		geometryBuilder.createMergedJunctions(); //, 24, 37, 21);
		Assert.assertEquals("lines", 22, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 16, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 4, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_25() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_25_SVG));
		geometryBuilder.createMergedJunctions();//no hatches; should be 25, 32, 20; l of Cl not circular enough, =O too near other bonds
		Assert.assertEquals("lines", 22, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 15, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 5, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging5_11() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_11_SVG));
		geometryBuilder.createMergedJunctions();//hatches and arrow; should be 36, 49, 26
		Assert.assertEquals("lines", 23, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 32, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 4, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging5_12() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_12_SVG));
		geometryBuilder.createMergedJunctions();
		Assert.assertEquals("lines", 23, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 31, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 6, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging5_13() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_13_SVG));
		geometryBuilder.createMergedJunctions();//first 37, 48, 26; second 39, 51, 27
		Assert.assertEquals("lines", 42, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 59, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 11, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionWithTram() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		List<SVGLine> lineList = geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 6, geometryBuilder.getSingleLineList().size());
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		lineList = tramLineManager.removeUsedTramLinePrimitives(lineList);
		Assert.assertEquals("tramLines", 1, tramLineList.size());
		List<Joinable> joinableList = JoinManager.makeJoinableList(lineList);
		Assert.assertEquals("no tram", 4, joinableList.size());
		joinableList.add(tramLineList.get(0));
		Assert.assertEquals("joinable", 5, joinableList.size());
		List<Junction> junctionList = geometryBuilder.createRawJunctionList();
		Assert.assertEquals("junction", 7, junctionList.size());
	}

	@Test
	public void testJunctionWithTramAndText() {
		SimpleBuilder geometryBuilder = new SimpleBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		List<SVGLine> lineList = geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 6, geometryBuilder.getSingleLineList().size());
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		lineList = tramLineManager.removeUsedTramLinePrimitives(lineList);
		List<Joinable> joinableList = JoinManager.makeJoinableList(lineList);
		joinableList.addAll(tramLineList);
		List<SVGText> textList = geometryBuilder.createExplicitTextList();
		for (SVGText svgText : textList) {
			joinableList.add(new JoinableText(svgText));
		}
		Assert.assertEquals("text", 11, joinableList.size());
		List<Junction> junctionList = geometryBuilder.createRawJunctionList();
		for (Junction junction : junctionList) {
			LOG.trace(junction);
		}
		Assert.assertEquals("junction", 7, junctionList.size());
	}

	// ================= HELPERS ===============



}
