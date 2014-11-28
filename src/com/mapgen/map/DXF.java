package com.mapgen.map;

import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DXF {
	public static final String DXF_header_fn = "header.dxf";
	public static final String facet_header_fn = "1stSet.txt";
	
	private String DXF_header = null;
	private String facet_header = null;
	
	public DXF() {
	}
	
	//create dxf from nodes data reflecting user adjusting
	void createDXFile(String fname, int choice,
			ArrayList<Polygon> builds,
			ArrayList<Double> heights,
			double xMargin,
			double yMargin) {
		
		try {
			FileWriter fw = new FileWriter(fname);
			
			if(DXF_header == null) DXF_header = readFile(DXF_header_fn);
			if(facet_header == null) facet_header = readFile(facet_header_fn);
			
			fw.write(DXF_header);
			fw.write("\n");
			
			for(int i = 0; i < builds.size(); i++) {
			    Polygon build = builds.get(i);
			    
			    fw.write(facet_header);
				fw.write("\n");
				
				double height = heights.get(i);
				
				for(int j = 0; j < build.npoints; j++) {
					double x = build.xpoints[j];
					double y = build.ypoints[j];
			        if ( j != build.npoints-1 ) {
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
				InputStream is=MapGenData.class.getClassLoader().getResourceAsStream("res/"+fn);
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
}
