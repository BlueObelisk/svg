package org.xmlcml.graphics.svg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xmlcml.euclid.Angle;
import org.xmlcml.euclid.EuclidConstants;
import org.xmlcml.euclid.Real;
import org.xmlcml.euclid.Real2;
import org.xmlcml.euclid.Real2Range;
import org.xmlcml.euclid.RealSquareMatrix;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.euclid.Util;
import org.xmlcml.euclid.Vector2;
import org.xmlcml.graphics.svg.fonts.FontWidths;
import org.xmlcml.xml.XMLConstants;
import org.xmlcml.xml.XMLUtil;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Text;

/** 
 * Draws text.
 * 
 * NOTE: text can be rotated and the additional fields manage some of the
 * metrics for this. Still very experimental.
 * 
 * @author pm286
 */
public class SVGText extends SVGElement {

	private static final String X = "x";

	private static Logger LOG = Logger.getLogger(SVGText.class);

	// just in case there is a scaling problem
	private static final double _SVG2AWT_FONT_SCALE = 1.0;
	
	public final static String TAG ="text";
	
    public static String SUB0 = XMLConstants.S_UNDER+XMLConstants.S_LCURLY;
    public static String SUP0 = XMLConstants.S_CARET+XMLConstants.S_LCURLY;
    public static String SUB1 = XMLConstants.S_RCURLY+XMLConstants.S_UNDER;
    public static String SUP1 = XMLConstants.S_RCURLY+XMLConstants.S_CARET;
    
    public final static Double DEFAULT_FONT_WIDTH_FACTOR = 10.0;
    public final static Double MIN_WIDTH = 0.001; // useful for non printing characters
	private final static Double SCALE1000 = 0.001; // width multiplied by 1000

	public final static String ALL_TEXT_XPATH = ".//svg:text";

	private static final String BOLD = "bold";
	private static final String ITALIC = "italic";

	public static final String FONT_NAME = "fontName";
	public static final String WIDTH = "width";

	/** 
	 * Rough average of width of "n" 
	 */
	private static final Double N_SPACE = 0.55;
	public static final Double SPACE_FACTOR = 0.2; //extra space that denotes a space
	private Double SPACE_WIDTH1000 = /*274.0*/ 200.;
	public final static Double DEFAULT_SPACE_FACTOR = 0.05;
	private static final double MIN_FONT_SIZE = 0.01;
	private static final Double DEFAULT_CHARACTER_WIDTH = 500.0;

	// these are all when text is used for concatenation, etc.
	private double estimatedHorizontallength = Double.NaN; 
	private double currentFontSize = Double.NaN;
	private double currentBaseY = Double.NaN;
	private String rotate = null;
	private double calculatedTextEndCoordinate = Double.NaN;
	private List<SVGTSpan> tspans;

	private FontWeight fontWeight;
	private Double widthOfFirstCharacter;
	private Double heightOfFirstCharacter;
	
	/** 
	 * Constructor
	 */
	public SVGText() {
		super(TAG);
		init();
	}
	
	/** 
	 * Constructor
	 * 
	 * @param xy
	 * @param text
	 */
	public SVGText(Real2 xy, String text) {
		this();
		setXYAndText(xy, text);
	}

	private void setXYAndText(Real2 xy, String text) {
		if (new Real2(0.0, 0.0).isEqualTo(xy, 0.000001)) {
			throw new RuntimeException("Text of ),0 is suspicious");
		}
		setXY(xy);
		setText(text);
	}

	/** 
	 * Constructor.
	 * 
	 * @param xy
	 * @param text
	 */
	protected SVGText(Real2 xy, String text, String tag) {
		this(tag);
		setXYAndText(xy, text);
	}

	protected void init() {
		super.setDefaultStyle();
		setDefaultStyle(this);
	}
	
	private void clearRotate() {
		estimatedHorizontallength = Double.NaN; 
		currentBaseY = Double.NaN;
		calculatedTextEndCoordinate = Double.NaN;
		setBoundingBoxCached(false);
	}

	public static void setDefaultStyle(SVGElement text) {
		text.setStroke("none");
		text.setFontSize(1.0);
	}
	
	/** constructor
	 */
	public SVGText(SVGElement element) {
        super(element);
	}
	
	/** constructor
	 */
	protected SVGText(SVGElement element, String tag) {
        super(element, tag);
	}
	
	/** constructor
	 */
	public SVGText(Element element) {
        super((SVGElement) element);
	}
	
	/** constructor
	 */
	protected SVGText(Element element, String tag) {
        super((SVGElement) element, tag);
	}
	
