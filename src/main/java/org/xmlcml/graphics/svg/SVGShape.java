package org.xmlcml.graphics.svg;

import java.util.ArrayList;
import java.util.List;

/** tags SVG primitives as geometric shapes.
 * <p>
 * Essentially implements Java2D.Shape
 * </p>
 * @author pm286
 *
 */
public abstract class SVGShape extends SVGElement {
	
	public static final String ALL_SHAPE_XPATH = "" +
			"//svg:circle | " +
			"//svg:ellipse | " +
			"//svg:image | " +
			"//svg:line | " +
			"//svg:path | " +
			"//svg:polygon | " +
			"//svg:polyline | " +
			"//svg:rect | " +
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
	public static List<SVGShape> extractShapes(SVGElement svgElement) {
		return SVGShape.extractShapes(SVGUtil.getQuerySVGElements(svgElement, ALL_SHAPE_XPATH));
	}

	public String getSignature() {
		return getGeometricHash();
	}

	
}
