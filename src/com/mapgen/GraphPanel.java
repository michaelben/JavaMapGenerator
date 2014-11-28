package com.mapgen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class GraphPanel extends JPanel implements Scrollable {
	public static final double TOLERANCE = 10;
    
    public static final Color fixedColor = Color.red;
    public static final Color selectColor = Color.pink;
    public static final Color edgeColor = new Color(160, 160, 160);
    public static final Color fillColor = new Color(200, 200, 200);
    public static final Color nodeColor = new Color(250, 220, 100);
    
    //we don't use Point/Point2D in Java2D because we need topology information stored in vertex.
    //and we use Polygon/Polygon2D in Java2D for easier graphics operation, draw/fill etc.
	public boolean isFill = true;
	public ArrayList<Node> nodes = new ArrayList<>();
    public ArrayList<Edge> edges;
	public ArrayList<Polygon> builds = new ArrayList<>();		//valid builds
    
    private Node pick;
    private Polygon polygonPick;
    private boolean pickfixed;
    private Image offscreen;
    private Dimension offscreensize;
    private Graphics offgraphics;
	private int maxUnitIncrement = 1;
    private int numMouseButtonsDown = 0;
    private RenderingHints rh;

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
	                
	                polygonPick = null;
	
	                repaint();
                } else {
                	pick = null;
                	
                	int i;
            		for(i=0; i<builds.size(); i++)
            			if(builds.get(i).contains(e.getX(), e.getY())) {
            				polygonPick = builds.get(i);
            				break;
            			}

            		if(i == builds.size())
            			polygonPick = null;
            		
	                repaint();
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
            
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
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
        
        setRequestFocusEnabled(true);
        addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DELETE) {
					deletePolygon();
					repaint();
				}
				e.consume();
			}


			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == 'd') {
					deletePolygon();
					repaint();
				}
				e.consume();
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
        	
        });
    }
    
    @Override
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
        
        //draw nodes
        offgraphics.setColor(Color.black);
        if(nodes != null)
        	for (Node n : nodes)
        		offgraphics.fillOval((int)n.x-2, (int)n.y-2, 4, 4);
        
        //draw polygon
        if(builds != null) {
        	//update builds info when dragging
        	if(pick != null)
	        	for(Node.FaceIndex faceind : pick.adjacentFaces) {
	        		Polygon face = faceind.face;
	        		int index = faceind.vertexInd;
	        		face.xpoints[index] = (int)pick.x;
	        		face.ypoints[index] = (int)pick.y;
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

        //draw picked point and its adjacent edges
        if(pick != null) {
	        offgraphics.setColor(Color.red);
	        offgraphics.fillOval((int)pick.x-2, (int)pick.y-2, 4, 4);
	        
	        for(Node n : pick.adjacentNodes)
	        	offgraphics.drawLine((int)pick.x, (int)pick.y, (int)n.x, (int)n.y);
        } else {
	        //draw picked polygon
        	if(polygonPick != null) {
		        offgraphics.setColor(Color.red);
				offgraphics.drawPolygon(polygonPick);
        	}
        }
		
        g.drawImage(offscreen, 0, 0, null);
    }

    //delete the picked polygon
    public void deletePolygon() {
    	if(polygonPick == null) return;
    	
    	ArrayList<Node> pnodes = new ArrayList<>();
    	for (int i=0; i<polygonPick.npoints; i++) {
    		Node node = new Node(polygonPick.xpoints[i], polygonPick.ypoints[i]);
    		//we need to get the node object in nodes rather than the newly created node because we need
    		//the topology information contained in nodes.
    		 int index = nodes.indexOf(node);
    		 if(index > 0) pnodes.add(nodes.get(index));
    	}
    	
    	//remove this facet
    	builds.remove(polygonPick);
    	
    	for(Node node : pnodes) {
    		//remove facet associated with this node.
    		//We need to use Iterator because we remove element during iteration.
    		Iterator<Node.FaceIndex> iterFace=node.adjacentFaces.iterator();
    		while(iterFace.hasNext()) {
    			Node.FaceIndex faceInd=(Node.FaceIndex)iterFace.next();
    			if(faceInd.face == polygonPick) {
    				iterFace.remove();
    				break;
    			}
    		}

    		//this node does not have any adjacent faces, remove it and all edges associated with it
    		if(node.adjacentFaces.size() == 0) {
    			nodes.remove(node);
    			
				Iterator<Node> iter=node.adjacentNodes.iterator();
				while(iter.hasNext()) {
				    Node n=(Node)iter.next();
				    iter.remove();
				    n.adjacentNodes.remove(node);
				}
    		}
    		
    		//If one of this node's adjacentNodes does not share any facet with this node,
    		//then remove the edge between the 2 nodes.
    		//We need to use Iterator because we remove element during iteration.
    		Iterator<Node> iter=node.adjacentNodes.iterator();
    		while(iter.hasNext()) {
    			Node n=(Node)iter.next();
    			HashSet<Polygon> np = new HashSet<>();
    			HashSet<Polygon> nodep = new HashSet<>();
    			
    			for(Node.FaceIndex faceInd : n.adjacentFaces)
    				np.add(faceInd.face);
    			
    			for(Node.FaceIndex faceInd : node.adjacentFaces)
    				nodep.add(faceInd.face);
    			
    			np.retainAll(nodep);
    			if(np.isEmpty()) {
    				iter.remove();
    				n.adjacentNodes.remove(node);
    			}
    		}
    	}
    	
    	polygonPick = null;
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Param.getMapWidth(), Param.getMapHeight());
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
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

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,
                                           int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width - maxUnitIncrement;
        } else {
            return visibleRect.height - maxUnitIncrement;
        }
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
    public void setMaxUnitIncrement(int pixels) {
        maxUnitIncrement = pixels;
    }
    
    public boolean getIsFilled() {
    	return isFill;
    }
    
    public void setIsFilled(boolean isFill) {
    	this.isFill = isFill;
    }
    
    public Polygon getPolygonPick() {
    	return this.polygonPick;
    }
    
    public void setPolygonPick(Polygon p) {
    	this.polygonPick = p;
    }
    
    public ArrayList<Polygon> getBuilds() {
    	return this.builds;
    }
    
    public void setBuilds(ArrayList<Polygon> builds) {
    	this.builds = builds;
    }
    
    public ArrayList<Node> getNodes() {
    	return this.nodes;
    }
    
    public void setNodes(ArrayList<Node> nodes) {
    	this.nodes = nodes;
    }
}

