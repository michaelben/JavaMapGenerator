package com.mapgen.map;

import java.awt.geom.Path2D;
import java.util.ArrayList;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;

public class Road {
	ArrayList<Point2D> pts;
	
	//4 points counter clock-wise
	Point2D[] bound;
	
	Road(ArrayList<Point2D> pts, Point2D[] bound) {
		this.pts = pts;
		this.bound = bound;
	}
	
	//optimize containment testing for bound instead of all points belong to the road.
	boolean contains(Point2D p) {
		if(bound != null) {
			Path2D poly = new Path2D.Double();
			poly.moveTo(bound[0].getX(), bound[0].getY());
			poly.lineTo(bound[1].getX(), bound[1].getY());
			poly.lineTo(bound[2].getX(), bound[2].getY());
			poly.closePath();
			
			if(poly.contains(p.getX(), p.getY()))
				return true;

			poly = new Path2D.Double();
			poly.moveTo(bound[0].getX(), bound[0].getY());
			poly.lineTo(bound[3].getX(), bound[3].getY());
			poly.lineTo(bound[2].getX(), bound[2].getY());
			poly.closePath();
			
			if(poly.contains(p.getX(), p.getY()))
				return true;
			
			return false;
		}
		
		return false;
	}
	
	//find intersections between facet and road
	//optimize intersection testing for bound instead of all points belong to road.
	Polygon2D polyxpoly(ArrayList<Point2D> vertices,
			ArrayList<Integer> facet) {
		SimplePolygon2D p1 = new SimplePolygon2D();
		SimplePolygon2D p2 = new SimplePolygon2D();
		
		for(int index : facet)
			p1.addVertex(vertices.get(index));
		
		for(Point2D p : bound)
			p2.addVertex(p);
		
		return Polygons2D.intersection(p1, p2);
	}
}
