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

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.testutil.JumboTestUtils;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealRange.Direction;
import org.xmlcml.euclid.RealRangeArray;

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

	@Test
	public final void testIsIncludedByMask() {
		SVGRect rect = new SVGRect(new Real2(10., 20.), new Real2(50., 100.));
		RealRange mask = new RealRange(9., 51.);
		Assert.assertTrue(rect.isIncludedBy(mask, Direction.HORIZONTAL));
	}

	@Test
	public final void testIsNotIncludedByMask() {
		SVGRect rect = new SVGRect(new Real2(10., 20.), new Real2(50., 100.));
		RealRange mask = new RealRange(11., 49.);
		Assert.assertFalse(rect.isIncludedBy(mask, Direction.HORIZONTAL));
	}
	
	@Test
	public final void testIsIncludedByMaskArray() {
		SVGRect rect = new SVGRect(new Real2(10., 20.), new Real2(50., 100.));
		RealRangeArray rangeArray = new RealRangeArray();
		rangeArray.add(new RealRange(9., 51.));
		rangeArray.add(new RealRange(2., 7.));
		Assert.assertTrue(rect.isIncludedBy(rangeArray, Direction.HORIZONTAL));
	}

	@Test
	public final void testIsNotIncludedByMaskArray() {
		SVGRect rect = new SVGRect(new Real2(10., 20.), new Real2(50., 100.));
		RealRangeArray rangeArray = new RealRangeArray();
		rangeArray.add(new RealRange(25., 51.));
		rangeArray.add(new RealRange(9., 15.));
		Assert.assertFalse(rect.isIncludedBy(rangeArray, Direction.HORIZONTAL));
	}
			
	@Test
	public final void testIsIncludedByVerticalMask() {
		SVGRect rect = new SVGRect(new Real2(10., 20.), new Real2(50., 100.));
		RealRange mask = new RealRange(19., 101.);
		Assert.assertTrue(rect.isIncludedBy(mask, Direction.VERTICAL));
	}

	@Test
	public final void testIsNotIncludedByVerticalMask() {
		SVGRect rect = new SVGRect(new Real2(10., 20.), new Real2(50., 100.));
		RealRange mask = new RealRange(21., 99.);
		Assert.assertFalse(rect.isIncludedBy(mask, Direction.VERTICAL));
	}

	@Test
	public final void testFilterHorizontally() {
		RealRangeArray horizontalMask = new RealRangeArray();
		horizontalMask.add(new RealRange(9.,21.));
		horizontalMask.add(new RealRange(29.,41.));
		horizontalMask.add(new RealRange(49.,61.));
		List<SVGElement> elementList = new ArrayList<SVGElement>();
		elementList.add(new SVGRect(new Real2(10., 20.), new Real2(20., 70.)));
		elementList.add(new SVGRect(new Real2(30., 0.), new Real2(40., 60.)));
		elementList.add(new SVGRect(new Real2(10., 0.), new Real2(40., 60.)));
		List<? extends SVGElement> newElementList = SVGElement.filterHorizontally(elementList, horizontalMask);
		Assert.assertEquals("filtered", 2, newElementList.size());
		elementList.add(new SVGRect(new Real2(50., 100.), new Real2(60., 160.)));
		newElementList = SVGElement.filterHorizontally(elementList, horizontalMask);
		Assert.assertEquals("filtered", 3, newElementList.size());
	}

	@Test
	public final void testFilterVertically() {
		RealRangeArray verticalMask = new RealRangeArray();
		verticalMask.add(new RealRange(9.,21.));
		verticalMask.add(new RealRange(29.,41.));
		verticalMask.add(new RealRange(49.,61.));
		List<SVGElement> elementList = new ArrayList<SVGElement>();
		elementList.add(new SVGRect(new Real2(10., 50.), new Real2(20., 60.)));
		elementList.add(new SVGRect(new Real2(30., 30.), new Real2(40., 40.)));
		elementList.add(new SVGRect(new Real2(10., 0.), new Real2(40., 60.)));
		List<? extends SVGElement> newElementList = SVGElement.filterHorizontally(elementList, verticalMask);
		Assert.assertEquals("filtered", 2, newElementList.size());
		elementList.add(new SVGRect(new Real2(50., 10.), new Real2(60., 20.)));
		newElementList = SVGElement.filterHorizontally(elementList, verticalMask);
		Assert.assertEquals("filtered", 3, newElementList.size());
	}

	/**
	 * @param elementList elements to create mask
	 * @return RealRange array corresponding to (overlapped) ranges of elements
	 */
	@Test
	public void testCreateMask() {
		List<SVGElement> elementList = new ArrayList<SVGElement>();
		elementList.add(new SVGRect(new Real2(0., 10.), new Real2(30., 40.)));
		elementList.add(new SVGRect(new Real2(40., 10.), new Real2(50., 40.)));
		elementList.add(new SVGRect(new Real2(45., 10.), new Real2(55., 40.)));
		RealRangeArray mask = SVGElement.createMask(elementList, Direction.HORIZONTAL);
		RealRangeArray maskRef = new RealRangeArray();
		maskRef.add(new RealRange(0., 30.));
		maskRef.add(new RealRange(40., 55.));
		Assert.assertEquals("create mask", maskRef, mask);
	 }

	/**
	 * @param elementList elements to create mask
	 * @return RealRange array corresponding to (overlapped) ranges of elements
	 */
	@Test
	public void testCreateMaskWithTolerance() {
		List<SVGElement> elementList = new ArrayList<SVGElement>();
		elementList.add(new SVGRect(new Real2(0., 10.), new Real2(30., 40.)));
		elementList.add(new SVGRect(new Real2(40., 10.), new Real2(50., 40.)));
		elementList.add(new SVGRect(new Real2(51., 10.), new Real2(55., 40.)));
		RealRangeArray mask = SVGElement.createMask(elementList, Direction.HORIZONTAL);
		RealRangeArray maskRef = new RealRangeArray();
		maskRef.add(new RealRange(0., 30.));
		maskRef.add(new RealRange(40., 50.));
		maskRef.add(new RealRange(51., 55.));
		mask = SVGElement.createMask(elementList, Direction.HORIZONTAL, 1.0);
		maskRef = new RealRangeArray();
		maskRef.add(new RealRange(-1.0, 31.));
		maskRef.add(new RealRange(39., 56.));
		Assert.assertEquals("create mask", maskRef, mask);
	 }

}
