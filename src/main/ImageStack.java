package main;

import java.util.*;
import java.io.*;
import javax.swing.*;

import misc.DiFile;
import misc.DiFileInputStream;
import misc.MyObservable;

/**
 * The ImageStack class represents all DicomFiles of a series and its segments.
 * It is the global data structure in YaDiV.
 * This class is implemented as singleton, meaning the constructor is private.
 * Use getInstance() instead.
 * 
 * @author  Karl-Ingo Friese
 */
public class ImageStack extends MyObservable {
	private static ImageStack _instance = null;
	private DiFile[] _dicom_files;
	private DefaultListModel<String> _seg_names = new DefaultListModel<>();
	private HashMap<String, Segment> _segment_map = new HashMap<>();
	private String _dir_name = "";
	private int _w, _h, _active = 0;
	private int _slope = 0;
	private int _intercept = 0;
	private int _window_center,_window_width = 0;
	private double _varianz_region_grow = 0.1;
	
	
	// voxel with original gray value (unskaliert)
	private int[][][] _voxel;
	
	
//	private int[][][] _skaliert_voxel;
	/**
	 * Default Constructor.
	 */
	private ImageStack() {
	}

	public static ImageStack getInstance() {
    	if (_instance==null) {
    		_instance = new ImageStack();
    	}
    	return _instance;
	}
	
	public int getGrayValue(int i, int j,int k) {
		return _voxel[i][j][k];
	}
	
	public void createVoxel() {
		_voxel = new int[_w][_h][getNumberOfImages()];
//		_skaliert_voxel = new int[_w][_h][getNumberOfImages()];
		DiFile diFile = getDiFile(0);
		int bits_allocated = diFile.getBitsAllocated();
		int bits_stored = getDiFile(0).getBitsStored();
		_window_center = 1<<(bits_stored-1);
		_window_width = 1<<(bits_stored);
		try {
			_window_center = getDiFile(0).getElement(0x00281050).getValueAsInt();
			_window_width = getDiFile(0).getElement(0x00281051).getValueAsInt();
		} catch (Exception e) {
//			System.out.println("There are no value of window center and window width");
		}
		_slope = diFile.getElement(0x00281053).getValueAsInt();
		_intercept = diFile.getElement(0x00281052).getValueAsInt();
		
		for (int k = 0; k < getNumberOfImages(); k++) {
			diFile = getDiFile(k);
			byte[] pixel_data_byte = diFile.getElement(0x7fe00010).getValues();
			int[] gray_value_unskaliert = new int[pixel_data_byte.length/(bits_allocated/8)];
//			int[] gray_value_skaliert = new int[pixel_data_byte.length/(bits_allocated/8)];
			for (int i = 0; i < pixel_data_byte.length-bits_allocated/8+1; i = i+bits_allocated/8) {
				int gray_value = 0;
				for (int j = 0; j < bits_allocated/8; j++) {
					int tmp = ((int)(pixel_data_byte[i+j] & 0xff))<<(8*j);
					gray_value = gray_value + tmp;
				}
				gray_value_unskaliert[i/(bits_allocated/8)] = gray_value;
				int skaliert = gray_value*_slope+_intercept;
				if (gray_value <= (_window_center-0.5-(_window_width-1)/2)) {
					skaliert = 0;
				}else if (gray_value > (_window_center-0.5+(_window_width)/2)) {
					skaliert = 255;
				}else {
					skaliert =(int)(((gray_value-(_window_center-0.5))/(_window_width-1)+0.5)*255);
				}
//				gray_value_skaliert[i/(bits_allocated/8)] = skaliert;
//				gray_value_unskaliert[i/(bits_allocated/8)] = gray_value*slope+intercept;
			}
			for (int i = 0; i < _w; i++) {
				for (int j = 0; j < _h; j++) {
					_voxel[i][j][k] = gray_value_unskaliert[j*_w+i];
//					_skaliert_voxel[i][j][k] = gray_value_skaliert[j*_w+i];
				}
			}
		}
//		System.out.println("created volume "+_w+" "+_h+" "+ getNumberOfImages());
	}
	public int getSkaliertGrayValue(int unskaliert) {
		int skaliert = unskaliert*_slope+_intercept;
		if (unskaliert <= (_window_center-0.5-(_window_width-1)/2)) {
			skaliert = 0;
		}else if (unskaliert > (_window_center-0.5+(_window_width)/2)) {
			skaliert = 255;
		}else {
			skaliert =(int)(((unskaliert-(_window_center-0.5))/(_window_width-1)+0.5)*255);
		}
		return skaliert;
	}
	
