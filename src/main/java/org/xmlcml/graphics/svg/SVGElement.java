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
import java.awt.Graphics2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Comment;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParentNode;
import nu.xom.ProcessingInstruction;
import nu.xom.Text;
import nu.xom.canonical.Canonicalizer;

import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.cml.base.CMLElement;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealArray;
import org.xmlcml.euclid.RealRange;
import org.xmlcml.euclid.RealSquareMatrix;
import org.xmlcml.euclid.Transform2;

/** base class for lightweight generic SVG element.
 * no checking - i.e. can take any name or attributes
 * @author pm286
 *
 */
public class SVGElement extends GraphicsElement {
	private static Logger LOG = Logger.getLogger(GraphicsElement.class);

	public final static String SVG_CLASS = "class";
	public final static String IMPROPER = "improper";
	public final static String IMPROPER_TRUE = "true";
	public final static String MATRIX = "matrix";
	public final static String ROTATE = "rotate";
	public final static String SCALE = "scale";
	public final static String STYLE = "style";
	public final static String TRANSFORM = "transform";
	public final static String TRANSLATE = "translate";
	public final static String X = "x";
	public final static String Y = "y";
	public final static String CX = "cx";
	public final static String CY = "cy";
	public final static String YMINUS = "-Y";
	public final static String YPLUS = "Y";
	public final static String TITLE = "title";
	public final static String ID = "id";

	protected static final String BOUNDING_BOX = "boundingBox";
	
	private Element userElement;
	private String strokeSave;
	private String fillSave;

	
	/** constructor.
	 * 
	 * @param name
	 */
	public SVGElement(String name) {
		super(name,SVG_NAMESPACE);
	}

	public SVGElement(SVGElement element) {
        super((GraphicsElement) element);
        this.userElement = element.userElement;
	}
	
	public SVGElement(SVGElement element, String tag) {
        super((GraphicsElement) element, tag);
        this.userElement = element.userElement;
	}
	
	/** copy constructor from non-subclassed elements
	 */
	public static SVGElement readAndCreateSVG(Element element) {
		SVGElement newElement = null;
		String tag = element.getLocalName();
		if (tag == null || tag.equals(S_EMPTY)) {
			throw new RuntimeException("no tag");
		} else if (tag.equals(SVGCircle.TAG)) {
			newElement = new SVGCircle();
		} else if (tag.equals(SVGClipPath.TAG)) {
			newElement = new SVGClipPath();
		} else if (tag.equals(SVGDefs.TAG)) {
			newElement = new SVGDefs();
		} else if (tag.equals(SVGDesc.TAG)) {
			newElement = new SVGDesc();
		} else if (tag.equals(SVGEllipse.TAG)) {
			newElement = new SVGEllipse();
		} else if (tag.equals(SVGG.TAG)) {
			newElement = new SVGG();
		} else if (tag.equals(SVGImage.TAG)) {
			newElement = new SVGImage();
		} else if (tag.equals(SVGLine.TAG)) {
			newElement = new SVGLine();
		} else if (tag.equals(SVGPath.TAG)) {
			newElement = new SVGPath();
		} else if (tag.equals(SVGPattern.TAG)) {
			newElement = new SVGPattern();
		} else if (tag.equals(SVGPolyline.TAG)) {
			newElement = new SVGPolyline();
		} else if (tag.equals(SVGPolygon.TAG)) {
			newElement = new SVGPolygon();
		} else if (tag.equals(SVGRect.TAG)) {
			newElement = new SVGRect();
		} else if (tag.equals(SVGScript.TAG)) {
			newElement = new SVGScript();
		} else if (tag.equals(SVGSVG.TAG)) {
			newElement = new SVGSVG();
		} else if (tag.equals(SVGText.TAG)) {
			newElement = new SVGText();
		} else if (tag.equals(SVGTSpan.TAG)) {
			newElement = new SVGTSpan();
		} else if (tag.equals(SVGTitle.TAG)) {
			newElement = new SVGTitle();
		} else {
			newElement = new SVGG();
			newElement.setClassName(tag);
			System.err.println("unsupported svg element: "+tag);
		}
		if (newElement != null) {
	        newElement.copyAttributesFrom(element);
	        createSubclassedChildren(element, newElement);
		}
        return newElement;
		
	}
	
