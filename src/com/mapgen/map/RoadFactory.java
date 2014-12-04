package com.mapgen.map;

import java.util.ArrayList;
import java.util.Random;

import math.geom2d.Point2D;

public class RoadFactory {

	private ArrayList<Double> roadCentre;	//a set of random points used as road centers to generate roads
	
	private double roadWidth;
	private double roadStd;
	private int numRoads;
	private double xMargin;
	private double yMargin;
	private double dist;

	private ArrayList<ArrayList<Point2D>> roads;		//roads
	private ArrayList<ArrayList<Point2D>> wpRoads;	//roads center line
	private ArrayList<ArrayList<Point2D>> smRoads;	//small roads between roads and roads center line
	
	public RoadFactory(double roadWidth, double roadStd, int numRoads, double xMargin, double yMargin, double dist) {
		this.roadWidth = roadWidth;
		this.roadStd = roadStd;
		this.numRoads = numRoads;
		this.xMargin = xMargin;
		this.yMargin = yMargin;
		this.dist = dist;
	}
	
	public void generateRoads() {
		roads = new ArrayList<>();
		wpRoads = new ArrayList<>();
		smRoads = new ArrayList<>();
		
		int numPoints;						//number points for one side of a road
		
		boolean equalSize = false;

		if (xMargin == yMargin)
		    equalSize = true;
		else
		    equalSize = false;
		
		//get a set of random values for roads' centers
		roadCentre = new ArrayList<Double>();
		Random rand = new Random();
		for(int i = 0; i < numRoads; i++) {
			double value = rand.nextDouble() * (yMargin - 2*roadWidth) + 2*roadWidth;
			roadCentre.add(value);
		}
		
		//if any 2 roads are too close, we adjust them accordingly
		minDistanceRoad();

	    ArrayList<Point2D> lowerSide = new ArrayList<>();		//lower side of the road
	    ArrayList<Point2D> upperSide = new ArrayList<>();		//upper side of the road
	    ArrayList<Point2D> centerLine = new ArrayList<>();		//road center line
	    ArrayList<Point2D> allSides = new ArrayList<>();		//lowerSide + upperSide + centerLine
	    ArrayList<Point2D> road;
	    ArrayList<Point2D> wpRoad;
	    ArrayList<Point2D> smRoad;
	    
		for(int q = 0; q < numRoads; q++) {    
			    lowerSide.clear();
			    upperSide.clear();
			    centerLine.clear();
			    allSides.clear();
			    
			    //obtain gaussian random road width based on user inputs
			    double width = Math.round(roadWidth + roadStd * rand.nextGaussian());
			    
			    if ( equalSize ) {
			        for(int i = 0; i < Math.ceil((xMargin + yMargin)/(2*dist)); i++) {
			            lowerSide.add(new Point2D(i*dist, roadCentre.get(q)-width));
			            upperSide.add(new Point2D(i*dist, roadCentre.get(q)+width));
			            centerLine.add(new Point2D(i*dist, roadCentre.get(q)));
			        }
			        allSides.addAll(lowerSide);
			        allSides.addAll(allSides.size(), upperSide);
			        allSides.addAll(allSides.size(), centerLine);
			    } else {
			    	//we can do something different here
			        for(int i = 0; i < Math.ceil((xMargin + yMargin)/(2*dist)); i++) {
			            lowerSide.add(new Point2D(i*dist, roadCentre.get(q)-width));
			            upperSide.add(new Point2D(i*dist, roadCentre.get(q)+width));
			            centerLine.add(new Point2D(i*dist, roadCentre.get(q)));
			        }
			        allSides.addAll(lowerSide);
			        allSides.addAll(allSides.size(), upperSide);
			        allSides.addAll(allSides.size(), centerLine);
			    }
			    
			    //Rotate this generated road for some random degree around its center
			    allSides = myRotateRoad(allSides, roadCentre.get(q));
				
			    numPoints = lowerSide.size();

			    //generate roads
			    road = new ArrayList<Point2D>();
			    road.addAll(allSides.subList(0, numPoints));
			    for(int i = 2*numPoints-1; i >= numPoints; i--)
			    	road.add(allSides.get(i));
			    road.add(allSides.get(0));
			    
			    roads.add(road);

			    //generate wpRoads
			    wpRoad = new ArrayList<Point2D>();
			    wpRoad.addAll(allSides.subList(2*numPoints, allSides.size()));
			    
			    wpRoads.add(wpRoad);
			    
			    //generate smRoads
			    smRoad = new ArrayList<Point2D>();
			    for(int i = 0; i < numPoints; i++)
			    	smRoad.add(new Point2D((road.get(i).getX() + wpRoad.get(i).getX())/2 + 1,
			    			(road.get(i).getY() + wpRoad.get(i).getY())/2 + 1));
			    for(int i = numPoints; i < 2*numPoints; i++)
			    	smRoad.add(new Point2D((road.get(i).getX() + wpRoad.get(2*numPoints-i-1).getX())/2 + 1,
			    			(road.get(i).getY() + wpRoad.get(2*numPoints-i-1).getY())/2 + 1));
			    smRoad.add(new Point2D((road.get(0).getX() + wpRoad.get(0).getX())/2 + 1,
		    			(road.get(0).getY() + wpRoad.get(0).getY())/2 + 1));
			    
			    smRoads.add(smRoad);
		}

	}
	