	public int getSkaliertGrayValue(int i,int j,int k) {
		
		return getSkaliertGrayValue(_voxel[i][j][k]);
	}
	
	public void setSegment(String seg_name,Segment seg) {
		if (!_segment_map.containsKey(seg_name)) {
			System.out.println("no such segment in imagestack"+seg_name);
			return;
		}
		_segment_map.replace(seg_name, seg);
		notifyObservers(new Message(Message.M_SEG_CHANGED,seg));
	}
	/**
	 * Reads all DICOM files from the given directory. All files are checked
	 * for correctness before loading. The load process is implemented as a thread.
	 * 
	 * @param dir_name	string contaning the directory name.
	 */
	public void initFromDirectory(String dir_name) {
		_dir_name = dir_name;
		_w = 0;
		_h = 0;

		// loading thread
		Thread t = new Thread() {
			JProgressBar progress_bar;
			// returns the image number of a dicom file or -1 if something wents wrong
			private int check_file(File file) {
				int result = -1;
				
				if (!file.isDirectory()) {
		        	try {
		        		DiFileInputStream candidate = new DiFileInputStream(file);
		        		
			    		if (candidate.skipHeader()) {
			    			result = candidate.quickscan_for_image_number();
			    		}				    	
						candidate.close();
		    		} catch (Exception ex) {
						System.out.println("this will work after exercise 1");
		    			result = -1;
		    		}
				}
				
	            return result;
			}
			
			// checks the DICOM files, retrieves their image number and loads them in the right order.
			public void run() {
				Hashtable<Integer, String> map_number_to_difile_name = new Hashtable<Integer, String>();
				DiFile df;

			    notifyObservers(new Message(Message.M_CLEAR));

				JFrame progress_win = new JFrame("checking ...");
				progress_win.setResizable(false);
				progress_win.setAlwaysOnTop(true);
				
				File dir = new File(_dir_name);
			    File[] files_unchecked = dir.listFiles();

				progress_bar = new JProgressBar(0, files_unchecked.length);
				progress_bar.setValue(0);
				progress_bar.setStringPainted(true);
				progress_win.add(progress_bar);
				progress_win.pack();
				// progress_bar.setIndeterminate(true);
				int main_width = (int)(LabMed.get_window().getSize().getWidth());
				int main_height = (int)(LabMed.get_window().getSize().getHeight());
				progress_win.setLocation((main_width-progress_win.getSize().width)/2, (main_height-progress_win.getSize().height)/2);
				progress_win.setVisible(true);		
								
			    for (int i=0; i<files_unchecked.length; i++) {
			    	int num = check_file(files_unchecked[i]);
			    	if (num >= 0) {
			    		map_number_to_difile_name.put(Integer.valueOf(num), files_unchecked[i].getAbsolutePath());			    					        		
		        	}
			    	progress_bar.setValue(i+1);
			    }
				
			    progress_win.setTitle("loading ...");
			    
				Enumeration<Integer> e = map_number_to_difile_name.keys();
		   	  	List<Integer> l = new ArrayList<Integer>();
		   	  	while(e.hasMoreElements()) {
		   	  		l.add((Integer)e.nextElement());
				}
				
			    String[] file_names = new String[l.size()];
		        Collections.sort(l);
		        Iterator<Integer> it = l.iterator();
		        int file_counter = 0;
		        while (it.hasNext()) {
		        	file_names[file_counter++] =  map_number_to_difile_name.get(it.next());
		        }
		        
				progress_bar.setMaximum(file_names.length);
				progress_bar.setValue(0);

				_dicom_files = new DiFile[file_names.length];
				
				for (int i=0; i<file_names.length; i++) {
			    	df = new DiFile();
			    	try {
			    		df.initFromFile(file_names[i]);
			    	} catch (Exception ex) {
			    		System.out.println(getClass()+"::initFromDirectory -> failed to open "+file_names[i]);
			    		System.out.println(ex);
			    		System.exit(0);
			    	}
			    	progress_bar.setValue(i+1);
			    	_dicom_files[i] = df;

					// initialize default image width and heigth from the first image read
					if (_w==0) _w = df.getImageWidth();
					if (_h==0) _h = df.getImageHeight();
					

				    notifyObservers(new Message(Message.M_NEW_IMAGE_LOADED));					
				}
			    progress_win.setVisible(false);
			    createVoxel();
			    setActiveImage(0);
			}
		};
		
		t.start();  
	}

