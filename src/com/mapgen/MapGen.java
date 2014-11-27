package com.mapgen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mapgen.map.MapGenData;

public class MapGen {

	//public static final int WINDOW_WIDTH = 1000;
	//public static final int WINDOW_HEIGTH = 800;
	public static final int CONTROL_WIDTH = 320;
	
	//Toolkit.getScreenSize();	screensize including system tray
	
	//get screen size excluding system tray
	public static Rectangle desktopBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    final JButton undoButton = new JButton("Undo");
    final JButton redoButton = new JButton("Redo");
    final JButton deleteButton = new JButton("Delete");
    final JToggleButton urbanToggleButton = new JToggleButton("Urban");
    static final JToggleButton filledToggleButton = new JToggleButton("Unfilled");
	//public static final int MAP_WIDTH = WINDOW_WIDTH - CONTROL_WIDTH;
	
	private JFrame frame;
	private final JPanel ctlPanel = new JPanel();
	private final ScrollGraphPanel mapPanel = new ScrollGraphPanel();
	private final JTextField dxfInputField = new JTextField();
	private final JButton outputDXFButton = new JButton("Output DXF");
	private final JPanel paramPanel = new JPanel();
	private final JPanel dxfPanel = new JPanel();
	private final JButton resetButton = new JButton("Reset");
	private final JButton genmapButton = new JButton("Generate Map");
	private final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
	JFileChooser jfc = new JFileChooser();
	
	ArrayList<NumberPicker> paramsUI = new ArrayList<NumberPicker>();
	
