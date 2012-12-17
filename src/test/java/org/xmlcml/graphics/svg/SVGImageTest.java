package org.xmlcml.graphics.svg;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.Test;

public class SVGImageTest {
	
	@Test
	public void testPNG() throws Exception {
		File pngFile = new File("src/test/resources/org/xmlcml/graphics/svg/imageTest.png");
		BufferedImage bufferedImage = ImageIO.read(pngFile);
		SVGImage svgImage = new SVGImage();
		svgImage.readImageData(bufferedImage, SVGImage.IMAGE_PNG);
		svgImage.debug("IMG");
	}

}