	protected SVGText(String tag) {
		super(tag);
	}
    /**
     * copy node .
     *
     * @return Element
     */
    public Element copy() {
        return new SVGText(this, TAG);
    }
    
	protected void drawElement(Graphics2D g2d) {
		saveGraphicsSettingsAndApplyTransform(g2d);
		String text = this.getText();
		if (text != null) {
			String fill = this.getFill();
			Color fillColor = getJava2DColor(fill); 
			float fontSize = (float) (double) this.getFontSize();
			fontSize *= cumulativeTransform.getMatrixAsArray()[0] * _SVG2AWT_FONT_SCALE;
			Font font = g2d.getFont();
			font = font.deriveFont(fontSize);
			if (isItalic()) {
				font = font.deriveFont(Font.ITALIC);
			}
			setTextAntialiasing(g2d, true);
			Real2 xy = this.getXY();
			xy = transform(xy, cumulativeTransform);
			LOG.trace("CUM "+cumulativeTransform+"XY "+xy);
			saveColor(g2d);
			if (fillColor != null) {
				g2d.setColor(fillColor);
			}
			g2d.setFont(font);
			g2d.drawString(text, (int)xy.x, (int)xy.y);
			restoreColor(g2d);
		}
		restoreGraphicsSettingsAndTransform(g2d);
	}

	public void applyTransform(Transform2 t2) {
		// transform the position and scale
		Real2 xy = getXY();
		xy.transformBy(t2);
		this.setXY(xy);
		transformFontSize(t2);
		Angle angle = t2.getAngleOfRotation();
		//rotate characters to preserve relative orientation
		if (angle != null && !angle.isEqualTo(0.0, EPS)) {
			angle = angle.multiplyBy(-1.0);
			Transform2 t = Transform2.getRotationAboutPoint(angle, xy);
			t = t.concatenate(t2);
			this.setTransform(t);
		}
	}

	/** result is always positive
	 * 
	 * @param t2
	 */
	public void transformFontSize(Transform2 t2) {
		Double fontSize = this.getFontSize();
		// transform fontSize
		if (fontSize != null) {
			Real2 ff = new Real2(fontSize, 1.0);
			Transform2 rotMat = new Transform2(t2);
			rotMat.setTranslation(new Real2(0.0,0.0));
			ff.transformBy(rotMat);
			double size = Math.max(ff.getX(), ff.getY()); // takes account of rotation
			LOG.trace("FS "+ff+" .. "+size);
			this.setFontSize(size);
		}
	}

    /** round to decimal places.
     * 
     * @param places
     * @return this
     */
    public void format(int places) {
    	super.format(places);
    	setXY(getXY().format(places));
    	Double fontSize = this.getFontSize();
    	if (fontSize != null) {
    		fontSize = Util.format(fontSize, places);
    		this.setFontSize(fontSize);
    	}
    }

    /** round to decimal places.
     * 
     * @param places
     * @return this
     */
    public void formatTransform(int places) {
    	super.formatTransform(places);
    	setXY(getXY().format(places));
    	Double fontSize = this.getFontSize();
    	if (fontSize != null) {
    		fontSize = Util.format(fontSize, places);
    		this.setFontSize(fontSize);
    	}
    }


	/**
	 * @return tag
	 */
	public String getTag() {
		return TAG;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		Nodes nodes = query("./text()");
		return (nodes.size() == 1 ? nodes.get(0).getValue() : null);
	}

	/**
	 * Clears text and replaces if not null
	 * 
	 * @param text the text to set
	 */
	public void setText(String text) {
		if (this.getChildCount() > 0) {
			Node node = this.getChild(0);
			if (node instanceof Text) {
				node.detach();
			} else if (node instanceof SVGTSpan) {
				// expected child
			} else if (node instanceof SVGTitle) {
				// expected child
			} else {
				LOG.debug("unexpected child of SVGText: "+node.getClass());
			}
		}
		if (text != null) {
			try {
				this.appendChild(text);
			} catch (nu.xom.IllegalCharacterDataException e) {
				//e.printStackTrace();
				throw new RuntimeException("Cannot append text: "+text+" (char-"+(int)text.charAt(0)+")", e);
			}
		}
		boundingBox = null;
		calculatedTextEndCoordinate = Double.NaN;
		estimatedHorizontallength = Double.NaN; 
	}

