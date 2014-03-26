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

import nu.xom.*;
import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.xml.XMLUtil;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** 
 * Base class for lightweight generic SVG element.
 * <p>
 * No checking - i.e. can take any name or attributes.
 * 
 * @author pm286
 */
public class GraphicsElement extends Element implements SVGConstants {

	private static final String NONE = "none";
	private static final String FILL = "fill";
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
	
	//save when drawing to Graphics2D	
	private Color saveColor;
	private Stroke saveStroke;
	private AffineTransform savedAffineTransform;
		
	/** 
	 * Constructor.
	 * 
	 * @param name
	 * @param namespace
	 */
	public GraphicsElement(String name, String namespace) {
		super(name, namespace);
		init();
	}
	
    /**
     * Main constructor.
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
    	//setOpacity(1.0);
    }
    
    protected Transform2 ensureCumulativeTransform() {
    	if (cumulativeTransform == null) {
    		cumulativeTransform = new Transform2();
    	}
    	return cumulativeTransform;
    }
    
    /**
     * Copy constructor. Copies attributes, children and properties using the
     * copyFoo() routines (q.v.).
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

	protected void copyAttributesChildrenElements(GraphicsElement element) {
		copyAttributesFrom(element);
        copyChildrenFrom(element);
        copyNamespaces(element);
	}

    /**
     * Copies namespaces.
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
     * Copies attributes. Makes subclass if necessary.
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

    
    /** 
     * Copies children of element make subclasses when required.
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
     * Copies node.
     * 
     * @return node
     */
    public Node copy() {
        return new GraphicsElement(this);
    }

    /**
     * Gets namespace.
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
    	addAttribute(new Attribute(StyleBundle.STYLE, styleBundle.toString()));
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
    	addAttribute(new Attribute(CLASS, svgClass));
    }
    
    public String getSvgClass() {
    	return getAttributeValue(CLASS);
    }
    
	/**
	 * @return the clip path
	 */
	public String getClipPath() {
		return (String) getSubStyle(StyleBundle.CLIP_PATH);
	}

	/**
	 * @param clipPath the clip path to set
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
	 * @return the stroke
	 */
	public String getStroke() {
		return (String) getSubStyle(StyleBundle.STROKE);
	}

	/**
	 * @param stroke the stroke to set
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
	 * @param fontFamily the font to set
	 */
	public void setFontFamily(String fontFamily) {
		setSubStyle(StyleBundle.FONT_FAMILY, fontFamily);
	}

	/**
	 * @return the font style
	 */
	public String getFontStyle() {
		return (String) getSubStyle(StyleBundle.FONT_STYLE);
	}

	/**
	 * @param fontStyle the font style to set
	 */
	public void setFontStyle(String fontStyle) {
		setSubStyle(StyleBundle.FONT_STYLE, fontStyle);
	}

	/**
	 * @param fontStyle the font style to set
	 */
	public void setFontStyle(FontStyle fontStyle) {
		this.setFontStyle(fontStyle == null ? null : fontStyle.toString().toLowerCase());
	}

	/**
	 * @return the font weight
	 */
	public String getFontWeight() {
		return (String) getSubStyle(StyleBundle.FONT_WEIGHT);
	}

	/**
	 * @param fontWeight the font weight to set
	 */
	public void setFontWeight(String fontWeight) {
		setSubStyle(StyleBundle.FONT_WEIGHT, fontWeight);
	}

	/**
	 * @param fontWeight the font weight to set
	 */
	public void setFontWeight(FontWeight fontWeight) {
		setFontWeight((fontWeight == null) ? null : fontWeight.toString().toLowerCase());
	}

	/**
	 * @return the opacity (1.0 if not present or error)
	 */
	public Double getOpacity() {
		Double opacity = getDouble(getSubStyle(StyleBundle.OPACITY));
		return (opacity == null ? null : opacity.doubleValue());
	}

	/**
	 * @param opacity the opacity to set
	 */
	public void setOpacity(double opacity) {
		setSubStyle(StyleBundle.OPACITY, getDouble(opacity));
	}

	/**
	 * @return the stroke width (default if not present or error)
	 */
	public Double getStrokeWidth() {
		Double strokeWidth = getDouble(getSubStyle(StyleBundle.STROKE_WIDTH));
		return (strokeWidth == null) ? null : strokeWidth.doubleValue();
	}

	/**
	 * @param strokeWidth the stroke width to set
	 */
	public void setStrokeWidth(Double strokeWidth) {
		setSubStyle(StyleBundle.STROKE_WIDTH, getDouble(strokeWidth));
	}
	
	public String getStrokeDashArray() {
		String dashes = (String) getSubStyle(StyleBundle.DASHARRAY);
		return (dashes == null ? null : dashes.toString());
	}

	public void setStrokeDashArray(String dashArray) {
		setSubStyle(StyleBundle.DASHARRAY, dashArray);
		addAttribute(new Attribute(StyleBundle.DASHARRAY, dashArray));
		LOG.trace("DASH "+dashArray);
	}

