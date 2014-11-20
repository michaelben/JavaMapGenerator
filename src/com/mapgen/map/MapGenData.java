package com.mapgen.map;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import math.geom2d.Point2D;
import math.geom2d.polygon.MultiPolygon2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

import com.mapgen.GraphPanel;
import com.mapgen.Node;
import com.mapgen.Param;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

//TODO: this entire class is directly converted from matlab code.
// better to use basic data structure and clean up all intermediate data structures and variable/method names.
// maybe remove dependency on EJML-core, EJML-equation and javaGeom libraries and rely on JTS only.
public class MapGenData {
	public static boolean isUrban = true;	//urban map or suburban map
	
	int numRoads;
	SimpleMatrix roadCentre;	//a set of random points composing roads

	ArrayList<ArrayList<Point2D>> roads = new ArrayList<ArrayList<Point2D>>();		//roads
	ArrayList<ArrayList<Point2D>> wpRoads = new ArrayList<ArrayList<Point2D>>();	//roads center line
	ArrayList<ArrayList<Point2D>> smRoads = new ArrayList<ArrayList<Point2D>>();	//small roads between roads and roads center line
	ArrayList<Point2D> tempD;		//roads into one list
	ArrayList<Point2D> wpRoad1;		//wpRoads into one list
	int numD;						//number of roads
	double maxHeight;
	
	public double xMargin;			//map x-axis length
	public double yMargin;			//map y-axis length
	private double roadWidth;		//average street width
	private double roadStd;			//street width divergence
	private double heightMean;		//average building height
	private double heightStd;		//building heigth divergence
	private double dist;			//building facade length
	
	private DenseMatrix64F B1;		//randomly generated points for voro_Points
	private DenseMatrix64F B;		//B1 subtract those points falling inside roads
	private ArrayList<Point2D> voro_Points;	//input points feeding into voronoi routine, composed of B, roads and wpRoads
	public static DenseMatrix64F v;			//result from voronoi. all vertices composed of voronoi diagram
	public static ArrayList<ArrayList<Integer>> c = new ArrayList<>();	//result from voronoi. all facets composed of voronoi diagram
	public static HashSet<Integer> ind;									//indices into c for valid facets
	public static ArrayList<Integer> indlist;							//arraylist of ind set
	public ArrayList<Double> heights;

	public static ArrayList<ArrayList<Integer>> nodeIndices;			//nodes indices for all facets
	public static ArrayList<Polygon> builds = new ArrayList<>();		//valid builds
	public static ArrayList<Polygon> builds2 = new ArrayList<>();		//all facets resulting from voronoi call
	
	public MapGenData() {
		initParam();
	}

	private void initParam() {
		xMargin = Param.params[0].value;
		yMargin = Param.params[1].value; 
		roadWidth = Param.params[2].value;
		roadStd = Param.params[3].value;
		heightMean = Param.params[4].value;
		heightStd = Param.params[5].value; 
		dist = Param.params[6].value;
		
		//reset collections
		if(voro_Points != null) voro_Points.clear();
		if(ind != null) ind.clear();
		if(indlist != null) indlist.clear();
		if(heights != null) heights.clear();
		if(builds != null) builds.clear();
		if(builds2 != null) builds2.clear();
	}
	
