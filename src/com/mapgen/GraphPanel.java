package com.mapgen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import com.mapgen.map.MapGenData;

@SuppressWarnings("serial")
public class GraphPanel extends JPanel implements Scrollable {
	public static boolean isFill = false;
	
	private int maxUnitIncrement = 1;
	
	public static final double TOLERANCE = 20;
	
    public static ArrayList<Node> nodes = new ArrayList<>();
    int nedges;
    Edge edges[] = new Edge[200];
    boolean random;
    int numMouseButtonsDown = 0;
    
    RenderingHints rh;

    GraphPanel(int m) {
    	maxUnitIncrement = m;
    	
    	this.setBackground(Color.white);
    	
    	//high rendering quality
        rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        rh.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        rh.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        rh.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        rh.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //rh.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        //rh.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    	
    	//init();
    	
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                numMouseButtonsDown++;
                double bestdist = Double.MAX_VALUE;

                int x = e.getX();
                int y = e.getY();
                for (int i = 0; i < nodes.size(); i++) {
                    Node n = nodes.get(i);
                    double dist = (n.x - x) * (n.x - x) + (n.y - y) * (n.y - y);
                    if (dist < bestdist) {
                        pick = n;
                        bestdist = dist;
                    }
                }
                
                if(bestdist < TOLERANCE) {
	                pickfixed = pick.fixed;
	                pick.fixed = true;
	                pick.x = x;
	                pick.y = y;
	
	                repaint();
                } else {
                	pick = null;
                }
                
                e.consume();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                numMouseButtonsDown--;

                if(pick != null) {
	                pick.fixed = pickfixed;
	                pick.x = e.getX();
	                pick.y = e.getY();
	                if (numMouseButtonsDown == 0) {
	                    pick = null;
	                }
	                
	
	                repaint();
                }
                
                e.consume();
            }
        });

        addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
            	if(pick != null) {
	                pick.x = e.getX();
	                pick.y = e.getY();
	                repaint();
            	}
                e.consume();
            }
        });
    }

    int findNode(String lbl) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).lbl.equals(lbl)) {
                return i;
            }
        }
        return addNode(lbl);
    }

    int addNode(String lbl) {
        Node n = new Node();
        n.x = 10 + 380 * Math.random();
        n.y = 10 + 380 * Math.random();
        n.lbl = lbl;
        nodes.add(n);
        return nodes.size();
    }

    void addEdge(String from, String to, int len) {
        Edge e = new Edge();
        e.from = findNode(from);
        e.to = findNode(to);
        e.len = len;
        edges[nedges++] = e;
    }
    
    Node pick;
    boolean pickfixed;
    Image offscreen;
    Dimension offscreensize;
    Graphics offgraphics;
    final Color fixedColor = Color.red;
    final Color selectColor = Color.pink;
    //final Color edgeColor = Color.gray;
    final Color edgeColor = new Color(160, 160, 160);
    final Color fillColor = new Color(200, 200, 200);
    final Color nodeColor = new Color(250, 220, 100);
    final Color stressColor = Color.black;
    final Color arcColor1 = Color.black;
    final Color arcColor2 = Color.pink;
    final Color arcColor3 = Color.red;

    public void paintNode(Graphics g, Node n, FontMetrics fm) {
        int x = (int) n.x;
        int y = (int) n.y;
        g.setColor((n == pick) ? selectColor
                : (n.fixed ? fixedColor : nodeColor));
        int w = fm.stringWidth(n.lbl) + 10;
        int h = fm.getHeight() + 4;
        g.fillRect(x - w / 2, y - h / 2, w, h);
        g.setColor(Color.black);
        g.drawRect(x - w / 2, y - h / 2, w - 1, h - 1);
        g.drawString(n.lbl, x - (w - 10) / 2, (y - (h - 4) / 2) + fm.getAscent());
    }

    @Override
    //public synchronized void update(Graphics g) {
    public void paintComponent(Graphics g) {
    	Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHints(rh);
       
        Dimension d = getSize();
        if ((offscreen == null) || (d.width != offscreensize.width)
                || (d.height != offscreensize.height)) {
            offscreen = createImage(d.width, d.height);
            offscreensize = d;
            if (offgraphics != null) {
                offgraphics.dispose();
            }
            offgraphics = offscreen.getGraphics();
            offgraphics.setFont(getFont());
        }

        //((Graphics2D)offgraphics).translate(0, d.height);
        //((Graphics2D)offgraphics).scale(1, -1);
		
        offgraphics.setColor(getBackground());
        offgraphics.fillRect(0, 0, d.width, d.height);
        
        /*
        offgraphics.setColor(Color.red);
        for(Polygon p: MapGenData.builds2)
        	offgraphics.drawPolygon(p);
        */
        
        //draw nodes
        offgraphics.setColor(Color.black);
        if(nodes != null)
        	for (Node n : nodes)
        		offgraphics.fillOval((int)n.x-2, (int)n.y-2, 4, 4);
        
        //draw polygon
        ArrayList<ArrayList<Integer>> nodeIndices = MapGenData.nodeIndices;
        ArrayList<Polygon> builds = MapGenData.builds;
        
        if(nodeIndices != null) {
        	builds.clear();
	        for(int i=0; i<nodeIndices.size(); i++) {
				ArrayList<Integer> pind = nodeIndices.get(i);
				Polygon p = new Polygon();
				for(int j=0; j<pind.size(); j++)
					p.addPoint((int)nodes.get(pind.get(j)).x, (int)nodes.get(pind.get(j)).y);
				
				builds.add(p);
			}

	        for(Polygon p: builds) {
	        	if(isFill) {
	        		offgraphics.setColor(fillColor);
	        		offgraphics.fillPolygon(p);
	        	}
	        	
        		offgraphics.setColor(edgeColor);
        		offgraphics.drawPolygon(p);
	        }
        }

        g.drawImage(offscreen, 0, 0, null);
    }

    public void init() {

        //String edges = "a1-a2,a2-a3,a3-a4,a4-a5,a5-a6,b1-b2,b2-b3,b3-b4,b4-b5,b5-b6,c1-c2,c2-c3,c3-c4,c4-c5,c5-c6,x-a1,x-b1,x-c1,x-a6,x-b6,x-c6";
        String edges = "zero-one,zero-two,zero-three,zero-four,zero-five,zero-six,zero-seven,zero-eight,zero-nine,one-ten,two-twenty,three-thirty,four-fourty,five-fifty,six-sixty,seven-seventy,eight-eighty,nine-ninety,ten-twenty/80,twenty-thirty/80,thirty-fourty/80,fourty-fifty/80,fifty-sixty/80,sixty-seventy/80,seventy-eighty/80,eighty-ninety/80,ninety-ten/80,one-two/30,two-three/30,three-four/30,four-five/30,five-six/30,six-seven/30,seven-eight/30,eight-nine/30,nine-one/30";
        for (StringTokenizer t = new StringTokenizer(edges, ","); t.
                hasMoreTokens();) {
            String str = t.nextToken();
            int i = str.indexOf('-');
            if (i > 0) {
                int len = 50;
                int j = str.indexOf('/');
                if (j > 0) {
                    len = Integer.valueOf(str.substring(j + 1)).intValue();
                    str = str.substring(0, j);
                }
                this.addEdge(str.substring(0, i), str.substring(i + 1), len);
            }
        }
        
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Param.getMapWidth(), Param.getMapHeight());
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction) {
        //Get the current position.
        int currentPosition = 0;
        if (orientation == SwingConstants.HORIZONTAL) {
            currentPosition = visibleRect.x;
        } else {
            currentPosition = visibleRect.y;
        }

        //Return the number of pixels between currentPosition
        //and the nearest tick mark in the indicated direction.
        if (direction < 0) {
            int newPosition = currentPosition -
                             (currentPosition / maxUnitIncrement)
                              * maxUnitIncrement;
            return (newPosition == 0) ? maxUnitIncrement : newPosition;
        } else {
            return ((currentPosition / maxUnitIncrement) + 1)
                   * maxUnitIncrement
                   - currentPosition;
        }
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,
                                           int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width - maxUnitIncrement;
        } else {
            return visibleRect.height - maxUnitIncrement;
        }
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
    public void setMaxUnitIncrement(int pixels) {
        maxUnitIncrement = pixels;
    }
}

