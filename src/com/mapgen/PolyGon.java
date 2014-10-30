package com.mapgen;

import java.awt.*;
import java.util.ArrayList;

public class PolyGon {
    ArrayList<Node> nodes;
    Color fillColor;

    int[] xpoints;
    int[] ypoints;
    int npoints;

    public PolyGon() {
        this(Color.gray);
    }

    public PolyGon(Color c) {
        this.fillColor = c;
        nodes = new ArrayList<Node>();
    }

    public void add(Node node) {
        nodes.add(node);
    }

    public PolyGon add(int x, int y) {
        nodes.add(new Node(x, y));
        return this;
    }

    public void normalizeForDraw() {
        npoints = nodes.size();
        xpoints = new int[npoints];
        ypoints = new int[npoints];
        for (int i = 0; i < npoints; i++) {
            xpoints[i] = (int) nodes.get(i).x;
            ypoints[i] = (int) nodes.get(i).y;
        }
    }
}
