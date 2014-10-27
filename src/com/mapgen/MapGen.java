package com.mapgen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class MapGen {

	public static final int WINDOW_WIDTH = 900;
	public static final int WINDOW_HEIGTH = 600;
	public static final int CONTROL_WIDTH = 320;
	public static final int MAP_WIDTH = WINDOW_WIDTH - CONTROL_WIDTH;
	
	private JFrame frame;
	private final JPanel ctlPanel = new JPanel();
	private final GraphPanel mapPanel = new GraphPanel();
	private final JTextField dxfInputField = new JTextField();
	private final JButton browseButton = new JButton("Browse...");
	private final JButton outputDXFButton = new JButton("Output DXF");
	private final JPanel paramPanel = new JPanel();
	private final JPanel dxfPanel = new JPanel();
	private final JButton resetButton = new JButton("Reset");
	private final JButton genmapButton = new JButton("Generate Map");
	
	ArrayList<NumberPicker> paramsUI = new ArrayList<NumberPicker>();
	

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
		frame.setBounds(10, 10, WINDOW_WIDTH, WINDOW_HEIGTH);
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
		
		genmapButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int i = 0; i < Param.params.length; i++) {
					Param param = Param.params[i];
					NumberPicker np = paramsUI.get(i);
					param.value = np.slider.getValue();
				}
				
			}
		});
		
		browseButton.addActionListener(new ActionListener() {
			JFileChooser jfc = new JFileChooser();
			public void actionPerformed(ActionEvent e) {
				int returnVal = jfc.showOpenDialog(frame);
				
		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = jfc.getSelectedFile();
		            dxfInputField.setText(file.getAbsolutePath());
		        }
			}
		});
		
		outputDXFButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fn = dxfInputField.getText().trim();
				Path path = FileSystems.getDefault().getPath(fn);
				BufferedWriter writer = null; 
				
				if("".equals(fn)) {
					JOptionPane.showMessageDialog(frame, "Please choose a file.");
					return;
				}
				
				if(Files.isDirectory(path)) {
					JOptionPane.showMessageDialog(frame, "This is a folder. Please choose a file.");
					return;
				}
				
				if(Files.exists(path) && !Files.isWritable(path)) {
					JOptionPane.showMessageDialog(frame, "This file is not writable.");
					return;
				}
				
				if(Files.exists(path) && !Files.isRegularFile(path)) {
					JOptionPane.showMessageDialog(frame, "This file is not a regular file.");
					return;
				}
				
				if(Files.isRegularFile(path) && Files.exists(path)) {
					 Object[] options = { "OK", "CANCEL" };
					 int ret = JOptionPane.showOptionDialog(null, "File exits. Do you want to override it?", "Warning",
					             JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
					             null, options, options[0]);
					 
					 if(ret != JOptionPane.OK_OPTION) return;
				}
				
				try {
					writer = new BufferedWriter(new FileWriter(fn));
					writer.write("test", 0, 4);
					
					JOptionPane.showMessageDialog(frame, "DXF File saved.");
						
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
		            if (writer != null) {
		                try {
		                	writer.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		            }
				}
			}
		});
		
		dxfInputField.setColumns(10);
		
		ctlPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		ctlPanel.setPreferredSize(new Dimension(CONTROL_WIDTH, WINDOW_WIDTH));
		frame.getContentPane().add(ctlPanel, BorderLayout.LINE_START);
		
		paramPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		dxfPanel.setMaximumSize(new Dimension(320, 200));
		dxfPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "DXF", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		dxfPanel.setLayout(new BoxLayout(dxfPanel, BoxLayout.PAGE_AXIS));
		JPanel dxfPanelx = new JPanel();
		dxfPanelx.setLayout(new BoxLayout(dxfPanelx, BoxLayout.LINE_AXIS));
		dxfPanelx.add(dxfInputField);
		dxfPanelx.add(browseButton);
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
							.addGap(54)
							.addComponent(resetButton)
							.addGap(20)
							.addComponent(genmapButton))
						.addGroup(gl_ctlPanel.createSequentialGroup()
							.addGap(1)
							.addComponent(paramPanel, GroupLayout.PREFERRED_SIZE, 317, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_ctlPanel.setVerticalGroup(
			gl_ctlPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_ctlPanel.createSequentialGroup()
					.addGap(2)
					.addComponent(paramPanel, GroupLayout.PREFERRED_SIZE, 451, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_ctlPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(resetButton)
						.addComponent(genmapButton))
					.addGap(2)
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
					.addContainerGap(35, Short.MAX_VALUE))
		);
		paramPanel.setLayout(gl_paramPanel);
		ctlPanel.setLayout(gl_ctlPanel);
		
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