	/** extent of text
	 * defined as the point in the middle of the visual string (
	 * e.g. near the middle of the crossbar in "H")
	 * @return
	 */
	public Real2Range getBoundingBoxForCenterOrigin() {
		
		//double fontWidthFactor = DEFAULT_FONT_WIDTH_FACTOR;
		//double fontWidthFactor = 1.0;
		// seems to work??
		double fontWidthFactor = 0.3;
		double halfWidth = getEstimatedHorizontalLength(fontWidthFactor) / 2.0;
		
		double height = this.getFontSize();
		Real2Range boundingBox = new Real2Range();
		Real2 center = getXY();
		boundingBox.add(center.plus(new Real2(halfWidth, 0.0)));
		boundingBox.add(center.plus(new Real2(-halfWidth, height)));
		return boundingBox;
	}

	/** 
	 * Extent of text.
	 * Defined as the point origin (i.e. does not include font).
	 * 
	 * @return
	 */
	public Real2Range getBoundingBox() {
		if (boundingBoxNeedsUpdating()) {
			getChildTSpans();
			Double width = null;
			Double height = null;
			if (tspans.size() > 0) {
				boundingBox = tspans.get(0).getBoundingBox();
				for (int i = 1; i < tspans.size(); i++) {
					Real2Range r2ra = tspans.get(i).getBoundingBox();
					boundingBox = boundingBox.plus(r2ra);
				}
			} else {
				double fontWidthFactor = 1.0;
				width = getEstimatedHorizontalLength(fontWidthFactor);
				if (width == null || Double.isNaN(width)) {
					width = MIN_WIDTH;
					String text = getText();
					if (text == null) {
						setText("");
					} else if (text.length() == 0) {
						throw new RuntimeException("found empty text ");
					} else if (text.contains("\n")) {
						throw new RuntimeException("found LF "+String.valueOf(((int) text.charAt(0))));
					} else {
						throw new RuntimeException("found strange Null text "+String.valueOf(((int) text.charAt(0))));
					}
				}
				height = getFontSize() * fontWidthFactor;
				Real2 xy = getXY();
				boundingBox = new Real2Range(xy, xy.plus(new Real2(width, -height)));
			}	
			if (!boundingBox.isValid()) {
				throw new RuntimeException("Invalid bbox: "+width+"/"+height);
			}
			rotateBoundingBoxForRotatedText();
				
		}
		return boundingBox;
	}
	
	private void rotateBoundingBoxForRotatedText() {
		Transform2 t2 = getTransform();
		if (t2 != null) {
			Angle rotation = t2.getAngleOfRotation();
			if (rotation == null) {
				LOG.trace("Null angle: "+t2);
			}
			// significant rotation?
			if (rotation != null && !rotation.isEqualTo(0., 0.001)) {
				Real2[] corners = boundingBox.getCorners();
				corners[0].transformBy(t2);
				corners[1].transformBy(t2);
				boundingBox = new Real2Range(corners[0], corners[1]);
			}
		}
	}

	/** 
	 * This is a hack and depends on what information is available.
	 * 
	 * Include fontSize and factor.
	 * 
	 * @param fontWidthFactor
	 * @return
	 */
	public Double getEstimatedHorizontalLength(double fontWidthFactor) {
		estimatedHorizontallength = Double.NaN;
		if (getChildTSpans().size() == 0) {
			String s = getText();
			if (s != null) {
				String family = getFontFamily();
				double[] lengths = FontWidths.getFontWidths(family);
				if (lengths == null) {
					lengths = FontWidths.SANS_SERIF;
				}
				double fontSize = getFontSize();
				estimatedHorizontallength = 0.0;
				for (int i = 0; i < s.length(); i++) {
					char c = s.charAt(i);
					if (c > 255) {
						c = 's';  // as good as any
					}
					double length = fontSize * fontWidthFactor * lengths[(int) c];
					estimatedHorizontallength += length;
				}
			}
		}
		return estimatedHorizontallength;
	}
	
	public Real2 getCalculatedTextEnd(double fontWidthFactor) {
		getRotate();
		Real2 xyEnd = null;
		getEstimatedHorizontalLength(fontWidthFactor);
		if (!Double.isNaN(estimatedHorizontallength)) {
			if (rotate == null) {
				xyEnd = this.getXY().plus(new Real2(estimatedHorizontallength, 0.0));
			} else if (rotate.equals(SVGElement.YPLUS)) {
				xyEnd = this.getXY().plus(new Real2(0.0, -estimatedHorizontallength));
			} else if (rotate.equals(SVGElement.YMINUS)) {
				xyEnd = this.getXY().plus(new Real2(0.0, estimatedHorizontallength));
			}
		}
		return xyEnd;
	}
	
