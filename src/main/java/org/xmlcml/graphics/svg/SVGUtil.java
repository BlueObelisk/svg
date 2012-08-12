package org.xmlcml.graphics.svg;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealSquareMatrix;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.euclid.Vector2;

public class SVGUtil {

	private static final String TRANSFORMS_APPLIED = "transformsApplied";
	private static final Logger LOG = Logger.getLogger(SVGUtil.class);

	/**
	 * adds a new svg:g between element and its children
	 * this can be used to set scales, rendering, etc.
	 * also copies ant transform attribute
	 * @param element to amend (is changed)
	 */
	public static SVGG interposeGBetweenChildren(SVGElement element) {
		SVGG g = new SVGG();
		element.appendChild(g);
		while (element.getChildCount() > 1) {
			Node child = element.getChild(0);
			child.detach();
			g.appendChild(child);
		}
		return g;
	}

	/** creates an SVGElement
	 * 
	 * @param is
	 * @return
	 */
	public static SVGElement parseToSVGElement(InputStream is) {
		Element element = null;
		try {
			element = new Builder().build(is).getRootElement();
			return SVGElement.readAndCreateSVG(element);
		} catch (Exception e) {
			throw new RuntimeException("cannot parse input stream", e);
		}
	}

	public static List<SVGElement> getQuerySVGElements(SVGElement svgElement, String xpath) {
		List<Element> elements = CMLUtil.getQueryElements(svgElement, xpath, SVGConstants.SVG_XPATH);
		List<SVGElement> svgElements = new ArrayList<SVGElement>();
		for (Element element : elements) {
			svgElements.add((SVGElement)element);
		}
		return svgElements;
	}

	public static Real2 getTransformedXY(SVGElement element) {
		Real2 xy = new Real2(element.getXY());
		Transform2 t2 = element.getCumulativeTransform();
		return xy.getTransformed(t2);
	}
	
	/** transform a (pair) of coordinates by context
	 * can be used for transforming scales, e.g. by 
	 * getTransformedXY(element, new Real2(scalex, scaley)) {
	 * @param element the context (for cumulative transformations)
	 * @param xy
	 * @return transformed pair
	 */
	public static Real2 getTransformedXY(SVGElement element, Real2 xy) {
		Transform2 t2 = element.getCumulativeTransform();
		return xy.getTransformed(t2);
	}

	public static Double decimalPlaces(Double width, int i) {
		int ii = (int) Math.pow(10., i);
		return (double)Math.round(width*(int)ii) / (double)ii;
	}

	/** applies to leaf nodes
	 * BUT removes all ancestral transformations, so be careful
	 * this doesn't remove transforms for other nodes without knowledge
	 * best not called directly
	 * @param svgElements
	 */
	public static void applyCumulativeTransforms(List<SVGElement> svgElements) {
		for (SVGElement svgElement : svgElements) {
			Transform2 t2 = svgElement.getCumulativeTransform();
			svgElement.applyTransform(t2);
		}
	}

	/** finds root SVG element ancestor and then removes all transformation in the tree
	 * @param element - any element in tree will do
	 */
	public static void applyAndRemoveCumulativeTransformsFromDocument(SVGElement element) {
		List<SVGElement> roots = SVGUtil.getQuerySVGElements(element, "/svg:svg");
		if (roots.size() == 1) {
			SVGSVG root = (SVGSVG) roots.get(0);
			if (root.getAttribute(TRANSFORMS_APPLIED) == null) {
				List<SVGElement> leafElements = SVGUtil.getQuerySVGElements(root, "//svg:*[count(*)=0]");
				applyCumulativeTransforms(leafElements);
				Nodes transformAttributes = root.query("//@transform");
				for (int i = 0; i < transformAttributes.size(); i++) {
					Attribute attribute = (Attribute) transformAttributes.get(i);
					if (attribute.getParent() instanceof SVGText) {
						SVGText text = (SVGText) attribute.getParent();
						Transform2 transform2 = text.getTransform();
						RealSquareMatrix rotMat = transform2.getRotationMatrix();
						Real2 xy = new Real2(text.getXY());
						Transform2 newTransform2 = new Transform2(new Vector2(xy)); 
						newTransform2 = newTransform2.concatenate(new Transform2(rotMat));
						newTransform2 = newTransform2.concatenate(new Transform2(new Vector2(xy.multiplyBy(-1.0))));
						text.setTransform(newTransform2);
					} else {
						attribute.detach();
					}
				}
				root.addAttribute(new Attribute(TRANSFORMS_APPLIED, "yes"));
			}
		}
	}

/**
<g>
  <defs id="defs1">
   <clipPath clipPathUnits="userSpaceOnUse" id="clipPath1">
    <path style=" fill : none; stroke : black; stroke-width : 0.5;" d="M0.0 0.0 L595.0 0.0 L595.0 793.0 L0.0 793.0 L0.0 0.0 Z"/>
   </clipPath>		 
   
   remove these. don't know whether we have to remove from style attribute but try this anyway
   <path style="clip-path:url(#clipPath1); stroke:none;" d="M327.397 218.897 L328.023 215.899 L329.074 215.899 C329.433 215.899 329.692 215.936 329.854 216.007 C330.011 216.08 330.167 216.231 330.321 216.46 C330.545 216.792 330.744 217.186 330.92 217.639 L331.403 218.897 L332.412 218.897 L331.898 217.626 C331.725 217.201 331.497 216.792 331.214 216.397 C331.088 216.222 330.903 216.044 330.657 215.863 C331.459 215.755 332.039 215.52 332.399 215.158 C332.759 214.796 332.937 214.341 332.937 213.791 C332.937 213.397 332.855 213.072 332.691 212.813 C332.527 212.557 332.3 212.38 332.013 212.287 C331.723 212.192 331.299 212.145 330.74 212.145 L327.907 212.145 L326.493 218.897 L327.397 218.897 ZM328.653 212.888 L330.856 212.888 C331.203 212.888 331.447 212.914 331.592 212.966 C331.736 213.018 331.855 213.117 331.941 213.264 C332.032 213.41 332.075 213.58 332.075 213.776 C332.075 214.011 332.016 214.227 331.898 214.43 C331.778 214.634 331.609 214.794 331.39 214.914 C331.17 215.033 330.893 215.11 330.552 215.145 C330.376 215.16 330.0 215.167 329.424 215.167 L328.175 215.167 L328.653 212.888 "/>

   */
	public static void removeAllClipPaths(SVGElement svg) {
		List<SVGElement> clipPathNodes = SVGUtil.getQuerySVGElements(svg, "//svg:defs/svg:clipPath");
		for (int i = 0; i < clipPathNodes.size(); i++) {
			clipPathNodes.get(i).detach();
		}
		Nodes clipPathElements = svg.query("//*[@style[contains(.,'clip-path')]]");
		for (int i = 0; i < clipPathElements.size(); i++) {
			((SVGElement) clipPathElements.get(i)).setClipPath(null);
		}
	}

