package com.mapgen.map;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import math.geom2d.Point2D;
import math.geom2d.polygon.MultiPolygon2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.simple.SimpleMatrix;

import com.mapgen.Param;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

public class MapGenData {
	int numRoads;
	SimpleMatrix roadCentre;

	ArrayList<ArrayList<Point2D>> roads = new ArrayList<ArrayList<Point2D>>();
	ArrayList<ArrayList<Point2D>> wpRoads = new ArrayList<ArrayList<Point2D>>();
	ArrayList<ArrayList<Point2D>> smRoads = new ArrayList<ArrayList<Point2D>>();
	DenseMatrix64F tempD;
	DenseMatrix64F wpRoad1;
	//DenseMatrix64F D;
	int numD;
	double maxHeight;
	
	public double xMargin;
	public double yMargin;
	private double roadWidth;
	private double roadStd;
	private double heightMean;
	private double heightStd;
	private double dist;
	
	private DenseMatrix64F B1;
	private DenseMatrix64F B;
	private float[][] voro_Points;
	public DenseMatrix64F v;
	public ArrayList<ArrayList<Integer>> c = new ArrayList();
	public ArrayList<Integer> ind;
	public ArrayList<Double> heights;

	public static ArrayList<Polygon> builds = new ArrayList();
	
	public MapGenData() {
		xMargin = Param.params[0].value;
		yMargin = Param.params[1].value; 
		roadWidth = Param.params[2].value;
		roadStd = Param.params[3].value;
		heightMean = Param.params[4].value;
		heightStd = Param.params[5].value; 
		dist = Param.params[6].value;
	}