	public double getCalculatedTextEndCoordinate(double fontWidthFactor) {
		if (Double.isNaN(calculatedTextEndCoordinate)) {
			getRotate();
			Real2 xyEnd = getCalculatedTextEnd(fontWidthFactor);
			if (xyEnd != null) {
				if (rotate == null) {
					calculatedTextEndCoordinate = xyEnd.getX();
				} else if (rotate.equals(YMINUS)){
					calculatedTextEndCoordinate = xyEnd.getY();
				} else if (rotate.equals(YPLUS)){
					calculatedTextEndCoordinate = xyEnd.getY();
				} else {
					calculatedTextEndCoordinate = xyEnd.getY();
				}
			}
		}
		return calculatedTextEndCoordinate;
	}
	
	public void setCalculatedTextEndCoordinate(double coord) {
		this.calculatedTextEndCoordinate = coord;
	}
	
	public double getCurrentFontSize() {
		if (Double.isNaN(currentFontSize)) {
			currentFontSize = this.getFontSize();
		}
		return currentFontSize;
	}
	public void setCurrentFontSize(double currentFontSize) {
		this.currentFontSize = currentFontSize;
	}
	
	public double getCurrentBaseY() {
		getRotate();
		if (Double.isNaN(currentBaseY)) {
			currentBaseY = (rotate == null) ? this.getY() : this.getX();
		}
		return currentBaseY;
	}
	public void setCurrentBaseY(double currentBaseY) {
		this.currentBaseY = currentBaseY;
	}
	
	public String getRotate() {
		if (rotate == null) {
			rotate = getAttributeValue(SVGElement.ROTATE);
		}
		return rotate;
	}
	
	public void setRotate(String rotate) {
		this.rotate = rotate;
		clearRotate();
	}
	
