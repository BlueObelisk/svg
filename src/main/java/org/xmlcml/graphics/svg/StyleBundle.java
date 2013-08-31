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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Attribute;

import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLConstants;

public class StyleBundle implements CMLConstants {


	private static Logger LOG = Logger.getLogger(StyleBundle.class);

	public static final String CLIP_PATH = "clip-path";
	public static final String DASHARRAY = "stroke-dasharray";
	public static final String FILL = "fill";
	public static final String FONT_FAMILY = "font-family";
	public static final String FONT_SIZE = "font-size";
	public static final String FONT_STYLE = "font-style";
	public static final String FONT_WEIGHT = "font-weight";
	public static final String OPACITY = "opacity";
	public static final String STROKE = "stroke";
	public static final String STROKE_WIDTH = "stroke-width";
	// not used in bundle
	private static final String STROKE_LINECAP = "stroke-linecap";

    static List<String> BUNDLE_ATTRIBUTES;
	static {
		String[] bundleAttributes = {
				CLIP_PATH,
				FILL,
				FONT_FAMILY,
				FONT_SIZE,
				FONT_STYLE,
				FONT_WEIGHT,
				OPACITY,
				STROKE,
				STROKE_WIDTH,
		};
		BUNDLE_ATTRIBUTES = Arrays.asList(bundleAttributes);
	}

	public final static StyleBundle DEFAULT_STYLE_BUNDLE = new StyleBundle(
		null,	
		"#000000",
		"sans-serif",
		8.0,
		"normal",
		"normal",
		1.0,
		"#000000",
		0.5
	);
	
	private String clipPath;
	private String fill;
	private String fontFamily;
	private Double fontSize;
	@SuppressWarnings("unused")
	private String fontStyle;
	private String fontWeight;
	private Double opacity;
	private String stroke;
	private Double strokeWidth;
	private Map<String, String> atts = new HashMap<String, String>();

	static final String STYLE = "style";


	StyleBundle() {
	}
	
	public StyleBundle(String style) {
		processStyle(style);
	}
	
	public StyleBundle(
		String clipPath,
		String fill,
		String fontFamily,
		double fontSize,
		String fontStyle,
		String fontWeight,
		double opacity,
		String stroke,
		double strokeWidth
		) {
		if (clipPath != null && !clipPath.trim().equals(S_EMPTY)) {
			this.clipPath = clipPath.trim();
		}
		if (fill != null && !fill.trim().equals(S_EMPTY)) {
			this.fill = fill.trim();
		}
		if (fontFamily != null && !fontFamily.trim().equals(S_EMPTY)) {
			this.fontFamily = fontFamily.trim();
		}
		if (fontSize > 0) {
			this.fontSize = new Double(fontSize);
		}
		if (fontStyle != null && !fontStyle.trim().equals(S_EMPTY)) {
			this.fontStyle = fontStyle.trim();
		}
		if (fontWeight != null && !fontWeight.trim().equals(S_EMPTY)) {
			this.fontWeight = fontWeight.trim();
		}
		if (opacity > 0) {
			this.opacity = new Double(opacity);
		}
		if (stroke != null && !stroke.trim().equals(S_EMPTY)) {
			this.stroke = stroke.trim();
		}
		if (strokeWidth > 0) {
			this.strokeWidth = new Double(strokeWidth);
		}
	}
	public StyleBundle(StyleBundle style) {
		this.copy(style);
	}
	
	public void copy(StyleBundle style) {
		if (style != null) {
			this.clipPath = style.clipPath;
			this.fill = style.fill;
			this.fontFamily = style.fontFamily;
			this.fontSize = style.fontSize;
			this.fontStyle = style.fontStyle;
			this.fontWeight = style.fontWeight;
			this.opacity = style.opacity;
			this.stroke = style.stroke;
			this.strokeWidth = style.strokeWidth;
			this.atts = new HashMap<String, String>();
			for (String name : style.atts.keySet()) {
				atts.put(name, atts.get(name));
			}
		}
	}
	
	void processStyle(String style) {
		if (style != null) {
			style = style.trim();
			if (!style.equals(S_EMPTY)) {
				String[] ss = style.split(S_SEMICOLON);
				for (String s : ss) {
					s = s.trim();
					if (s.equals(S_EMPTY)) {
						continue;
					}
					String[] aa = s.split(S_COLON);
					String attName = aa[0].trim();
					String attVal = aa[1].trim();
					if (attName.equals(CLIP_PATH)) {
						clipPath = attVal;
					} if (attName.equals(FILL)) {
						fill = attVal;
					} else if (attName.equals(FONT_FAMILY)) {
						fontFamily = attVal; 
					} else if (attName.equals(FONT_SIZE)) {
						fontSize = getDouble(attVal); 
					} else if (attName.equals(FONT_STYLE)) {
						fontStyle = attVal; 
					} else if (attName.equals(FONT_WEIGHT)) {
						fontWeight = attVal; 
					} else if (attName.equals(OPACITY)) {
						opacity = getDouble(attVal); 
					} else if (attName.equals(STROKE)) {
						stroke = attVal;
					} else if (attName.equals(STROKE_WIDTH)) {
						strokeWidth = getDouble(attVal); 
					} else {
						atts.put(attName, attVal);
					}
				}
			}
		} else {
//			copy(DEFAULT_STYLE_BUNDLE);
 		}
	}
	
