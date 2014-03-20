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

import nu.xom.Element;
import nu.xom.Node;

/** supports defs
 * 
 * @author pm286
 *
 */
public class SVGDefs extends SVGElement {

	public final static String TAG ="defs";
	/** constructor
	 */
	public SVGDefs() {
		super(TAG);
		init();
	}
	
	/** constructor
	 */
	public SVGDefs(SVGElement element) {
        super(element);
	}
	
	/** constructor
	 */
	public SVGDefs(Element element) {
        super((SVGElement) element);
	}
	
    /**
     * copy node .
     *
     * @return Node
     */
    public Node copy() {
        return new SVGDefs(this);
    }

	

	/**
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	
}