	/**
	 * tries to concatenate text1 onto this. If success (true) alters this,
	 * else leaves this unaltered
	 * 
	 * @param fontWidthFactor
	 * @param fontHeightFactor
	 * @param text1 left text
	 * @param subVert fraction of large font size to determine subscript
	 * @param supVert fraction of large font size to determine superscript
	 * @return null if concatenated
	 */
	public boolean concatenateText(double fontWidthFactor, double fontHeightFactor, 
			SVGText text1, double subVert, double supVert, double eps) {

		String rotate0 = this.getAttributeValue(SVGElement.ROTATE);
		String rotate1 = text1.getAttributeValue(SVGElement.ROTATE);
		// only compare text in same orientation
		boolean rotated = false;
		if (rotate0 == null) {
			rotated = (rotate1 != null);
		} else {
			rotated = (rotate1 == null || !rotate0.equals(rotate1));
		}
		if (rotated) {
			LOG.debug("text orientation changed");
			return false;
		}
		String newText = null;
		String string0 = this.getText();
		double fontSize0 = this.getCurrentFontSize();
		Real2 xy0 = this.getXY();
		String string1 = text1.getText();
		double fontSize1 = text1.getFontSize();
		Real2 xy1 = text1.getXY();
		double fontRatio0to1 = fontSize0 / fontSize1;
		double fontWidth = fontSize0 * fontWidthFactor;
		double fontHeight = fontSize0 * fontHeightFactor;
		// TODO update for different orientation
		double coordHoriz0 = (rotate0 == null) ? xy0.getX() : xy0.getY();
		double coordHoriz1 = (rotate1 == null) ? xy1.getX() : xy1.getY();
		double coordVert0 = this.getCurrentBaseY();
		double coordVert1 = (rotate1 == null) ? xy1.getY() : xy1.getX();
		double deltaVert = coordVert0 - coordVert1;
		double maxFontSize = Math.max(fontSize0, fontSize1);
		double unscriptFontSize = Double.NaN;
		String linker = null;
		// anticlockwise Y rotation changes order
		double sign = (YPLUS.equals(rotate)) ? -1.0 : 1.0;
		double[] fontWidths = FontWidths.getFontWidths(this.getFontFamily());
		double spaceWidth = fontWidths[(int)C_SPACE] * maxFontSize * fontWidthFactor;
		
		// same size of font?
		LOG.debug(String.valueOf(this.getText())+"]["+text1.getText()+ " ...fonts... " + fontSize0+"/"+fontSize1);
		// has vertical changed by more than the larger font size?
		if (!Real.isEqual(coordVert0, coordVert1, maxFontSize * fontHeightFactor)) {
			LOG.debug("changed vertical height "+coordVert0+" => "+coordVert1+" ... "+maxFontSize);
			LOG.trace("COORDS "+xy0+"..."+xy1);
			LOG.trace("BASEY "+this.getCurrentBaseY()+"..."+text1.getCurrentBaseY());
		} else if (fontRatio0to1 > 0.95 && fontRatio0to1 < 1.05) {
			// no change of size
			if (Real.isEqual(coordVert0, coordVert1, eps)) {
				// still on same line?
				// allow a space
				double gapXX = (coordHoriz1 - coordHoriz0) * sign;
				double calcEnd = this.getCalculatedTextEndCoordinate(fontWidthFactor);
				double gapX = (coordHoriz1 - calcEnd) *sign;
				double nspaces = (gapX / spaceWidth);
				if (gapXX < 0) {
					LOG.debug("text R to L ... "+gapXX);
					// in front of preceding (axes sometime go backwards
					linker = null;
				} else if (nspaces < 0.5) {
					nspaces = 0;
				} else if (nspaces > 2) {
					nspaces = 100;
				} else {
					nspaces = 1;
				}
				linker = null;
				if (nspaces == 0) {
					linker = XMLConstants.S_EMPTY;
				} else if (nspaces == 1) {
					linker = XMLConstants.S_SPACE;
				}
			} else {
				LOG.debug("slight vertical change: "+coordVert0+" => "+coordVert1);
			}
		} else if (fontRatio0to1 > 1.05) {
			// coords down the page?
			LOG.debug("Trying sscript "+deltaVert);
			// sub/superScript
			if (deltaVert > 0 && Real.isEqual(deltaVert, subVert * fontHeight, maxFontSize)) {
				// start of subscript?
				linker = SUB0;
				LOG.debug("INSUB");
				// save font as larger size
				this.setFontSize(text1.getFontSize());
			} else if (deltaVert < 0 && Real.isEqual(deltaVert, supVert * fontHeight, maxFontSize)) {
				// start of superscript?
				linker = SUP0;
				LOG.debug("INSUP");
				// save font as larger size
				this.setFontSize(text1.getFontSize());
			} else {
				LOG.debug("ignored font change");
			}
		} else if (fontRatio0to1 < 0.95) {
			LOG.debug("Trying unscript "+deltaVert);
			// end of sub/superScript
			if (deltaVert > 0 && Real.isEqual(deltaVert, -supVert * fontHeight, maxFontSize)) {
				// end of superscript?
				linker = SUP1;
				LOG.debug("OUTSUP");
			} else if (deltaVert < 0 && Real.isEqual(deltaVert, -subVert * fontHeight, maxFontSize)) {
				// end of subscript?
				linker = SUB1;
				LOG.debug("OUTSUB");
			} else {
				LOG.debug("ignored font change");
			}
			if (newText != null) {
				setCurrentBaseY(text1.getCurrentBaseY());
			}
			unscriptFontSize = text1.getFontSize();
		} else {
			LOG.debug("change of font size: "+fontSize0+"/"+fontSize1+" .... "+getText()+" ... "+text1.getText());
		}
		if (linker != null) {
			newText = string0 + linker + string1;
			setText(newText);
			setCurrentFontSize(text1.getFontSize());
			// preserve best estimate of text length
			setCalculatedTextEndCoordinate(text1.getCalculatedTextEndCoordinate(fontWidthFactor));
			if (!Double.isNaN(unscriptFontSize)) {
				setFontSize(unscriptFontSize);
				setCurrentFontSize(unscriptFontSize);
				LOG.debug("setting font to "+unscriptFontSize);
			}
			LOG.debug("merged => "+newText);
		}
		LOG.debug("new...."+newText);
		return (newText != null);
	}
	
	public SVGRect getBoundingSVGRect() {
		Real2Range r2r = getBoundingBox();
		SVGRect rect = new SVGRect();
		rect.setBounds(r2r);
		return rect;
	}
	
	/** 
	 * Property of graphic bounding box.
	 * Can be overridden.
	 * 
	 * @return default none
	 */
	protected String getBBFill() {
		return "none";
	}

	/** 
	 * Property of graphic bounding box.
	 * Can be overridden.
	 * 
	 * @return default magenta
	 */
	protected String getBBStroke() {
		return "magenta";
	}

	/** property of graphic bounding box
	 * can be overridden
	 * @return default 0.5
	 */
	protected double getBBStrokeWidth() {
		return 0.2;
	}
	
