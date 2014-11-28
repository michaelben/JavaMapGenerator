package com.mapgen;

import java.awt.Color;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class NumberPicker extends JPanel {
	JSlider slider;
	private final JLabel minLabel = new JLabel();
	private final JLabel maxLabel = new JLabel();
	
	String label = "Parameter 1";
	int max = 100;
	int min = 0;
	int def = 50;
	int step = 1;
	int value;
	JSpinner spinner;

	/**
	 * Create the panel.
	 */
	
	public NumberPicker() {
		super();
		
		initGUI();
	}
	
	public NumberPicker(String label, int max, int min, int def, int step) {
		super();

		this.label = label;
		this.max = max;
		this.min = min;
		this.def = def;
		this.step = step;
		
		initGUI();
	}

	//this is used to fix a small bug in FormattedTextField attached to Spinner,
	//also allows customization.
	private static class NumberEditorFormatter extends NumberFormatter {
        private final SpinnerNumberModel model;

        NumberEditorFormatter(SpinnerNumberModel model, NumberFormat format) {
            super(format);
            this.model = model;
            setValueClass(model.getValue().getClass());
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void setMinimum(Comparable min) {
            model.setMinimum(min);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Comparable getMinimum() {
            return  model.getMinimum();
        }

        @SuppressWarnings("rawtypes")
		@Override
        public void setMaximum(Comparable max) {
            model.setMaximum(max);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Comparable getMaximum() {
            return model.getMaximum();
        }
    }
	
	private void initGUI() {
		setLayout(null);

		SpinnerNumberModel model = new SpinnerNumberModel(def, min, max, step);
		spinner = new JSpinner(model);
		JSpinner.NumberEditor editor = (JSpinner.NumberEditor)spinner.getEditor();
		JFormattedTextField ftf= editor.getTextField();
		
		NumberFormatter numberFormatter = new NumberEditorFormatter(model, new DecimalFormatExt());
		
		numberFormatter.setAllowsInvalid(false);
		numberFormatter.setCommitsOnValidEdit(true);
		numberFormatter.setOverwriteMode(true);
		
		DefaultFormatterFactory dff = new DefaultFormatterFactory(
				numberFormatter,
				numberFormatter,
				numberFormatter);
				
		ftf.setFormatterFactory(dff);
		
		spinner.setEditor(editor);
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				slider.setValue((int)spinner.getValue());
			}
		});
		spinner.setBounds(211, 0, 75, 22);
		
		add(spinner);
		
		slider = new JSlider(min, max, def);
		//slider.setExtent(step);	//slider obeys value+extent<=max so the maximal value is max-extent
		slider.setBounds(40, 22, 221, 25);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				spinner.setValue((int)slider.getValue());
			}
		});
		
		add(slider);
		minLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		minLabel.setBounds(9, 25, 25, 15);
		minLabel.setText(""+min);
		add(minLabel);
		
		maxLabel.setBounds(262, 25, 36, 15);
		maxLabel.setText(""+max);
		add(maxLabel);
		
		this.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), label, TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
	}
}
