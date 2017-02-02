package org.xmlcml.graphics.svg;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;

/** tags SVG primitives as geometric shapes.
 * <p>
 * Essentially implements Java2D.Shape
 * </p>
 * @author pm286
 *
 */
public abstract class SVGShape extends SVGElement {
	
	public static final String ALL_SHAPE_XPATH = "" +
			".//svg:circle[not(ancestor::svg:defs)] | " +
			".//svg:ellipse[not(ancestor::svg:defs)] | " +
			".//svg:image[not(ancestor::svg:defs)] | " +
			".//svg:line[not(ancestor::svg:defs)] | " +
			".//svg:path[not(ancestor::svg:defs)] | " +
			".//svg:polygon[not(ancestor::svg:defs)] | " +
			".//svg:polyline[not(ancestor::svg:defs)] | " +
			".//svg:rect[not(ancestor::svg:defs)]" +
			"";

	protected SVGShape(String name) {
		super(name);
	}

	protected SVGShape(SVGElement element) {
		super(element);
	}

	/** a string that uniquely defines the geometric position without attributes.
	 * 
	 * Fairly crude. Object is to identify precise duplicates of the object.
	 * Requires exact consistent formatting of the coordinates.
	 * 
	 * @return
	 */
	public abstract String getGeometricHash();
	
	/** makes a new list composed of the shapes in the list
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGShape> extractShapes(List<SVGElement> elements) {
		List<SVGShape> shapeList = new ArrayList<SVGShape>();
		for (SVGElement element : elements) {
			if (element instanceof SVGShape) {
				shapeList.add((SVGShape) element);
			}
		}
		return shapeList;
	}

	/** convenience method to extract list of svgShapes in element
	 * 
	 * @param svgElement
	 * @return
	 */
	public static List<SVGShape> extractSelfAndDescendantShapes(SVGElement svgElement) {
		return SVGShape.extractShapes(SVGUtil.getQuerySVGElements(svgElement, ALL_SHAPE_XPATH));
	}

	private static void replaceLineAndCloseUp(int iline, SVGLine newLine, List<SVGLine> lineListNew) {
		lineListNew.set(iline, newLine);
		lineListNew.remove(iline + 1);
	}

	public String getSignature() {
		return getGeometricHash();
	}

	public void setMarkerEndRef(SVGMarker marker) {
		String id = marker.getId();
		this.setMarkerEnd(makeUrlRef(id));
	}

	private void setMarkerEnd(String markerEnd) {
		this.addAttribute(new Attribute(SVGMarker.MARKER_END, markerEnd));
	}

	public void setMarkerStartRef(SVGMarker marker) {
		String id = marker.getId();
		this.setMarkerStart(makeUrlRef(id));
	}

	private void setMarkerStart(String markerStart) {
		this.addAttribute(new Attribute(SVGMarker.MARKER_START, markerStart));
	}

	public void setMarkerMidRef(SVGMarker marker) {
		String id = marker.getId();
		this.setMarkerMid(makeUrlRef(id));
	}

	private String makeUrlRef(String id) {
		return "url(#"+id+")";
	}

	private void setMarkerMid(String markerMid) {
		this.addAttribute(new Attribute(SVGMarker.MARKER_MID, markerMid));
	}

	/** is this an zero-dimensional shape?
	 * 
	 * @return
	 */
	public boolean isZeroDimensional() {
		if (boundingBox == null) {
			getBoundingBox();
		}
		boolean isZeroDimensional = boundingBox == null ||
			(boundingBox.getXRange().getRange() == 0.0 &&
			boundingBox.getYRange().getRange() == 0.0);
		return isZeroDimensional;
	}


	
}