	public void createWordWrappedTSpans(Double textWidthFactor, Real2Range boundingBox, Double fSize) {
		String textS = getText().trim();
		if (textS.length() == 0) {
			return;
		}
		setText(null);
		double fontSize = (fSize == null ? getFontSize() : fSize);
		String[] tokens = textS.split(EuclidConstants.S_WHITEREGEX);
		Double x0 = boundingBox.getXMin();
		Double x1 = boundingBox.getXMax();
		Double x = x0;
		Double y0 = boundingBox.getYMin();
		Double y = y0;
		Double deltay = fontSize*1.2;
		y += deltay;
		SVGTSpan span = createSpan(tokens[0], new Real2(x0, y), fontSize);
		int ntok = 1;
		while (ntok < tokens.length) { 
			String s = span.getText();
			span.setText(s+" "+tokens[ntok]);
			double xx = span.getCalculatedTextEndCoordinate(textWidthFactor);
			if (xx > x1) {
				span.setText(s);
				y += deltay;
				span = createSpan(tokens[ntok], new Real2(x0, y), fontSize);
			}
			ntok++;
		}
		clearRotate();
	}
	
	public SVGTSpan createSpan(String text, Real2 xy, Double fontSize) {
		SVGTSpan span = new SVGTSpan();
		span.setXY(xy);
		span.setFontSize(fontSize);
		span.setText(text);
		appendChild(span);
		return span;
	}

	/** makes a new list composed of the texts in the list
	 * 
	 * @param elements
	 * @return
	 */
	public static List<SVGText> extractTexts(List<SVGElement> elements) {
		List<SVGText> textList = new ArrayList<SVGText>();
		for (SVGElement element : elements) {
			if (element instanceof SVGText) {
				textList.add((SVGText) element);
			}
		}
		return textList;
	}
	
	/** 
	 * Convenience method to extract list of svgTexts in element
	 * 
	 * @param svgElement
	 * @return
	 */
	public static List<SVGText> extractSelfAndDescendantTexts(SVGElement svgElement) {
		return SVGText.extractTexts(SVGUtil.getQuerySVGElements(svgElement, ALL_TEXT_XPATH));
	}
	
	/** 
	 * Convenience method to extract list of svgTexts in element
	 * 
	 * @param svgElement element to query
	 * @param rotAngle angle rotated from 0
	 * @param eps tolerance in rotation angle
	 * @return
	 */
	public static List<SVGText> extractSelfAndDescendantTextsWithSpecificAngle(SVGElement svgElement, Angle targetAngle, double eps) {
		List<SVGText> textList = extractSelfAndDescendantTexts(svgElement);
		List<SVGText> newTextList = new ArrayList<SVGText>();
		for (SVGText text : textList) {
			Transform2 t2 = text.getTransform();
			boolean rotated = false;
			if (t2 == null) {
				rotated = targetAngle.isEqualTo(0.0, eps);
			} else {
				Angle rotAngle = t2.getAngleOfRotation();
				rotated = Real.isEqual(targetAngle.getRadian(), rotAngle.getRadian(), eps);
			}
			if (rotated) {
				newTextList.add(text);
			}
		}
		return newTextList;
	}
	


	/** 
	 * Special routine to make sure characters are correctly oriented
	 */
	public void setTransformToRotateAboutTextOrigin() {
		Transform2 transform2 = getTransform();
		RealSquareMatrix rotMat = transform2.getRotationMatrix();
		setTransformToRotateAboutTextOrigin(rotMat);
	}

	public void setTransformToRotateAboutTextOrigin(RealSquareMatrix rotMat) {
		Real2 xy = new Real2(this.getXY());
		Transform2 newTransform2 = new Transform2(new Vector2(xy)); 
		newTransform2 = newTransform2.concatenate(new Transform2(rotMat));
		newTransform2 = newTransform2.concatenate(new Transform2(new Vector2(xy.multiplyBy(-1.0))));
		this.setTransform(newTransform2);
	}

	public List<SVGTSpan> getChildTSpans() {
		tspans = SVGTSpan.extractTSpans(SVGUtil.getQuerySVGElements(this, "./svg:tspan"));
		return tspans;
	}
	
	public String createAndSetFontWeight() {
		String f = super.getFontWeight();
		if (f == null) {
			FontWeight fw = FontWeight.NORMAL;
			String fontFamily = this.getFontFamily();
			if (fontFamily != null) {
				if (fontFamily.toLowerCase().contains(FontWeight.BOLD.toString().toLowerCase())) {
					fw = FontWeight.BOLD;
				}
			}
			this.setFontWeight(fw);
			f = fw.toString();
		}
		return f;
	}
	
	
	public String createAndSetFontStyle() {
		String f = super.getFontStyle();
		if (f == null) {
			FontStyle fs = FontStyle.NORMAL;
			String fontFamily = this.getFontFamily();
			if (fontFamily != null) {
				String ff = fontFamily.toLowerCase();
				if (ff.contains("italic") || ff.contains("oblique")) {
					fs = FontStyle.ITALIC;
				}
			}
			this.setFontStyle(fs);
			f = fs.toString();
		}
		return f;
	}
	