	public void generateMap() {
		initParam();
		
		int dense = (int)Math.floor((xMargin + yMargin)/8);
		numRoads = (int)Math.floor((xMargin + yMargin)/200);

		//result into roads, wpRoads and smRoads
		generateRoads();
		
		//pick random points for vonoroi routine
		B1 = SimpleMatrix.random(dense, 2, 0.0, 1.0, new Random()).getMatrix();
		if ( xMargin == yMargin) {
		    CommonOps.scale(xMargin, B1);
		} else {			
			for(int i=0; i<B1.getNumRows(); i++) {
				B1.set(i, 0, B1.get(i, 0) * xMargin);
				B1.set(i, 1, B1.get(i, 1) * yMargin);
			}
		}

		//get all unique points from roads
		tempD = new ArrayList<>();
		for(ArrayList<Point2D> ps : roads) {
			for(Point2D p: ps) {
				if(exist(tempD, p) >= 0) continue;
				else tempD.add(p);
			}
		}
		
		//get all unique points from wpRoads
		wpRoad1 = new ArrayList<>();
		for(ArrayList<Point2D> ps : wpRoads) {
			for(Point2D p: ps) {
				if(exist(wpRoad1, p) >= 0) continue;
				else wpRoad1.add(p);
			}
		}
		
		//checking if the randomly picked points fall into the roads 
		//in order to find and render only buildings that do not fall into roads
		B = find(B1, roads);
		
		System.out.println("B1="+B1.getNumRows());
		System.out.println("B="+B.getNumRows());
		System.out.println("tempD="+tempD.size());
		System.out.println("wpRoad1="+wpRoad1.size());
		
		//voronoi points composed of B, unique roads points and unique wpRoads points
		voro_Points = new ArrayList<>();
		for(int i=0; i<B.getNumRows(); i++)
			voro_Points.add(new Point2D(B.get(i, 0), B.get(i, 1)));

		for(int i=0; i<tempD.size(); i++)
			voro_Points.add(tempD.get(i));
		
		for(int i=0; i<wpRoad1.size(); i++)
			voro_Points.add(wpRoad1.get(i));
		
		//discard all points falling out of the map area
        ArrayList<Point2D> invalid = new ArrayList<>();
        for(Point2D p:voro_Points) {
        	if(p.getX() <= 0 || p.getX() >= xMargin || p.getY() <= 0 || p.getY() >= yMargin)
        		invalid.add(p);
        }
        
        //we comment out the following 2 lines because JTS voronoi routine can accept duplicate points and negative values
        //voro_Points.removeAll(invalid);
		//removeTooClose(voro_Points);
		
        //call vonoroi routine using JTS library
        //Note: depending on vonoroi implementation, some (such as matlab) can accept duplicate points and negative values.
        // some (such as matlab) can output c and v directly
		VoronoiDiagramBuilder vb = new VoronoiDiagramBuilder();
		vb.setTolerance(2.0);		//snapping factor
		vb.setSites(toCoords(voro_Points));
		GeometryCollection faces = (GeometryCollection) vb.getDiagram(new GeometryFactory());

		//JTS vonoroi routine does not return c and v directly, so we have to obtain them manually
		c.clear();
        v = getVC(faces);
        
        //System.out.println("vertices="+v);
        //System.out.println("c="+c);
        
        //for some vonoroi routine(matlab), the order of faces returned from vonoroi routine is the same as the order of input points.
        //but for JTS, the order of faces returned from vonoroi routine is not the same as the order of input points.
        //we need to make them same in order to group them into B, roads and wpRoads for further processing        
        ArrayList<ArrayList<Integer>> sameOrder = new ArrayList<>();
        for(Point2D p : voro_Points) {
	        for(ArrayList<Integer> face: c) {
	        	if(inpolygon(p, face, v)) {
	        		sameOrder.add(face);
	        		break;
	        	}
	        }
        }
		
        c = sameOrder;
        
        //System.out.println("c="+c);
        
        //System.out.println("voro_Points="+voro_Points);
        System.out.println("faces="+faces.getNumGeometries());
        System.out.println("voro_Points="+voro_Points.size());
		System.out.println("total="+v.getNumRows());
		System.out.println("c="+c.size());
		
		int N;
		if(isUrban)
			N = B.getNumRows();
		else
			N = c.size();
		
		HashSet<Integer> ind1 = new HashSet<>();	//indices for those invalid facets crossing over roads
		HashSet<Integer> ind2 = new HashSet<>();	//indices for those invalid facets near main Roads crossing over smRoads
		ind = new HashSet<>();
		
		//close open polygons and discard everything that may be outside the map area
		for (int i = 0; i<N; i++) {
			ArrayList<Integer> temp = c.get(i);
		    if (temp.get(0).intValue() != temp.get(temp.size() - 1).intValue())
		        c.get(i).add(temp.get(0));  // closing polygons
		}
		
		int numPoints;
		if(isUrban)
			numPoints = B.getNumRows()+tempD.size();
		else
			numPoints = N;
		
		//get all valid facets inside the map area
		for(int i = 0; i < numPoints; i++) {
			if(i>=c.size()) break;
			
			ArrayList<Integer> facet = c.get(i);
			int j;
			for(j = 0; j < facet.size(); j++)
		    if ((v.get(facet.get(j),0) >= 0)
		    	&& (v.get(facet.get(j),0) <= xMargin)
		    	&& (v.get(facet.get(j),1) >= 0)
		    	&& (v.get(facet.get(j),1) <= yMargin))
		    	continue;
		    else break;
			
		    if(j == facet.size()) ind.add(i);
		}
		
		System.out.println("ind size="+ind.size());
		
		//find those invalid facets crossing over roads
		for(int i = 0; i < N; i++) {    
			    // checking if one or more sides of buildings intersect roads
			    for(int k = 0; k < roads.size(); k++) {
			    	 Polygon2D p = polyxpoly(v, c.get(i), roads.get(k));
			    	 //if at least one vector [x1 y1], [x2 y2] is not 'empty' ( [] ) then one or more sides of the building fall into a road
			    	 if(p != null && !p.isEmpty())
			    		// and the coordinates of this building are set to NaN so it wont be rendered
			            ind1.add(i); 
			    }
		}

		System.out.println("ind1 size="+ind1.size());
		
		// we check if the buildings located close to the main road (Road) fall into the 
		// smaller road (smRoad)
		for(int i = B.getNumRows(); i < B.getNumRows()+tempD.size(); i++) {
			if(i>=c.size()) break;
			
		    if(c.get(i).size() != 0) {
				ArrayList<Integer> temp = c.get(i);
		    	if (temp.get(0).intValue() != temp.get(temp.size() - 1).intValue())
		        c.get(i).add(temp.get(0));  // closing polygons
		        
		        for(int k = 0; k < smRoads.size(); k++) {    
		            Polygon2D p = polyxpoly(v, c.get(i), smRoads.get(k));
		            if(p != null && !p.isEmpty())
		                ind2.add(i);
		        }
		    }	
		}
		          
		System.out.println("ind2 size="+ind2.size());
		
		ind1.addAll(ind2);
		
		ind.removeAll(ind1);
		
		System.out.println("ind size="+ind.size());
		
		// now all valid buildings are c.get(ind.get(i)) which are a set of indices into v
		
		//calculate the area of each building
		ArrayList<Double> buildArea = new ArrayList<>();
		indlist = new ArrayList<>(ind);
		for(int i = 0; i < indlist.size(); i++) {
			ArrayList<Integer> polyindex = c.get(indlist.get(i));
			SimplePolygon2D poly = new SimplePolygon2D();
			for(int j : polyindex)
				poly.addVertex(new Point2D(v.get(j, 0), v.get(j, 1)));
			
			buildArea.add(Math.abs(poly.area()));
		}
		
		// we find the buildings with area greater than a percentage of 
		// the maximum building area or smaller than a percentage of the minimum
		// and we discard them
		
		double areaMax = Collections.max(buildArea);
		double areaMin = Collections.min(buildArea);
		double percent1 = 0.6;
		double percent2 = 3;
		ArrayList<Integer> rem = new ArrayList<>();
		for(int i = 0; i < indlist.size(); i++) {
			if((buildArea.get(i) > percent1*areaMax)
				|| (buildArea.get(i) < percent2*areaMin))
				rem.add(indlist.get(i));
		}
		
		System.out.println("#removed="+rem.size());
		//System.out.println("buildArea="+buildArea);
		System.out.println("#buildArea="+buildArea.size());
		
		ind.removeAll(rem);
		System.out.println("ind size after area="+ind.size());
		
		//we create a database of variable heights 
		//and we assign a height to each building
		heights = new ArrayList<>();
		Random rand = new Random();
		for(int i = 0; i < ind.size(); i++) {
		        // random height calculation using a normal Gaussian distribution with 
		        // average value (heightMean) and standard divergence (heightStd) that have been given by the user
		    
		        // checking to prevent negative values for heights
		        double height = -10;
		        while (height < 6)
		            height = heightMean + heightStd * rand.nextGaussian();  

		        heights.add(height);
		}
		
		createBuild();
		//createBuild2(faces);

	}

	
	//create all valid facets only
	private void createBuild() {
		builds.clear();
	    
		//create builds for draw
	    for(int i=0; i<indlist.size(); i++) {
			Polygon p = new Polygon();
			ArrayList<Integer> pind = c.get(indlist.get(i));
			for(int j=0; j<pind.size(); j++)
				p.addPoint((int)v.get(pind.get(j), 0), (int)v.get(pind.get(j), 1));
			
			//eliminates triangular and degenerate cases
			if(p.npoints > 4)	//it is 4 because we repeat the last point with the initial point
				builds.add(p);
		}
	    
	    //generate unique valid points only, and topology information and their indices for drawing
	    ArrayList<Node> nodeset = new ArrayList<>();
	    nodeIndices = new ArrayList<>();
	    for(Polygon p : builds) {
	    	ArrayList<Integer> facetIndices = new ArrayList<>();
	    	for(int i=0; i<p.npoints; i++) {
	    		Node n = new Node(p.xpoints[i], p.ypoints[i]);
	    		int index = addNode(nodeset, n);
	    		
	    		facetIndices.add(index);
	    		
	    		//add into topology information
	    		nodeset.get(index).adjacentFaces.add(new Node.FaceIndex(p, i));
	    		
	    		//get its prev and next nodes
	    		int prev = i - 1;
	    		int next = i + 1;
	    		if(i==0 || i==p.npoints-1) {
	    			prev = p.npoints - 2;
	    			next = 1;
	    		}
	    		
	    		//add into topology information
	    		Node prevNode = new Node(p.xpoints[prev], p.ypoints[prev]);
	    		int prevIndex = addNode(nodeset, prevNode);
	    		nodeset.get(index).adjacentNodes.add(nodeset.get(prevIndex));
	    		
	    		Node nextNode = new Node(p.xpoints[next], p.ypoints[next]);
	    		int nextIndex = addNode(nodeset, nextNode);
	    		nodeset.get(index).adjacentNodes.add(nodeset.get(nextIndex));
	    	}
	    	nodeIndices.add(facetIndices);
	    }
	    
	    GraphPanel.nodes = nodeset;
		
		System.out.println("builds#="+builds.size());
	}
	
