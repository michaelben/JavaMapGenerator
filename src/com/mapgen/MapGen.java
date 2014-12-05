package com.mapgen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mapgen.map.MapGenData;

public class MapGen implements ActionListener {
	public static final int CONTROL_WIDTH = 320;
	//public static final int MAP_WIDTH = WINDOW_WIDTH - CONTROL_WIDTH;
	
	//Toolkit.getScreenSize();	screensize including system tray
	//get screen size excluding system tray
	static final Rectangle desktopBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();		
    

	private JFrame frame;
	private final JPanel ctlPanel = new JPanel();
	private final ScrollGraphPanel mapPanel = new ScrollGraphPanel();
	private final JTextField dxfInputField = new JTextField();
	private final JButton outputDXFButton = new JButton("Output DXF");
	private final JPanel paramPanel = new JPanel();
	private final JPanel dxfPanel = new JPanel();
	private final JButton resetButton = new JButton("Reset");
	private final JButton genmapButton = new JButton("Generate Map");
	private final JRadioButton urbanRadioButton = new JRadioButton("Urban");
    private final JRadioButton suburbanRadioButton = new JRadioButton("Suburban");
    private final ButtonGroup radioButtonGroup = new ButtonGroup();
	private final JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
	private final JFileChooser jfc = new JFileChooser();
	
	private final ArrayList<NumberPicker> paramsUI = new ArrayList<NumberPicker>();
	
	private MapGenData mapData = new MapGenData(this);
	
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
		
		resetButton.addActionListener(resetButtonActionListener);

        urbanRadioButton.setSelected(true);
        urbanRadioButton.setActionCommand("urban");
        suburbanRadioButton.setActionCommand("suburban");
        urbanRadioButton.addActionListener(this);
        suburbanRadioButton.addActionListener(this);

        //Group the radio buttons.
        radioButtonGroup.add(urbanRadioButton);
        radioButtonGroup.add(suburbanRadioButton);
        
		outputDXFButton.setEnabled(false);
		
		genmapButton.addActionListener(genMapButtonActionListener);
		
		outputDXFButton.addActionListener(outputDXFButtonActionListener);
		
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
		gl_ctlPanel.setHorizontalGroup(
			gl_ctlPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_ctlPanel.createSequentialGroup()
					.addGroup(gl_ctlPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(dxfPanel, GroupLayout.PREFERRED_SIZE, 317, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_ctlPanel.createSequentialGroup()
							.addGap(20)
							.addComponent(resetButton)
							.addGap(20)
							.addComponent(urbanRadioButton)
							.addGap(20)
							.addComponent(suburbanRadioButton)
							.addGap(20))
						.addGroup(gl_ctlPanel.createSequentialGroup()
							.addGap(100)
							.addComponent(genmapButton)
							.addGap(10))
						.addComponent(separator)	
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
						.addComponent(urbanRadioButton)
						.addComponent(suburbanRadioButton))
					.addGap(10)
					.addComponent(genmapButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(separator)	
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
					.addContainerGap(35, Short.MAX_VALUE))
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
    
	public void actionPerformed(ActionEvent e) {
		if("urban".equalsIgnoreCase(e.getActionCommand()))
			mapData.setIsUrban(true);
		else
			mapData.setIsUrban(false);
	}
	
	public ScrollGraphPanel getMapPanel() {
		return mapPanel;
	}
	
	private final ActionListener resetButtonActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			for(int i = 0; i < Param.params.length; i++) {
				Param param = Param.params[i];
				NumberPicker np = paramsUI.get(i);
				np.slider.setMaximum(param.max);
				np.slider.setMinimum(param.min);
				np.slider.setValue(param.def);
				
				mapData.setIsUrban(true);
				urbanRadioButton.setSelected(true);
			}
		}
	};
	
	private final ActionListener genMapButtonActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			resetButton.setEnabled(false);
			urbanRadioButton.setEnabled(false);
			genmapButton.setEnabled(false);
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
					e1.printStackTrace();
				}
			}
			
			if(i<5) mapPanel.repaint();
			else JOptionPane.showMessageDialog(frame, "Error in map generation.");
			
			resetButton.setEnabled(true);
			urbanRadioButton.setEnabled(true);
			genmapButton.setEnabled(true);
			outputDXFButton.setEnabled(true);
		}
	};
	
	private final ActionListener outputDXFButtonActionListener = new ActionListener() {
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
	};
}
