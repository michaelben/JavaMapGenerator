package com.mapgen.map;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.Rectangle2D;
import math.geom2d.polygon.SimplePolygon2D;

import com.mapgen.GraphPanel;
import com.mapgen.MapGen;
import com.mapgen.Node;
import com.mapgen.Param;
import com.mapgen.util.Range;
import com.mapgen.util.Utils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

/* This class is converted from matlab code.
 * 
 * JTS is used for voronoi diagram generation.
   JavaGeom is used for polygon intersection and polygon area calculation.(there is no area calculation in Java2D library!)
   
   It might possibly remove dependency on JavaGeom libraries, and rely on JTS only.
   */
public class MapGenData {
	private boolean isUrban = true;	//urban map or suburban map
	
	private double xMargin;			//map x-axis length
	private double yMargin;			//map y-axis length
	private double roadWidth;		//average street width
	private double roadStd;			//street width divergence
	private double heightMean;		//average building height
	private double heightStd;		//building heigth divergence
	private double dist;			//building facade length
	private double densityFactor;	//roads and building density factor
	
	private int numRoads;			//number of roads to be generated
	private RoadFactory roadFactory;
	private ArrayList<ArrayList<Point2D>> roads;		//roads to be generated
	private ArrayList<ArrayList<Point2D>> wpRoads;		//roads center line
	private ArrayList<ArrayList<Point2D>> smRoads;		//small roads between roads and roads center line
	
	private ArrayList<Point2D> uniqueRoads;				//unique set of points for roads
	private ArrayList<Point2D> uniqueWpRoads;			//unique set of points for wpRoads
	
	//randomly generated points for voro_Points
	private ArrayList<Point2D> randomPoints;
	
	//randomPoints minus those points falling inside roads
	private ArrayList<Point2D> validRandomPoints;
	
	//input points feeding into voronoi routine, composed of validRandomPoints, uniqueRoads and uniqueWpRoads
	private ArrayList<Point2D> voronoiPoints;
	
	//result from voronoi diagram generation. all unique vertices for all facets comprised of voronoi diagram
	private ArrayList<Point2D> voronoiDiagramVertices;
	
	//result from voronoi diagram generation. all facets comprised of voronoi diagram
	//each facet contains a set of vertices pointing to the index of voronoiDiagramVertices
	private ArrayList<ArrayList<Integer>> voronoiDiagramFacets = new ArrayList<>();
	
	//indices into voronoiDiagramFacets for valid facets
	private HashSet<Integer> validFacetsIndices;
	
	//arraylist of validFacetsIndices set
	private ArrayList<Integer> validFacetsIndexlist;
	
	//building heights
	private ArrayList<Double> heights;

	//nodes indices for all builds(facets)
	private ArrayList<ArrayList<Integer>> nodeIndices;
	
	//valid builds to be generated
	private ArrayList<Polygon> builds;
	
	private MapGen mapgen;
	private GraphPanel graphPanel;

	private DXF dxf;
	
	public MapGenData(MapGen mapgen) {
		this.mapgen = mapgen;
		this.graphPanel = mapgen.getMapPanel().getGraphPanel();
		this.builds = graphPanel.getBuilds();
		
		initParam();
		
		roadFactory = new RoadFactory(roadWidth, roadStd, 0, xMargin, yMargin, dist);
		dxf = new DXF();
	}

	private void initParam() {
		xMargin = Param.getMapWidth();
		yMargin = Param.getMapHeight(); 
		roadWidth = Param.getAvgStreetWidth();
		roadStd = Param.getStreetWidthStd();
		heightMean = Param.getAvgBuildingHeight();
		heightStd = Param.getBuildingHeightStd(); 
		dist = Param.getBuildingFacadeLength();
		densityFactor = Param.getDensityFactor();
		
		builds = mapgen.getMapPanel().getGraphPanel().getBuilds();
				
		//reset collections
		if(voronoiPoints != null) voronoiPoints.clear();
		if(validFacetsIndices != null) validFacetsIndices.clear();
		if(validFacetsIndexlist != null) validFacetsIndexlist.clear();
		if(heights != null) heights.clear();
		if(builds != null) builds.clear();
	}
	
