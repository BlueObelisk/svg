package org.xmlcml.graphics.svg.join;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Angle.Units;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.SVGText;

/** test reading molecules
 * 
 * Reads SVG and uses heuristics to create chemistry.
 * 
 * @author pm286
 *
 */
public class JunctionTest {

	private final static Logger LOG = Logger.getLogger(JunctionTest.class);
	public static final Angle MAX_ANGLE = new Angle(0.12, Units.RADIANS);
	public static final Double MAX_WIDTH = 2.0;

	
	@Test
	public void testJunction0() {
		JunctionCreator junctionCreator = new JunctionCreator();
		List<SVGLine> lineList = junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		Assert.assertEquals("lines", 6, junctionCreator.getLineList().size());
		Assert.assertEquals("paths", 6, junctionCreator.getPathList().size());
		List<Joinable> joinableList = JoinManager.makeJoinableList(lineList);
		// remove lines which are better described as tramlines
		joinableList.remove(5);
		joinableList.remove(4);
		Assert.assertEquals("no tram", 4, joinableList.size());
		List<Junction> junctionList = junctionCreator.makeRawJunctionList(joinableList);
		Assert.assertEquals("junction", 3, junctionList.size());
	}

	
	@Test
	public void testnonWedgeBondsAndElements() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_13_SVG);
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.createLinesFromOutlines(svgRoot);
		Assert.assertEquals("lines", 13, junctionCreator.getLineList().size());
	}

	
	@Test
	public void testnonWedgeBondsAndElements1() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_16_SVG);
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.createLinesFromOutlines(svgRoot);
		Assert.assertEquals("lines", 20, junctionCreator.getLineList().size());
	}
	
	@Test
	public void testSubscripts() {
		SVGElement svgRoot = SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG);
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.createLinesFromOutlines(svgRoot);
		Assert.assertEquals("lines", 6, junctionCreator.getLineList().size());
		
	}
	
	@Test
	public void testWedges() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_18_SVG));
		Assert.assertEquals("lines", 21, junctionCreator.getLineList().size());
	}

	@Test
	public void testNoRingsOrWedges() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_23_SVG));
		Assert.assertEquals("lines", 24, junctionCreator.getLineList().size());
	}
	
	@Test
	public void testHard() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_25_SVG));
		Assert.assertEquals("lines", 24, junctionCreator.getLineList().size());
	}
	
	@Test
	public void test00100() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_02_00100_65_SVG)); // should be 27??
		Assert.assertEquals("lines", 30, junctionCreator.getLineList().size()); // should be 34
		junctionCreator.makeTramLines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_02_00100_65_SVG));
// FIXME		Assert.assertEquals("lines", 6, junctionCreator.getTramLineList().size());
	}
	
	
	@Test
	public void testWithElementPNG5_11() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_11_SVG));
		Assert.assertEquals("lines", 46, junctionCreator.getLineList().size()); // FIXME should be 48
	}		
	@Test
	public void testWithElementPNG5_12() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_12_SVG));
// FIXME		Assert.assertEquals("lines", 51, junctionCreator.getLineList().size());
	}		
	@Test
	public void testWithElementPNG5_13() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_13_SVG));
// FIXME		Assert.assertEquals("lines", 88, junctionCreator.getLineList().size());  
	}		
	@Test
	public void testWithElementPNG5_14() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_14_SVG));
		Assert.assertEquals("lines", 95, junctionCreator.getLineList().size());
	}
	
	@Test
	public void testTramLinesG2_11() {
		JunctionCreator junctionCreator = new JunctionCreator();
		List<SVGLine> lineList = junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		Assert.assertEquals("lines", 6, junctionCreator.getLineList().size());
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.makeTramLineList(lineList);
		Assert.assertEquals("tramLines", 1, tramLineList.size());
	}
	
	@Test
	public void testTramLines() {
		JunctionCreator junctionCreator = new JunctionCreator();
		List<SVGLine> lineList = junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_13_SVG));
		Assert.assertEquals("linelist", 13, lineList.size());
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.makeTramLineList(lineList);
		Assert.assertEquals("tramLines", 3, tramLineList.size());

	}
	
	@Test
	public void testTramLines2_13() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.makeTramLines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_13_SVG));