	private int addNode(ArrayList<Node> nodeset, Node n) {
		int index;
		if(nodeset.contains(n))
			index = nodeset.indexOf(n);
		else {
			nodeset.add(n);
			index = nodeset.size() - 1;
		}
		
		return index;
	}
	
	//create all facets, including those invalid facets
	@SuppressWarnings("unused")
	private void createBuild2(GeometryCollection polygons) {
		builds2.clear();
		
		int numPolygons = polygons.getNumGeometries();

		for(int i=0; i<numPolygons; i++) {
			Coordinate[] regionCoordinates = polygons.getGeometryN(i).getCoordinates();
			Polygon poly = new Polygon();
			for(int j=0; j<regionCoordinates.length; j++)
				poly.addPoint((int)regionCoordinates[j].x, (int)regionCoordinates[j].y);

			builds2.add(poly);
		}

		System.out.println("builds total#="+builds2.size());
	}
	
	public static final double TOLERANCE = 1.0;
	
	@SuppressWarnings("unused")
	private void removeTooClose(ArrayList<Point2D> pts) {
		ArrayList<Point2D> tooclose = new ArrayList<Point2D>();
		
		for(int i=0; i<pts.size(); i++)
			for(int j=i+1; j<pts.size(); j++)
				if(Point2D.distance(pts.get(i), pts.get(j)) <= TOLERANCE) {
					tooclose.add(pts.get(i));
					break;
				}
				
		pts.removeAll(tooclose);
	}
	
