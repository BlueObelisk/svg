package org.xmlcml.graphics.svg.path;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.graphics.svg.Fixtures;
import org.xmlcml.graphics.svg.SVGCircle;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;
import org.xmlcml.graphics.svg.SVGPath;
import org.xmlcml.graphics.svg.SVGPolygon;
import org.xmlcml.graphics.svg.SVGRect;
import org.xmlcml.graphics.svg.SVGShape;
import org.xmlcml.graphics.svg.SVGUtil;

public class Path2ShapeConverterTest {

	private final static Logger LOG = Logger.getLogger(Path2ShapeConverter.class);
	
	@Test
	public void nopathTest() {
		List<SVGShape> shapeList = createShapeList(Fixtures.PATHS_NOPATH_SVG);
		Assert.assertEquals("converted", 0, shapeList.size());
	}

	@Test
	public void rectTest() {
		List<SVGShape> shapeList = createShapeList(Fixtures.PATHS_TEXT_LINE_SVG);
		Assert.assertEquals("converted", 1, shapeList.size());
		Assert.assertEquals("rect", 
				"<rect fill=\"#000000\" stroke=\"black\" stroke-width=\"0.0\" x=\"42.52\" y=\"144.433\" height=\"3.005\" width=\"520.044\" id=\"rect.0\" />", 
				shapeList.get(0).toXML());
	}
	
	
	@Test
	/** The BMC Evolutionary Biology logo as paths.
	 * The small 'l' should be interpreted as a rect
	 * The small 'o' should be a circle // no longer works
	 * The 'M' should be a polyline
	 * The 'E' should ultimately break into two polylines
	 * The 'u', 'm', etc have curves and so won't be changed
	 */
	@Ignore // output of path analysis may have changed
	public void bmcLogoTest() {
		List<SVGShape> shapeList = createShapeList(Fixtures.PATHS_BMCLOGO_SVG);
		Assert.assertEquals("converted", 23, shapeList.size());
		Assert.assertEquals("E", 
				"<polygon fill=\"magenta\" stroke=\"green\" stroke-width=\"0.5\"" +
				" points=\"435.968 61.082 440.167 61.082 440.167 62.027 437.042 62.027 437.042 64.218 439.888 64.218 439.888 65.163" +
				" 437.042 65.163 437.042 67.633 440.167 67.633 440.167 68.578 435.968 68.578\" id=\"polygon.0\" />",
				shapeList.get(0).toXML());
		// "v" omitted
//		Assert.assertEquals("o", 
//				"<circle fill=\"magenta\" stroke=\"green\" stroke-width=\"0.5\"" +
//				" cx=\"449.58\" cy=\"65.84\" r=\"2.665\" id=\"circle.2\" />",
//				shapeList.get(2).toXML());
		Assert.assertEquals("lower case l", 
				"<rect fill=\"magenta\" stroke=\"green\" stroke-width=\"0.5\"" +
				" x=\"453.849\" y=\"60.523\" height=\"8.055\" width=\"1.009\" id=\"rect.3\" />",
				shapeList.get(3).toXML());
		Assert.assertEquals("unconverted u", 
				"<path xmlns=\"http://www.w3.org/2000/svg\" clip-path=\"url(#clipPath1)\" fill=\"magenta\" stroke=\"green\"" +
				" stroke-width=\"0.5\" d=\"M461.548 68.578 L460.571 68.578 L460.571 67.708 L460.55 67.708" +
				" C460.249 68.331 459.519 68.707 458.756 68.707 C457.338 68.707 456.705 67.827 456.705 66.355" +
				" L456.705 63.101 L457.714 63.101 L457.714 65.936 C457.714 67.214 457.994 67.837 458.874 67.891" +
				" C460.023 67.891 460.539 66.967 460.539 65.636 L460.539 63.101 L461.548 63.101 L461.548 68.578 \" id=\"path.4\" />",
				shapeList.get(4).toXML());
		Assert.assertEquals("half moon", 
				"<path xmlns=\"http://www.w3.org/2000/svg\" clip-path=\"url(#clipPath3)\" fill=\"pink\" stroke=\"purple\"" +
				" stroke-width=\"0.5\" d=\"M428.911 60.844 C425.543 55.314 425.357 48.129 429.037 42.27" +
				" C434.222 34.019 444.971 31.662 453.224 36.848 C456.212 38.725 458.352 42.501 458.352 42.501" +
				" C457.189 40.651 455.601 39.024 453.626 37.784 C446.595 33.367 437.315 35.487 432.898 42.518" +
				" C429.731 47.56 429.925 53.76 432.882 58.492 L428.911 60.844 \" id=\"path.22\" />",
				shapeList.get(22).toXML());
	}
	
