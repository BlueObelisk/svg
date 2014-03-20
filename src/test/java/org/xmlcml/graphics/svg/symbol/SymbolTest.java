package org.xmlcml.graphics.svg.symbol;

import org.junit.Test;
import org.xmlcml.euclid.Transform2;
import org.xmlcml.euclid.Vector2;
import org.xmlcml.graphics.svg.SVGG;
import org.xmlcml.graphics.svg.SVGSVG;
import org.xmlcml.graphics.svg.symbol.AbstractSymbol.SymbolFill;

import java.io.File;

public class SymbolTest {
	
	@Test
	public void testDraw() {
		CircledCross circledCross = new CircledCross();
		circledCross.setTransform(new Transform2(new Vector2(100., 100.)));
		circledCross.setFontSize(20.);
		SVGSVG.wrapAndWriteAsSVG(circledCross, new File("target/circledCross.svg"));
	}

	@Test
	public void testDraw1() {
		SVGG g = new SVGG();
		CircledCross circledCross = new CircledCross();
		circledCross.setTransform(new Transform2(new Vector2(100., 100.)));
		circledCross.setFontSize(20.);
		g.appendChild(circledCross);
		Square square = new Square();
		square.setTransform(new Transform2(new Vector2(120., 120.)));
		square.setFontSize(15.);
		g.appendChild(square);
		square = new Square();
		square.setTransform(new Transform2(new Vector2(150., 150.)));
		square.setStroke("blue");
		square.setFontSize(20.);
		square.setStrokeWidth(2.5); // no effect as stroke is none
		g.appendChild(square);
		square = new Square();
		square.setTransform(new Transform2(new Vector2(250., 250.)));
		square.setStroke("red");
		square.setSymbolFill(SymbolFill.ALL);
		square.setFontSize(30.);
		g.appendChild(square);
		
		SVGSVG.wrapAndWriteAsSVG(g, new File("target/draw1.svg"));
	}

}
