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

import java.awt.Polygon;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.xml.XMLUtil;

public class SVGPolygonTest {

	private static final Logger LOG = Logger.getLogger(SVGPolygonTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	public final static String ARROWHEAD_S = "<polygon  points=\"178.9 130.294 175.648 122.336 178.9 124.225 182.152 122.336\"/>";
	public final static SVGPolygon ARROWHEAD;
	static {
		ARROWHEAD = (SVGPolygon) SVGElement.readAndCreateSVG(XMLUtil.parseXML(ARROWHEAD_S));
		ARROWHEAD.getReal2Array();
	}

	@Test
	public void testIsMirror() {
		Assert.assertFalse(ARROWHEAD.hasMirror(0, SVGPolygon.EPS));
		Assert.assertTrue(ARROWHEAD.hasMirror(1, SVGPolygon.EPS));
	}
}