// FIXME		Assert.assertEquals("tramLines", 3, junctionCreator.getTramLineList().size());
		Assert.assertEquals("paths", 13, junctionCreator.getPathList().size());
	}
	@Test
	public void testTramLines2_11() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.makeTramLines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
// FIXME		Assert.assertEquals("tramLines", 1, junctionCreator.getTramLineList().size());
		Assert.assertEquals("paths", 6, junctionCreator.getPathList().size());
	}
	@Test
	public void testTramLines2_18() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.makeTramLines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_18_SVG));
		Assert.assertEquals("paths", 21, junctionCreator.getPathList().size());
// FIXME		Assert.assertEquals("tramLines", 5, junctionCreator.getTramLineList().size());
	}
	@Test
	public void testTramLines2_23() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.makeTramLines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_23_SVG));
// FIXME		Assert.assertEquals("tramLines", 4, junctionCreator.getTramLineList().size());
		Assert.assertEquals("paths", 24, junctionCreator.getPathList().size());
	}
	@Test
	public void testTramLines2_25() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.makeTramLines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_25_SVG));
// FIXME		Assert.assertEquals("tramLines", 5, junctionCreator.getTramLineList().size());
		Assert.assertEquals("paths", 24, junctionCreator.getPathList().size());
	}
	@Test
	public void testTramLines5_11() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.makeTramLines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_11_SVG));
// FIXME		Assert.assertEquals("tramLines", 4, junctionCreator.getTramLineList().size());
		Assert.assertEquals("paths", 48, junctionCreator.getPathList().size());
	}
	@Test
	public void testTramLines5_12() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.makeTramLines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_12_SVG));
// FIXME		Assert.assertEquals("tramLines", 6, junctionCreator.getTramLineList().size());
		Assert.assertEquals("paths", 51, junctionCreator.getPathList().size());
	}
	@Test
	public void testTramLines5_13() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.makeTramLines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_13_SVG));
// FIXME		Assert.assertEquals("tramLines", 11, junctionCreator.getTramLineList().size());
		Assert.assertEquals("paths", 88, junctionCreator.getPathList().size());
	}
	@Test
	public void testTramLines5_14() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.makeTramLines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_14_SVG));