	/** attVal may be null 
	 * 
	 * @param attName
	 * @param attVal
	 */
	public void setSubStyle(String attName, Object attVal) {
		if (attName == null) {
			throw new RuntimeException("null style");
		} else if (attName.equals(CLIP_PATH)) {
			clipPath = (String) attVal;
		} else if (attName.equals(FILL)) {
			fill = (String) attVal;
		} else if (attName.equals(FONT_FAMILY)) {
			fontFamily = (String) attVal; 
		} else if (attName.equals(FONT_SIZE)) {
			fontSize = getDouble(String.valueOf(attVal)); 
		} else if (attName.equals(FONT_STYLE)) {
			fontStyle = (String) attVal; 
		} else if (attName.equals(FONT_WEIGHT)) {
			fontWeight = (String) attVal; 
		} else if (attName.equals(OPACITY)) {
			opacity = getDouble(String.valueOf(attVal)); 
		} else if (attName.equals(STROKE)) {
			stroke = (String) attVal;
		} else if (attName.equals(STROKE_WIDTH)) {
			strokeWidth = getDouble(String.valueOf(attVal)); 
		} else {
			atts.put(attName, String.valueOf(attVal));
		}

	}
	
	public Object getSubStyle(String attName) {
		Object subStyle = null;
		if (attName.equals(CLIP_PATH)) {
			subStyle = getClipPath();
		} else if (attName.equals(FILL)) {
			subStyle = getFill();
		} else if (attName.equals(FONT_FAMILY)) {
			subStyle = getFontFamily();
		} else if (attName.equals(FONT_SIZE)) {
			subStyle = getFontSize();
		} else if (attName.equals(FONT_WEIGHT)) {
			subStyle = getFontWeight();
		} else if (attName.equals(FONT_STYLE)) {
			subStyle = getFontStyle();
		} else if (attName.equals(OPACITY)) {
			subStyle = getOpacity();
		} else if (attName.equals(STROKE_LINECAP)) {
			LOG.debug("ignored style: "+attName);
		} else if (attName.equals(STROKE)) {
			subStyle = getStroke();
		} else if (attName.equals(STROKE_WIDTH)) {
			subStyle = getStrokeWidth();
		} else {
			subStyle = atts.get(attName);
		}
		return subStyle;
	}
	
	void convertAndRemoveExplicitAttributes(GraphicsElement element) {
		for (String attName : StyleBundle.BUNDLE_ATTRIBUTES) {
			Attribute att = element.getAttribute(attName);
			if (att != null) {
				this.setSubStyle(attName, att.getValue());
				att.detach();
			}
		}
		for (String attName : atts.keySet()) {
			this.setSubStyle(attName, atts.get(attName));
		}
		String cssString = this.toString();
		if (cssString != null && cssString.trim().length() > 0) {
			element.addAttribute(new Attribute(STYLE, cssString));
		}
 	}

	void removeStyleAttributesAndMakeExplicit(GraphicsElement element) {
		for (String attName : StyleBundle.BUNDLE_ATTRIBUTES) {
			Object attVal = this.getSubStyle(attName);
			if (attVal != null) {
				element.addAttribute(new Attribute(attName, String.valueOf(attVal)));
				this.removeStyle(attName);
			}
		}
		String cssString = this.toString();
		Attribute styleAttribute = element.getAttribute(STYLE);
		// remove or modify old CSS style
		if (cssString == null || cssString.trim().length() == 0) {
			if (styleAttribute != null) {
				styleAttribute.detach();
			}
		} else {
			// make sure anything left is still kep
			element.addAttribute(new Attribute(STYLE, cssString));
		}
	}

	public void removeStyle(String attName) {
		setSubStyle(attName, null);
	}

	private Double getDouble(String s) {
		Double d = null;
		if (s != null && !"null".equals(s)) {
			try {
				d = new Double(s).doubleValue();
			} catch (NumberFormatException e) {
				throw new RuntimeException("bad double in style: "+s);
			}
		}
		return d;
	}
	
	public String getClipPath() {
		return clipPath;
	}
	
	public void setClipPath(String clipPath) {
		this.clipPath = clipPath;
	}

	public String getFill() {
		return fill;
	}

	public void setFill(String fill) {
		this.fill = fill;
	}

	public String getStroke() {
		return stroke;
	}

	public void setStroke(String stroke) {
		this.stroke = stroke;
	}

	public Double getStrokeWidth() {
		return strokeWidth;
	}

	public void setStrokeWidth(Double strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public Double getFontSize() {
		return fontSize;
	}

	public void setFontSize(double fontSize) {
		this.fontSize = fontSize;
	}

	public String getFontStyle() {
		return fontStyle;
	}

	public void setFontStyle(String fontStyle) {
		this.fontStyle = fontStyle;
	}

	public String getFontWeight() {
		return fontWeight;
	}

	public void setFontWeight(String fontWeight) {
		this.fontWeight = fontWeight;
	}

	public Double getOpacity() {
		return opacity;
	}

	public void setOpacity(double opacity) {
		this.opacity = opacity;
	}
	
	public String toString() {
		String s = "";
		s = addString(s, clipPath, CLIP_PATH);
		s = addString(s, fill, FILL);
		s = addString(s, stroke, STROKE);
		s = addDouble(s, strokeWidth, STROKE_WIDTH);
		s = addString(s, fontFamily, FONT_FAMILY);
		s = addDouble(s, fontSize, FONT_SIZE);
		s = addString(s, fontStyle, FONT_STYLE);
		s = addString(s, fontWeight, FONT_WEIGHT);
		s = addDouble(s, opacity, "opacity");
		for (String attName : atts.keySet()) {
			s = addString(s, atts.get(attName), attName);
		}
		return s;
	}

	private String addDouble(String s, Double value, String name) {
		if (value != null && !Double.isNaN(value)) {
			s += " "+name+" : "+value+S_SEMICOLON;
		}
		return s;
	}
	private String addString(String s, String value, String name) {
		if (value != null && !value.trim().equals(S_EMPTY)) {
			s += " "+name+" : "+value+S_SEMICOLON;
		}
		return s;
	}

}