	public void generateMap() {
		initParam();

		//number of roads to be generated
		numRoads = (int)(Math.floor((xMargin + yMargin)/200) * densityFactor/50);

		//generated random roads into roads, wpRoads and smRoads
		roadFactory.setParam(roadWidth, roadStd, numRoads, xMargin, yMargin, dist);		
		roadFactory.generateRoads();		
		roads = roadFactory.getRoads();
		wpRoads = roadFactory.getWpRoads();
		smRoads = roadFactory.getSmRoads();

		//pick random points for vonoroi routine
		int pointsDensity = (int)(Math.floor((xMargin + yMargin)/8) * densityFactor/50);
		randomPoints = Utils.generateRandomPoints(pointsDensity, new Range(0, 0, xMargin, yMargin));

		//get all unique points from roads
		HashSet<Point2D> pset = new HashSet<>();
		for(ArrayList<Point2D> ps : roads)
			for(Point2D p: ps)
				pset.add(p);
		uniqueRoads = new ArrayList<>(pset);
	    
		//get all unique points from wpRoads
		pset.clear();
		for(ArrayList<Point2D> ps : wpRoads)
			for(Point2D p: ps)
				pset.add(p);
		uniqueWpRoads = new ArrayList<>(pset);
	    
		//checking if the randomly picked points fall into the roads 
		//in order to find and render only buildings that do not fall into roads
	    // TODO: Optimization. This is an expensive operation. for 3000x2000 map area, it takes ~2 seconds on (2.5GCPU 8GRAM)
		validRandomPoints = getValidRandomPoints(randomPoints, roads);

		//voronoi points composed of validRandomPoints, unique roads points and unique wpRoads points
		voronoiPoints = new ArrayList<>();
		for(int i=0; i<validRandomPoints.size(); i++)
			voronoiPoints.add(validRandomPoints.get(i));

		for(int i=0; i<uniqueRoads.size(); i++)
			voronoiPoints.add(uniqueRoads.get(i));
		
		for(int i=0; i<uniqueWpRoads.size(); i++)
			voronoiPoints.add(uniqueWpRoads.get(i));
	    
		//discard all points falling out of the map area
        ArrayList<Point2D> invalid = new ArrayList<>();
        for(Point2D p:voronoiPoints) {
        	if(p.getX() <= 0 || p.getX() >= xMargin || p.getY() <= 0 || p.getY() >= yMargin)
        		invalid.add(p);
        }
	    
        //we can comment out the following 2 lines because JTS voronoi routine can accept duplicate points and negative values
        voronoiPoints.removeAll(invalid);
        // TODO: Optimization. This is an expensive operation. for 3000x2000 map area, it takes ~2 seconds on (2.5GCPU 8GRAM)
		//removeTooClose(voronoiPoints);
	    
        getVoronoiDiagram(voronoiPoints);
	    
        //for some vonoroi routine(matlab), the order of faces returned from vonoroi routine is the same as the order of input points.
        //but for JTS, the order of faces returned from vonoroi routine is not the same as the order of input points.
        //we need to make them same in order to group them into validRandomPoints, roads and wpRoads for further processing.
	    // TODO: Optimization. This is an expensive operation. for 3000x2000 map area, it takes 1-2 seconds on (2.5GCPU 8GRAM)
        ArrayList<ArrayList<Integer>> sameOrder = new ArrayList<>();
        for(Point2D p : voronoiPoints) {
	        for(ArrayList<Integer> face: voronoiDiagramFacets) {
	        	if(inpolygon(p, face, voronoiDiagramVertices)) {
	        		sameOrder.add(face);
	        		break;
	        	}
	        }
        }
		
        voronoiDiagramFacets = sameOrder;
		
		int numFacets;		//the number of facets to be processed
		if(isUrban)
			numFacets = validRandomPoints.size();
		else
			numFacets = voronoiDiagramFacets.size();
		
		HashSet<Integer> invalidFacetsRoads = new HashSet<>();		//indices for those invalid facets crossing over roads
		HashSet<Integer> invalidFacetsSmRoads = new HashSet<>();	//indices for those invalid facets near main Roads crossing over smRoads
		validFacetsIndices = new HashSet<>();
		
		//close open polygons and discard everything that may be outside the map area
		for (int i = 0; i<numFacets; i++) {
			ArrayList<Integer> temp = voronoiDiagramFacets.get(i);
		    if (temp.get(0).intValue() != temp.get(temp.size() - 1).intValue())
		        voronoiDiagramFacets.get(i).add(temp.get(0));  // closing polygons
		}
		
		int numPoints;	//valid points to be processed
		if(isUrban)
			numPoints = validRandomPoints.size()+uniqueRoads.size();
		else
			numPoints = numFacets;
		
		//get all valid facets inside the map area
		Rectangle2D rect = new Rectangle2D(0, 0, xMargin, yMargin);
		for(int i = 0; i < numPoints; i++) {
			if(i>=voronoiDiagramFacets.size()) break;
			
			ArrayList<Integer> facet = voronoiDiagramFacets.get(i);
			int j;
			for(j = 0; j < facet.size(); j++)
				if(!rect.contains(voronoiDiagramVertices.get(facet.get(j))))
					break;
			
		    if(j == facet.size()) validFacetsIndices.add(i);
		}
		
		//find those invalid facets crossing over roads
	    // TODO: Optimization. This is an expensive operation. for 3000x2000 map area, it takes 1-2 seconds on (2.5GCPU 8GRAM)
		for(int i = 0; i < numFacets; i++) {    
			    // checking if this building intersects one of roads
			    for(int k = 0; k < roads.size(); k++) {
			    	 Polygon2D p = polyxpoly(voronoiDiagramVertices, voronoiDiagramFacets.get(i), roads.get(k));
			    	 if(p != null && !p.isEmpty()) {
			            invalidFacetsRoads.add(i);
			            break;
			    	 }
			    }
		}

		// we check if the buildings located close to the main road (Road) fall into the smaller road (smRoad)
	    // TODO: Optimization. This is an expensive operation. for 3000x2000 map area, it takes 4-7 seconds on (2.5GCPU 8GRAM)
		for(int i = validRandomPoints.size(); i < validRandomPoints.size()+uniqueRoads.size(); i++) {
			if(i>=voronoiDiagramFacets.size()) break;
			
		    if(voronoiDiagramFacets.get(i).size() != 0) {
				ArrayList<Integer> temp = voronoiDiagramFacets.get(i);
		    	if (temp.get(0).intValue() != temp.get(temp.size() - 1).intValue())
		        voronoiDiagramFacets.get(i).add(temp.get(0));  // closing polygons
		        
		        for(int k = 0; k < smRoads.size(); k++) {  
		            Polygon2D p = polyxpoly(voronoiDiagramVertices, voronoiDiagramFacets.get(i), smRoads.get(k));
		            if(p != null && !p.isEmpty()) {
		                invalidFacetsSmRoads.add(i);
		                break;
		            }
		        }
		    }	
		}
		
		invalidFacetsRoads.addAll(invalidFacetsSmRoads);
		
		validFacetsIndices.removeAll(invalidFacetsRoads);
		
		//Now all valid buildings are voronoiDiagramFacets.get(ind.get(validFacetsIndices))
		//which are a set of indices into voronoiDiagramVertices
		
		//calculate the area of each building
		ArrayList<Double> buildArea = new ArrayList<>();
		validFacetsIndexlist = new ArrayList<>(validFacetsIndices);
		for(int i = 0; i < validFacetsIndexlist.size(); i++) {
			ArrayList<Integer> polyindex = voronoiDiagramFacets.get(validFacetsIndexlist.get(i));
			SimplePolygon2D poly = new SimplePolygon2D();
			for(int j : polyindex)
				poly.addVertex(voronoiDiagramVertices.get(j));
			
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
		for(int i = 0; i < validFacetsIndexlist.size(); i++) {
			if((buildArea.get(i) > percent1*areaMax)
				|| (buildArea.get(i) < percent2*areaMin))
				rem.add(validFacetsIndexlist.get(i));
		}
		
		validFacetsIndices.removeAll(rem);
		
		//we create a database of variable heights 
		//and we assign a height to each building
		heights = new ArrayList<>();
		Random rand = new Random();
		validFacetsIndexlist = new ArrayList<>(validFacetsIndices);
		for(int i = 0; i < validFacetsIndexlist.size(); i++) {
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

	
	//construct builds(facets) and nodes(vertices) to be rendered on screen.
	//construct topology information for deletion operation and other operation such as undo/redo
	private void createBuild() {
		builds.clear();
	    
		int removed = 0;
		
		//create builds for draw
	    for(int i=0; i<validFacetsIndexlist.size(); i++) {
			Polygon p = new Polygon();
			ArrayList<Integer> pind = voronoiDiagramFacets.get(validFacetsIndexlist.get(i));
			for(int j=0; j<pind.size(); j++)
				p.addPoint((int)voronoiDiagramVertices.get(pind.get(j)).getX(), (int)voronoiDiagramVertices.get(pind.get(j)).getY());
			
			//eliminates triangular and degenerate cases
			if(p.npoints > 4)	//it is 4 because we repeat the last point with the initial point
				builds.add(p);
			else removed++;
		}
	    
	    heights.subList(0, removed).clear();
	    
	    //generate unique valid points only, and topology information and their indices for drawing
	    ArrayList<Node> nodeset = new ArrayList<>();
	    nodeIndices = new ArrayList<>();
	    for(Polygon p : builds) {
	    	ArrayList<Integer> facetIndices = new ArrayList<>();
	    	for(int i=0; i<p.npoints; i++) {
	    		Node n = new Node(p.xpoints[i], p.ypoints[i]);
	    		int index = addNode(nodeset, n);
	    		
	    		facetIndices.add(index);
	    		
	    		//add into face topology information
	    		nodeset.get(index).adjacentFaces.add(new Node.FaceIndex(p, i));
	    		
	    		//get its prev and next nodes
	    		int prev = i - 1;
	    		int next = i + 1;
	    		if(i==0 || i==p.npoints-1) {
	    			prev = p.npoints - 2;
	    			next = 1;
	    		}
	    		
	    		//add into edge topology information
	    		Node prevNode = new Node(p.xpoints[prev], p.ypoints[prev]);
	    		int prevIndex = addNode(nodeset, prevNode);
	    		nodeset.get(index).adjacentNodes.add(nodeset.get(prevIndex));
	    		
	    		Node nextNode = new Node(p.xpoints[next], p.ypoints[next]);
	    		int nextIndex = addNode(nodeset, nextNode);
	    		nodeset.get(index).adjacentNodes.add(nodeset.get(nextIndex));
	    	}
	    	nodeIndices.add(facetIndices);
	    }
	    
	    graphPanel.setNodes(nodeset);
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
	
	//find intersections between facet and road
	private Polygon2D polyxpoly(ArrayList<Point2D> vertices,
			ArrayList<Integer> facet, ArrayList<Point2D> road) {
		SimplePolygon2D p1 = new SimplePolygon2D();
		SimplePolygon2D p2 = new SimplePolygon2D();
		
		for(int index : facet)
			p1.addVertex(vertices.get(index));
		
		for(Point2D p : road)
			p2.addVertex(p);
		
		return Polygons2D.intersection(p1, p2);
	}
	
	private boolean inpolygon(Point2D p, ArrayList<Integer> face, ArrayList<Point2D> voronoiDiagramVertices) {
		//we cannot use javaGeom polygon contains method because it buggy cannot handle degenerated line2D.
		//Since there is no Polygon2D in current Java2D implementation, we have to use either Polygon or Path2D.Double to handle
		//double precision values. We have to be precise here because the result are from voronoi call,
		//but we can use Polygon for screen rendering.
		/*
		Path2D poly = new Path2D.Double();
		poly.moveTo(voronoiDiagramVertices.get(0).getX(), voronoiDiagramVertices.get(0).getY());
		for(int i = 1; i < face.size(); i++)
			poly.lineTo(voronoiDiagramVertices.get(face.get(i)).getX(), voronoiDiagramVertices.get(face.get(i)).getY());
		poly.closePath();
		*/
		
		//java.awt.geom.Polygon.contains is faster than Path2D.Double.contains
		Polygon poly = new Polygon();
		for(int index : face)
			poly.addPoint((int)voronoiDiagramVertices.get(index).getX(), (int)voronoiDiagramVertices.get(index).getY());

		return poly.contains(p.getX(), p.getY());
	}
	
	//find all points fall inside of roads and discard them, return the rest points
	private ArrayList<Point2D> getValidRandomPoints(ArrayList<Point2D> randomPoints, ArrayList<ArrayList<Point2D>> roads) {
		HashSet<Point2D> invalid = new HashSet<>();
		
		for(Point2D p : randomPoints)
			for(ArrayList<Point2D> road : roads) {
				/*
				SimplePolygon2D poly = new SimplePolygon2D(road);
				if(poly.contains(p)) {
					invalid.add(p);
					break;
				}*/
				//java.awt.geom.Polygon.contains is faster than SimplePolygon2D.contains
				Polygon poly = new Polygon();
				for(Point2D pt : road)
					poly.addPoint((int)pt.getX(), (int)pt.getY());
				
				if(poly.contains(p.getX(), p.getY())) {
					invalid.add(p);
					break;
				}
			}
		
		randomPoints.removeAll(invalid);
		
		return randomPoints;
	}
	
	private ArrayList<Coordinate> toCoords(ArrayList<Point2D> pts) {
		ArrayList<Coordinate> coords = new ArrayList<>();
		for(int i=0; i<pts.size(); i++)
			coords.add(new Coordinate(pts.get(i).getX(), pts.get(i).getY()));
		
		return coords;
	}
	
	//get voronoiDiagramVertices and voronoiDiagramFacets from result of JTS voronoi routine call
	private void getVoronoiDiagram(ArrayList<Point2D> voronoiPoints) {
        //call vonoroi routine using JTS library
        //Note: depending on vonoroi implementation, some (such as matlab) can accept duplicate points and negative values.
        //some (such as matlab) can output voronoiDiagramVertices and voronoiDiagramFacets directly
		VoronoiDiagramBuilder vb = new VoronoiDiagramBuilder();
		vb.setTolerance(2.0);		//snapping factor to improve algorithm robustness
		vb.setSites(toCoords(voronoiPoints));
		GeometryCollection polygons = (GeometryCollection) vb.getDiagram(new GeometryFactory());

		//JTS vonoroi routine does not return voronoiDiagramVertices and voronoiDiagramFacets directly, so we have to get them manually
		voronoiDiagramFacets.clear();
        
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
			
			voronoiDiagramFacets.add(facet);
		}
		
		voronoiDiagramVertices = totalp;
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
	
	//for testing
	public void readCsv(String fn) {
		try {
			String line = null;
			builds.clear();
			Polygon poly = new Polygon();
			
			BufferedReader r = new BufferedReader(new FileReader(fn));
			while((line = r.readLine()) != null) {
				String[] result = line.split("[,\\s]");
				if(result.length == 1) {
					builds.add(poly);
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
	
	public boolean getIsUrban() {
		return isUrban;
	}
	
	public void setIsUrban(boolean isUrban) {
		this.isUrban = isUrban;
	}
	
	public void createDXFile(String fname, int choice) {
		if(builds != null)
			dxf.createDXFile(fname, choice, builds, heights, xMargin, yMargin);
	}
}
