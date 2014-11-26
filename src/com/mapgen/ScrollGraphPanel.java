package com.mapgen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

@SuppressWarnings("serial")
public class ScrollGraphPanel extends JPanel implements ItemListener {
	private Rule columnView;
	private Rule rowView;
	private JToggleButton isMetric;
	private GraphPanel graphPanel;

	public ScrollGraphPanel() {
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		// Create the row and column headers.
		columnView = new Rule(Rule.HORIZONTAL, true);
		rowView = new Rule(Rule.VERTICAL, true);

		columnView.setPreferredWidth(Param.getMapWidth());
		rowView.setPreferredHeight(Param.getMapHeight());

		// Create the corners.
		JPanel buttonCorner = new JPanel(); // use FlowLayout
		isMetric = new JToggleButton("tm", false);
		isMetric.setFont(new Font("SansSerif", Font.PLAIN, 11));
		isMetric.setMargin(new Insets(2, 2, 2, 2));
		isMetric.addItemListener(this);
		buttonCorner.add(isMetric);

		// Set up the scroll pane.
		graphPanel = new GraphPanel(1);
		JScrollPane pictureScrollPane = new JScrollPane(graphPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		pictureScrollPane.setPreferredSize(new Dimension(MapGen.desktopBounds.width - MapGen.CONTROL_WIDTH, MapGen.desktopBounds.height));
		pictureScrollPane.setViewportBorder(BorderFactory
				.createLineBorder(Color.black));

		pictureScrollPane.setColumnHeaderView(columnView);
		pictureScrollPane.setRowHeaderView(rowView);

		// Set the corners.
		// In theory, to support internationalization you would change
		// UPPER_LEFT_CORNER to UPPER_LEADING_CORNER,
		// LOWER_LEFT_CORNER to LOWER_LEADING_CORNER, and
		// UPPER_RIGHT_CORNER to UPPER_TRAILING_CORNER. In practice,
		// bug #4467063 makes that impossible (in 1.4, at least).
		pictureScrollPane
				.setCorner(JScrollPane.UPPER_LEFT_CORNER, buttonCorner);
		pictureScrollPane
				.setCorner(JScrollPane.LOWER_LEFT_CORNER, new Corner());
		pictureScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER,
				new Corner());

		// Put it in this panel.
		add(pictureScrollPane);
		//setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	}

	public void setMapSize() {
		graphPanel.setPreferredSize(new Dimension(Param.getMapWidth(), Param.getMapHeight()));
		columnView.setPreferredWidth(Param.getMapWidth());
		rowView.setPreferredHeight(Param.getMapHeight());
		graphPanel.polygonPick = null;
	}
	
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			// Turn it to metric.
			//rowView.setIsMetric(true);
			//columnView.setIsMetric(true);
			GraphPanel.isFill = true;
		} else {
			// Turn it to inches.
			//rowView.setIsMetric(false);
			//columnView.setIsMetric(false);
			GraphPanel.isFill = false;
		}
		
		graphPanel.setMaxUnitIncrement(rowView.getIncrement());
		
		graphPanel.repaint();
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = ScrollGraphPanel.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
}
