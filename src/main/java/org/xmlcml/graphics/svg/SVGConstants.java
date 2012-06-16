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

import nu.xom.XPathContext;

import org.xmlcml.cml.base.CMLConstants;

public interface SVGConstants extends CMLConstants {

	/** standard namespace for SVG
	 * 
	 */
	public static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
	public static final String XLINK_NS = "http://www.w3.org/1999/xlink";

    /** XPathContext for CML.
     */
    XPathContext SVG_XPATH = new XPathContext("svg", SVG_NAMESPACE);
    
}
