/**
 *    Copyright 2011 Peter Murray-Rust et. al.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.xmlcml.graphics.svg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;


public class HiddenGraphics {

	private final static Logger LOG = Logger.getLogger(HiddenGraphics.class);
	
	private static final String PNG = "png";
	private Dimension dimension;
	private BufferedImage img;
	private Graphics2D g;
	private String type;
	private Color backgroundColor;
	
	public HiddenGraphics() {
		setDefaults();
	}
	private void setDefaults() {
		this.type = PNG;
		this.setDimension(new Dimension(400, 400));
		this.setBackgroundColor(Color.WHITE);
	}
	private void setBackgroundColor(Color color) {
		this.backgroundColor = color;
	}
	public void setDimension(Dimension d) {
		this.dimension = d;
	}
	
	public Graphics2D createGraphics() {
		img = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB);
		g = img.createGraphics();
		g.setBackground(backgroundColor);
		g.clearRect(0, 0, dimension.width, dimension.height);
		return g;
	}
	
	public void setOutputType(String type) {
		this.type = type;
	}
	
	public void write(File file) throws IOException {
		LOG.debug("Writing: "+file.getAbsolutePath());
		ImageIO.write(img, type, file);
	}
	
	public BufferedImage createImage(SVGElement element) {
		Graphics2D g2D = this.createGraphics();
		element.draw(g2D);
		return img;
	}
	
}
