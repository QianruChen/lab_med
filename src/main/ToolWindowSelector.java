package main;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.*;
import javax.swing.event.*;

public class ToolWindowSelector extends JPanel {
	private static final long serialVersionUID = 1L;
	private int _window_center, _window_width;
	private JSlider _window_center_slider, _window_width_slider;
	private JLabel _window_settings_label,_window_center_label,_window_width_label;

	/**
	 * Default Constructor. Creates the GUI element and connects it to a
	 * segmentation.
	 * 
	 * @param slices	the global image stack
	 * @param seg		the segmentation to be modified
	 */
	public ToolWindowSelector() {
		final ImageStack slices = ImageStack.getInstance();		
		

		// range_max needs to be calculated from the bits_stored value
		// in the current dicom series
		int window_center_min = 0;
		int window_width_min = 0;
		int window_center_max = 1<<(slices.getDiFile(0).getBitsStored());
		int window_width_max = 1<<(slices.getDiFile(0).getBitsStored());
		_window_center = slices.getWindowCenter();
		_window_width = slices.getWindowWidth();
		
		_window_settings_label = new JLabel("Window Settings");
		_window_center_label = new JLabel("Window Center:");
		_window_width_label = new JLabel("Window Width:");
		
		_window_center_slider = new JSlider(window_center_min, window_center_max, _window_center);
		_window_center_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_window_center = (int)source.getValue();
//					System.out.println("_window_center_slider state Changed: "+_window_center);
					slices.setWindowCenter(_window_center);
				}
			}
		});		
		
		_window_width_slider= new JSlider(window_width_min, window_width_max, _window_width);
		_window_width_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_window_width = (int)source.getValue();
//					System.out.println("_window_width_slider state Changed: "+_window_width);
					slices.setWindowWidth(_window_width);
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
		c.gridx = 0; c.gridy = 0; this.add(_window_settings_label, c);
		c.gridwidth=1;

		c.weightx = 0;
		c.gridx = 0; c.gridy = 1; this.add(_window_center_label, c);
		c.gridx = 0; c.gridy = 2; this.add(_window_width_label, c);
		c.gridx = 1; c.gridy = 1; this.add(_window_center_slider, c);
		c.gridx = 1; c.gridy = 2; this.add(_window_width_slider, c);
		

	}	
}