	public void generateMap() {
		int dense = (int)Math.floor((Param.params[0].value + Param.params[1].value)/8);
		numRoads = (int) Math.floor((Param.params[0].value + Param.params[1].value)/200);

		generateRoads();
		
		//pick random points
		B1 = SimpleMatrix.random(dense, 2, 0.0, 1.0, new Random()).getMatrix();
		if ( xMargin == yMargin) {
		    CommonOps.scale(xMargin, B1);
		} else {			
			for(int i=0; i<B1.getNumRows(); i++) {
				B1.set(i, 0, B1.get(i, 0) * xMargin);
				B1.set(i, 1, B1.get(i, 1) * yMargin);
			}
		}
		
		tempD = cell2mat(roads);
		wpRoad1 = cell2mat(wpRoads);
		
		//checking if the randomly picked points fall into the roads 
		//in order to find and render only buildings that do not fall into roads
		B = find(B1, roads);
		
		System.out.println("B1="+B1.getNumRows());
		System.out.println("B="+B.getNumRows());
		System.out.println("tempD="+tempD.getNumRows());
		System.out.println("wpRoad1="+wpRoad1.getNumRows());
		
		voro_Points = new float[B.getNumRows()+tempD.getNumRows()+wpRoad1.getNumRows()][2];
		for(int i=0; i<B.getNumRows(); i++) {
			voro_Points[i][0] = (float)B.get(i, 0);
			voro_Points[i][1] = (float)B.get(i, 1);
		}
		for(int i=0; i<tempD.getNumRows(); i++) {
			voro_Points[i+B.getNumRows()][0] = (float)tempD.get(i, 0);
			voro_Points[i+B.getNumRows()][1] = (float)tempD.get(i, 1);
		}
		for(int i=0; i<wpRoad1.getNumRows(); i++) {
			voro_Points[i+B.getNumRows()+tempD.getNumRows()][0] = (float)wpRoad1.get(i, 0);
			voro_Points[i+B.getNumRows()+tempD.getNumRows()][1] = (float)wpRoad1.get(i, 1);
		}

		ArrayList<Point2D> pts = new ArrayList();
		for(ArrayList<Point2D> ps : roads) {
			for(Point2D p: ps) {
				if(exist(pts, p) >= 0) continue;
				else pts.add(p);
			}
		}
		
		System.out.println("num of distinct points in roads="+pts.size());
		
		pts = new ArrayList();
		for(ArrayList<Point2D> ps : wpRoads) {
			for(Point2D p: ps) {
				if(exist(pts, p) >= 0) continue;
				else pts.add(p);
			}
		}
		
		System.out.println("num of distinct points in wpRoads="+pts.size());
		
		pts = new ArrayList();
		for(float[] p : voro_Points) {
			if(exist(pts, p) >= 0) continue;
			else pts.add(new Point2D(p[0], p[1]));
		}
		
		System.out.println("num of distinct points="+pts.size());
		
		VoronoiDiagramBuilder vb = new VoronoiDiagramBuilder();
		//vb.setTolerance(0);	//no snapping
		vb.setSites(toCoords(voro_Points));
		GeometryCollection faces = (GeometryCollection) vb.getDiagram(new GeometryFactory());
		
		c.clear();
		
        v = getVC(faces);
		
        //System.out.println("voro_Points="+voro_Points);
        System.out.println("faces="+faces.getNumGeometries());
        System.out.println("voro_Points="+voro_Points.length);
		System.out.println("total="+v.getNumRows());
		System.out.println("c="+c.size());
		
		int N = B.getNumRows();

		ArrayList<Integer> ind1 = new ArrayList();
		ArrayList<Integer> ind2 = new ArrayList();
		ArrayList<Integer> ind3 = new ArrayList();
		ind = new ArrayList();
		
		//close open polygons and discard everything that may be outside the map area
		for (int i = 0; i<N; i++) {
			ArrayList<Integer> temp = c.get(i);
		    if (temp.get(0) != temp.get(temp.size() - 1))
		        c.get(i).add(temp.get(0));  // closing polygons
		}
		
		for(int i = 0; i < B.getNumRows()+tempD.getNumRows(); i++) {
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

		// we check if the buildings located close to the main road (Road) fall into the 
		// smaller road (smRoad)
		for(int i = B.getNumRows(); i < B.getNumRows()+tempD.getNumRows(); i++) {
			if(i>=c.size()) break;
			
		    if(c.get(i).size() != 0) {
				ArrayList<Integer> temp = c.get(i);
		    	if (temp.get(0) != temp.get(temp.size() - 1))
		        c.get(i).add(temp.get(0));  // closing polygons
		        
		        for(int k = 0; k < smRoads.size(); k++) {    
		            Polygon2D p = polyxpoly(v, c.get(i), smRoads.get(k));
		            if(p != null && !p.isEmpty())
		                ind2.add(i);
		        }
		    }	
		}
		          
		ind1.addAll(ind1.size(), ind2);
		
		ind.removeAll(ind1);
		
		// now all valid buildings are c.get(ind.get(i)) which are a set of indices into v
		
		//calculate the area of each building
		ArrayList<Double> buildArea = new ArrayList();
		for(int i = 0; i < ind.size(); i++) {
			ArrayList<Integer> polyindex = c.get(ind.get(i));
			SimplePolygon2D poly = new SimplePolygon2D();
			for(int j : polyindex)
				poly.addVertex(new Point2D(v.get(j, 0), v.get(j, 1)));
			
			buildArea.add(poly.area());
		}
		
		// we find the buildings with area greater than a percentage of 
		// the maximum building area or smaller than a percentage of the minimum
		// and we discard them
		
		double areaMax = Collections.max(buildArea);
		double areaMin = Collections.min(buildArea);
		double percent1 = 0.6;
		double percent2 = 3;
		for(int i = 0; i < ind.size(); i++) {
			if((buildArea.get(i) > percent1*areaMax)
				&& (buildArea.get(i) < percent2*areaMin))
				ind.remove(i);
		}
		
		//we create a database of variable heights 
		//and we assign a height to each building
		heights = new ArrayList();
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

	}
	
	private void createBuild() {

		FileWriter csvfacets = null;
	    
	    try {
	    	csvfacets = new FileWriter("d:\\iso\\csvfacets");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		//create builds for draw
		//for(int i=0; i<ind.size(); i++) {
	    for(int i=0; i<c.size(); i++) {
			try {
				csvfacets.write(String.format("%d\n", i));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Polygon p = new Polygon();
			ArrayList<Integer> pind = c.get(i);
			for(int j=0; j<pind.size(); j++) {
				p.addPoint((int)v.get(j, 0), (int)v.get(j, 1));
				try {
					csvfacets.write(String.format("%f,%f\n", v.get(j, 0), v.get(j, 1)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			builds.add(p);
		}
		
		try {
			csvfacets.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("builds#="+builds.size());
	}
	
	public void generateRoads() {
		roads = new ArrayList();
		boolean equalSize = false;

		if (xMargin == yMargin)
		    equalSize = true;
		else
		    equalSize = false;
		
		roadCentre = SimpleMatrix.random(numRoads, 1, 2*roadWidth, xMargin-(2*roadWidth), new Random());
		minDistanceRoad();
		
		System.out.println(roadCentre);
		
		Random rand = new Random();

	    ArrayList<Point2D> D1 = new ArrayList();
	    ArrayList<Point2D> D2 = new ArrayList();
	    ArrayList<Point2D> D3 = new ArrayList();
	    ArrayList<Point2D> D = new ArrayList();
	    ArrayList<Point2D> road;
	    ArrayList<Point2D> wpRoad;
	    ArrayList<Point2D> smRoad;
	    
	    FileWriter csvroad = null;
	    
	    try {
			csvroad = new FileWriter("d:\\iso\\csvroads");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		for(int q = 0; q < numRoads; q++) {    
			    D1.clear();
			    D2.clear();
			    D3.clear();
			    D.clear();
			    
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
			    
			    D = myRotateRoad(D, roadCentre.get(q));

			    ArrayList<Point2D> pts = new ArrayList();
				for(Point2D p : D) {
					if(exist(pts, p) >= 0) continue;
					else pts.add(p);
				}
				
				System.out.println("num of distinct points in D="+pts.size());
				
			    for(int i=0; i<D.size(); i++)
					try {
						csvroad.write(String.format("%f,%f\n", D.get(i).getX(), D.get(i).getY()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    	
			    numD = D1.size();

			    road = new ArrayList<Point2D>();
			    road.addAll(D.subList(0, numD));
			    for(int i = 2*numD-1; i >= numD; i--)
			    	road.add(D.get(i));
			    road.add(D.get(0));
			    
			    roads.add(road);

			    wpRoad = new ArrayList<Point2D>();
			    wpRoad.addAll(D.subList(2*numD, D.size()));
			    
			    wpRoads.add(wpRoad);
			    
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
		
		try {
			csvroad.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<Point2D> pts = new ArrayList();
		for(ArrayList<Point2D> ps : roads) {
			for(Point2D p: ps) {
				if(exist(pts, p) >= 0) continue;
				else pts.add(p);
			}
		}
		
		System.out.println("num of distinct points in roads="+pts.size());
		
		pts = new ArrayList();
		for(ArrayList<Point2D> ps : wpRoads) {
			for(Point2D p: ps) {
				if(exist(pts, p) >= 0) continue;
				else pts.add(p);
			}
		}
		
		System.out.println("num of distinct points in wpRoads="+pts.size());
	}
	
	public ArrayList<Point2D> myRotateRoad(ArrayList<Point2D> D, double roadCenter) {
		//Point2D center = Point2D.centroid(D);
		double x = D.get(0).getX();
		double y = D.get(0).getY();
		double minx = x;
		double maxx = x;
		double miny = y;
		double maxy = y;
		
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
		
		ArrayList<Point2D> D1 = new ArrayList();
		
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
		this.createDXFile(fname, choice, v, c, ind, heights, xMargin, yMargin);
	}
	
	public void createDXFile(String fname, int choice,
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
			    	fw.write(String.format("%.1f\n",k));
			    	fw.write(String.format("%3s\n","20"));
			    	fw.write(String.format("%.1f\n",l));
			    	fw.write(String.format("%3s\n","30"));
			    	fw.write(String.format("%.1f\n",0));
			    }
		
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String readFile(String fn) {
			StringBuilder sb = null;
			char[] cbuf = new char[1024];
			int i;
			
			try {
				FileReader in = new FileReader(fn);
				while((i = in.read(cbuf)) != -1)
					sb.append(cbuf, 0, i);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return sb.toString();
	}
	
	public Polygon2D polyxpoly(DenseMatrix64F vertices,
			ArrayList<Integer> facet, ArrayList<Point2D> road) {
		SimplePolygon2D p1 = new SimplePolygon2D();
		SimplePolygon2D p2 = new SimplePolygon2D();
		Polygon2D res = new MultiPolygon2D();
		
		for(int i: facet)
			p1.addVertex(new Point2D(vertices.get(i, 0), vertices.get(i, 1)));
		
		for(int i=0; i<road.size(); i++)
			p1.addVertex(new Point2D(road.get(i).getX(), road.get(i).getY()));
		
		res = Polygons2D.intersection(p1, p2);
		
		return res;
	}
	
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
		ArrayList<Point2D> pnts = new ArrayList();
		
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
		SimplePolygon2D poly = new SimplePolygon2D(polygon);
		
		return poly.contains(x, y);
	}
	
	public ArrayList<Coordinate> toCoords(float[][] pts) {
		ArrayList<Coordinate> coords = new ArrayList();
		for(int i=0; i<pts.length; i++)
			coords.add(new Coordinate(pts[i][0], pts[i][1]));
		
		return coords;
	}
	
	public DenseMatrix64F getVC(GeometryCollection polygons) {
		int numPolygons = polygons.getNumGeometries();
		int total = 0;
		
		for(int i=0; i<numPolygons; i++)
			total += polygons.getGeometryN(i).getNumPoints();
		
		ArrayList<Point2D> totalp = new ArrayList();
		
		for(int i=0; i<numPolygons; i++) {
			ArrayList<Integer> facet = new ArrayList();
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
	
	private int exist(ArrayList<Point2D> pts, float[] coord) {
		for(int i=0; i<pts.size(); i++) {
			double x = coord[0];
			double y = coord[1];
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
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
