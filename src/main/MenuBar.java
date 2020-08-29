package main;

import java.awt.event.*;
import javax.swing.*;

import org.omg.CORBA.IdentifierHelper;

import com.sun.xml.internal.bind.v2.runtime.Name;

import misc.DiFileFilter;

import java.io.*;

/**
 * This class represents the main menu of YaDiV (lab version).
 * 
 * @author  Karl-Ingo Friese
 */
public class MenuBar extends JMenuBar {
	private static final long serialVersionUID = 1L;

	private JMenu _menuFile;
	private JMenu _menu2d;
	private JMenu _menu3d;
	private JMenu _menuTools;
	private JMenuItem _no_entries2d;
	private JMenuItem _no_entries3d;
	private InfoWindow _info_frame;
	private ToolPane _tools;
	
	private Viewport2d _v2d;
	private Viewport3d _v3d;
	private MainWindow _win;

	/**
	 * Constructor. Needs many references, since the MenuBar has to trigger a
	 * lot of functions.
	 * 
	 * @param slices	the global image stack reference
	 * @param v2d		the Viewport2d reference
	 * @param v3d		the Viewport3d reference
	 * @param tools		the ToolPane reference
	 */
	public MenuBar(Viewport2d v2d, Viewport3d v3d, ToolPane tools) {
		JMenuItem item;

		_v2d = v2d;
		_v3d = v3d;
		_tools = tools;
		
		_menuFile = new JMenu("File");
		_menu2d = new JMenu("2D View");
		_menu3d = new JMenu("3D View");
		_menuTools = new JMenu("Tools");
		_info_frame = null;		

		// -------------------------------------------------------------------------------------

		item = new JMenuItem(new String("Load"), 'L');
		item.addActionListener(loadListener);
		_menuFile.add(item);

		item = new JMenuItem(new String("Save"));
		item.addActionListener(saveListener);
		item.setEnabled(false);
		_menuFile.add(item);

		item = new JMenuItem(new String("Save as ..."));
		item.addActionListener(saveAsListener);
		item.setEnabled(false);
		_menuFile.add(item);
		
		item = new JMenuItem(new String("Quit"), 'Q');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		_menuFile.add(item);

		// -------------------------------------------------------------------------------------

		item = new JMenuItem(new String("DICOM Info"));		
		item.addActionListener(showInfoListener);
		_menu2d.add(item);

		_menu2d.addSeparator();		
		item = new JCheckBoxMenuItem(new String("Show original data"), true);		
		item.addActionListener(toggleBGListener2d);		
		_menu2d.add(item);
		
		item = new JCheckBoxMenuItem(new String("Region Grow Progress"),false);
		item.addActionListener(toggleRegionGrowListener);
		_menu2d.add(item);
		
		JRadioButtonMenuItem rbMenuItem;
		ButtonGroup group = new ButtonGroup();

		rbMenuItem = new JRadioButtonMenuItem("Transversal");
		rbMenuItem.addActionListener(setViewModeListener);
		group.add(rbMenuItem);
		_menu2d.add(rbMenuItem);
		rbMenuItem.setSelected(true);

		rbMenuItem = new JRadioButtonMenuItem("Sagittal");
		rbMenuItem.addActionListener(setViewModeListener);
		group.add(rbMenuItem);
		_menu2d.add(rbMenuItem);

		rbMenuItem = new JRadioButtonMenuItem("Frontal");
		rbMenuItem.addActionListener(setViewModeListener);
		group.add(rbMenuItem);
		_menu2d.add(rbMenuItem);

		_menu2d.addSeparator();		

		_no_entries2d = new JMenuItem(new String("no segmentations yet"));
		_no_entries2d.setEnabled(false);
		_menu2d.add(_no_entries2d);
		
		
		// -------------------------------------------------------------------------------------

		item = new JMenuItem(new String("3d Item 1"));
		// item.addActionListener(...);
		item = new JCheckBoxMenuItem(new String("Show original Data"), true);		
		item.addActionListener(toggleBGListener3d);		
		_menu3d.add(item);
				
		
		JRadioButtonMenuItem rbMenuItem3d;
		ButtonGroup group3d = new ButtonGroup();

		rbMenuItem3d = new JRadioButtonMenuItem("Transversal Rendering");
		rbMenuItem3d.addActionListener(setRenderingModeListener);
		group3d.add(rbMenuItem3d);
		_menu3d.add(rbMenuItem3d);
		rbMenuItem3d.setSelected(true);

		rbMenuItem3d = new JRadioButtonMenuItem("Sagittal Rendering");
		rbMenuItem3d.addActionListener(setRenderingModeListener);
		group3d.add(rbMenuItem3d);
		_menu3d.add(rbMenuItem3d);

		rbMenuItem3d = new JRadioButtonMenuItem("Frontal Rendering");
		rbMenuItem3d.addActionListener(setRenderingModeListener);
		group3d.add(rbMenuItem3d);
		_menu3d.add(rbMenuItem3d);
		
		rbMenuItem3d = new JRadioButtonMenuItem("Alle Ebenen Rendering");
		rbMenuItem3d.addActionListener(setRenderingModeListener);
		group3d.add(rbMenuItem3d);
		_menu3d.add(rbMenuItem3d);

		
		_menu3d.addSeparator();	
		
		JRadioButtonMenuItem rbMenuItemMagic;
		ButtonGroup groupMagic = new ButtonGroup();

		rbMenuItemMagic = new JRadioButtonMenuItem("Point Darstellung");
		rbMenuItemMagic.addActionListener(setMagicListener);
		groupMagic.add(rbMenuItemMagic);
		_menu3d.add(rbMenuItemMagic);
		rbMenuItemMagic.setSelected(true);
		
		rbMenuItemMagic = new JRadioButtonMenuItem("Magic size 1");
		rbMenuItemMagic.addActionListener(setMagicListener);
		groupMagic.add(rbMenuItemMagic);
		_menu3d.add(rbMenuItemMagic);

		rbMenuItemMagic = new JRadioButtonMenuItem("Magic size 2");
		rbMenuItemMagic.addActionListener(setMagicListener);
		groupMagic.add(rbMenuItemMagic);
		_menu3d.add(rbMenuItemMagic);

		rbMenuItemMagic = new JRadioButtonMenuItem("Magic size 3");
		rbMenuItemMagic.addActionListener(setMagicListener);
		groupMagic.add(rbMenuItemMagic);
		_menu3d.add(rbMenuItemMagic);
		
		rbMenuItemMagic = new JRadioButtonMenuItem("Magic size 4");
		rbMenuItemMagic.addActionListener(setMagicListener);
		groupMagic.add(rbMenuItemMagic);
		_menu3d.add(rbMenuItemMagic);
		
		_menu3d.addSeparator();	

		_no_entries3d = new JMenuItem(new String("no segmentations yet"));
		_no_entries3d.setEnabled(false);
		_menu3d.add(_no_entries3d);
		
		// -------------------------------------------------------------------------------------

		item = new JMenuItem(new String("Neue Segmentierung"));
		item.addActionListener(newSegmentListener);
		_menuTools.add(item);

		item = new JMenuItem(new String("Fenster Einstellung"));
		item.addActionListener(openWindowSettings);
		_menuTools.add(item);
		
		item = new JMenuItem(new String("Abstand des Raumgitters"));
		item.addActionListener(distanceSettings);
		_menuTools.add(item);
		// -------------------------------------------------------------------------------------

		add(_menuFile);
		add(_menu2d);
		add(_menu3d);
		add(_menuTools);
	}

