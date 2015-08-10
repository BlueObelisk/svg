package org.xmlcml.graphics.svg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

public class Fixtures {


	public static final File RESOURCES_DIR = new File("src/test/resources/");
	
	public static final File SVG_DIR = new File(RESOURCES_DIR, "org/xmlcml/graphics/svg");
	
	public static final File IMAGES_DIR = new File(SVG_DIR, "images");
	public static final File IMAGE_G_2_2_SVG = new File(IMAGES_DIR, "image.g.2.2.svg");
	public static final File IMAGE_G_2_2_PNG = new File(IMAGES_DIR, "image.g.2.2.png");
	public static final File IMAGE_G_3_2_SVG = new File(IMAGES_DIR, "image.g.3.2.svg");
	public static final File IMAGE_G_8_0_SVG = new File(IMAGES_DIR, "image.g.8.0.svg");
	public static final File IMAGE_G_8_2_SVG = new File(IMAGES_DIR, "image.g.8.2.svg");
	
	public static final File PATHS_DIR = new File(SVG_DIR, "paths");
	public static final File PATHS_BMCLOGO_SVG = new File(PATHS_DIR, "bmclogo.svg");
	public static final File PATHS_NOPATH_SVG = new File(PATHS_DIR, "nopath.svg");
	public static final File PATHS_RECT_LINE_SVG = new File(PATHS_DIR, "rectLine.svg");
	public static final File PATHS_TEXT_LINE_SVG = new File(PATHS_DIR, "textLine.svg");
	public static final File PATHS_SIMPLE_TREE_SVG = new File(Fixtures.PATHS_DIR, "simpleTree.svg");

	public static final File CC0_SVG = new File(Fixtures.IMAGES_DIR, "cc0.png");
	public static final File CCBY_PNG = new File(Fixtures.IMAGES_DIR, "ccby.png");
	public static final File CHEM_BMP = new File(Fixtures.IMAGES_DIR, "chem.bmp");
	public static final File FIGSHARE1138891_PNG = new File(Fixtures.IMAGES_DIR, "figshare1138891.png");
	public static final File IMAGE_TEST_PNG = new File(Fixtures.IMAGES_DIR, "imageTest.png");

	public static final String MONOCHROME = "monochrome";

	public static final File MONOCHROME1_PNG = new File(Fixtures.IMAGES_DIR, "monochrome1.png");
	public static final File MONOCHROME2_PNG = new File(Fixtures.IMAGES_DIR, "monochrome2.png");
	public static final File MONOCHROME2PMRCC0_PNG = new File(Fixtures.IMAGES_DIR, "monochrome2pmrcc0.png");
	public static final File MONOCHROME2PUBDOM_PNG = new File(Fixtures.IMAGES_DIR, "monochrome2pubdom.png");
	public static final File MONOCHROME2PUBDOM_STREAM_PNG = new File(Fixtures.IMAGES_DIR, "monochrome2pubdomStream.png");
	public static final File MONOCHROME2TEXT_PNG = new File(Fixtures.IMAGES_DIR, "monochrome2text.png");
	public static final File PLOS_GRAPH_SVG = new File(Fixtures.IMAGES_DIR, "plosGraph.svg");
	public static final File PLOTS_PUBDOM_PNG = new File(Fixtures.IMAGES_DIR, "plotspubdom.png");
	public static final File PLOTS1_BMP = new File(Fixtures.IMAGES_DIR, "plots1.bmp");
	public static final File PLOTS_CC0_PNG = new File(Fixtures.IMAGES_DIR, "plotscc0.png");
	public static final File PMRCC0_PNG_ = new File(Fixtures.IMAGES_DIR, "pmrcc0.png");
	public static final File PUBDOM_PNG = new File(Fixtures.IMAGES_DIR, "pubdom.png");
	public static final File TEST_PNG = new File(Fixtures.IMAGES_DIR, "test.png");
	public static final File TEST1MINI_BMP = new File(Fixtures.IMAGES_DIR, "test1mini.bmp");

	public static final File SVG_G_8_0_SVG = new File(Fixtures.SVG_DIR, "image.g.8.0.svg");
	public static final File SVG_G_8_2_SVG = new File(Fixtures.SVG_DIR, "image.g.8.2.svg");
	public static final File SVG_PAGE6_SVG = new File(Fixtures.SVG_DIR, "page6.svg");

