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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLUtil;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Transform2;

/** base class for lightweight generic SVG element.
 * no checking - i.e. can take any name or attributes
 * @author pm286
 *
 */
public class GraphicsElement extends Element implements SVGConstants {

	private final static Logger LOG = Logger.getLogger(GraphicsElement.class);
	
	public enum FontWeight {
		BOLD,
		NORMAL
	}
	
	public enum FontStyle {
		ITALIC,
		NORMAL
	}
	
	private static final String BOLD = "bold";
	private static final String CLASS = "class";
	static Map<String, Color> colorMap;

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

	
	protected Transform2 cumulativeTransform = null/*new Transform2()*/;
	protected boolean useStyleAttribute = false;
	private StyleBundle styleBundle;
	
// save when drawing to Graphics2D	
	private Color saveColor;
	private Stroke saveStroke;
	private AffineTransform savedAffineTransform;
		
	/** constructor.
	 * 
	 * @param name
	 * @param namespace
	 */
	public GraphicsElement(String name, String namespace) {
		super(name, namespace);
		init();
	}
	
    /**
     * main constructor.
     * 
     * @param name tagname
     */
    public GraphicsElement(String name) {
        this(name, SVG_NAMESPACE);
        init();
    }
    
    protected void init() {
    	setDefaultStyle();
    }
    
    public void setDefaultStyle() {
//		setOpacity(1.0);
    }
    
    protected Transform2 ensureCumulativeTransform() {
    	if (cumulativeTransform == null) {
    		cumulativeTransform = new Transform2();
    	}
    	return cumulativeTransform;
    }
    
    /**
     * copy constructor. copies attributes, children and properties using the
     * copyFoo() routines (q.v.)
     * 
     * @param element
     */
    public GraphicsElement(GraphicsElement element) {
        this(element.getLocalName());
        copyAttributesChildrenElements(element);
    }

    protected GraphicsElement(GraphicsElement element, String tag) {
        this(tag);
        copyAttributesChildrenElements(element);
    }

	private void copyAttributesChildrenElements(GraphicsElement element) {
		copyAttributesFrom(element);
        copyChildrenFrom(element);
        copyNamespaces(element);
	}

    /**
     * copies namespaces.
     * @param element to copy from
     */
    public void copyNamespaces(GraphicsElement element) {
        int n = element.getNamespaceDeclarationCount();
        for (int i = 0; i < n; i++) {
            String namespacePrefix = element.getNamespacePrefix(i);
            String namespaceURI = element.getNamespaceURIForPrefix(namespacePrefix);
            this.addNamespaceDeclaration(namespacePrefix, namespaceURI);
        }
    }

    /**
     * copies attributes. makes subclass if necessary.
     * 
     * @param element to copy from
     */
    public void copyAttributesFrom(Element element) {
    	if (element != null) {
	        for (int i = 0; i < element.getAttributeCount(); i++) {
	            Attribute att = element.getAttribute(i);
	            Attribute newAtt = (Attribute) att.copy();
	            this.addAttribute(newAtt);
	        }
    	}
    }

    
    /** copies children of element make subclasses when required
     * 
     * @param element to copy from
     */
    public void copyChildrenFrom(Element element) {
        for (int i = 0; i < element.getChildCount(); i++) {
            Node childNode = element.getChild(i);
            Node newNode = childNode.copy();
            this.appendChild(newNode);
        }
    }
    
    
    /**
     * copy node.
     * 
     * @return node
     */
    public Node copy() {
        return new GraphicsElement(this);
    }

    /**
     * get namespace.
     * 
     * @param prefix
     * @return namespace
     */
    public String getNamespaceURIForPrefix(String prefix) {
        String namespace = null;
        Element current = this;
        while (true) {
            namespace = current.getNamespaceURI(prefix);
            if (namespace != null) {
                break;
            }
            Node parent = current.getParent();
            if (parent == null || parent instanceof Document) {
                break;
            }
            current = (Element) parent;
        }
        return namespace;
    }

    public void applyStyles() {
    	this.addAttribute(new Attribute(StyleBundle.STYLE, styleBundle.toString()));
    }
    
	public boolean isUseStyleAttribute() {
		return useStyleAttribute;
	}

	public void setUseStyleAttribute(boolean useStyleAttribute) {
		this.useStyleAttribute = useStyleAttribute;
		if (useStyleAttribute) {
			convertFromExplicitAttributes();
		} else {
			convertToExplicitAttributes();
		}
	}

    public void setSvgClass(String svgClass) {
    	this.addAttribute(new Attribute(CLASS, svgClass));
    }
    
    public String getSvgClass() {
    	return this.getAttributeValue(CLASS);
    }
    
