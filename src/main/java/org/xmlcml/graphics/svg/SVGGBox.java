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

import nu.xom.Elements;
import nu.xom.Nodes;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.euclid.Vector2;

public class SVGGBox extends SVGG {
	private static Logger LOG = Logger.getLogger(SVGGBox.class);
	
	protected SVGLayout layout;
	private String id;
	
	public SVGGBox() {
		
	}
	public static SVGGBox copy(SVGGBox box) {
		SVGGBox newBox = new SVGGBox();
	    newBox.copyAttributesFrom(box);
	    createSubclassedChildren(box, newBox);
		return newBox;
	}
	
	public static SVGGBox createSVGGBox(SVGSVG svgSvg) {
		SVGGBox box = null;
		Elements childSVGs = svgSvg.getChildElements();
		if (childSVGs.size() == 1) {
			box = createSVGGBox((SVGG)childSVGs.get(0));
		} else {
			box = new SVGGBox();
			SVGGBox lastBox = null;
			for (int i = 0; i < childSVGs.size(); i++) {
				SVGGBox childBox = createSVGGBox((SVGG)childSVGs.get(i));
				if (lastBox != null) {
//					lastBox.debug("RRXXX");
					SVGRect rect = lastBox.getRect();
					LOG.trace("RECT "+rect);
					Transform2 t2 = lastBox.getTransform2FromAttribute();
					LOG.trace(">>>"+t2);
					childBox.debug("SVGRRRRRRRRRR");
				}
				box.appendChild(childBox);
				lastBox = childBox;
			}
		}
		return box;
	}
	
	public static SVGGBox createSVGGBox(SVGG svgg) {
		SVGGBox newBox = new SVGGBox();
	    newBox.copyAttributesFrom(svgg);
	    createSubclassedChildren(svgg, newBox);
		return newBox;
	}
	
//	public static SVGGBox createSVGGBox(Elements svgElements) {
//		SVGGBox newBox = new SVGGBox();
//		for (int i = 0; i < svgElements.size(); i++) {
//			Element svgChild = svgElements.get(i);
//			SVGGBox box = createSVGGBox((SVGG)svgChild);
//		}
//		return newBox;
//	}
	public void setLayout(SVGLayout layout) {
		this.layout = layout;
	}
	public SVGLayout getLayout() {
		return layout;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	private SVGRect getRect() {
		Nodes rects = this.query("./*[local-name()='"+SVGRect.TAG+"']");
		return (rects.size() == 1) ? (SVGRect) rects.get(0) : null;
	}
	protected Real2 getOffset(SVGLayout layout) {
		SVGRect rect = this.getRect();
		Real2 offset = new Real2();
		if (rect != null) {
			if (SVGLayout.LEFT2RIGHT.equals(layout)) {
				offset.x = rect.getWidth();
			} else {
				offset.y = rect.getHeight();
			}
		}
		return offset;
	}
	
	public void addSVGG(SVGGBox childSvg) {
		Transform2 childTransform = childSvg.getTransform2FromAttribute();
		if (childTransform == null) {
			childTransform = new Transform2();
		}
		Real2 totalDelta = new Real2();
		List<SVGGBox> previousChildSvgs = this.getSVGGBoxChildren();
		if (previousChildSvgs.size() > 0) {
			for (SVGGBox previousChildSvg : previousChildSvgs) {
				Real2 delta = previousChildSvg.getOffset(this.layout);
				totalDelta.plusEquals(delta);
			}
			childTransform = childTransform.concatenate(new Transform2(new Vector2(totalDelta)));
		}
		SVGGBox g = SVGGBox.copy(childSvg);
		g.setTransform(childTransform);
		this.appendChild(g);
	}
	
	public List<SVGGBox> getSVGGBoxChildren() {
		// "./g"
		Nodes gNodes = this.query("./*[local-name()='"+SVGG.TAG+"']");
		List<SVGGBox> gList = new ArrayList<SVGGBox>(gNodes.size());
		for (int i = 0; i < gNodes.size(); i++) {
			gList.add(SVGGBox.createSVGGBox((SVGG)gNodes.get(i)));
		}
		return gList;
	}
	
	public void detachRect() {
		SVGRect rect = getRect();
		if (rect != null) {
			rect.detach();
		} else {
			this.debug("DDDDDDDDDDDDDDDDDD");
		}
	}
}
