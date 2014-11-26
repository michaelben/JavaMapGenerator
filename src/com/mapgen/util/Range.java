package com.mapgen.util;

public class Range {
	double x;
	double y;
	double width;
	double height;
	
	//(x,y) is upper-left point
	public Range(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	//p1 is upper-left, p2 is lower-right
	public Range(Point p1, Point p2) {
		this.x = p1.getX();
		this.y = p1.getY();
		this.width = p2.getX() - p1.getX();
		this.height = p2.getY() - p1.getY();
	}
	
	public double getMinX() { 
		return x;
	}
	
	public double getMinY() {
		return y;
	}
	
	public double getWidth() {
		return width;
	}
	
	public double getHeight() {
		return height;
	}
	
	public boolean isInRange(Point p) {
		double x = p.getX();
		double y = p.getY();
		
		if(x >= this.x
			&& x <= this.x+this.width
			&& y >= this.y
			&& y <= this.y + height)
			return true;
		else
			return false;
	}
}