// FIXME		Assert.assertEquals("tramLines", 13, junctionCreator.getTramLineList().size());
		Assert.assertEquals("paths", 95, junctionCreator.getPathList().size());
	}
	
	@Test
	public void testJunctionMerging2_11() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.mergeJunctionsFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG)); //, 6, 8, 6);    // fails
		Assert.assertEquals("lines", 4, junctionCreator.getLineList().size());
		Assert.assertEquals("lines", 6, junctionCreator.getMergedJunctionList().size());
		Assert.assertEquals("lines", 0, junctionCreator.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_13() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.mergeJunctionsFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_13_SVG)); //, 13, 17, 10);
		Assert.assertEquals("lines", 10, junctionCreator.getMergedJunctionList().size());
		Assert.assertEquals("lines", 7, junctionCreator.getLineList().size());
		Assert.assertEquals("lines", 0, junctionCreator.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_18() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.mergeJunctionsFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_18_SVG)); //, 21, 23, 14);
		Assert.assertEquals("lines", 15, junctionCreator.getMergedJunctionList().size());
		Assert.assertEquals("lines", 11, junctionCreator.getLineList().size());
		Assert.assertEquals("lines", 0, junctionCreator.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_23() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.mergeJunctionsFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_23_SVG)); //, 24, 37, 21);
		Assert.assertEquals("lines", 22, junctionCreator.getMergedJunctionList().size());
		Assert.assertEquals("lines", 16, junctionCreator.getLineList().size());
		Assert.assertEquals("lines", 0, junctionCreator.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging2_25() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.mergeJunctionsFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_25_SVG));//no hatches; should be 25, 32, 20; l of Cl not circular enough, =O too near other bonds
		Assert.assertEquals("lines", 22, junctionCreator.getMergedJunctionList().size());
		Assert.assertEquals("lines", 14, junctionCreator.getLineList().size());
		Assert.assertEquals("lines", 0, junctionCreator.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging5_11() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.mergeJunctionsFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_11_SVG));//hatches and arrow; should be 36, 49, 26
		Assert.assertEquals("lines", 24, junctionCreator.getMergedJunctionList().size());
		Assert.assertEquals("lines", 38, junctionCreator.getLineList().size());
		Assert.assertEquals("lines", 0, junctionCreator.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging5_12() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.mergeJunctionsFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_12_SVG));
		Assert.assertEquals("lines", 23, junctionCreator.getMergedJunctionList().size());
		Assert.assertEquals("lines", 33, junctionCreator.getLineList().size());
		Assert.assertEquals("lines", 0, junctionCreator.getTramLineList().size());
	}
	
	@Test
	public void testJunctionMerging5_13() {
		JunctionCreator junctionCreator = new JunctionCreator();
		junctionCreator.mergeJunctionsFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_5_13_SVG));//first 37, 48, 26; second 39, 51, 27
		Assert.assertEquals("lines", 45, junctionCreator.getMergedJunctionList().size());
		Assert.assertEquals("lines", 64, junctionCreator.getLineList().size());
		Assert.assertEquals("lines", 0, junctionCreator.getTramLineList().size());
	}
	
	@Test
	public void testJunctionWithTram() {
		JunctionCreator junctionCreator = new JunctionCreator();
		List<SVGLine> lineList = junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		Assert.assertEquals("lines", 6, junctionCreator.getLineList().size());
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.makeTramLineList(lineList);
		lineList = tramLineManager.removeUsedTramLinePrimitives(lineList);
		Assert.assertEquals("tramLines", 1, tramLineList.size());
		List<Joinable> joinableList = JoinManager.makeJoinableList(lineList);
		Assert.assertEquals("no tram", 4, joinableList.size());
		joinableList.add(tramLineList.get(0));
		Assert.assertEquals("joinable", 5, joinableList.size());
		List<Junction> junctionList = junctionCreator.makeRawJunctionList(joinableList);
		Assert.assertEquals("junction", 5, junctionList.size());
	}

	@Test
	public void testJunctionWithTramAndText() {
		JunctionCreator junctionCreator = new JunctionCreator();
		List<SVGLine> lineList = junctionCreator.createLinesFromOutlines(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		Assert.assertEquals("lines", 6, junctionCreator.getLineList().size());
		TramLineManager tramLineManager = new TramLineManager();
		List<TramLine> tramLineList = tramLineManager.makeTramLineList(lineList);
		lineList = tramLineManager.removeUsedTramLinePrimitives(lineList);
		List<Joinable> joinableList = JoinManager.makeJoinableList(lineList);
		joinableList.addAll(tramLineList);
		List<SVGText> textList = junctionCreator.createTextListAndAddIds(SVGElement.readAndCreateSVG(Fixtures.IMAGE_2_11_SVG));
		for (SVGText svgText : textList) {
			joinableList.add(new JoinableText(svgText));
		}
		Assert.assertEquals("text", 11, joinableList.size());
		List<Junction> junctionList = junctionCreator.makeRawJunctionList(joinableList);
		for (Junction junction : junctionList) {
			LOG.trace(junction);
		}
		Assert.assertEquals("junction", 8, junctionList.size());
	}

	// ================= HELPERS ===============



}
