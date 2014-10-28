package com.mapgen;

public class Param {
	String label;
	int max;
	int min;
	int def;
	int step;
	
	int value;
	
	public Param() {
		this("Parameter", 100, 0, 50, 1);
	}
	
	public Param(String label, int max, int min, int def, int step) {
		this.label = label;
		this.max = max;
		this.min = min;
		this.def = def;
		this.step = step;
		this.value = def;
	}
	
	public static Param[] params = {
		new Param("map x-axis length(~1000m)", 10000, 0, 1000, 100),
		new Param("map y-axis length(~1000m)", 10000, 0, 800, 100),
		new Param("average street width(>15m)", 100, 0, 15, 1),
		new Param("street width divergence(m)", 100, 0, 2, 1),
		new Param("average building height(m)", 100, 0, 7, 1),
		new Param("building height divergence(m)", 100, 0, 6, 1),
		new Param("building facade length(m)", 100, 0, 30, 1)
	};
	
	public static int getMapWidth() {
		return params[0].value;
	}
	
	public static int getMapHeight() {
		return params[1].value;
	}
}
