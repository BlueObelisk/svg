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

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;

import org.xmlcml.cml.base.CMLConstants;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.Transform2;

/** supports defs
 * 
 * @author pm286
 *
 */
public class SVGScript extends SVGElement {

	public final static String TAG ="script";
	/** constructor
	 */
	public SVGScript() {
		super(TAG);
	}
	
	/** constructor
	 */
	public SVGScript(SVGElement element) {
        super((SVGElement) element);
	}
	
	/** constructor
	 */
	public SVGScript(Element element) {
        super((SVGElement) element);
	}
	
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGScript(this);
    }

	

	/**
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	public Real2Range getBoundingBox() {
		return null;
	}

	public void applyTransform(Transform2 t2) {
		// no-op
	}
	
	public void setHRef(String href, String value) {
		this.addAttribute(new Attribute(CMLConstants.XLINK_PREFIX+CMLConstants.S_COLON+href, CMLConstants.XLINK_NS, value));
	}
	
}
