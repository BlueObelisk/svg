package org.xmlcml.graphics.svg;

import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Real2;

public class SVGUtilTest {

	@Test
	public void testInterposeGBetweenChildren() {
		SVGSVG svg = new SVGSVG();
		svg.appendChild(new SVGCircle(new Real2(10, 20), 5));
		svg.appendChild(new SVGText(new Real2(40, 50), "test"));
		Assert.assertEquals("before child", 2, svg.getChildCount());
		Assert.assertEquals("before child", SVGCircle.class, svg.getChild(0).getClass());
		SVGG g = SVGUtil.interposeGBetweenChildren(svg);
		Assert.assertEquals("after child", 1, svg.getChildCount());
		Assert.assertEquals("after child", 2, g.getChildCount());
		Assert.assertEquals("after child", SVGCircle.class, g.getChild(0).getClass());
	}
}
