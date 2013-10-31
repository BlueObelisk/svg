package org.xmlcml.graphics.svg.builder;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;
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
public class GeometryBuilderTest {

	private final static Logger LOG = Logger.getLogger(GeometryBuilderTest.class);
	public static final Angle MAX_ANGLE = new Angle(0.12, Units.RADIANS);
	public static final Double MAX_WIDTH = 2.0;

	
	@Test
	public void testAllLists() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
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
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
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
		GeometryBuilder geometryBuilder = new GeometryBuilder(svgRoot);
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 13, geometryBuilder.getSingleLineList().size());
	}

	
	@Test
	public void testnonWedgeBondsAndElements1() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_16_SVG);
		GeometryBuilder geometryBuilder = new GeometryBuilder(svgRoot);
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 20, geometryBuilder.getSingleLineList().size());
	}
	
	@Test
	public void testSubscripts() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG);
		GeometryBuilder geometryBuilder = new GeometryBuilder(svgRoot);
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 6, geometryBuilder.getSingleLineList().size());
		
	}
	
	@Test
	public void testWedges() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_18_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 22, geometryBuilder.getSingleLineList().size());
	}

	@Test
	public void testNoRingsOrWedges() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_23_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 24, geometryBuilder.getSingleLineList().size());
	}
	
	@Test
	public void testHard() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_25_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 25, geometryBuilder.getSingleLineList().size());
	}
	
	@Test
	public void test00100() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_02_00100_65_SVG));
		geometryBuilder.createExplicitAndImplicitLines(); // should be 27??
		Assert.assertEquals("lines", 32, geometryBuilder.getSingleLineList().size()); // should be 34
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("lines", 6, geometryBuilder.getTramLineList().size());
	}
		
	@Test
	public void testWithElementPNG5_11() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_11_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		drawFromGeometryBuilder(geometryBuilder);
		Assert.assertEquals("lines", 46, geometryBuilder.getSingleLineList().size());//FIXME should be 47
	}
	@Test
	public void testWithElementPNG5_12() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_12_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		drawFromGeometryBuilder(geometryBuilder);
		Assert.assertEquals("lines", 46, geometryBuilder.getSingleLineList().size());//FIXME should be 47
	}		
	@Test
	public void testWithElementPNG5_13() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_13_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		drawFromGeometryBuilder(geometryBuilder);
		Assert.assertEquals("lines", 87, geometryBuilder.getSingleLineList().size());//FIXME should be 88; missing lines in SVG
	}		
	@Test
	public void testWithElementPNG5_14() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_14_SVG));
		geometryBuilder.createExplicitAndImplicitLines();
		drawFromGeometryBuilder(geometryBuilder);
		Assert.assertEquals("lines", 96, geometryBuilder.getSingleLineList().size());//FIXME should be 95
	}
	
	@Test
	public void testTramLinesG2_11() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		List<SVGLine> lineList = geometryBuilder.createExplicitAndImplicitLines();
		Assert.assertEquals("lines", 6, geometryBuilder.getSingleLineList().size());
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.createTramLineList(lineList);
		Assert.assertEquals("tramLines", 1, tramLineList.size());
	}
	
	@Test
	public void testTramLines() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_13_SVG));
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
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("lines", 4, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("tramLines", 1, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 0, geometryBuilder.getExplicitLineList().size());
	}
	@Test
	public void testTramLines2_13() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_13_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 3, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("singleLines", 13, geometryBuilder.getImplicitLineList().size());
	}
	@Test
	public void testTramLines2_18() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_18_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("paths", 1, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("tramLines", 5, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("singleLines", 21, geometryBuilder.getImplicitLineList().size());
	}
	
	@Test
	public void testTramLines2_23() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_23_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 4, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 0, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 16, geometryBuilder.getSingleLineList().size());
	}
	@Test
	public void testTramLines2_25() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_25_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 5, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 1, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 15, geometryBuilder.getSingleLineList().size());
	}
	@Test
	public void testTramLines5_11() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_11_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		drawFromGeometryBuilder(geometryBuilder);
		Assert.assertEquals("tramLines", 4, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 0, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 38, geometryBuilder.getSingleLineList().size());//FIXME should be 39
	}
	@Test
	public void testTramLines5_12() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_12_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		drawFromGeometryBuilder(geometryBuilder);
		Assert.assertEquals("tramLines", 6, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 1, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 34, geometryBuilder.getSingleLineList().size());//FIXME should be 35, above 0
	}
	@Test
	public void testTramLines5_13() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_13_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		drawFromGeometryBuilder(geometryBuilder);
		Assert.assertEquals("tramLines", 11, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 1, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 65, geometryBuilder.getSingleLineList().size());//FIXME should be 66, above 0
	}
	@Test
	public void testTramLines5_14() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_14_SVG));
		geometryBuilder.createTramLineListAndRemoveUsedLines();
		Assert.assertEquals("tramLines", 13, geometryBuilder.getTramLineList().size());
		Assert.assertEquals("paths", 1, geometryBuilder.getExplicitLineList().size());
		Assert.assertEquals("paths", 70, geometryBuilder.getSingleLineList().size());//FIXME should be 69, above 0
	}
	
	@Test
	public void testWedgeHash() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_18_SVG));
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
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		geometryBuilder.createMergedJunctions(); //, 6, 8, 6);    // fails
		Assert.assertEquals("lines", 4, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 6, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 1, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_13() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_13_SVG));
		geometryBuilder.createMergedJunctions(); //, 13, 17, 10);
		Assert.assertEquals("lines", 10, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 7, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 3, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_18() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_18_SVG));
		geometryBuilder.createMergedJunctions(); //, 21, 23, 14);
		Assert.assertEquals("lines", 15, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 12, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 5, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_23() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_23_SVG));
		geometryBuilder.createMergedJunctions(); //, 24, 37, 21);
		Assert.assertEquals("lines", 22, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 16, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 4, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_25() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_25_SVG));
		geometryBuilder.createMergedJunctions();//no hatches; should be 25, 32, 20; l of Cl not circular enough, =O too near other bonds
		Assert.assertEquals("lines", 22, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 15, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 5, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging5_11() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_11_SVG));
		geometryBuilder.createRawJunctionList();
		drawFromGeometryBuilder(geometryBuilder);
		geometryBuilder.createMergedJunctions();//hatches and arrow; should be 36, 49, 26
		Assert.assertEquals("lines", 24, geometryBuilder.getMergedJunctionList().size());//FIXME should be 26 as not picking up text; 29 with hatches and wedges
		Assert.assertEquals("lines", 38, geometryBuilder.getSingleLineList().size());//Should be 39
		Assert.assertEquals("lines", 4, geometryBuilder.getTramLineList().size());
	}
	
	/*@Test
	public void testJunctionMergingReduced5_11() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(new File("src/test/resources/org/xmlcml/graphics/svg/molecules/image.g.5.11reduced2.svg")));
		geometryBuilder.createRawJunctionList();
		drawFromGeometryBuilder(geometryBuilder);
		geometryBuilder.createMergedJunctions();//hatches and arrow; should be 36, 49, 26
		Assert.assertEquals("lines", 1, geometryBuilder.getMergedJunctionList().size());//FIXME should be 26 as not picking up text; 29 with hatches and wedges
		Assert.assertEquals("lines", 8, geometryBuilder.getSingleLineList().size());//Should be 39
		Assert.assertEquals("lines", 0, geometryBuilder.getTramLineList().size());
	}*/
	
	@Test
	public void testJunctionMerging5_12() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_12_SVG));
		geometryBuilder.createMergedJunctions();
		Assert.assertEquals("lines", 23, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 31, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 6, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging5_13() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_13_SVG));
		geometryBuilder.createMergedJunctions();//first 37, 48, 26; second 39, 51, 27
		Assert.assertEquals("lines", 42, geometryBuilder.getMergedJunctionList().size());
		Assert.assertEquals("lines", 59, geometryBuilder.getSingleLineList().size());
		Assert.assertEquals("lines", 11, geometryBuilder.getTramLineList().size());
	}
	
	@Test
	public void testJunctionWithTram() {
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
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
		GeometryBuilder geometryBuilder = new GeometryBuilder(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
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

	private void drawFromGeometryBuilder(GeometryBuilder geometryBuilder) {
		SVGG out = new SVGG();
		for (Junction j : geometryBuilder.getRawJunctionList()) {
			SVGCircle c = new SVGCircle(j.getCoordinates(), 1.2);
			c.setFill("#FF9999");
			c.setOpacity(0.7);
			c.setStrokeWidth(0.0);
			out.appendChild(c);
			SVGText t = new SVGText(j.getCoordinates().plus(new Real2(1.5, 0)), j.getId());
			out.appendChild(t);
		}
		for (SVGShape l : geometryBuilder.getImplicitShapeList()) {
			SVGShape o = (SVGShape) l.copy();
			o.setStrokeWidth(0.1);
			o.setFill("white");
			out.appendChild(o);
		}
		for (SVGPath l : geometryBuilder.getExplicitPathList()) {
			SVGPath o = (SVGPath) l.copy();
			o.setStrokeWidth(0.1);
			o.setFill("white");
			out.appendChild(o);
		}
		for (SVGLine l : geometryBuilder.getSingleLineList()) {
			SVGLine o = (SVGLine) l.copy();
			o.setStrokeWidth(0.4);
			out.appendChild(o);
		}
		SVGSVG.wrapAndWriteAsSVG(out, new File("target/andy.svg"));
	}

}
