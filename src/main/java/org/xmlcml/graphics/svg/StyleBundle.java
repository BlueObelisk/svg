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

import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLConstants;

public class StyleBundle implements CMLConstants {
	private static Logger LOG = Logger.getLogger(StyleBundle.class);
	
	public final static StyleBundle DEFAULT_STYLE_BUNDLE = new StyleBundle(
		null,	
		"#000000",
		"#000000",
		0.5,
		"sans-serif",
		8.0,
		"normal",
		1.0
	);
	private String clipPath;
	private String fill;
	private String stroke;
	private String fontFamily;
	private Double fontSize;
	@SuppressWarnings("unused")
	private String fontStyle;
	private String fontWeight;
	private Double strokeWidth;
	private Double opacity;

	StyleBundle() {
	}
	
	public StyleBundle(String style) {
		processStyle(style);
	}
	
	public StyleBundle(
		String clipPath,
		String fill,
		String Stroke,
		double strokeWidth,
		String fontFamily,
		double fontSize,
		String fontWeight,
		double opacity
		) {
		if (clipPath != null && !clipPath.trim().equals(S_EMPTY)) {
			this.clipPath = clipPath.trim();
		}
		if (fill != null && !fill.trim().equals(S_EMPTY)) {
			this.fill = fill.trim();
		}
		if (stroke != null && !stroke.trim().equals(S_EMPTY)) {
			this.stroke = stroke.trim();
		}
		if (strokeWidth > 0) {
			this.strokeWidth = new Double(strokeWidth);
		}
		if (fontFamily != null && !fontFamily.trim().equals(S_EMPTY)) {
			this.fontFamily = fontFamily.trim();
		}
		if (fontSize > 0) {
			this.fontSize = new Double(fontSize);
		}
		if (fontWeight != null && !fontWeight.trim().equals(S_EMPTY)) {
			this.fontWeight = fontWeight.trim();
		}
		if (opacity > 0) {
			this.opacity = new Double(opacity);
		}
	}
	
	public StyleBundle(StyleBundle style) {
		this.copy(style);
	}
	public void copy(StyleBundle style) {
		if (style != null) {
			this.clipPath = style.clipPath;
			this.fill = style.fill;
			this.stroke = style.stroke;
			this.strokeWidth = style.strokeWidth;
			this.fontFamily = style.fontFamily;
			this.fontSize = style.fontSize;
			this.fontWeight = style.fontWeight;
			this.opacity = style.opacity;
		}
	}
	
	private void processStyle(String style) {
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
					aa[0] = aa[0].trim();
					aa[1] = aa[1].trim();
					if (aa[0].equals("fill")) {
						fill = aa[1];
					} else if (aa[0].equals("stroke")) {
						stroke = aa[1];
					} else if (aa[0].equals("stroke-width")) {
						strokeWidth = getDouble(aa[1]); 
					} else if (aa[0].equals("font-family")) {
						fontFamily = aa[1]; 
					} else if (aa[0].equals("font-size")) {
						fontSize = getDouble(aa[1]); 
					} else if (aa[0].equals("font-weight")) {
						fontWeight = aa[1]; 
					} else if (aa[0].equals("opacity")) {
						opacity = getDouble(aa[1]); 
					} else if (aa[0].equals("stroke-linecap")) {
						LOG.trace("Ignored style: "+aa[0]);
					} else {
						LOG.trace("unsupported style: "+aa[0]);
					}
				}
			}
		} else {
//			copy(DEFAULT_STYLE_BUNDLE);
 		}
	}
	
	public void setSubStyle(String subStyle, Object object) {
		if (subStyle == null) {
			throw new RuntimeException("null style");
		} else if (subStyle.equals("clip-path")) {
			clipPath = (String) object;
		} else if (subStyle.equals("fill")) {
			fill = (String) object;
		} else if (subStyle.equals("stroke")) {
			stroke = (String) object;
		} else if (subStyle.equals("stroke-width")) {
			strokeWidth = (Double) object; 
		} else if (subStyle.equals("font-family")) {
			fontFamily = (String) object; 
		} else if (subStyle.equals("font-size")) {
			fontSize = (Double) object; 
		} else if (subStyle.equals("font-style")) {
			fontStyle = (String) object; 
		} else if (subStyle.equals("font-weight")) {
			fontWeight = (String) object; 
		} else if (subStyle.equals("opacity")) {
			opacity = (Double) object; 
		} else if (subStyle.equals("stroke-linecap")) {
		} else {
			LOG.trace("unsupported style: "+subStyle);
		}

	}
	
	public Object getSubStyle(String ss) {
		Object subStyle = null;
		if (ss.equals("fill")) {
			subStyle = getFill();
		} else if (ss.equals("stroke")) {
			subStyle = getStroke();
		} else if (ss.equals("stroke-width")) {
			subStyle = getStrokeWidth();
		} else if (ss.equals("font-family")) {
			subStyle = getFontFamily();
		} else if (ss.equals("font-size")) {
			subStyle = getFontSize();
		} else if (ss.equals("font-weight")) {
			subStyle = getFontWeight();
		} else if (ss.equals("opacity")) {
			subStyle = getOpacity();
		} else if (ss.equals("clip-path")) {
			subStyle = getClipPath();
		} else if (ss.equals("stroke-linecap")) {
			LOG.debug("ignored style: "+ss);
		} else {
			LOG.trace("unknown subStyle: "+ss);
		}
		return subStyle;
	}

	private double getDouble(String s) {
		double d = Double.NaN;
		try {
			d = new Double(s).doubleValue();
		} catch (NumberFormatException e) {
			throw new RuntimeException("bad double in style: "+s);
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
		s = addString(s, clipPath, "clip-path");
		s = addString(s, fill, "fill");
		s = addString(s, stroke, "stroke");
		s = addDouble(s, strokeWidth, "stroke-width");
		s = addString(s, fontFamily, "font-family");
		s = addDouble(s, fontSize, "font-size");
		s = addString(s, fontWeight, "font-weight");
		s = addDouble(s, opacity, "opacity");
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