	/** converts a SVG file to SVGElement
	 * 
	 * @param file
	 * @return
	 */
	public static SVGElement readAndCreateSVG(File file) {
		Element element = CMLUtil.parseQuietlyToDocument(file).getRootElement();
		return (element == null) ? null : (SVGElement) readAndCreateSVG(element);
	}
	
	/** converts a SVG file to SVGElement
	 * 
	 * @param file
	 * @return
	 */
	public static SVGElement readAndCreateSVG(InputStream is) {
		Element element = CMLUtil.parseQuietlyToDocument(is).getRootElement();
		return (element == null) ? null : (SVGElement) readAndCreateSVG(element);
	}
	
	protected static void createSubclassedChildren(Element oldElement, SVGElement newElement) {
		if (oldElement != null) {
			for (int i = 0; i < oldElement.getChildCount(); i++) {
				Node node = oldElement.getChild(i);
				Node newNode = null;
				if (node instanceof Text) {
					String value = node.getValue();
//					LOG.debug(value+"/"+(int)value.charAt(0));
					newNode = new Text(value);
				} else if (node instanceof Comment) {
					newNode = new Comment(node.getValue());
				} else if (node instanceof ProcessingInstruction) {
					newNode = new ProcessingInstruction((ProcessingInstruction) node);
				} else if (node instanceof Element) {
					newNode = readAndCreateSVG((Element) node);
				} else {
					throw new RuntimeException("Cannot create new node: "+node.getClass());
				}
				newElement.appendChild(newNode);
			}
		}
	}
	
	public boolean isEqualTo(SVGElement element) {
		boolean equals = false;
		if (element.getClass().equals(this.getClass())) {
			CMLUtil.equalsCanonically(element, this, true);
		}
		return equals;
	}
	
