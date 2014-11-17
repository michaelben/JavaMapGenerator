package com.mapgen;

public class Node {
    double x;
    double y;
    double dx;
    double dy;
    boolean fixed;
    String lbl;
    
    public Node () {
    	this(0,0);
    }
    
    public Node(double x, double y) {
    	this.x = x;
    	this.y = y;
    }
}
