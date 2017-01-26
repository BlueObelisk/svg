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

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.euclid.Real2;

public class SVGPolylineTest {
	public static final Logger LOG = Logger.getLogger(SVGPolylineTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}
	public final static String SVG_STRING =
			"<svg xmlns=\"http://www.w3.org/2000/svg\">"
	 + "<polyline points=\""
	 + " 96.071 211.257"
	 + " 142.448 211.257"
	 + " 188.492 211.257"
	 + " 222.003 211.257"
	 + " 383.366 211.257"
	 + " 469.885 211.257"
	 + " 508.313 211.257"
	 + " 546.773 211.257"
	 + " 584.984 211.257"
	 + " 623.194 211.256"
	 + " 661.155 211.256"
	 + " 732.949 211.256"
	 + " 750.695 211.256\" />"
	 + "<polyline points=\""
	 + " 193.144 469.885 "
	 + " 193.144 508.313"
	 + " 193.144 546.773"
	 + " 193.144 584.984"
	 + " 193.143 623.194"
	 + " 193.143 661.155\" />"
	 + "<polyline points=\""
	 + " 96.071 400.725"
	 + " 142.448 400.725"
	 + " 188.492 400.725"
	 + " 222.003 400.725"
	 + " 383.366 400.724"
	 + " 469.885 400.724"
	 + " 508.313 400.724"
	 + " 546.77 400.724"
	 + " 584.984 400.724"
	 + " 623.194 400.724"
	 + " 661.155 400.724"
	 + " 732.949 400.724"
	 + " 750.695 400.724\" />"
	 + "</svg>"
	;


	@Test
	@Ignore // FIXME ANDY
	public void testCreateLineList() {
		String d = "M379.558 218.898 L380.967 212.146 L380.134 212.146 L378.725 218.898 L379.558 218.898";
		SVGPath path = new SVGPath(d);
		SVGPoly polyline = path.createPolyline();
//		polyline.debug("POLY");
		List<SVGLine> lineList = polyline.createLineList();
		Assert.assertEquals("line count", 4, lineList.size());
	}
	
	@Test
	public void testCreateLineListFromPolyList() {
		SVGElement svg = SVGElement.readAndCreateSVG(Fixtures.SVG_G_8_2_SVG);
		List<SVGPolyline> polylineList = SVGPolyline.extractSelfAndDescendantPolylines(svg);
		Assert.assertEquals("polylines", 21, polylineList.size());
		List<SVGLine> lineList = SVGPoly.splitPolylinesToLines(polylineList);
		Assert.assertEquals("split polylines", 42, lineList.size());
	}

	@Test
	@Ignore // FIXME ANDY line of zero length
	public void testReplacePolyLineBySplitLines() {
		String d = "M379.558 118.898 L480.967 212.146 L380.134 312.146 L278.725 218.898 L379.558 118.898";
		SVGPath path = new SVGPath(d);
		SVGPoly polyline = path.createPolyline();
		SVGSVG svg = new SVGSVG();
		SVGCircle circle1 = new SVGCircle(new Real2(100., 200.), 10.);
		svg.appendChild(circle1);
		Assert.assertEquals("circle1", 0, svg.indexOf(circle1));
		svg.appendChild(polyline);
		Assert.assertEquals("poly", 1, svg.indexOf(polyline));
		SVGCircle circle2 = new SVGCircle(new Real2(300., 100.), 20.);
		svg.appendChild(circle2);
		Assert.assertEquals("circle2", 2, svg.indexOf(circle2));
		SVGUtil.debug(svg, "target/beforesplit.svg", 1);
		SVGPolyline.replacePolyLineBySplitLines(polyline);
		Assert.assertNull("polyline", polyline.getParent());
		svg.debug("lines");
		Assert.assertEquals("split", 5, svg.indexOf(circle2));
		SVGUtil.debug(svg, "target/aftersplit.svg", 1);
	}
	
	@Test
	public void testReplacePolylinesBySplitLines() {
		SVGElement svg = SVGElement.readAndCreateSVG(Fixtures.SVG_G_8_2_SVG);
		SVGUtil.debug(svg, "target/beforesplitlines.svg", 1);
		Assert.assertEquals("before polylines", 21, SVGPolyline.extractSelfAndDescendantPolylines(svg).size());
		Assert.assertEquals("lines", 1, SVGLine.extractSelfAndDescendantLines(svg).size());
		SVGPoly.replacePolyLinesBySplitLines(svg);
		SVGUtil.debug(svg, "target/aftersplitlines.svg", 1);
		Assert.assertEquals("split polylines", 0, SVGPolyline.extractSelfAndDescendantPolylines(svg).size());
		Assert.assertEquals("lines", 43, SVGLine.extractSelfAndDescendantLines(svg).size());
	}

	@Test
	public void testReplaceRedundantSubLinesIntoSingleLine() {
		SVGSVG svg = (SVGSVG) SVGUtil.parseToSVGElement(SVG_STRING);
		List<SVGPolyline> polylineList = SVGPolyline.extractSelfAndDescendantPolylines(svg);
		Assert.assertEquals(3, polylineList.size());
		SVGPolyline poly1 = polylineList.get(0);
		poly1.createVerticalOrHorizontalLine(0.03);
		
	}
}
