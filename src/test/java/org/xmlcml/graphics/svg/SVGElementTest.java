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

import nu.xom.Element;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.testutil.JumboTestUtils;

public class SVGElementTest {

	public final static String GRAPHICS_RESOURCE = "org/xmlcml/cml/graphics/examples";

	@Test
	@Ignore
	public final void testcreateSVGElement() {
		Element oldElement =JumboTestUtils.parseValidFile(GRAPHICS_RESOURCE + CMLConstants.U_S
				+ "image12.svg");
		SVGElement newSvg = SVGElement.readAndCreateSVG(oldElement);
		Assert.assertEquals("class", SVGSVG.class, newSvg.getClass());
		JumboTestUtils.assertEqualsCanonically("copy",JumboTestUtils.parseValidFile(GRAPHICS_RESOURCE + CMLConstants.U_S
				+ "image12.svg"), newSvg, true);
	}

}