	public String getCanonicalizedXML() {
		OutputStream out = new ByteArrayOutputStream();
		Canonicalizer canonicalizer = new Canonicalizer(out, false);
		String s = null;
		try {
			canonicalizer.write(this);
			s = out.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return s;
	}
	
	/**
	 * @return the sVG_NAMESPACE
	 */
	public static String getSVG_NAMESPACE() {
		return SVG_NAMESPACE;
	}

	/**
	 * @param g2d
	 */
	public void draw(Graphics2D g2d) {
		drawElement(g2d);
	}
	
	/** draws children recursively
	 * 
	 * @param g2d
	 */
	protected void drawElement(Graphics2D g2d) {
		Elements gList = this.getChildElements();
		for (int i = 0; i < gList.size(); i++) {
			SVGElement svge = (SVGElement) gList.get(i);
			svge.drawElement(g2d);
		}
	}
	
	/**
	 * @return the transform
	 */
	public Transform2 getTransform() {
		Transform2 t2 = ensureTransform();
		return t2;
	}
	/**
	 * @param transform the transform to set
	 */
	public void setTransform(Transform2 transform) {
		processTransform(transform);
	}
	
	protected void processTransform(Transform2 transform) {
		double[] matrix = transform.getMatrixAsArray();
		this.addAttribute(new Attribute(TRANSFORM, MATRIX+"(" +
				matrix[0] +"," +
				matrix[3] +"," +
				matrix[1] +"," +
				matrix[4] +"," +
				matrix[2]+","+
				matrix[5]+
			")"));
	}

	/** applies any transform attribute and removes it.
	 * not yet hierarchical, so only use on lines, text, etc.
	 */
	public void applyTransformAttributeAndRemove() {
		Attribute transformAttribute = this.getAttribute(TRANSFORM);
		if (transformAttribute != null) {
			Transform2 transform2 = createTransform2FromTransformAttribute(transformAttribute.getValue());
			this.applyTransform(transform2);
			transformAttribute.detach();
			double det = transform2.determinant();
			// improper rotation ?
			if (det < 0) {
				Transform2 t = new Transform2(
					new double[] {
							1.0,  0.0,  0.0,
							0.0, -1.0,  0.0,
							0.0,  0.0,  1.0,
					});
				transform2 = transform2.concatenate(t);
				this.addAttribute(new Attribute(IMPROPER, IMPROPER_TRUE));
			}
			// is object rotated?
			Angle angle = transform2.getAngleOfRotation();
			if (angle.getRadian() > Math.PI/4.) {
				this.addAttribute(new Attribute(ROTATE, YPLUS));
			}
			if (angle.getRadian() < -Math.PI/4.) {
				this.addAttribute(new Attribute(ROTATE, YMINUS));
			}
		}
	}
	
	/**
	 * uses transform attribute to get angle, 0 if absent
	 * @return angle in degrees
	 */
	public Double getAngleOfRotationFromTransformInDegrees() {
		Double angle = 0.0;
		Transform2 transform = this.getTransform();
		if (transform != null) {
			Angle rotationAngle = transform.getAngleOfRotation();
			if (rotationAngle != null) {
				angle = rotationAngle.getDegrees();
			}
		}
		return angle;
	}
	
	/** currently a no-op.
	 * subclassed by elements with coordinates
	 * @param transform
	 */
	public void applyTransform(Transform2 transform) {
		LOG.trace("No transform applied to: "+this.getClass());
	}
	
	public static Transform2 createTransform2FromTransformAttribute(String transformAttributeValue) {
/**
    * matrix(<a> <b> <c> <d> <e> <f>)
    * translate(<tx> [<ty>])
    * scale(<sx> [<sy>]),
    * rotate(<rotate-angle> [<cx> <cy>])
    * skewX(<skew-angle>)
    * skewY(<skew-angle>)
 */
		Transform2 transform2 = null;
		if (transformAttributeValue != null) {
			transform2 = new Transform2();
			List<Transform2> transformList = new ArrayList<Transform2>();
			String s = transformAttributeValue.trim();
			while (s.length() > 0) {
				int lb = s.indexOf(CMLConstants.S_LBRAK);
				int rb = s.indexOf(CMLConstants.S_RBRAK);
				if (lb == -1 || rb == -1 || rb < lb) {
					throw new RuntimeException("Unbalanced or missing brackets in transform");
				}
				String kw = s.substring(0, lb);
				String values = s.substring(lb+1, rb);
				// remove unwanted spaces
				values = values.replaceAll("  *", " ");
				s = s.substring(rb+1).trim();
				Transform2 t2 = makeTransform(kw, values);
				transformList.add(t2);
			}
			for (Transform2 t2 : transformList) {
				transform2 = transform2.concatenate(t2);
			}
		}
		return transform2;
	}
	
	private static Transform2 makeTransform(String keyword, String valueString) {
		// remove unwanted space
		valueString = valueString.replace(S_SPACE+S_PLUS, S_SPACE);
		valueString = valueString.replace(S_COMMA+S_SPACE, S_COMMA);
		valueString = valueString.replace(S_PIPE+S_SPACE, S_PIPE);
		LOG.trace("Transform "+valueString);
		Transform2 t2 = new Transform2();
		String[] vv = valueString.trim().split(S_COMMA+S_PIPE+S_SPACE);
		RealArray ra = new RealArray(vv);
		double[] raa = ra.getArray();
		double[][] array = t2.getMatrix();
		if (keyword.equals(SCALE) && ra.size() > 0) {
			array[0][0] = raa[0];
			if (ra.size() == 1) {
				array[1][1] = raa[0];
			} else if (ra.size() == 2) {
				array[1][1] = raa[1];
			} else if (ra.size() != 1){
				throw new RuntimeException("Only 1 or 2 scales allowed");
			}
		} else if (keyword.equals(TRANSLATE) && ra.size() > 0) {
			array[0][2] = raa[0];
			if (ra.size() == 1) {
				array[1][2] = 0.0;
			} else if (ra.size() == 2) {
				array[1][2] = raa[1];
			} else {
				throw new RuntimeException("Only 1 or 2 translate allowed");
			}
		} else if (keyword.equals(ROTATE) && ra.size() == 1) {
			double c = Math.cos(raa[0]*Math.PI/180.);
			double s = Math.sin(raa[0]*Math.PI/180.);
			array[0][0] = c;
			array[0][1] = s;
			array[1][0] = -s;
			array[1][1] = c;
		} else if (keyword.equals(ROTATE) && ra.size() == 3) {
			throw new RuntimeException("rotate about point not yet supported");
		} else if (keyword.equals(MATRIX) && ra.size() == 6) {
			t2 = createTransformFrom1D(ra.getArray());
		} else {
			throw new RuntimeException("Unknown/unsuported transform keyword: "+keyword);
		}

		return t2;
	}

	private static Transform2 createTransformFrom1D(double[] raa) {
		double[][] array = new double[3][];
		for (int i = 0; i < 3; i++) {
			array[i] = new double[3];
		}
		array[0][0] = raa[0];
		array[0][1] = raa[2];
		array[0][2] = raa[4];
		array[1][0] = raa[1];
		array[1][1] = raa[3];
		array[1][2] = raa[5];
		array[2][0] = 0.0;
		array[2][1] = 0.0;
		array[2][2] = 1.0;
		return new Transform2(new RealSquareMatrix(array));	
	}
	/**
	 * 
	 * @param s
	 */
	public void setScale(double s) {
		Transform2 transform = ensureTransform();
		Transform2 t = new Transform2(
				new double[]{
				s, 0., 0.,
				0., s, 0.,
				0., 0., 1.
				});
		transform = transform.concatenate(t);
		processTransform(transform);
	}

	protected Transform2 ensureTransform() {
		Transform2 t2 = new Transform2();
		String t2Value = this.getAttributeValue(TRANSFORM);
		if (t2Value != null) {
			t2 = createTransform2FromTransformAttribute(t2Value);		
		}
		return t2;
	}

//	/** set properties.
//	 * 
//	 * @param abstractDisplay
//	 */
//	public void setProperties(AbstractDisplay abstractDisplay) {
//		this.setFontStyle(abstractDisplay.getFontStyle());
//		this.setFontWeight(abstractDisplay.getFontWeight());
//		this.setFontFamily(abstractDisplay.getFontFamily());
//		this.setFontSize(abstractDisplay.getFontSize());
//		this.setFill(abstractDisplay.getFill());
//		this.setStroke(abstractDisplay.getStroke());
//		this.setOpacity(abstractDisplay.getOpacity());
//		
//	}
//	
	/**
	 */
	public void setCumulativeTransformRecursively() {
		setCumulativeTransformRecursively("set");
	}

	/**
	 */
	public void clearCumulativeTransformRecursively() {
		setCumulativeTransformRecursively(null);
	}
	
	/**
	 * @param value if null clear the transform else concatenate
	 * may be overridden by children such as Text
	 */
	protected void setCumulativeTransformRecursively(Object value) {
		if (value != null) {
			Transform2 thisTransform = this.getTransform2FromAttribute();
			ParentNode parentNode = this.getParent();
			Transform2 parentTransform = (parentNode instanceof GraphicsElement) ?
					((GraphicsElement) parentNode).getCumulativeTransform() : new Transform2();
			this.cumulativeTransform = (thisTransform == null) ? parentTransform : parentTransform.concatenate(thisTransform);
			for (int i = 0; i < this.getChildElements().size(); i++) {
				Node child = this.getChild(i);
				if (child instanceof SVGElement) {
					((SVGElement) child).setCumulativeTransformRecursively(value);
				}
			}
		}
	}
	
	static Map<String, Color> colorMap;

	protected Real2Range boundingBox = null;

	protected boolean boundingBoxCached = false;
	
	static {
		colorMap = new HashMap<String, Color>();
		colorMap.put("black", new Color(0, 0, 0));
		colorMap.put("white", new Color(255, 255, 255));
		colorMap.put("red", new Color(255, 0, 0));
		colorMap.put("green", new Color(0, 255, 0));
		colorMap.put("blue", new Color(0, 0, 255));
		colorMap.put("yellow", new Color(255, 255, 0));
		colorMap.put("orange", new Color(255, 127, 0));
		colorMap.put("#ff00ff", new Color(255, 0, 255));
	}

	/**
	 * 
	 * @param attName
	 * @return color
	 */
	public Color getColor(String attName) {
		String attVal = this.getAttributeValue(attName);
		return getJava2DColor(attVal, this.getOpacity());
	}

	/**
	 * translate SVG string to Java2D
	 * opacity defaults to 1.0
	 * @param color
	 * @param colorS
	 * @return
	 */
	public static Color getJava2DColor(String colorS) {
		return getJava2DColor(colorS, 1.0);
	}

	/**
	 * 
	 * @param colorS common colors ("yellow"), etc or hexString
	 * @param opacity 0.0 to 1.0
	 * @return java Color or null
	 */
	public static Color getJava2DColor(String colorS, double opacity) {
		Color color = null;
		if ("none".equals(colorS)) {
		} else if (colorS != null) {
			color = colorMap.get(colorS);
			if (color == null) {
				if (colorS.length() == 7 && colorS.startsWith(S_HASH)) {
					try {
						int red = Integer.parseInt(colorS.substring(1, 3), 16);
						int green = Integer.parseInt(colorS.substring(3, 5), 16);
						int blue = Integer.parseInt(colorS.substring(5, 7), 16);
						color = new Color(red, green, blue, 0);
					} catch (Exception e) {
						throw new RuntimeException("Cannot parse: "+colorS);
					}
					colorS = colorS.substring(1);
				} else {
//					System.err.println("Unknown color: "+colorS);
				}
			}
		}
		if (color != null) {
			color = (Double.isNaN(opacity)) ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255.0 * opacity));
		} else {
			color = new Color(255, 255, 255, 0);
		}
		return color;
	}
	
	/**
	 * transforms xy
	 * messy
	 * @param xy is transformed
	 * @param transform
	 * @return transformed xy
	 */
	public static Real2 transform(Real2 xy, Transform2 transform) {
		xy.transformBy(transform);
//		xy = xy.plus(new Real2(250, 250));
		return xy;
	}

	protected double getDouble(String attName) {
		String attVal = this.getAttributeValue(attName);
		double xx = Double.NaN;
		if (attVal != null) {
			try {
				xx = new Double(attVal).doubleValue();
			} catch (NumberFormatException e) {
				throw e;
			}
		}
		return xx;
	}

	/**
	 * uses attribute value to calculate transform
	 * @return current transform or null
	 */
	public Transform2 getTransform2FromAttribute() {
		Transform2 t = null;
		String ts = this.getAttributeValue(TRANSFORM);
		if (ts != null) {
			if (!ts.startsWith(MATRIX+"(")) {
				throw new RuntimeException("Bad transform: "+ts);
			}
			ts = ts.substring((MATRIX+"(").length());
			ts = ts.substring(0, ts.length()-1);
			ts = ts.replace(S_COMMA, S_SPACE);
			RealArray realArray = new RealArray(ts);
			t = createTransformFrom1D(realArray.getArray());
		}
		return t;
	}
	
	public Transform2 ensureTransform2() {
		Transform2 t = getTransform2FromAttribute();
		if (t == null) {
			t = new Transform2();
			setTransform(t);
		}
		return t;
	}
	
	/**
	 * sets attribute value from transform
	 * @param transform
	 */
	public void setAttributeFromTransform2(Transform2 transform) {
		if (transform != null) {
			double[] dd = transform.getMatrixAsArray();
			String ts = "matrix"+
			S_LBRAK+
			dd[0]+S_COMMA+
			dd[1]+S_COMMA+
			dd[3]+S_COMMA+
			dd[4]+S_COMMA+
			dd[2]+S_COMMA+
			dd[5]+
			S_RBRAK;
			this.addAttribute(new Attribute("transform", ts));
		}
	}
	

	/**
	 */
	public void draw() {
//		FileOutputStream fos = new FileOutputStream(outfile);
//		SVGElement g = MoleculeTool.getOrCreateTool(molecule).
//		    createSVG();
//		int indent = 2;
//		SVGSVG svg = new SVGSVG();
//		svg.appendChild(g);
//		CMLUtil.debug(svg, fos, indent);
//		fos.close();
//		LOG.debug("wrote SVG "+outfile);
	}

	/**
	 * 
	 * @param xy
	 */
	public void translate(Real2 xy) {
		Transform2 transform = ensureTransform();
		Transform2 t = new Transform2(
			new double[] {
			1., 0., xy.getX(),
			0., 1., xy.getY(),
			0., 0., 1.
		});
		transform = transform.concatenate(t);
		processTransform(transform);
	}

	public void addDashedStyle(double bondWidth) {
		String style = this.getAttributeValue(STYLE);
		style += "stroke-dasharray : "+bondWidth*2+" "+bondWidth*2+";";
		this.addAttribute(new Attribute(STYLE, style));
	}
	
	public void toggleFill(String fill) {
		this.fillSave = this.getFill();
		this.setFill(fill);
	}
    
	public void toggleFill() {
		this.setFill(fillSave);
	}
	
	public void toggleStroke(String stroke) {
		this.strokeSave = this.getStroke();
		this.setStroke(stroke);
	}
    
	public void toggleStroke() {
		this.setStroke(strokeSave);
	}
    
	public void applyAttributes(Graphics2D g2d) {
		applyStrokeColor(g2d);
//		applyFillColor(g2d);
	}

	/**
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public static void test(String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		SVGSVG svg = new SVGSVG();
		SVGElement g = new SVGG();
		g.setFill("yellow");
		svg.appendChild(g);
		SVGElement line = new SVGLine(new Real2(100, 200), new Real2(300, 50));
		line.setFill("red");
		line.setStrokeWidth(3.);
		line.setStroke("blue");
		g.appendChild(line);
		SVGElement circle = new SVGCircle(new Real2(300, 150), 20);
		circle.setStroke("red");
		circle.setFill("yellow");
		circle.setStrokeWidth(3.);
		g.appendChild(circle);
		SVGElement text = new SVGText(new Real2(50, 100), "Foo");
		text.setFontFamily("TimesRoman");
		text.setStroke("green");
		text.setFill("red");
		text.setStrokeWidth(1.5);
		text.setFontSize(new Double(20.));
		text.setFontStyle(FontStyle.ITALIC);
		text.setFontWeight(FontWeight.BOLD);
		g.appendChild(text);
		CMLUtil.debug(svg, fos, 2);
		fos.close();		
	}
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
			test(args[0]);
		}
	}

	public Element getUserElement() {
		return userElement;
	}

	public void setUserElement(Element userElement) {
		this.userElement = userElement;
	}

	protected void applyStrokeColor(Graphics2D g2d) {
		String colorS = "black";
		String stroke = this.getStroke();
		if (stroke != null) {
			colorS = stroke;
		}
		Color color = colorMap.get(colorS);
		if (color != null && g2d != null) {
			g2d.setColor(color);
		}
	}
	
	protected void applyFillColor(Graphics2D g2d) {
		String colorS = "black";
		String fill = this.getFill();
		if (fill != null) {
			colorS = fill;
		}
		Color color = colorMap.get(colorS);
		if (color != null && g2d != null) {
			g2d.setColor(color);
		}
	}

	/**
	 * get double value of attribute.
	 * the full spec includes units but here we expect only numbers. Maybe later...
	 * @param attName
	 * @return
	 */
	public double getCoordinateValueDefaultZero(String attName) {
		double d = Double.NaN;
		String v = this.getAttributeValue(attName);
		try {
			d = (v == null) ? 0.0 : new Double(v).doubleValue();
		} catch (NumberFormatException e) {
			throw new RuntimeException("Cannot parse SVG coordinate "+v);
		}
		return d;
	}
	
	/** subclassed to tidy format.
	 * by default formats children
	 * @param places decimal places
	 */
	public void format(int places) {
		formatCommonAttributes(places);
		List<SVGElement> childElements = SVGUtil.getQuerySVGElements(this,  "./svg:*");
		for (SVGElement childElement : childElements) {
			childElement.format(places);
		}
	}

	private void formatCommonAttributes(int places) {
		Transform2 t2 = this.getTransform();
		if (t2 != null) {
			if (!t2.isUnit()) {
				t2 = formatTransform(t2, places);
				this.setTransform(t2);
			}
		}
	}

	private Transform2 formatTransform(Transform2 t2, int places) {
		RealArray ra = new RealArray(t2.getMatrixAsArray());
		ra.format(places);
		t2 = new Transform2(ra.getArray());
		return t2;
	}

	public double getX() {
		return this.getCoordinateValueDefaultZero(X);
	}

	public double getY() {
		return this.getCoordinateValueDefaultZero(Y);
	}

	public double getCX() {
		return this.getCoordinateValueDefaultZero(CX);
	}

	public double getCY() {
		return this.getCoordinateValueDefaultZero(CY);
	}
	
	public void setBoundingBoxAttribute(Integer decimalPlaces) {
		Real2Range r2r = this.getBoundingBox();
		if (r2r != null) {
			if (decimalPlaces != null) {
				r2r.format(decimalPlaces);
			}
			CMLElement.addCMLXAttribute(this, BOUNDING_BOX, r2r.toString());
		}
	}

	/**
	 * @param x1 the x1 to set
	 */
	public void setCXY(Real2 x1) {
		this.setCX(x1.getX());
		this.setCY(x1.getY());
	}

	public void setCX(double x) {
		this.addAttribute(new Attribute(CX, ""+x));
	}

	public void setCY(double y) {
		this.addAttribute(new Attribute(CY, ""+y));
	}

	public Real2 getCXY() {
		return new Real2(this.getCX(), this.getCY());
	}

	public void setX(double x) {
		this.addAttribute(new Attribute(X, ""+x));
	}

	public void setY(double y) {
		this.addAttribute(new Attribute(Y, ""+y));
	}
	
	public Real2 getXY() {
		return new Real2(this.getX(), this.getY());
	}
	
	public void setXY(Real2 xy) {
		setX(xy.getX());
		setY(xy.getY());
	}
	
	public double getWidth() {
		return new Double(this.getAttributeValue("width")).doubleValue();
	}
	
	public double getHeight() {
		return new Double(this.getAttributeValue("height")).doubleValue();
	}

	public void setWidth(double w) {
		this.addAttribute(new Attribute("width", ""+w));
	}
	
	public void setHeight(double h) {
		this.addAttribute(new Attribute("height", ""+h));
	}
	
	public void setClassName(String name) {
		this.addAttribute(new Attribute(SVG_CLASS, name));
	}
	
	public String getSVGClassName() {
		return this.getAttributeValue(SVG_CLASS);
	}

	/** traverse all children recursively
	 * often  copied to subclasses to improve readability
	 * @return null by default
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			aggregateBBfromSelfAndDescendants();
		}
		return boundingBox;
	}

	protected void aggregateBBfromSelfAndDescendants() {
		Nodes childNodes = this.query("./svg:*", CMLConstants.SVG_XPATH);
		if (childNodes.size() > 0) {
			boundingBox = new Real2Range();
		}
		for (int i = 0; i < childNodes.size(); i++) {
			SVGElement child = (SVGElement)childNodes.get(i);
			Real2Range childBoundingBox = child.getBoundingBox();
			if (childBoundingBox != null) {
				if (!childBoundingBox.isValid()) {
					throw new RuntimeException("invalid child BBox: "+child.getClass());
				}
				boundingBox = boundingBox.plus(childBoundingBox);
			}
		}
	}

	protected boolean boundingBoxNeedsUpdating() {
		return boundingBox == null || !boundingBoxCached ;
	}
	
	public void setBoundingBoxCached(boolean boundingBoxCached) {
		this.boundingBoxCached = boundingBoxCached;
	}

	public SVGRect createGraphicalBoundingBox() {
		Real2Range r2r = this.getBoundingBox();
		SVGRect rect = createGraphicalBox(r2r, getBBStroke(), getBBFill(), getBBStrokeWidth(), getBBOpacity());
		if (this.getAttribute("transform") != null) {
			Transform2 t2 = this.getTransform();
			if (t2 != null) {
				if (!t2.isUnit()) {
					Real2 txy = t2.getTranslation();
					rect.setTransform(t2);
				}
			}
		}
		return rect;
	}
	
	public static SVGRect createGraphicalBox(Real2Range r2r, String stroke, String fill, Double strokeWidth, Double opacity) {
		SVGRect rect = null;
		if (r2r != null) {
			RealRange xr = r2r.getXRange();
			RealRange yr = r2r.getYRange();
			if (xr == null || yr == null) {
				LOG.trace("null bbox");
				return null;
			}
			double dx = (xr.getRange() < Real.EPS) ? 1.0 : 0.0; 
			double dy = (yr.getRange() < Real.EPS) ? 1.0 : 0.0; 
			rect = new SVGRect(new Real2(xr.getMin()-dx, yr.getMin()-dy), new Real2(xr.getMax()+dx, yr.getMax()+dy));
			rect.setStrokeWidth(strokeWidth);
			rect.setStroke(stroke);
			rect.setFill(fill);
			rect.setOpacity(opacity);
		}
		return rect;
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default none
	 */
	protected String getBBFill() {
		return "none";
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default red
	 */
	protected String getBBStroke() {
		return "red";
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default 1.0
	 */
	protected double getBBStrokeWidth() {
		return 0.4;
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default 1.0
	 */
	protected double getBBOpacity() {
		return 1.0;
	}

	public static void drawBoundingBoxes(List<SVGElement> elements, SVGElement svgParent, String stroke, String fill, double strokeWidth, double opacity) {
		for (SVGElement element : elements) {
			SVGRect svgBox = SVGElement.drawBox(element.getBoundingBox(), svgParent, stroke, fill, strokeWidth, opacity);
		}
	}
	public static void drawBoundingBoxes(List<SVGElement> elements, String stroke, String fill, double strokeWidth, double opacity) {
		for (SVGElement element : elements) {
			SVGRect svgBox = SVGElement.drawBox(element.getBoundingBox(), null, stroke, fill, strokeWidth, opacity);
		}
	}
	public static void drawBoxes(List<Real2Range> boxes, SVGElement svgParent, String stroke, String fill, double strokeWidth, double opacity) {
		for (Real2Range box : boxes) {
			SVGRect svgBox = SVGElement.drawBox(box, svgParent, stroke, fill, strokeWidth, opacity);
		}
	}
	public static SVGRect drawBox(Real2Range box, SVGElement svgParent,
			String stroke, String fill, double strokeWidth, double opacity) {
		SVGRect svgBox = createGraphicalBox(box, stroke, fill, strokeWidth, opacity);
		if (svgBox != null) {
			svgParent.appendChild(svgBox);
		}
		return svgBox;
	}

	public SVGRect drawBox(String stroke, String fill, double strokeWidth, double opacity) {
		return SVGElement.drawBox(this.getBoundingBox(), this, stroke, fill, strokeWidth, opacity);
	}

	public static void applyTransformsWithinElementsAndFormat(SVGElement svgElement) {
		List<SVGElement> elementList = generateElementList(svgElement, ".//svg:*[@transform]");
		LOG.debug("NODES "+elementList.size());
		for (SVGElement element : elementList) {
			element.applyTransformAttributeAndRemove();
			element.format(2);
		}
		LOG.debug("... applied transformations");
	}

	/**
	 * @return
	 */
	public static List<SVGElement> generateElementList(Element element, String xpath) {
		Nodes childNodes = element.query(xpath, CMLConstants.SVG_XPATH);
		List<SVGElement> elementList = new ArrayList<SVGElement>();
		for (int i = 0; i < childNodes.size(); i++) {
			elementList.add((SVGElement) childNodes.get(i));
		}
		return elementList;
	}
	
	public void setTitle(String title) {
		this.addAttribute(new Attribute(TITLE, title));
	}
	
	public String getTitle() {
		return this.getAttributeValue(TITLE);
	}
	
	public void setId(String id) {
		this.addAttribute(new Attribute(ID, id));
	}
	
	public String getId() {
		return this.getAttributeValue(ID);
	}

	/** removes all transformation attributes
	 * @transform
	 * THIS IS NORMALLY ONLY DONE AFTER APPLYING CUMULATIVE TRANSFORMATIONS
	 * also dangerous as the ancestor may govern other descendants
	 */
	public void removeAncestorTransformations() {
		Nodes ancestorAttributes = this.query("ancestor::*/@transform");
		for (int i = 0; i < ancestorAttributes.size(); i++) {
			ancestorAttributes.get(i).detach();
		}
	}

	public void removeEmptySVGG() {
		List<SVGElement> emptyGList = SVGUtil.getQuerySVGElements(this, ".//svg:g[(count(*)+count(svg:*))=0]");
		for (SVGElement g : emptyGList) {
			g.detach();
		}
		LOG.trace("removed emptyG: "+emptyGList.size());
	}

	/** tests whether element is geometricallyContained within this
	 * for most elements uses this.getBoundingBox()
	 * can be overridden for special cases such as circle
	 * @param element
	 * @return
	 */
	public boolean includes(SVGElement element) {
		Real2Range thisBbox = this.getBoundingBox();
		Real2Range elementBox = (element == null) ? null : element.getBoundingBox();
		return thisBbox != null && thisBbox.includes(elementBox);
	}
}
