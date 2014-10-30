package com.mapgen;

/**
 * Created by BQ on 2014/10/30 0030.
 */
public class UndoRedo {
    Node node;
    double oldx;
    double oldy;

    UndoRedo() {
        this(null, 0, 0);
    }

    UndoRedo(Node node, double oldx, double oldy) {
        this.node = node;
        this.oldx = oldx;
        this.oldy = oldy;
    }
}
