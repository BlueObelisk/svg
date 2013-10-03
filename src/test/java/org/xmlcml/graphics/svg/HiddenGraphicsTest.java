package org.xmlcml.graphics.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

import nu.xom.Document;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Real2;
import org.xmlcml.xml.XMLUtil;

public class HiddenGraphicsTest {

	@Test
	public void testHiddenGraphics() throws IOException {
		HiddenGraphics hg = new HiddenGraphics();
		Graphics2D g = hg.createGraphics();
		g.setColor(Color.GREEN);
		g.fillOval(100, 170, 200, 200);
		g.fillRect(165, 25, 70, 200);
		g.fillRect(155, 25, 90, 20);
		hg.write(SVGImage.IMAGE_PNG, new File("target/image.png"));
	}
	
	@Test
	public void testHiddenGraphics0() {
		HiddenGraphics hg = new HiddenGraphics();
		SVGElement g = createExampleSvg();
    }

	@Test
	public void testHiddenGraphicsWrite() throws Exception {
		HiddenGraphics hg = new HiddenGraphics();
		SVGElement g = createExampleSvg();
		hg.createImage(g);
		hg.write(SVGImage.IMAGE_PNG, new File("target/exampleSvg.png"));
	}
	
	@Test
	// FIXME does not write sensible file
	public void testHiddenGraphicsWriteBMP() throws Exception {
		HiddenGraphics hg = new HiddenGraphics();
		SVGElement g = createExampleSvg();
		hg.createImage(g);
		try {
			hg.write(SVGImage.IMAGE_BMP, new File("target/exampleSvg.bmp"));
			Assert.fail("throws Image cannot be encoded with comprression BI_RGB");
		} catch (Exception e) {
			Assert.assertTrue("failed", true);
		}
	}
	
	@Test
	// FIXME does not write sensible file
	public void testHiddenGraphicsWriteJPG() throws Exception {
		HiddenGraphics hg = new HiddenGraphics();
		SVGElement g = createExampleSvg();
		hg.createImage(g);
		hg.write(SVGImage.IMAGE_JPG, new File("target/exampleSvg.jpg"));
	}
	
	@Test
	/** this one has a bad antialias locally
	 * 
	 */
	public void testAntialias() throws Exception {
		HiddenGraphics hg = new HiddenGraphics();
		File file = Fixtures.IMAGE_G_2_2_SVG;
		Document document = XMLUtil.parseQuietlyToDocument(file);
		SVGElement svg = SVGElement.readAndCreateSVG(Fixtures.IMAGE_G_2_2_SVG);
		hg.createImage(svg);
		hg.write(SVGImage.IMAGE_PNG, new File("target/image.g.2.2.png"));
	}
	
	@Test
	/** 
	 * 
	 */
	public void testImage3_2() throws Exception {
		HiddenGraphics hg = new HiddenGraphics();
		File file = Fixtures.IMAGE_G_3_2_SVG;
		Document document = XMLUtil.parseQuietlyToDocument(file);
		SVGElement svg = SVGElement.readAndCreateSVG(Fixtures.IMAGE_G_3_2_SVG);
		hg.createImage(svg);
		hg.write(SVGImage.IMAGE_PNG, new File("target/image.g.3.2.png"));
	}
	
	@Test
	/** simple tree
	 * 
	 */
	public void testImage8_2() throws Exception {
		HiddenGraphics hg = new HiddenGraphics();
		File file = Fixtures.IMAGE_G_8_2_SVG;
		Document document = XMLUtil.parseQuietlyToDocument(file);
		SVGElement svg = SVGElement.readAndCreateSVG(Fixtures.IMAGE_G_8_2_SVG);
		hg.createImage(svg);
		hg.write(SVGImage.IMAGE_PNG, new File("target/image.g.8.2.png"));
	}
	
	private SVGElement createExampleSvg() {
		SVGSVG g = new SVGSVG();
		SVGCircle circle;
		circle = new SVGCircle(new Real2(220., 120.), 30.);
		circle.setFill("green");
		g.appendChild(circle);
		circle = new SVGCircle(new Real2(120., 120.), 30.);
		circle.setFill("red");
		g.appendChild(circle);
		SVGLine line = new SVGLine(new Real2(100., 200.), new Real2(150, 50.));
		g.appendChild(line);
		return g;
	}
	
}
