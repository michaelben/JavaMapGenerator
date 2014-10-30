package com.mapgen;

public class Node {
    double x;
    double y;
    double dx;
    double dy;
    boolean fixed;
    String lbl;

    Node() {
        this(0, 0, null);
    }

    Node(double x, double y) {
        this(x, y, null);
    }

    Node(double x, double y, String lbl) {
        this.x = x;
        this.y = y;
        this.lbl = lbl;
    }
}