	/**
	 * @return the clipPath
	 */
	public String getClipPath() {
		return (String) getSubStyle(StyleBundle.CLIP_PATH);
	}

	/**
	 * @param clip-path
	 */
	public void setClipPath(String clipPath) {
		setSubStyle(StyleBundle.CLIP_PATH, clipPath);
	}

	/**
	 * @return the fill
	 */
	public String getFill() {
		return (String) getSubStyle(StyleBundle.FILL);
	}

	/**
	 * @param fill the fill to set
	 */
	public void setFill(String fill) {
		setSubStyle(StyleBundle.FILL, fill);
	}

	/**
	 * @return the fill
	 */
	public String getStroke() {
		return (String) getSubStyle(StyleBundle.STROKE);
	}

	/**
	 * @param fill the fill to set
	 */
	public void setStroke(String stroke) {
		setSubStyle(StyleBundle.STROKE, stroke);
	}

	/**
	 * @return the font
	 */
	public String getFontFamily() {
		return (String) getSubStyle(StyleBundle.FONT_FAMILY);
	}

	/**
	 * @param fill the fill to set
	 */
	public void setFontFamily(String fontFamily) {
		setSubStyle(StyleBundle.FONT_FAMILY, fontFamily);
	}

	/**
	 * @return the font
	 */
	public String getFontStyle() {
		return (String) getSubStyle(StyleBundle.FONT_STYLE);
	}

	/**
	 * @param fill the fill to set
	 */
	public void setFontStyle(String fontStyle) {
		setSubStyle(StyleBundle.FONT_STYLE, fontStyle);
	}

	/**
	 * @param fill the fill to set
	 */
	public void setFontStyle(FontStyle fontStyle) {
		this.setFontStyle(fontStyle == null ? null : fontStyle.toString().toLowerCase());
	}

	/**
	 * @return the font
	 */
	public String getFontWeight() {
		return (String) getSubStyle(StyleBundle.FONT_WEIGHT);
	}

	/**
	 * @param fill the font weight to set
	 */
	public void setFontWeight(String fontWeight) {
		setSubStyle(StyleBundle.FONT_WEIGHT, fontWeight);
	}

	/**
	 * @param fill the font weight to set
	 */
	public void setFontWeight(FontWeight fontWeight) {
		this.setFontWeight((fontWeight == null) ? null : fontWeight.toString().toLowerCase());
	}

	/**
	 * @return the opacity (1.0 if not present or error
	 */
	public Double getOpacity() {
		Double opacity = getDouble(getSubStyle(StyleBundle.OPACITY));
		return (opacity == null) ? null : opacity.doubleValue();
	}

	/**
	 * @param opacity the opacity to set
	 */
	public void setOpacity(double opacity) {
		setSubStyle(StyleBundle.OPACITY, getDouble(opacity));
	}

	/**
	 * @return the stroke-width (default if not present or error)
	 */
	public Double getStrokeWidth() {
		Double strokeWidth = getDouble(getSubStyle(StyleBundle.STROKE_WIDTH));
		return (strokeWidth == null) ? null : strokeWidth.doubleValue();
	}

	/**
	 * 
	 * @param strokeWidth
	 */
	public void setStrokeWidth(Double strokeWidth) {
		setSubStyle(StyleBundle.STROKE_WIDTH, getDouble(strokeWidth));
	}
	
	public String getStrokeDashArray() {
		String dashes = (String) getSubStyle(StyleBundle.DASHARRAY);
		return (dashes == null) ? null : dashes.toString();
	}

	public void setStrokeDashArray(String dashArray) {
		setSubStyle(StyleBundle.DASHARRAY, dashArray);
		addAttribute(new Attribute(StyleBundle.DASHARRAY, dashArray));
		LOG.trace("DASH "+dashArray);
	}

	/**
	 * @return the font-size 
	 */
	public Double getFontSize() {
		Double fontSize = getDouble(getSubStyle(StyleBundle.FONT_SIZE));
		return (fontSize == null) ? null : fontSize.doubleValue();
	}

	/**
	 * 
	 * @param fontSize
	 */
	public void setFontSize(Double fontSize) {
		if (fontSize == null) {
			setSubStyle(StyleBundle.FONT_SIZE, null);
			Attribute fontSizeAttribute = this.getAttribute(StyleBundle.FONT_SIZE);
			if (fontSizeAttribute != null) {
				this.removeAttribute(fontSizeAttribute);
			}
		} else {
			setSubStyle(StyleBundle.FONT_SIZE, new Double(fontSize));
		}
	}

	private Double getDouble(Object subStyle) {
		Double d = null;
		try {
			d = new Double(""+subStyle);
		} catch (Exception e) {
			// return null
		}
		return d;
	}

