package com.mapgen;

public class Param {
	public String label;
	public int max;
	public int min;
	public int def;
	public int step;
	
	public int value;
	
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
		new Param("map x-axis length(~1000m)", 10000, 100, 1000, 100),
		new Param("map y-axis length(~1000m)", 10000, 100, 800, 100),
		new Param("average street width(>15m)", 100, 1, 15, 1),
		new Param("street width divergence(m)", 100, 0, 2, 1),
		new Param("average building height(m)", 100, 1, 7, 1),
		new Param("building height divergence(m)", 100, 0, 6, 1),
		new Param("building facade length(m)", 100, 1, 30, 1),
		new Param("roads & building density factor", 100, 5, 50, 1)
	};
	
	public static int getMapWidth() {
		return params[0].value;
	}
	
	public static int getMapHeight() {
		return params[1].value;
	}
	
	public static int getAvgStreetWidth() {
		return params[2].value;
	}
	
	public static int getStreetWidthStd() {
		return params[3].value;
	}
	
	public static int getAvgBuildingHeight() {
		return params[4].value;
	}
	
	public static int getBuildingHeightStd() {
		return params[5].value;
	}
	
	public static int getBuildingFacadeLength() {
		return params[6].value;
	}
	
	public static int getDensityFactor() {
		return params[7].value;
	}
}