	public static void addIdsToAllElements(SVGSVG svg) {
		LOG.debug("adding ids");
		List<SVGElement> elems = SVGUtil.getQuerySVGElements(svg, "//svg:*");
		LOG.debug("found elements "+elems.size());
		int i = 0;
		for (SVGElement elem : elems) {
			if (elem.getId() == null) {
				elem.setId(elem.getTag()+(i++));
			}
		}
		LOG.debug("added ids "+elems.size());
	}

	/**
	 * find all text elements managed by a parent <g> and explicitly copy any font-size
	 *?  //svg:g[@font-size]/svg:text[not(@font-size)] >> //svg:g[@font-size]/svg:text[@font-size]
	 * @param svg
	 */
//	public static void denormalizeFontSizes(SVGSVG svg) {
//		List<SVGElement> texts = SVGUtil.getQuerySVGElements(svg, "//svg:g[@font-size]/svg:text[not(@font-size)]");
//		for (SVGElement text : texts) {
//			((SVGText)text).setFontSize(((SVGG)text.getParent()).getFontSize());
//		}
//	}
	
	public static void denormalizeFontSizes(SVGSVG svg) {
		List<SVGElement> gs = SVGUtil.getQuerySVGElements(svg, "//svg:g[@font-size and svg:text[not(@font-size)]]");
		for (SVGElement g : gs) {
			Double fontSize = g.getFontSize();
			System.out.println("FS "+fontSize);
			g.getAttribute("font-size").detach();
			List<SVGElement> texts = SVGUtil.getQuerySVGElements(g, "./svg:text[not(@font-size)]");
			for (SVGElement text : texts) {
				((SVGText)text).setFontSize(fontSize);
			}
		}
	}


	public static void removeXmlSpace(SVGSVG svg) {
		Nodes nodes = svg.query(".//@*[local-name()='space' and .='preserve']");
		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).detach();
		}
	}

	public static void drawBoxes(List<? extends SVGElement> elementList, SVGElement svgParent,
			String stroke, String fill, double strokeWidth, double opacity) {
		for (SVGElement element : elementList) {
			SVGElement.drawBox(element.getBoundingBox(), element, stroke, fill, strokeWidth, opacity);
		}
	}

	public static void drawBoxes(List<? extends SVGElement> elementList, SVGElement svgParent, double strokeWidth, double opacity) {
		for (SVGElement element : elementList) {
			SVGElement.drawBox(element.getBoundingBox(), element, element.getStroke(), element.getFill(), strokeWidth, opacity);
		}
	}
	
	public static void setBoundingBoxCached(List<? extends SVGElement> elementList, boolean cached) {
		for (SVGElement element : elementList) {
			element.setBoundingBoxCached(cached);
		}
	}
	
	public static Real2Range createBoundingBox(List<SVGElement> elementList) {
		Real2Range r2r = null;
		if (elementList != null && elementList.size() > 0) {
			r2r = elementList.get(0).getBoundingBox();
			for (int i = 1; i < elementList.size(); i++) {
				r2r = r2r.plus(elementList.get(i).getBoundingBox());
			}
		}
		return r2r;
	}
}