	protected String getTag() {
		return "DUMMY";
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

	/**
	 * @return the cumulativeTransform
	 */
	public Transform2 getCumulativeTransform() {
		Nodes transforms = this.query("ancestor-or-self::*/@transform");
		cumulativeTransform = new Transform2();
		for (int i = transforms.size()-1; i >= 0; i--) {
			Transform2 t2 = ((SVGElement) transforms.get(i).getParent()).getTransform();
			cumulativeTransform = t2.concatenate(cumulativeTransform);
		}
		return cumulativeTransform;
	}

	public StyleBundle getStyleBundle() {
		String style = this.getStyle();
		if (style != null) {
			styleBundle = new StyleBundle(style);
		}
		return styleBundle;
	}
	
	public String getStyle() {
		return this.getAttributeValue(StyleBundle.STYLE);
	}

	private void setSubStyle(String attName, Object value) {
		if (useStyleAttribute) {
			convertFromExplicitAttributes();
			styleBundle.setSubStyle(attName, value);
			applyStyles();
		} else {
			convertToExplicitAttributes();
			if (value != null) {
				this.addAttribute(new Attribute(attName, ""+value));
			} else {
				Attribute att = this.getAttribute(attName);
				if (att != null) {
					att.detach();
				}
			}
		}
	}

	private StyleBundle convertFromExplicitAttributes() {
		if (styleBundle == null) {
			styleBundle = new StyleBundle();
		}
		styleBundle.processStyle(this.getAttributeValue(StyleBundle.STYLE));
		styleBundle.convertAndRemoveExplicitAttributes(this);
		return styleBundle;
	}

    void convertToExplicitAttributes() {
		if (styleBundle != null) {
			styleBundle.removeStyleAttributesAndMakeExplicit(this);
		}
	}

	private Object getSubStyle(String attName) {
		if (useStyleAttribute) {
			StyleBundle styleBundle = getStyleBundle();
			return (styleBundle == null) ? null : styleBundle.getSubStyle(attName);
		} else {
			return this.getAttributeValue(attName);
		}
	}

	public void debug(String msg) {
		CMLUtil.debug(this, msg);
	}
	
	protected void saveGraphicsSettingsAndApplyTransform(Graphics2D g2d) {
		this.processTransformToAffineTransform(g2d);
		saveColor(g2d);
		saveStroke(g2d);
		ensureCumulativeTransform();
	}

	private void processTransformToAffineTransform(Graphics2D g2d) {
		// all transforms done in SVG...  ???
		this.savedAffineTransform = g2d.getTransform();
		Transform2 transform2 = this.getCumulativeTransform();
//		AffineTransform currentAffineTransform = (transform2 == null) ? null : transform2.getAffineTransform();
//		LOG.debug(""+this.getClass().getName()+" saved "+savedAffineTransform+" CUM: "+transform2+" "+currentAffineTransform);
//		g2d.transform(currentAffineTransform);
	}
	
	private void resetAffineTransform(Graphics2D g2d) {
		g2d.setTransform(savedAffineTransform);
	}

	protected void restoreGraphicsSettingsAndTransform(Graphics2D g2d) {
		this.resetAffineTransform(g2d);
		restoreColor(g2d);
		restoreStroke(g2d);
	}

	protected void saveStroke(Graphics2D g2d) {
		this.saveStroke = g2d.getStroke();
	}

	protected void saveColor(Graphics2D g2d) {
		this.saveColor = g2d.getColor();
	}

	protected void restoreColor(Graphics2D g2d) {
		g2d.setColor(this.saveColor);
	}

	protected void restoreStroke(Graphics2D g2d) {
		g2d.setStroke(this.saveStroke);
	}

	protected void fill(Graphics2D g2d, Shape shape) {
		String fill = this.getAttributeValue("fill");
		if (fill != null) {
			Color fillColor = colorMap.get(fill);
			g2d.setColor(fillColor);
			g2d.fill(shape);
			restoreColor(g2d);
		}
	}

	protected void drawStroke(Graphics2D g2d, Shape shape) {
		String stroke = this.getAttributeValue("stroke");
		if (stroke != null) {
			Color strokeColor = colorMap.get(stroke);
			Double strokeWidth = this.getStrokeWidth();
			strokeWidth = (strokeWidth == null) ? 1.0 : strokeWidth;
			strokeWidth = SVGElement.transform(strokeWidth, cumulativeTransform);
			Stroke s = new BasicStroke((float) (double) strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
			g2d.setStroke(s);
			g2d.setColor(strokeColor);
			g2d.draw(shape);
			restoreColor(g2d);
			restoreStroke(g2d);
		}
	}
	

}