	public static final File FIGURE_DIR = new File(SVG_DIR, "unplot");
	public static final File LINEPLOTS_10_2_SVG = new File(FIGURE_DIR, "lineplots.g.10.2.svg");
	public static final File SCATTERPLOT_FIVE_7_2_SVG = new File(FIGURE_DIR, "scatterplot5.g.7.2.svg");
	public static final File SCATTERPLOT_7_2_SVG = new File(FIGURE_DIR, "scatterplot.g.7.2.svg");

	//	public final static String IMAGE_SVG = ""
	//	 		+ "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" >"
	//	 		+ "  <image transform=\"matrix(0.05999946966767311,-0.0,-0.0,-0.05999946966767311,197.92599487304688,562.9089965820312)\" x=\"0.0\" y=\"0.0\" "
	//	 		+ "   width=\"16.0\" height=\"16.0\" "
	//	 		+ "   xlink:href=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAMklEQVR42mP4J8LwHx0zAAE2cWzyeBUSgxnw2UwMnzouINVmnF4YwmEwmg7Is3kYhQEA6pzZRchLX5wAAAAASUVORK5CYII=\" "
	//	 		+ "  />"
	//	 		+ "</svg>";
	public static final File SVG_IMAGES_DIR = new File(SVG_DIR, "svgimages");
	
	public final static String IMAGE_SVG = ""
		 + "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><image  x=\"0.0\" y=\"0.0\" width=\"16.0\" height=\"16.0\" xlink:href=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAMklEQVR42mP4J8LwHx0zAAE2cWzyeBUSgxnw2UwMnzouINVmnF4YwmEwmg7Is3kYhQEA6pzZRchLX5wAAAAASUVORK5CYII=\"/></svg>";
	public final static File LARGE_IMAGE_SVG = new File(SVG_IMAGES_DIR, "multiple-image-page6.svg"); 
//	public final static String IMAGE_SCALE_SVG = ""
//	 +"<svg xmlns=\"http://www.w3.org/2000/svg\" >"
//	 +"<image transform=\"matrix(0.160000000808920095,-0.0,-0.0,-0.159999995788164284,261.77996826171875,801.2996826171875)\" "
//	 + "x=\"0.0\" y=\"0.0\" width=\"56.0\" height=\"330.0\" "
//	 + "xlink:href=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADgAAAFKCAYAAABIG5xgAAABQ0lEQVR42u3aQQ7CMAwAwf7/0+HEjYJa4thOZySOkCyIplgcBwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA8GDjxyNqjfSwGZuKet3pYXc2E/3mhcRd2dBYHTkCHlHrlYj7tpmx8KsQHvhpQ2Nl5MpP586a314//Tg4e+7MN7HElSzyIlXmLIo6aqYtmnHW9los4xYt5V5QoECBlxcT2D1wdA48nhC4OlJg1GRt1aJpgatmoemB0Z9micDIyDKBUaHlAmeHlg2ctcHygf9utE3g3dFHu8AZI8I2gTND29g+sMKo8nQDFY+ZkjfYArNHFkOgQIECOyyYdtBvP1F7xMiw0h8Rtp2ktRwXlvyZlPWPwrbjhy3GhO1/uW8b1uri8fYCr8WfmTxzW6MAAAAASUVORK5CYII=\""
//	 + " xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
//	 + "</svg>";
	public final static File LETTERA_SVG_FILE = new File(Fixtures.IMAGES_DIR, "lettera.svg");

	public final static File ROUNDED_LINE_SVG_FILE = new File(Fixtures.PATHS_DIR, "roundedline.svg");

	public static final File MOLECULES_DIR = new File(SVG_DIR, "molecules");
	public static final File IMAGE_2_13_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.13.svg");
	public static final File IMAGE_2_11_NO2_SVG = new File(Fixtures.MOLECULES_DIR, "image.g.2.11.no2.svg");
	
	public static final double EPS = 0.5;

	public static void writeImageQuietly(BufferedImage image, File file) {
		if (image == null) {
			throw new RuntimeException("Cannot write null image: "+file);
		}
		try {
			// DONT EDIT!
			String type = FilenameUtils.getExtension(file.getName());
			file.getParentFile().mkdirs();
			ImageIO.write(image, type, new FileOutputStream(file));
		} catch (Exception e) {
			throw new RuntimeException("cannot write image "+file, e);
		}
	}


}
