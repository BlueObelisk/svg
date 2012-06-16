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
import java.awt.Font;
import java.awt.Graphics2D;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

import org.apache.log4j.Logger;
import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.euclid.Util;

/** draws text.
 * 
 * NOTE: Text can be rotated and the additonal fields manage some of the
 * metrics for this. Still very experimental
 * 
 * @author pm286
 *
 */
public class SVGTSpan extends SVGText {
	private static Logger LOG = Logger.getLogger(SVGTSpan.class);
	public final static String TAG ="tspan";
	
	
	/** constructor
	 */
	public SVGTSpan() {
		super(TAG);
		init();
	}
	protected void init() {
		super.setDefaultStyle();
		setDefaultStyle(this);
	}
	
	
	/** constructor
	 */
	public SVGTSpan(SVGTSpan element) {
        super((SVGElement) element);
	}
	
	/** constructor
	 */
	public SVGTSpan(Element element) {
        super((SVGElement) element);
	}
	
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGTSpan(this);
    }


	/**
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}


}