	//rotate the road D for some random degree around its center.
	//To do this:
	//first translate to its center as origin(0,0)
	//then rotate random degree,
	//then translate back to its original location
	public ArrayList<Point2D> myRotateRoad(ArrayList<Point2D> allSides, double roadCenter) {
		double x = allSides.get(0).getX();
		double y = allSides.get(0).getY();
		double minx = x;
		double maxx = x;
		double miny = y;
		double maxy = y;
		
		//get road center
		for(Point2D p: allSides) {
			x = p.getX();
			y = p.getY();
			if(x > maxx) maxx = x;
			else if(x < minx) minx = x;
			if(y < miny) miny = y;
			else if(y > maxy) maxy = y;
		}
		
		double cx = (minx + maxx)/2;
		double cy = (miny + maxy)/2;
		
		//set up randomness
		int[] choice = new int[]{-90, -75, -60, -45, -30, -15, 0, 15, 30, 45, 60, 75, 90};

		int ind = new Random().nextInt(choice.length);
		int th = choice[ind];

		double theta=(th*Math.PI)/180.0;
		
		ArrayList<Point2D> result = new ArrayList<>();
		
		for(Point2D p: allSides) {
			//translate to the road center
			x = p.getX();
			y = p.getY();
			x=x-cx;
			y=y-cy;
			
			//rotate the random degree
			double x1 = x*Math.cos(theta) - y*Math.sin(theta);
			double y1 = x*Math.sin(theta) + y*Math.cos(theta);

			x = x1;
			y = y1;
			
			//translate back
			x=x+cx;
			y=y+cy;
			
			result.add(new Point2D(x,y));
		}
		
		return result;
	}
	
	//adjust road centers so that no 2 road centers are too close
	public void minDistanceRoad() {
		Random rand = new Random();
		
		for(int i = 1; i < numRoads; i++) {
			    boolean change = true;
			    int times = 0;
			    while ((change) && (times < 10*numRoads)) {
			        change = false;
			        for(int j = 0; j < i-1; j++) {
			            if  ( Math.abs(roadCentre.get(i) - roadCentre.get(j)) < (3*roadWidth) ) {
			                change = true;
			                //get a random double value with minimum of roadWidth and maximum of xMargin-roadWidth
			                double value = rand.nextDouble() * (xMargin - 2 * roadWidth) + roadWidth;
			                roadCentre.set(i, value);
			                times = times + 1;
			            }
			        }
			        if (times >= 10*numRoads) {
			            numRoads = i;
			        }
			    }
		}
	}
	
	public ArrayList<ArrayList<Point2D>> getRoads() {
		return roads;
	}
	
	public ArrayList<ArrayList<Point2D>> getWpRoads() {
		return wpRoads;
	}
	
	public ArrayList<ArrayList<Point2D>> getSmRoads() {
		return smRoads;
	}
	
	public void setNumRoads(int numRoads) {
		this.numRoads = numRoads;
	}
	
	public void setRoadWidth(double roadWidth) {
		this.roadWidth = roadWidth;
	}
	
	public void setRoadStd(double roadStd) {
		this.roadStd = roadStd;
	}
	
	public void setXMargin(double xMargin) {
		this.xMargin = xMargin;
	}
	
	public void setYMargin(double yMargin) {
		this.yMargin = yMargin;
	}
	
	public void setDist(double dist) {
		this.dist = dist;
	}
}
