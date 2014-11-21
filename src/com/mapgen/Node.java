package com.mapgen;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;

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
    	this.adjacentFaces = new ArrayList<>();
    	this.adjacentNodes = new HashSet<>();
    }
    
    public double getX() {
    	return x;
    }
    
    public double getY() {
    	return y;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        return true;
    }
    
    @Override
    public String toString(){
        return String.format("\nx:%.2f y:%.2f", x, y);
    }

    public boolean equalsInt(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if ((int)x != (int)other.x)
            return false;
        if ((int)y != (int)other.y)
            return false;
        return true;
    }
    
    public static class FaceIndex {
    	public Polygon face;
    	public int vertexInd;
    	
    	public FaceIndex(Polygon face, int vertexInd) {
    		this.face = face;
    		this.vertexInd = vertexInd;
    	}
    }
    
    public ArrayList<FaceIndex> adjacentFaces;
    public HashSet<Node> adjacentNodes;
}