	public void generateRoads() {
		roads = new ArrayList<>();
		wpRoads = new ArrayList<>();
		smRoads = new ArrayList<>();
		
		boolean equalSize = false;

		if (xMargin == yMargin)
		    equalSize = true;
		else
		    equalSize = false;
		
		//get a set of random values for roads' centers
		roadCentre = SimpleMatrix.random(numRoads, 1, 2*roadWidth, xMargin-(2*roadWidth), new Random());
		//if any 2 roads are too close, we adjust them
		minDistanceRoad();
		
		System.out.println(roadCentre);
		
		Random rand = new Random();

	    ArrayList<Point2D> D1 = new ArrayList<>();		//one side of the road
	    ArrayList<Point2D> D2 = new ArrayList<>();		//anothe side of the road
	    ArrayList<Point2D> D3 = new ArrayList<>();		//road center line
	    ArrayList<Point2D> D = new ArrayList<>();		//D1 + D2 + D3
	    ArrayList<Point2D> road;
	    ArrayList<Point2D> wpRoad;
	    ArrayList<Point2D> smRoad;
	    
		for(int q = 0; q < numRoads; q++) {    
			    D1.clear();
			    D2.clear();
			    D3.clear();
			    D.clear();
			    
			    //obtain gaussian random road width based on user inputs
			    double width = Math.round(roadWidth + roadStd * rand.nextGaussian());
			    
			    if ( equalSize ) {
			        for(int i = 0; i < Math.ceil((xMargin + yMargin)/(2*dist)); i++) {
			            D1.add(new Point2D(i*dist, roadCentre.get(q)-width));
			            D2.add(new Point2D(i*dist, roadCentre.get(q)+width));
			            D3.add(new Point2D(i*dist, roadCentre.get(q)));
			        }
			        D.addAll(D1);
			        D.addAll(D.size(), D2);
			        D.addAll(D.size(), D3);
			    } else {
			        for(int i = 0; i < Math.ceil((xMargin + yMargin)/(2*dist)); i++) {
			            D1.add(new Point2D(i*dist, roadCentre.get(q)-width));
			            D2.add(new Point2D(i*dist, roadCentre.get(q)+width));
			            D3.add(new Point2D(i*dist, roadCentre.get(q)));
			        }
			        D.addAll(D1);
			        D.addAll(D.size(), D2);
			        D.addAll(D.size(), D3);
			    }
			    
			    //Rotate this generated road for some random degree around its center
			    D = myRotateRoad(D, roadCentre.get(q));
				
			    numD = D1.size();

			    //generate roads
			    road = new ArrayList<Point2D>();
			    road.addAll(D.subList(0, numD));
			    for(int i = 2*numD-1; i >= numD; i--)
			    	road.add(D.get(i));
			    road.add(D.get(0));
			    
			    roads.add(road);

			    //generate wpRoads
			    wpRoad = new ArrayList<Point2D>();
			    wpRoad.addAll(D.subList(2*numD, D.size()));
			    
			    wpRoads.add(wpRoad);
			    
			    //generate smRoads
			    smRoad = new ArrayList<Point2D>();
			    for(int i = 0; i < numD; i++)
			    	smRoad.add(new Point2D((road.get(i).getX() + wpRoad.get(i).getX())/2 + 1,
			    			(road.get(i).getY() + wpRoad.get(i).getY())/2 + 1));
			    for(int i = numD; i < 2*numD; i++)
			    	smRoad.add(new Point2D((road.get(i).getX() + wpRoad.get(2*numD-i-1).getX())/2 + 1,
			    			(road.get(i).getY() + wpRoad.get(2*numD-i-1).getY())/2 + 1));
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
	public ArrayList<Point2D> myRotateRoad(ArrayList<Point2D> D, double roadCenter) {
		double x = D.get(0).getX();
		double y = D.get(0).getY();
		double minx = x;
		double maxx = x;
		double miny = y;
		double maxy = y;
		
		//get road center
		for(Point2D p: D) {
			x = p.getX();
			y = p.getY();
			if(x > maxx) maxx = x;
			else if(x < minx) minx = x;
			if(y < miny) miny = y;
			else if(y > maxy) maxy = y;
		}
		
		double cx = (minx + maxx)/2;
		double cy = (miny + maxy)/2;
		
		int[] choice = new int[]{-90, -75, -60, -45, -30, -15, 0, 15, 30, 45, 60, 75, 90};

		int ind = new Random().nextInt(choice.length);
		int th = choice[ind];

		double theta=(th*Math.PI)/180.0;
		
		ArrayList<Point2D> D1 = new ArrayList<>();
		
		for(Point2D p: D) {
			//p = p.rotate(center, theta);
			x = p.getX();
			y = p.getY();
			x=x-cx;
			y=y-cy;
			double x1 = x*Math.cos(theta) - y*Math.sin(theta);
			double y1 = x*Math.sin(theta) + y*Math.cos(theta);

			x = x1;
			y = y1;
			
			x=x+cx;
			y=y+cy;
			
			D1.add(new Point2D(x,y));
		}
		
		return D1;
	}
	
	//adjust road centers so that no 2 road centers are too close
	public void minDistanceRoad() {
		for(int i = 1; i < numRoads; i++) {
			    boolean change = true;
			    int times = 0;
			    while ((change) && (times < 10*numRoads)) {
			        change = false;
			        for(int j = 0; j < i-1; j++) {
			            if  ( Math.abs(roadCentre.get(i,0) - roadCentre.get(j,0)) < (3*roadWidth) ) {
			                change = true;
			                double value = SimpleMatrix.random(1, 1, roadWidth, xMargin-roadWidth, new Random()).get(0);
			                roadCentre.set(i, 0, value);
			                times = times + 1;
			            }
			        }
			        if (times >= 10*numRoads) {
			            numRoads = i;
			        }
			    }
		}
	}
	
	public static final String DXF_header_fn = "header.dxf";
	public static final String facet_header_fn = "1stSet.txt";
	public static String DXF_header = null;
	public static String facet_header = null;
	
	public void createDXFile(String fname, int choice) {
		if(GraphPanel.nodes != null)
			this.createDXFile(fname, choice, GraphPanel.nodes, c, new ArrayList<>(ind), heights, xMargin, yMargin);
		else
			this.createDXFile(fname, choice, v, c, new ArrayList<>(ind), heights, xMargin, yMargin);
	}
	
	//create dxf from nodes data reflecting user adjusting
	private void createDXFile(String fname, int choice,
			ArrayList<Node> nodes,
			ArrayList<ArrayList<Integer>> builds,
			ArrayList<Integer> ind,
			ArrayList<Double> heights,
			double xMargin,
			double yMargin) {
		
		try {
			FileWriter fw = new FileWriter(fname);
			
			if(DXF_header == null) DXF_header = readFile(DXF_header_fn);
			if(facet_header == null) facet_header = readFile(facet_header_fn);
			
			fw.write(DXF_header);
			fw.write("\n");
			
			for(int i = 0; i < ind.size(); i++) {
			    ArrayList<Integer> build = builds.get(ind.get(i));
			    
			    fw.write(facet_header);
				fw.write("\n");
				
				double height = heights.get(i);
				
				for(int j = 0; j < build.size(); j++) {
					double x = nodes.get(build.get(j)).getX();
					double y = nodes.get(build.get(j)).getY();
			        if ( j != build.size()-1 ) {
			        	fw.write(String.format("%s\n","BUILD1"));
			        	fw.write(String.format("%3s\n","10"));
			        	fw.write(String.format("%.1f\n",x));
			        	fw.write(String.format("%3s\n","20"));
			        	fw.write(String.format("%.1f\n",y));
			        	fw.write(String.format("%3s\n","30"));
			        	fw.write(String.format("%.1f\n",height));
			        	fw.write(String.format("%3s\n","70"));
			        	fw.write(String.format("%6s\n","32"));
			        	fw.write(String.format("%3s\n","0"));
			        	fw.write(String.format("%s\n","VERTEX"));
			        	fw.write(String.format("%3s\n","8"));
			        } else {
			        	fw.write(String.format("%s\n","BUILD1"));
			        	fw.write(String.format("%3s\n","10"));
			        	fw.write(String.format("%.1f\n",x));
			        	fw.write(String.format("%3s\n","20"));
			        	fw.write(String.format("%.1f\n",y));
			        	fw.write(String.format("%3s\n","30"));
			        	fw.write(String.format("%.1f\n",height));
			        	fw.write(String.format("%3s\n","70"));
			        	fw.write(String.format("%6s\n","32"));
			        	fw.write(String.format("%3s\n","0"));
			        	fw.write(String.format("%s\n","SEQEND"));
			        	fw.write(String.format("%3s\n","8"));
			        	fw.write(String.format("%s\n","POLYLINE"));
			        	fw.write(String.format("%3s\n","8"));
			        }
				}
			}
			
			for(int k = 0; k <= xMargin; k = k + 10)
			    for(int l = 0; l <= yMargin; l = l + 10) {
			    	fw.write(String.format("%s\n","INSERT"));
			    	fw.write(String.format("%3s\n","8"));
			    	fw.write(String.format("%s\n","DEM_10M_CROSS"));
			    	fw.write(String.format("%3s\n","2"));
			    	fw.write(String.format("%s\n","CROSS"));
			    	fw.write(String.format("%3s\n","10"));
			    	fw.write(String.format("%.1f\n",(float)k));
			    	fw.write(String.format("%3s\n","20"));
			    	fw.write(String.format("%.1f\n",(float)l));
			    	fw.write(String.format("%3s\n","30"));
			    	fw.write(String.format("%.1f\n",0.0));
			    }
		
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//create dxf from builds data
	private void createDXFile(String fname, int choice,
			DenseMatrix64F vertices,
			ArrayList<ArrayList<Integer>> builds,
			ArrayList<Integer> ind,
			ArrayList<Double> heights,
			double xMargin,
			double yMargin) {
		
		try {
			FileWriter fw = new FileWriter(fname);
			
			if(DXF_header == null) DXF_header = readFile(DXF_header_fn);
			if(facet_header == null) facet_header = readFile(facet_header_fn);
			
			fw.write(DXF_header);
			fw.write("\n");
			
			for(int i = 0; i < ind.size(); i++) {
			    ArrayList<Integer> build = builds.get(ind.get(i));
			    
			    fw.write(facet_header);
				fw.write("\n");
				
				double height = heights.get(i);
				
				for(int j = 0; j < build.size(); j++) {
					double x = vertices.get(build.get(j), 0);
					double y = vertices.get(build.get(j), 1);
			        if ( j != build.size()-1 ) {
			        	fw.write(String.format("%s\n","BUILD1"));
			        	fw.write(String.format("%3s\n","10"));
			        	fw.write(String.format("%.1f\n",x));
			        	fw.write(String.format("%3s\n","20"));
			        	fw.write(String.format("%.1f\n",y));
			        	fw.write(String.format("%3s\n","30"));
			        	fw.write(String.format("%.1f\n",height));
			        	fw.write(String.format("%3s\n","70"));
			        	fw.write(String.format("%6s\n","32"));
			        	fw.write(String.format("%3s\n","0"));
			        	fw.write(String.format("%s\n","VERTEX"));
			        	fw.write(String.format("%3s\n","8"));
			        } else {
			        	fw.write(String.format("%s\n","BUILD1"));
			        	fw.write(String.format("%3s\n","10"));
			        	fw.write(String.format("%.1f\n",x));
			        	fw.write(String.format("%3s\n","20"));
			        	fw.write(String.format("%.1f\n",y));
			        	fw.write(String.format("%3s\n","30"));
			        	fw.write(String.format("%.1f\n",height));
			        	fw.write(String.format("%3s\n","70"));
			        	fw.write(String.format("%6s\n","32"));
			        	fw.write(String.format("%3s\n","0"));
			        	fw.write(String.format("%s\n","SEQEND"));
			        	fw.write(String.format("%3s\n","8"));
			        	fw.write(String.format("%s\n","POLYLINE"));
			        	fw.write(String.format("%3s\n","8"));
			        }
				}
			}
			
			for(int k = 0; k <= xMargin; k = k + 10)
			    for(int l = 0; l <= yMargin; l = l + 10) {
			    	fw.write(String.format("%s\n","INSERT"));
			    	fw.write(String.format("%3s\n","8"));
			    	fw.write(String.format("%s\n","DEM_10M_CROSS"));
			    	fw.write(String.format("%3s\n","2"));
			    	fw.write(String.format("%s\n","CROSS"));
			    	fw.write(String.format("%3s\n","10"));
			    	fw.write(String.format("%.1f\n",(float)k));
			    	fw.write(String.format("%3s\n","20"));
			    	fw.write(String.format("%.1f\n",(float)l));
			    	fw.write(String.format("%3s\n","30"));
			    	fw.write(String.format("%.1f\n",0.0));
			    }
		
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String readFile(String fn) {
			StringBuilder sb = new StringBuilder();
			String buf;
			
			try {
				InputStream is=MapGenData.class.getClassLoader().getResourceAsStream(fn);
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				while((buf= in.readLine()) != null) {
					sb.append(buf, 0, buf.length());
					sb.append('\n');
				}
				
				in.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return sb.toString();
	}
	
	//find intersections between facet and road
	public Polygon2D polyxpoly(DenseMatrix64F vertices,
			ArrayList<Integer> facet, ArrayList<Point2D> road) {
		SimplePolygon2D p1 = new SimplePolygon2D();
		SimplePolygon2D p2 = new SimplePolygon2D();
		Polygon2D res = new MultiPolygon2D();
		
		for(int i: facet)
			p1.addVertex(new Point2D(vertices.get(i, 0), vertices.get(i, 1)));
		
		for(int i=0; i<road.size(); i++)
			p2.addVertex(new Point2D(road.get(i).getX(), road.get(i).getY()));
		
		res = Polygons2D.intersection(p1, p2);
		
		return res;
	}
	
	//matlab cell2mat
	public DenseMatrix64F cell2mat(ArrayList<ArrayList<Point2D>> cell) {
		if(cell == null || cell.size() == 0) return null;
		
		int total = 0;
		for(int i=0; i< cell.size(); i++)
			total += cell.get(i).size();
		
		int k = 0;
		DenseMatrix64F res = new SimpleMatrix(total, 2).getMatrix();
		for(int i=0; i< cell.size(); i++) {
			ArrayList<Point2D> poly = cell.get(i);
			for(int j=0; j< poly.size(); j++) {
				res.set(k, 0, poly.get(j).getX());
				res.set(k, 1, poly.get(j).getY());
				k++;
			}
		}
		
		return res;
	}
	
	//find all points fall inside of roads and discard them, return the rest points
	public DenseMatrix64F find(DenseMatrix64F b1, ArrayList<ArrayList<Point2D>> roads) {
		ArrayList<Point2D> pnts = new ArrayList<>();
		
		for(int i = 0; i < b1.getNumRows(); i++) {
			double x = b1.get(i,0);
			double y = b1.get(i,1);
			int j;
			for(j = 0; j < roads.size(); j++) {
				if(inpolygon(x, y, roads.get(j)))
					break;
			}
			
			if(j == roads.size())
				pnts.add(new Point2D((float)x,(float)y));
		}
		
		DenseMatrix64F res = new DenseMatrix64F(pnts.size(), 2);
		for (int i = 0; i < pnts.size(); i++) {
			res.set(i, 0, pnts.get(i).getX());
			res.set(i, 1, pnts.get(i).getY());
		}
		
		return res;
	}
	
	public static final int initialSize = 65535;
	
	//if (x,y) is inside of polygon
	public boolean inpolygon(double x, double y, ArrayList<Point2D> polygon) {
		Polygon poly = new Polygon();
		for(Point2D p: polygon)
			poly.addPoint((int)p.getX(), (int)p.getY());
		
		return poly.contains(x, y);
	}
	
	public boolean inpolygon(Point2D p, ArrayList<Integer> polygon, DenseMatrix64F vertices) {
		Polygon poly = new Polygon();
		for(int i : polygon)
			poly.addPoint((int)vertices.get(i, 0), (int)vertices.get(i, 1));
    	
		return poly.contains(p.getX(), p.getY());
	}
	
	public ArrayList<Coordinate> toCoords(ArrayList<Point2D> pts) {
		ArrayList<Coordinate> coords = new ArrayList<>();
		for(int i=0; i<pts.size(); i++)
			coords.add(new Coordinate(pts.get(i).getX(), pts.get(i).getY()));
		
		return coords;
	}
	
	//get v and c from result of JTS voronoi routine
	private DenseMatrix64F getVC(GeometryCollection polygons) {
		int numPolygons = polygons.getNumGeometries();
		
		ArrayList<Point2D> totalp = new ArrayList<>();
		
		for(int i=0; i<numPolygons; i++) {
			ArrayList<Integer> facet = new ArrayList<>();
			Coordinate[] regionCoordinates = polygons.getGeometryN(i).getCoordinates();
			for(int j=0; j<regionCoordinates.length; j++) {
				int index = exist(totalp, regionCoordinates[j]);
				if(index != -1) {
					facet.add(index);
				} else {
					totalp.add(new Point2D(regionCoordinates[j].x, regionCoordinates[j].y));
					facet.add(totalp.size()-1);
				}
			}
			
			c.add(facet);
		}
		
		return toMatrix(totalp);
	}
	
	//find duplicate points
	private int exist(ArrayList<Point2D> pts, Point2D coord) {
		for(int i=0; i<pts.size(); i++) {
			double x = coord.getX();
			double y = coord.getY();
			if((x == pts.get(i).getX()) && (y == pts.get(i).getY())) {
				System.out.println("x="+x+", y="+y);
					return i;
			}
			else continue;
		}
		
		return -1;
	}
	
	//find duplicate points
	private int exist(ArrayList<Point2D> pts, Coordinate coord) {
		for(int i=0; i<pts.size(); i++) {
			double x = coord.x;
			double y = coord.y;
			if((x == pts.get(i).getX()) && (y == pts.get(i).getY()))
					return i;
			else continue;
		}
		
		return -1;
	}
	
	private DenseMatrix64F toMatrix(ArrayList<Point2D> pts) {
		DenseMatrix64F res = new DenseMatrix64F(pts.size(), 2);
		
		for(int i=0; i<pts.size(); i++) {
			res.set(i, 0, pts.get(i).getX());
			res.set(i, 1, pts.get(i).getY());
		}
		
		return res;
	}
	
	//for testing
	public void readCsv(String fn) {
		try {
			String line = null;
			MapGenData.builds.clear();
			Polygon poly = new Polygon();
			
			BufferedReader r = new BufferedReader(new FileReader(fn));
			while((line = r.readLine()) != null) {
				String[] result = line.split("[,\\s]");
				if(result.length == 1) {
					MapGenData.builds.add(poly);
					poly = new Polygon();
				} else
					poly.addPoint((int)Float.parseFloat(result[0]), (int)Float.parseFloat(result[1]));
			}
			r.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