	/**
	 * Adds a new segment with the given name.
	 * 
	 * @param name	the name of the new segment (must be unique)
	 * @return		the new segment or null if the name was not unique
	 */
	public Segment createSegment(String name) {
		Segment seg;

		if (_segment_map.containsKey(name)) {
			seg = null;
		} else {
			int[] def_colors = {0xff0000, 0x00ff00, 0x0000ff};
			seg = new Segment(name, _w, _h, _dicom_files.length);
			seg.setColor(def_colors[_segment_map.size()]);
			_segment_map.put(name, seg);
			_seg_names.addElement(name);
			notifyObservers(new Message(Message.M_NEW_SEGMENTATION, seg));
		}
		
		return seg;
	}
	
	
	/**
	 * Returns the DicomFile from the series with image number i; 
	 * 
	 * @param i	image number
	 * @return the DIOCM file
	 */
	public DiFile getDiFile(int i) {
		return (DiFile)(_dicom_files[i]);
	}
	
	/**
	 * Returns the segment with the given name.
	 * 
	 * @param name	the name of a segment
	 * @return		the segment
	 */
	public Segment getSegment(String name) {
		return (Segment)(_segment_map.get(name));
	}

	/**
	 * Returns the number of segments.
	 * 
	 * @return		the number of segments
	 */
	public int getSegmentNumber() {
		return _segment_map.size();
	}

	/**
	 * Returns the Number of DicomFiles in the ImageStack.
	 *   
	 * @return the number of files
	 */
	public int getNumberOfImages() {
		return (_dicom_files==null? 0 : _dicom_files.length);
	}
	
	/**
	 * Returns the DefaultListModel containing the segment names.
	 *   
	 * @return guess what
	 */
	public DefaultListModel<String> getSegNames() {
		return _seg_names;
	}

	/**
	 * Returns the width of the images in the image stack.
	 *   
	 * @return the image width
	 */
	public int getImageWidth() {
		return _w;
	}
	
	/**
	 * Returns the height of the images in the image stack.
	 *   
	 * @return the image height
	 */
	public int getImageHeight() {
		return _h;
	}
	
	/**
	 * Returns the currently active image.
	 * 
	 * @return the currently active image
	 */
	public int getActiveImageID() {
		return _active;
	}

	/**
	 * Sets the currently active image in the viewmode.
	 * Notifys Observers with M_NEW_ACTIVE_IMAGE Message. Object is the new active image value;
	 * 
	 * @param i	the active image
	 */
	public void setActiveImage(int i) {
		_active = i;
	    notifyObservers(new Message(Message.M_NEW_ACTIVE_IMAGE, Integer.valueOf(i)));
	}
	public void setWindowCenter(int window_center) {
		_window_center = window_center;
		notifyObservers(new Message(Message.M_WINDOW_CHANGED));
	}
	public void setWindowWidth(int window_width) {
		_window_width = window_width;
		notifyObservers(new Message(Message.M_WINDOW_CHANGED));
	}
	public int getWindowCenter() {
		return _window_center;
	}
	public int getWindowWidth() {
		return _window_width;
	}
	