	public boolean isBold() {
		String fontWeight = this.getFontWeight();
		return SVGText.BOLD.equalsIgnoreCase(fontWeight);
	}
	
	public boolean isItalic() {
		String fontStyle = this.getFontStyle();
		return SVGText.ITALIC.equalsIgnoreCase(fontStyle);
	}

	/** normally only present when added by PDF2SVG
	 * of form svgx:fontName="ABCDEF+FOOBar"
	 * @return name (or null)
	 */
	public String getSVGXFontName() {
		String fontName = SVGUtil.getSVGXAttribute(this, FONT_NAME);
		return fontName; 
	}
	
	/** 
	 * Normally only present when added by PDF2SVG.
	 * <p>
	 * Of form svgx:width="234.0".
	 * <p>
	 * Different to getWidth, which uses "width" attribute and is probably wrong for SVGText.
	 * 
	 * @return width (or null)
	 */
	public Double getSVGXFontWidth() {
		String width = SVGUtil.getSVGXAttribute(this, WIDTH);
		return width == null ? null : Double.valueOf(width); 
	}
	
	/** 
	 * Adds svgx:width attribute.
	 * <p>
	 * Only use when constructing new characters, such as spaces, and deconstructing
	 * ligatures.
	 * </p>
	 * <p>
	 * Of form svgx:width="234.0".
	 * </p>
	 * <p>
	 * Different to getWidth, which uses "width" attribute and is probably wrong for SVGText.
	 * </p>
	 * 
	 * @return width (or null)
	 */
	public void setSVGXFontWidth(Double width) {
//		if (width != null) {
//			Attribute widthAtt = SVGUtil.getSVGXAttributeAttribute(this, WIDTH);
//			if (widthAtt != null) {
//				widthAtt.detach();
//			}
//		}
		SVGUtil.setSVGXAttribute(this, WIDTH, String.valueOf(width));
//		}
	}

	public GlyphVector getGlyphVector() {
		if (getFontSize() == null) {
			return null;
		}
		int arbitraryFontSize = 20;
		Font font = new Font(getFontFamily(), (isItalic() ? (isBold() ? Font.BOLD | Font.ITALIC : Font.ITALIC): (isBold() ? Font.BOLD : Font.PLAIN)), arbitraryFontSize);
		font = font.deriveFont((float) (double) getFontSize());
		GlyphVector glyphVector = font.createGlyphVector(new FontRenderContext(new AffineTransform(), true, true), getText());
		return glyphVector;
	}
	
	/**
	 * @return width of first character
	 * @deprecated Use getWidthOfFirstCharacter(), which now calculates width if it isn't available (so this method does too).
	 */
	@Deprecated
	public Double getScaledWidth() {
		return getWidthOfFirstCharacter();
	}

	/**
	 * @param guessWidth
	 * @return width of first character
	 * @deprecated Use getWidthOfFirstCharacter(), which now calculates width if it isn't available (so this method does too).
	 */
	@Deprecated
	public Double getScaledWidth(boolean guessWidth) {
		return getWidthOfFirstCharacter();
	}

	/** 
	 * Get separation between two characters.
	 * <p>
	 * This is from the end of "this" to the start of nextText.
	 * 
	 * @param nextText
	 * @return
	 */
	public Double getSeparation(SVGText nextText) {
		Double separation = null;
		Double x = getX();
		Double xNext = (nextText == null ? null : nextText.getX());
		Double scaledWidth = getScaledWidth();
		if (x != null && xNext != null && scaledWidth != null) {
			separation = xNext - (x + scaledWidth); 
		}
		return separation;
	}
	
	/** 
	 * Will be zero if fontSize is zero.
	 * 
	 * @return
	 */
	public Double getScaledWidthOfEnSpace() {
		Double fontSize = getFontSize();
		return (fontSize == null ? null : N_SPACE * fontSize);
	}
	

	public Double getEnSpaceCount(SVGText nextText) {
		Double separation = getSeparation(nextText);
		Double enSpace = getScaledWidthOfEnSpace();
		Double scaledWidth = nextText.getScaledWidthOfEnSpace();
		enSpace = enSpace == null || scaledWidth == null ? null : Math.max(enSpace, scaledWidth);
		return (separation == null || enSpace == null || Math.abs(enSpace) < MIN_FONT_SIZE ? null : separation / enSpace);
	}

