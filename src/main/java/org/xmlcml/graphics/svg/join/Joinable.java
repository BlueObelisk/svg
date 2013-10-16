package org.xmlcml.graphics.svg.join;

import org.xmlcml.euclid.Real2;
import org.xmlcml.graphics.svg.SVGElement;
import org.xmlcml.graphics.svg.SVGLine;


/**
 * Holds SVGElements such as SVGLine, TramLine or SVGText.
 * 
 * <p>A Joinable exposes the element, its id, a Joiner (which interacts
 * with Joiners on other elements), a backbone (the abstract line representing
 * the Joinable) and/or a point. Normally a Joinable has either a backbone (e.g. TramLine)
 * or a point (JoianbleText).</p>
 * 
 * <p>will be expanded to include other objects later</p>
 *
 * * 
 * @author pm286
 *
 */
public interface Joinable {

	/** get the delegated element.
	 * 
	 * @return
	 */
	SVGElement getSVGElement();

	/** get the id.
	 * 
	 * normally of the delegated element.
	 * 
	 * @return
	 */
	String getId();
	
	/** expose the Joiner.
	 * 
	 * Testing connectedness is done through the joiners for each joinable.
	 * 
	 * @return the joiner
	 */
	JoinPointList getJoinPointList();

	/** get the backbone.
	 * 
	 * the backbone is the abstract line representing the connectivity and function of the element. 
	 * For example it is midway between TramLine lines or runs through rungs of a ladder.
	 * It may not exist as an explicit primitive in the diagram.
	 * 
	 * @return
	 */
	SVGLine getBackbone();

	/** get intersection with another joinable.
	 * 
	 * @param joinable
	 * 
	 * @return intersection or null
	 */
	Real2 intersectionWith(Joinable joinable);

	/** some joinables such as single SVGText characters have a unique point.
	 * 
	 * @return the point or null for those which don't have points;
	 */
	Real2 getPoint();
	
	/** polymorphic intesections 
	 * 
	 * Must be implemented for each type of shape , shape
	 * @param shape
	 * @return
	 */
    JoinPoint getIntersectionPoint(Joinable shape);
    JoinPoint getIntersectionPoint(JoinableLine line);
    JoinPoint getIntersectionPoint(JoinableText text);
    JoinPoint getIntersectionPoint(TramLine tramLine);

	double getPriority();
}
