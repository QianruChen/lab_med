package main;

/**
 * LabMed main program.
 * 
 * @author  Karl-Ingo Friese
 */
public class LabMed {
	private static ImageStack _is; // the global image stack
	private static MainWindow _win; // the main window
	private static Viewport2d _v2d; // the 2d view area
	private static Viewport3d _v3d; // the 3d view area
	
	/**
	 * Returns the reference of the global image stack.
	 * @return the image stack
	 */
	public static ImageStack get_is() {
		return _is;
	}
	
	/**
	 * Returns the reference of the main window
	 * @return the main window
	 */
	public static MainWindow get_window() {
		return _win;
	}

	/**
	 * Returns the reference of the Viewport2d
	 * @return the 2d viewport
	 */
	public static Viewport2d get_v2d() {
		return _v2d;
	}

	/**
	 * Returns the reference of the Viewport3d
	 * @return the 3d viewport
	 */
	public static Viewport3d get_v3d() {
		return _v3d;
	}

	/**
	 * Good old main.
	 * 
	 * @param args currently no program specific arguments
	 */
	public static void main(String[] args) {				
		// the global image stack
		_is = ImageStack.getInstance(); 		
		_v2d = new Viewport2d();
		_v3d = new Viewport3d();
		
		// the viewports need to update when the global image stack data changes
		_is.addObserver(_v2d);
		_is.addObserver(_v3d);
		
		_win = new MainWindow("LabMed 2020", _v2d, _v3d);
		_win.setVisible(true);
	}
}