	/**
	 * @param newCharacters
	 * @param endOfLastCharacterX
	 * @param templateText to copy attributes from
	 */
	public SVGText createSpaceCharacterAfter() {
		SVGText spaceText = new SVGText();
		XMLUtil.copyAttributesFromTo(this, spaceText);
		spaceText.setText(" ");
		spaceText.setX(getCalculatedTextEndX());
		spaceText.setSVGXFontWidth(SPACE_WIDTH1000);
		return spaceText;
	}

	public Double getCalculatedTextEndX() {
		Double scaledWidth = getScaledWidth(); 
		Double x = getX();
		return (x == null || scaledWidth == null ? null : x + scaledWidth);
	}

	public String getString() {
		String s = "";
		List<SVGTSpan> tspans = getChildTSpans();
		if (tspans == null|| tspans.size() == 0) {
			s += toXML();
		} else {
			for (SVGTSpan tspan : tspans) {
				s += tspan.toXML()+"\n";
			}
		}
		return s;
	}

	/** 
	 * Get centre point of text.
	 * <p>
	 * Only works for single character.
	 * 
	 * @param i position of character (currently only 0)
	 * @return
	 */
	public Real2 getCentrePointOfFirstCharacter() {
		getWidthOfFirstCharacter();
		heightOfFirstCharacter = getFontSize();
		Real2 delta = new Real2(widthOfFirstCharacter / 2.0, -heightOfFirstCharacter / 2.0); 
		Real2 xy = getXY();
		return xy.plus(delta);
	}

	public Double getWidthOfFirstCharacter() {
		Double scaledWidth = null;
		Double width = getSVGXFontWidth();
		Double fontSize = getFontSize();
		if (width == null) {
			GlyphVector glyphVector = getGlyphVector();
			if (glyphVector != null) {
				scaledWidth = glyphVector.getGlyphLogicalBounds(0).getBounds2D().getWidth();
			}
		} else if (fontSize != null) {
			scaledWidth = width * SCALE1000 * fontSize;
		}
		return (widthOfFirstCharacter = scaledWidth);
	}
	
	public Double getHeightOfFirstCharacter() {
		return (heightOfFirstCharacter = getFontSize());
	}
	
	public Double getRadiusOfFirstCharacter() {
		getWidthOfFirstCharacter();
		getHeightOfFirstCharacter();
		return (heightOfFirstCharacter == null || widthOfFirstCharacter == null ? null :
			Math.sqrt(heightOfFirstCharacter * heightOfFirstCharacter + widthOfFirstCharacter * widthOfFirstCharacter) / 2.0);
	}

	public BufferedImage createImage(int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) image.createGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, width, height);
		int fontStyle = (isItalic()) ? Font.ITALIC : Font.PLAIN;
		String fontFamily = getFontFamily();
		int fontSize = (int)(double)getFontSize();
		Font font = new Font(fontFamily, fontStyle, fontSize);
		g.setFont(font);
		g.setColor(Color.BLACK);
		String value = getValue();
		float x = (float) (double) getX();
		float y = (float) (double) getY();
		g.drawString(value, x, y);
		return image;
	}

	public void removeAttributes() {
		int natt = this.getAttributeCount();
		for (int i = 0; i < natt; i++) {
			this.getAttribute(0).detach();
		}
	}
	
	@Override
	public String toString() {
		return "["+this.getText()+"("+this.getXY()+")"+"]";
	}

	public void rotateText(Angle angle) {
		Transform2 transform2;
		transform2 = new Transform2(new Vector2(this.getXY()));
		transform2 = transform2.concatenate(new Transform2(angle));
		transform2 = transform2.concatenate(new Transform2(new Vector2(this.getXY().multiplyBy(-1.0))));
		setTransform(transform2);
	}

	public static List<String> extractStrings(List<SVGText> textList) {
		List<String> stringList = null;
		if (textList != null) {
			stringList = new ArrayList<String>();
			for (SVGText text : textList) {
				stringList.add(text.getText());
			}
		}
		return stringList;
	}

	/** utility to draw a list of text.
	 * 
	 * @param textList
	 * @param file
	 */
	public static void drawTextList(List<? extends SVGElement> textList, File file) {
		SVGG g = new SVGG();
		for (SVGElement text : textList) {
			g.appendChild(text.copy());
		}
		SVGSVG.wrapAndWriteAsSVG(g, file);
	}

	public static List<SVGText> getRotatedElements(List<SVGText> characterList, Angle angle, double eps) {
		List<SVGElement> elements = SVGElement.getRotatedElementList(characterList, angle, eps);
		List<SVGText> textList = new ArrayList<SVGText>();
		for (SVGElement element : elements) {
			textList.add((SVGText) element);
		}
		return textList;
	}

}