package com.mapgen;

import java.awt.Polygon;
import java.util.ArrayList;

//Use the private nodes as Double precision Polygon, and use java.awt.polygon for rendering on screen.
//In this way, we can preserve (unmodified) double precision values when output DXF file.
public class Polygon2D extends Polygon {
	/**
	 * This class is supposed not to change after the initial release, for subsequent serialization
	 */
	private static final long serialVersionUID = 7836037643384895415L;
	
	public ArrayList<Node> nodes;
	
	public Polygon2D() {
		super();
		
		this.nodes = new ArrayList<>();
	}
	
	public Polygon2D(ArrayList<Node> nodes) {
		this.nodes = nodes;
		
		if(this.nodes != null)
			for(Node node : nodes)
				super.addPoint((int)node.getX(), (int)node.getY());
		
		this.nodes = new ArrayList<>();
	}
	
}
