package main;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.*;
import javax.swing.event.*;

public class ToolDistanceSelector extends JPanel {
	private static final long serialVersionUID = 1L;
	private int _distance;
	private JSlider _distance_slider;
	private JLabel _distance_settings_label,_distance_range_label;

	/**
	 * Default Constructor. Creates the GUI element and connects it to a
	 * segmentation.
	 * 
	 * @param slices	the global image stack
	 * @param seg		the segmentation to be modified
	 */
	public ToolDistanceSelector() {
		final ImageStack slices = ImageStack.getInstance();		
		_distance = 3;
		// range_max needs to be calculated from the bits_stored value
		// in the current dicom series
		int distace_min = 1;
		int distance_max = 10;

		
		_distance_settings_label = new JLabel("Distance of the grid Settings");
		_distance_range_label = new JLabel("Distance:");
		
		_distance_slider = new JSlider(distace_min, distance_max, _distance);
		_distance_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_distance = (int)source.getValue();
					System.out.println("_distance_slider state Changed: "+_distance);
					slices.setDistance(_distance);
				}
			}
		});		
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 0.3;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2,2,2,2); // top,left,bottom,right
		c.weightx = 0.1;
		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 0; this.add(_distance_settings_label, c);
		c.gridwidth=1;

		c.weightx = 0;
		c.gridx = 0; c.gridy = 1; this.add(_distance_range_label, c);
		c.gridx = 1; c.gridy = 1; this.add(_distance_slider, c);
		

	}	
}