	public MapGenData mapData = new MapGenData();
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MapGen window = new MapGen();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MapGen() {
		for(Param param : Param.params)
			paramsUI.add(new NumberPicker(param.label, param.max, param.min, param.def, param.step));

		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		/*
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
            e.printStackTrace();
        }
        */		
		
		frame = new JFrame();
		frame.setTitle("Auto Map Generator");
		//frame.setBounds(10, 10, WINDOW_WIDTH, WINDOW_HEIGTH);
		//frame.setMaximizedBounds(bounds);
		frame.setBounds(desktopBounds);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int i = 0; i < Param.params.length; i++) {
					Param param = Param.params[i];
					NumberPicker np = paramsUI.get(i);
					np.slider.setMaximum(param.max);
					np.slider.setMinimum(param.min);
					np.slider.setValue(param.def);
				}
			}
		});
		
        undoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GraphPanel gp = mapPanel.graphPanel;
                ArrayDeque<HashMap<String, Object>> undoStack = gp.undo;
                ArrayDeque<HashMap<String, Object>> redoStack = gp.redo;
                if (!undoStack.isEmpty()) {
                    HashMap<String, Object> action = undoStack.pop();
                    String type = (String)action.get("actionType");
                    if("vertexDrag".equalsIgnoreCase(type)) {
	                    Node node = (Node)action.get("node");
	                    int oldx = ((Integer)action.get("oldx")).intValue();
	                    int oldy = ((Integer)action.get("oldy")).intValue();
	                    action.put("oldx", (int)node.x);
	                    action.put("oldy", (int)node.y);
	                    redoStack.push(action);
	                    node.x = oldx;
	                    node.y = oldy;
	                    
	    	        	for(Node.FaceIndex faceind : node.adjacentFaces) {
	    	        		Polygon face = faceind.face;
	    	        		int index = faceind.vertexInd;
	    	        		face.xpoints[index] = (int)node.x;
	    	        		face.ypoints[index] = (int)node.y;
	    	        	}
	                    
	                    gp.repaint();
                    } else if("delete".equalsIgnoreCase(type)) {
                    	Polygon polygon = (Polygon)action.get("polygon");
                    	@SuppressWarnings("unchecked")
						ArrayList<Node> pnodes = (ArrayList<Node>)action.get("pnodes");
                    	redoStack.push(action);
                    	gp.unDeletePolygon(polygon, pnodes);
                    	
                    	gp.repaint();
                    }
                }
            }
        });

        redoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GraphPanel gp = mapPanel.graphPanel;
                ArrayDeque<HashMap<String, Object>> undoStack = gp.undo;
                ArrayDeque<HashMap<String, Object>> redoStack = gp.redo;
                if (!redoStack.isEmpty()) {
                	HashMap<String, Object> action = redoStack.pop();
                	String type = (String)action.get("actionType");
                    if("vertexDrag".equalsIgnoreCase(type)) {
                    	Node node = (Node)action.get("node");
	                    int oldx = ((Integer)action.get("oldx")).intValue();
	                    int oldy = ((Integer)action.get("oldy")).intValue();
	                    action.put("oldx", (int)node.x);
	                    action.put("oldy", (int)node.y);
	                    undoStack.push(action);
	                    node.x = oldx;
	                    node.y = oldy;
	                    
	    	        	for(Node.FaceIndex faceind : node.adjacentFaces) {
	    	        		Polygon face = faceind.face;
	    	        		int index = faceind.vertexInd;
	    	        		face.xpoints[index] = (int)node.x;
	    	        		face.ypoints[index] = (int)node.y;
	    	        	}
                    	
                    	gp.repaint();
                    } else if("delete".equalsIgnoreCase(type)) {
                    	Polygon polygon = (Polygon)action.get("polygon");
                    	@SuppressWarnings("unchecked")
						ArrayList<Node> pnodes = (ArrayList<Node>)action.get("pnodes");
                    	undoStack.push(action);
                    	gp.deletePolygon(polygon, pnodes);
                    	
                    	gp.repaint();
                    }
                }
            }
        });
        
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                GraphPanel gp = mapPanel.graphPanel;
                
                gp.deletePolygon(gp.polygonPick);
				gp.repaint();
            }
        });

        urbanToggleButton.setSelected(true);
        urbanToggleButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                MapGenData.isUrban = e.getStateChange() == ItemEvent.SELECTED;

                if(MapGenData.isUrban)
                	urbanToggleButton.setText("Urban");
                else
                	urbanToggleButton.setText("Suburban");
            }
        });

        filledToggleButton.setSelected(false);
        filledToggleButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                GraphPanel.isFill = e.getStateChange() == ItemEvent.SELECTED;

                if(GraphPanel.isFill)
                	filledToggleButton.setText("Filled");
                else
                	filledToggleButton.setText("Unfilled");
                
                mapPanel.isMetric.setSelected(GraphPanel.isFill);
                mapPanel.graphPanel.repaint();
            }
        });

        undoButton.setEnabled(false);
		redoButton.setEnabled(false);
		deleteButton.setEnabled(false);
		//filledToggleButton.setEnabled(false);
		outputDXFButton.setEnabled(false);
		
		genmapButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetButton.setEnabled(false);
				urbanToggleButton.setEnabled(false);
				genmapButton.setEnabled(false);
				undoButton.setEnabled(false);
				redoButton.setEnabled(false);
				deleteButton.setEnabled(false);
				filledToggleButton.setEnabled(false);
				outputDXFButton.setEnabled(false);
				
				for(int i = 0; i < Param.params.length; i++) {
					Param param = Param.params[i];
					NumberPicker np = paramsUI.get(i);
					param.value = np.slider.getValue();
				}
				
				mapPanel.setMapSize();
				
				//in rare case where the generateMap failed due to failed voronoi routine call, repeat the process 5 times
				int i=0;
				while(i<5) {
					try{
						i++;
						
						//mapData.readCsv("csv");
						mapData.generateMap();
						break;
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						//e1.printStackTrace();
					}
				}
				
				if(i<5) mapPanel.repaint();
				else JOptionPane.showMessageDialog(frame, "Error in map generation.");
				
				resetButton.setEnabled(true);
				urbanToggleButton.setEnabled(true);
				genmapButton.setEnabled(true);
				undoButton.setEnabled(true);
				redoButton.setEnabled(true);
				deleteButton.setEnabled(true);
				filledToggleButton.setEnabled(true);
				outputDXFButton.setEnabled(true);
			}
		});
		
		outputDXFButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
				        "DXF file", "dxf");
				jfc.setFileFilter(filter);
				int returnVal = jfc.showSaveDialog(frame);
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            String file = null;
					try {
						file = jfc.getSelectedFile().getCanonicalPath();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if(file != null) {
			            dxfInputField.setText(file);   
			            mapData.createDXFile(file, 1);
					}
		        }
			}
		});
		
		dxfInputField.setEditable(false);;
		
		ctlPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		ctlPanel.setPreferredSize(new Dimension(CONTROL_WIDTH, desktopBounds.height));
		frame.getContentPane().add(ctlPanel, BorderLayout.LINE_START);
		
		paramPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		dxfPanel.setMaximumSize(new Dimension(320, 200));
		dxfPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "DXF", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		dxfPanel.setLayout(new BoxLayout(dxfPanel, BoxLayout.PAGE_AXIS));
		JPanel dxfPanelx = new JPanel();
		dxfPanelx.setLayout(new BoxLayout(dxfPanelx, BoxLayout.LINE_AXIS));
		dxfPanelx.add(dxfInputField);
		JPanel dxfPanely = new JPanel();
		dxfPanely.setLayout(new BoxLayout(dxfPanely, BoxLayout.LINE_AXIS));
		dxfPanely.add(outputDXFButton);
		dxfPanel.add(dxfPanelx);
		dxfPanel.add(dxfPanely);
		GroupLayout gl_ctlPanel = new GroupLayout(ctlPanel);
		//gl_ctlPanel.linkSize(SwingConstants.HORIZONTAL, resetButton, urbanToggleButton);
		//gl_ctlPanel.linkSize(SwingConstants.HORIZONTAL, undoButton, redoButton, deleteButton, filledToggleButton);
		gl_ctlPanel.setHorizontalGroup(
			gl_ctlPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_ctlPanel.createSequentialGroup()
					.addGroup(gl_ctlPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(dxfPanel, GroupLayout.PREFERRED_SIZE, 317, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_ctlPanel.createSequentialGroup()
							.addGap(50)
							.addComponent(resetButton)
							.addGap(50)
							.addComponent(urbanToggleButton)
							.addGap(10))
						.addGroup(gl_ctlPanel.createSequentialGroup()
							.addGap(100)
							.addComponent(genmapButton)
							.addGap(10))
						.addComponent(separator)	
						.addGroup(gl_ctlPanel.createSequentialGroup()
							.addGap(10)
                            .addComponent(undoButton)
                            .addGap(10)
                            .addComponent(redoButton)
                            .addGap(10)
                            .addComponent(deleteButton)
                            .addGap(10)
                            .addComponent(filledToggleButton)
                            .addGap(10))
						.addGroup(gl_ctlPanel.createSequentialGroup()
							.addGap(1)
							.addComponent(paramPanel, GroupLayout.PREFERRED_SIZE, 317, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_ctlPanel.setVerticalGroup(
			gl_ctlPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_ctlPanel.createSequentialGroup()
					.addGap(2)
					.addComponent(paramPanel, GroupLayout.PREFERRED_SIZE, 480, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_ctlPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(resetButton)
						.addComponent(urbanToggleButton))
					.addGap(10)
					.addComponent(genmapButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(separator)	
					.addGap(30)
	                .addGroup(gl_ctlPanel.createParallelGroup(Alignment.LEADING)
	                		.addComponent(undoButton)
	                		.addComponent(redoButton)
	                        .addComponent(deleteButton)
	                        .addComponent(filledToggleButton))
	                .addGap(30)
					.addComponent(dxfPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		GroupLayout gl_paramPanel = new GroupLayout(paramPanel);
		gl_paramPanel.setHorizontalGroup(
			gl_paramPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_paramPanel.createSequentialGroup()
					.addComponent(paramsUI.get(2), GroupLayout.PREFERRED_SIZE, 305, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(gl_paramPanel.createSequentialGroup()
					.addComponent(paramsUI.get(3), GroupLayout.PREFERRED_SIZE, 305, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(gl_paramPanel.createSequentialGroup()
					.addComponent(paramsUI.get(4), GroupLayout.PREFERRED_SIZE, 305, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(gl_paramPanel.createSequentialGroup()
					.addComponent(paramsUI.get(5), GroupLayout.PREFERRED_SIZE, 305, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(gl_paramPanel.createSequentialGroup()
					.addComponent(paramsUI.get(6), GroupLayout.PREFERRED_SIZE, 305, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(gl_paramPanel.createSequentialGroup()
					.addComponent(paramsUI.get(7), GroupLayout.PREFERRED_SIZE, 305, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(gl_paramPanel.createSequentialGroup()
					.addGroup(gl_paramPanel.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(paramsUI.get(0), Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(paramsUI.get(1), Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_paramPanel.setVerticalGroup(
			gl_paramPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_paramPanel.createSequentialGroup()
					.addComponent(paramsUI.get(0), GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(paramsUI.get(1), GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(paramsUI.get(2), GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(paramsUI.get(3), GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(paramsUI.get(4), GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(paramsUI.get(5), GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(paramsUI.get(6), GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(paramsUI.get(7), GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(50, Short.MAX_VALUE))
		);
		paramPanel.setLayout(gl_paramPanel);
		ctlPanel.setLayout(gl_ctlPanel);
		
		mapPanel.setOpaque(true); //content panes must be opaque
		frame.getContentPane().add(mapPanel, BorderLayout.CENTER);
		
		/*
        // See http://stackoverflow.com/a/7143398/418556 for demo.
        frame.setLocationByPlatform(true);

        // ensures the frame is the minimum size it needs to be
        // in order display the components within it
        frame.pack();
        // should be done last, to avoid flickering, moving,
        // resizing artifacts.
        frame.setVisible(true);
        */
		
		frame.setLocationRelativeTo(null);		//screen center
		frame.setVisible(true);
	}
}