	@Test
	@Ignore
	public void bmcLogoTestInSitu() throws IOException {
		SVGElement svgElement = createAndProcessElement(Fixtures.PATHS_BMCLOGO_SVG);
		SVGUtil.debug(svgElement, new FileOutputStream("target/converted"+System.currentTimeMillis()+".svg"), 1);
		List<SVGElement> svgElements = SVGUtil.getQuerySVGElements(svgElement, "/*/*/svg:*");
		Assert.assertEquals("converted", 23, svgElements.size());
		Assert.assertTrue("0 "+svgElements.get(0).getClass().getSimpleName(), svgElements.get(0) instanceof SVGPolygon);
		Assert.assertTrue("1 "+svgElements.get(1).getClass().getSimpleName(), svgElements.get(1) instanceof SVGPolygon);
//		Assert.assertTrue("2 "+svgElements.get(2).getClass().getSimpleName(), svgElements.get(2) instanceof SVGCircle);
		Assert.assertTrue("3 "+svgElements.get(3).getClass().getSimpleName(), svgElements.get(3) instanceof SVGRect);
		Assert.assertTrue("4 "+svgElements.get(4).getClass().getSimpleName(), svgElements.get(4) instanceof SVGPath);
	}
	
	/**
<svg xmlns="http://www.w3.org/2000/svg">
 <g>
  <path stroke="black" fill="#000000" stroke-width="0.0" 
  d="M172.14 512.58 
  L172.14 504.3   // A
  C172.14 504.18 172.26 504.06 172.38 504.06 
  C172.5 504.06 172.62 504.18 172.62 504.3 
  L172.62 512.58  // D
  C172.62 512.76 172.5 512.88 172.38 512.88 
  C172.26 512.88 172.14 512.76 172.14 512.58 "
  svgx:z="1737"/>
 </g>
</svg>	 */
	
/**
<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg">
 <path stroke="black" fill="#000000" stroke-width="0.0" 
 d="M172.14 512.58 
 L172.14 504.3 
 L172.62 504.3 
 L172.62 512.58 
 L172.14 512.58 "/>
</svg>
*/
	@Test
	public void testReplaceTwoQuadrantCapsByButt() {
		SVGElement svgElement = createAndProcessElement(new File(Fixtures.PATHS_DIR, "roundedline.svg"));
		SVGLine line = (SVGLine) svgElement.getChildElements().get(0).getChildElements().get(0);
		Assert.assertEquals("path converted to line", 
				"<line fill=\"#000000\" stroke=\"black\" stroke-width=\"0.0\" x1=\"172.38\" y1=\"504.3\" x2=\"172.38\" y2=\"512.58\" id=\"line.0\" />",
				line.toXML());
	}
	
	@Test
	public void testHollowline() {
		SVGPath svgPath = (SVGPath) SVGElement.readAndCreateSVG(new File(Fixtures.PATHS_DIR, "hollowline.svg"))
				.getChildElements().get(0);
		Assert.assertEquals("sig",  "MLLL", svgPath.getSignature());
		Path2ShapeConverter p2sConverter = new Path2ShapeConverter(svgPath);
		SVGLine line = p2sConverter.createNarrowLine();
		Assert.assertNotNull("line", line);
	}
	
	@Test
	public void testCircle() {
//		 <path fill="none" stroke="#ff0000" stroke-width="0.840" 
//		d="M311.86 149.088 
//		C311.86 149.833 311.256 150.431 310.517 150.431 
//		C309.772 150.431 309.175 149.833 309.175 149.088 
//		C309.175 148.35 309.772 147.745 310.517 147.745 
//		C311.256 147.745 311.86 148.35 311.86 149.088 
//		Z"/>
		SVGPath svgPath = (SVGPath) SVGElement.readAndCreateSVG(new File(Fixtures.PATHS_DIR, "circle.svg"))
				.getChildElements().get(0);
		Assert.assertEquals("sig",  "MCCCCZ", svgPath.getSignature());
		Path2ShapeConverter p2sConverter = new Path2ShapeConverter(svgPath);
		SVGShape circle = p2sConverter.convertPathToShape();
		Assert.assertNotNull("circle", circle);
		Assert.assertTrue("circle", circle instanceof SVGCircle);
		SVGCircle svgCircle = (SVGCircle) circle;
		svgCircle.format(3);
		Assert.assertEquals("circle", 
				"<circle xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" stroke=\"#ff0000\" stroke-width=\"0.840\" cx=\"310.517\" cy=\"149.088\" r=\"1.379\" />", circle.toXML());
	}
	// ============================================================================
	private List<SVGShape> createShapeList(File file) {
		SVGElement svgElement = SVGElement.readAndCreateSVG(file);
		Path2ShapeConverter converter = new Path2ShapeConverter();
		converter.convertPathsToShapes(svgElement);
		List<SVGShape> shapeList = converter.getShapeListOut();
		return shapeList;
	}
	
	private static SVGElement createAndProcessElement(File file) {
		SVGElement svgElement = SVGElement.readAndCreateSVG(file);
		Path2ShapeConverter converter = new Path2ShapeConverter();
		converter.convertPathsToShapes(svgElement);
		return svgElement;
	}

	

}
