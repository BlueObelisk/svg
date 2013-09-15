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

import junit.framework.Assert;

import org.junit.Test;
import org.xmlcml.euclid.Real2;

public class SVGPolylineTest {

	@Test
	public void testCreateLineList() {
		String d = "M379.558 218.898 L380.967 212.146 L380.134 212.146 L378.725 218.898 L379.558 218.898";
		SVGPath path = new SVGPath(d);
		SVGPolyline polyline = path.createPolyline();
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
	public void testReplacePolyLineBySplitLines() {
		String d = "M379.558 118.898 L480.967 212.146 L380.134 312.146 L278.725 218.898 L379.558 118.898";
		SVGPath path = new SVGPath(d);
		SVGPolyline polyline = path.createPolyline();
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
		Assert.assertEquals("circle2", 5, svg.indexOf(circle2));
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

}