	/**
	 * @return the font size 
	 */
	public Double getFontSize() {
		return getDouble(getSubStyle(StyleBundle.FONT_SIZE));
	}

	/**
	 * @param fontSize the font size to set
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
			d = new Double(String.valueOf(subStyle));
		} catch (Exception e) {
			//return null
		}
		return d;
	}

	protected String getTag() {
		return "DUMMY";
	}
	
	/**
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
		SVGUtil.debug(svg, fos, 2);
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
		for (int i = transforms.size() - 1; i >= 0; i--) {
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
				this.addAttribute(new Attribute(attName, String.valueOf(value)));
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
			return (styleBundle == null ? null : styleBundle.getSubStyle(attName));
		} else {
			return getAttributeValue(attName);
		}
	}

	public void debug(String msg) {
		XMLUtil.debug(this, msg);
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
//		LOG.debug(String.valueOf(this.getClass().getName())+" saved "+savedAffineTransform+" CUM: "+transform2+" "+currentAffineTransform);
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
		String fill = this.getAttributeValue(FILL);
		if (fill != null && !NONE.equalsIgnoreCase(fill)) {
			Color fillColor = colorMap.get(fill);
			g2d.setColor(fillColor);
			g2d.fill(shape);
		}
		restoreColor(g2d);
	}

	protected void draw(Graphics2D g2d, Shape shape) {
		String stroke = this.getStroke();
		if (stroke != null && !NONE.equalsIgnoreCase(stroke)) {
			Color strokeColor = convertStroke(stroke);
			Double strokeWidth = this.getStrokeWidth();
			strokeWidth = (strokeWidth == null) ? 0.3 : strokeWidth;
			strokeWidth = SVGElement.transform(strokeWidth, cumulativeTransform);
			Stroke s = new BasicStroke((float) (double) strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
			g2d.setStroke(s);
			g2d.setColor(strokeColor);
			g2d.draw(shape);
			restoreColor(g2d);
			restoreStroke(g2d);
		}
	}
	
	private Color convertStroke(String stroke) {
		Color strokeColor = null;
		if (stroke == null) {
			colorMap.get(stroke);
		} else if (stroke.startsWith("#")) {
			String hex = stroke.substring(1);
			if (hex.length() == 6) {
				Integer red = Integer.parseInt(hex.substring(0, 2), 16);
				Integer green = Integer.parseInt(hex.substring(2, 4), 16);
				Integer blue = Integer.parseInt(hex.substring(4, 6), 16);
				strokeColor = new Color(red, green, blue);
			} else if (hex.length() == 3) {
				Integer red = Integer.parseInt(hex.substring(0, 1), 16);
				Integer green = Integer.parseInt(hex.substring(1, 2), 16);
				Integer blue = Integer.parseInt(hex.substring(2, 3), 16);
				strokeColor = new Color(red * 16, green * 16, blue * 16);
			} else {
				LOG.error("Cannot parse color: "+stroke);
			}

		} else {
			strokeColor = colorMap.get(stroke); 
			if (strokeColor == null) {
				LOG.error("Cannot parse color: "+stroke);
			}
		}
		if (strokeColor == null) strokeColor = Color.RED;
		return strokeColor;
	}

	protected void setAntialiasing(Graphics2D g2d, boolean on) {
		g2d.setRenderingHint(
		    RenderingHints.KEY_ANTIALIASING,
		    (on ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF)
	    );
	}

	protected void setTextAntialiasing(Graphics2D g2d, boolean on) {
		g2d.setRenderingHint(
		    RenderingHints.KEY_TEXT_ANTIALIASING,
		    (on ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)
	    );
	}

	protected void setGraphicsFill(Graphics2D g2d) {
		String fillS = this.getFill();
		if (fillS != null) {
			Color fillColor = GraphicsElement.getJava2DColor(fillS);
			g2d.setColor(fillColor);
		} else {
			g2d.setColor(null);
		}
	}

	protected void setGraphicsStroke(Graphics2D g2d) {
		String strokeS = this.getStroke();
		if (strokeS != null) {
			Color strokeColor = GraphicsElement.getJava2DColor(strokeS);
			g2d.setColor(strokeColor);
		} else {
			g2d.setColor(null);
		}
	}

	protected void drawFill(Graphics2D g2d, GeneralPath path) {
		setGraphicsStroke(g2d);
		draw(g2d, path);
		setGraphicsFill(g2d);
		fill(g2d, path);
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
	public static Color getJava2DColor(String colorS, Double opacity) {
		Color color = null;
		if (NONE.equals(colorS)) {
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
			if (opacity == null) {
				opacity = 1.0;
			}
			color = (Double.isNaN(opacity)) ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255.0 * opacity));
		} else {
			color = new Color(255, 255, 255, 0);
		}
		return color;
	}
	

}