	public void calculate_region_grow(int x, int y, int z) {
		if (!_segment_map.containsKey("region grow")) {
			createSegment("region grow");
		}
		Segment checked_segment = new Segment("checked", _w, _h, getNumberOfImages());
		Queue<String> uncheckedQueue = new LinkedList<>();
//		Queue<String> checkedStrings = new LinkedList<>();
		uncheckedQueue.add(position2String(x, y, z));
		checked_segment.getMask(z).set(x, y, true);
		String[] candidates = new String[6];
		String seed = position2String(x, y, z);
		String candidate = "";
		_segment_map.get("region grow").getMask(z).set(x, y, true);
		int[] index_candidate = new int[3];
		int tmp = 0;
		String center = "";
		while (!uncheckedQueue.isEmpty()) {
			tmp++;
			center = uncheckedQueue.poll();
			candidates = getNeighborCandidates(center);
			for (int i = 0; i < candidates.length; i++) {
				candidate = candidates[i];
				index_candidate = string2position(candidate);
				if (index_candidate[0]==-1) {
					continue;
				}
				if (checked_segment.getMask(index_candidate[2]).get(index_candidate[0], index_candidate[1])) {
					continue;
				}
				checked_segment.getMask(index_candidate[2]).set(index_candidate[0], index_candidate[1],true);
				if (is_belong(seed, candidate)) {
					uncheckedQueue.add(candidate);
					_segment_map.get("region grow").getMask(index_candidate[2]).set(index_candidate[0], index_candidate[1], true);
				}else {
					_segment_map.get("region grow").getMask(index_candidate[2]).set(index_candidate[0], index_candidate[1], false);
				}
			}
		}
		System.out.println(tmp);
		notifyObservers(new Message(Message.M_SEG_CHANGED, _segment_map.get("region grow")));
	}
	private boolean is_belong(String seed,String candidate) {
		int[] seed_position = string2position(seed);
		int[] candidate_position = string2position(candidate);
		int seed_value = getGrayValue(seed_position[0], seed_position[1], seed_position[2]);
		int candidate_value = getGrayValue(candidate_position[0], candidate_position[1], candidate_position[2]);
		if (candidate_value<(seed_value-_varianz_region_grow*seed_value)) {
			return false;
		}
		if (candidate_value>(seed_value+_varianz_region_grow*seed_value)) {
			return false;
		}
		return true;
	}
	private String[] getNeighborCandidates(String string) {
		int[] position = string2position(string);
		int x = position[0];
		int y = position[1];
		int z = position[2];
		String[] candidates = new String[6];
		candidates[0] = (x-1>=0)?(position2String(x-1, y, z)):"";
		candidates[1] = (y-1>=0)?(position2String(x, y-1, z)):"";
		candidates[2] = (z-1>=0)?(position2String(x, y, z-1)):"";
		candidates[3] = (x+1<_w)?(position2String(x+1, y, z)):"";
		candidates[4] = (y+1<_h)?(position2String(x, y+1, z)):"";
		candidates[5] = (z+1<getNumberOfImages())?(position2String(x, y, z+1)):"";
		return candidates;
	}
	private int[] string2position(String string) {
		int[] index = new int[3];
		if (string.length()!=6) {
			index[0] = -1;
			return index;
		}
		index[0] = Integer.parseInt(string.substring(0,2),16);
		index[1] = Integer.parseInt(string.substring(2,4),16);
		index[2] = Integer.parseInt(string.substring(4,6),16);
		return index;
	}
	private String position2String(int x,int y,int z) {
		String position = Integer.toHexString(0x100|x).substring(1)+Integer.toHexString(0x100|y).substring(1)+Integer.toHexString(0x100|z).substring(1);
		return position;
	}

	public void setDistance(int distance) {
		// TODO Auto-generated method stub
		notifyObservers(new Message(Message.M_DISTANCE_CHANGED,distance));
	}
}