	/**
	 * This function is called when someone chooses to load DICOM series.
	 */
	ActionListener loadListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			openDialog(false);
		}
	};

	/**
	 * This function is called when someone chooses to save the current project.
	 */
	ActionListener saveListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			// saveFile(...);
		}
	};
	
	/**
	 * This function is called when someone chooses to save the current project
	 * under a new name.
	 */
	ActionListener saveAsListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			openDialog(true);
		}
	};


	/**
	 * Opens a file chooser dialog with a DICOM file filter and directory.
	 * 
	 * @param save	true if the dialog should be a save file dialog, false if not
	 */
	private void openDialog(boolean save) {
		int returnVal;
		File file;
		JFileChooser chooser;
		String default_dir = new String("/usr/common/gdv/medic_data/public");
		
		if (new File(default_dir).exists()) {
			chooser = new JFileChooser(default_dir);
		} else {
			chooser = new JFileChooser();
		}

		DiFileFilter filter = new DiFileFilter();
		filter.setDescription("Dicom Image Files");
		chooser.setFileFilter(filter);
		
		if (save) {
			returnVal = chooser.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
				if (!file.canWrite()) {
					System.out.println("could not read!");
					return;
				}
				// saveFile(file);
			}		
		} else {
			returnVal = chooser.showOpenDialog(null);			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
				if (!file.canRead()) {
					System.out.println("could not read!");
					return;
				}
				System.out.println(file.getParent());
				LabMed.get_is().initFromDirectory(file.getParent());
			}		
		}
		
	}	
	
	/**
	 * This function is called when someone wants to see the dicome file info
	 * window.
	 */
	ActionListener showInfoListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			if (LabMed.get_is().getNumberOfImages()==0) {
				JOptionPane.showMessageDialog(null,
					    "Fehler: Keine DICOM Datei geöffnet",
					    "Inane error",
					    JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (_info_frame == null) {
				_info_frame = new InfoWindow();
				LabMed.get_is().addObserver(_info_frame);
			}
			
			if (!_info_frame.isVisible()) {
				_info_frame.setVisible(true);
			}
			
			_info_frame.showInfo(_v2d.currentFile());		

		}
	};

	/**
	 * Actionlistener for changing the 2d viewmode.
	 */
	ActionListener setViewModeListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {			
			String name = event.getActionCommand();
			if (name.equals("Transversal")) {
				_v2d.setViewMode(0);
			} else if (name.equals("Sagittal")) {
				_v2d.setViewMode(1);
			} else if (name.equals("Frontal")) {
				_v2d.setViewMode(2);
			}
		}
	};

	ActionListener setRenderingModeListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			String name = event.getActionCommand();
			if (name.equals("Transversal Rendering")) {
				_v3d.setRenderingMode(0);
			} else if (name.equals("Sagittal Rendering")) {
				_v3d.setRenderingMode(1);
			} else if (name.equals("Frontal Rendering")) {
				_v3d.setRenderingMode(2);
			}else {
				_v3d.setRenderingMode(3);
			}
		}
	};
	ActionListener setMagicListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
			String name = event.getActionCommand();
			if (name.equals("Magic size 1")) {
				_v3d.setMagic(1);
			} else if (name.equals("Magic size 2")) {
				_v3d.setMagic(2);
			} else if (name.equals("Magic size 3")) {
				_v3d.setMagic(3);
			}else if (name.equals("Magic size 4")) {
				_v3d.setMagic(4);
			}else {
				_v3d.setMagic(0);
			}
		}
	};
	/**
	 * ActionListener for toggling the 2d background image.
	 */
	ActionListener toggleBGListener2d = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			_v2d.toggleBG();
		}
	};

	ActionListener toggleRegionGrowListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			ImageStack is = LabMed.get_is();
			if (is.getNumberOfImages()==0) {
				JOptionPane.showMessageDialog(_win,
					    "Fenster Einstellung ohne geöffneten DICOM Datensatz nicht möglich.",
					    "Inane error",
					    JOptionPane.ERROR_MESSAGE);
				_v2d.toggleRegionGrow();
			}else if (is.getSegmentNumber()==3) {
				JOptionPane.showMessageDialog(_win,
					    "In der Laborversion werden nicht mehr als drei Segmentierungen benütigt.",
					    "Inane error",
					    JOptionPane.ERROR_MESSAGE);
				_v2d.toggleRegionGrow();
			}else {
				String name = "region grow";
				_no_entries2d.setVisible(false);
				_no_entries3d.setVisible(false);
				if (is.getSegNames().contains(name)) {
					_v2d.toggleRegionGrow();
					return;
				}
				Segment segment = is.createSegment("region grow");
				_v2d.toggleRegionGrow();
				_v2d.toggleSeg(segment);
				JMenuItem item = new JCheckBoxMenuItem(name, true);
				item.addActionListener(toggleSegListener2d);
				_menu2d.add(item);
				item = new JCheckBoxMenuItem(name, false);
				item.addActionListener(toggleSegListener3d);
				_menu3d.add(item);
			}
		}
	};
	/**
	 * ActionListener for toggling the 3d background image.
	 */
	ActionListener toggleBGListener3d = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			_v3d.toggleBG();
		}
	};

	/**
	 * ActionListener for toggling a segmentation in the 2d viewport.
	 */
	ActionListener toggleSegListener2d = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			String name = event.getActionCommand();
			_v2d.toggleSeg(LabMed.get_is().getSegment(name));
		}
	};

	/**
	 * ActionListener for toggling a segmentation in the 3d viewport.
	 */
	ActionListener toggleSegListener3d = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			String name = event.getActionCommand();
			_v3d.toggleSeg(LabMed.get_is().getSegment(name));
		}
	};


	/**
	 * ActionListener for adding a new segmentation to the global image stack.
	 */
	ActionListener newSegmentListener = new ActionListener() {		
		public void actionPerformed(ActionEvent event) {
			ImageStack is = LabMed.get_is();
			if (is.getNumberOfImages()==0) {
				JOptionPane.showMessageDialog(_win,
					    "Segmentierung ohne geöffneten DICOM Datensatz nicht möglich.",
					    "Inane error",
					    JOptionPane.ERROR_MESSAGE);
			} else if (is.getSegmentNumber()==3) {
					JOptionPane.showMessageDialog(_win,
						    "In der Laborversion werden nicht mehr als drei Segmentierungen benütigt.",
						    "Inane error",
						    JOptionPane.ERROR_MESSAGE);
			} else {
				String name = JOptionPane.showInputDialog(_win, "Name der Segmentierung");
				if (name != null) {
					_no_entries2d.setVisible(false);
					_no_entries3d.setVisible(false);
					Segment seg = is.createSegment(name);
					_v2d.toggleSeg(seg);
					JMenuItem item = new JCheckBoxMenuItem(name, true);
					item.addActionListener(toggleSegListener2d);
					_menu2d.add(item);
					item = new JCheckBoxMenuItem(name, false);
					item.addActionListener(toggleSegListener3d);
					_menu3d.add(item);
					_tools.showTool(new ToolRangeSelector(seg));
				}
			}
		}
	};
	ActionListener openWindowSettings = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			ImageStack is = LabMed.get_is();
			if (is.getNumberOfImages()==0) {
				JOptionPane.showMessageDialog(_win,
					    "Fenster Einstellung ohne geöffneten DICOM Datensatz nicht möglich.",
					    "Inane error",
					    JOptionPane.ERROR_MESSAGE);
			}else {
				_tools.showTool(new ToolWindowSelector());
			}
		}
	};
	ActionListener distanceSettings = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			ImageStack is = LabMed.get_is();
			if (is.getNumberOfImages()==0) {
				JOptionPane.showMessageDialog(_win,
					    "Fenster Einstellung ohne geöffneten DICOM Datensatz nicht möglich.",
					    "Inane error",
					    JOptionPane.ERROR_MESSAGE);
			}else {
				_tools.showTool(new ToolDistanceSelector());
			}
		}
	};
}
