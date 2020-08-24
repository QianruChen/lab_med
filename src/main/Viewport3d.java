package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.PointArray;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;

import javafx.scene.shape.Shape3D;
import misc.BitMask;
import misc.MyObservable;
import misc.MyObserver;

/**
 * Three dimensional viewport for viewing the dicom images + segmentations.
 * 
 * @author  Karl-Ingo Friese
 */
public class Viewport3d extends Viewport implements MyObserver  {
	private static final long serialVersionUID = 1L;

	/**
	 * Private class, implementing the GUI element for displaying the 3d data.
	 */
	public class Panel3d extends Canvas3D{
		private static final long serialVersionUID = 1L;
		public SimpleUniverse _simple_u;
		public BranchGroup _scene;
		public Panel3d(GraphicsConfiguration config){	
			super(config);
			setMinimumSize(new Dimension(DEF_WIDTH,DEF_HEIGHT));
			setMaximumSize(new Dimension(DEF_WIDTH,DEF_HEIGHT));
			setPreferredSize(new Dimension(DEF_WIDTH,DEF_HEIGHT));
			setBackground(Color.black);

			_simple_u = new SimpleUniverse(this);
		    _simple_u.getViewingPlatform().setNominalViewingTransform();
		    _scene = null;

	        createScene();
	        super.getView().setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);          
		}
	 
		public void createScene() {
			if (_scene != null) {
				_scene.detach( );
			}
			_scene = new BranchGroup();
			_scene.setCapability( BranchGroup.ALLOW_DETACH );
			// create a ColorCube object of size 0.5
			ColorCube c = new ColorCube(0.5f);
			TransformGroup transformGroup = new TransformGroup();
			
			MouseRotate mouseRotate = new MouseRotate();
		    MouseZoom mouseZoom = new MouseZoom();
		    MouseTranslate mouseTranslate = new MouseTranslate();
			transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
			transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

			Bounds bounds = new BoundingBox();
			
		    mouseRotate.setTransformGroup(transformGroup);
		    transformGroup.addChild(mouseRotate);
		    mouseRotate.setSchedulingBounds(bounds);

		    mouseZoom.setTransformGroup(transformGroup);
		    transformGroup.addChild(mouseZoom);
		    mouseZoom.setSchedulingBounds(bounds);
		    
		    mouseTranslate.setTransformGroup(transformGroup);
		    transformGroup.addChild(mouseTranslate);
		    mouseTranslate.setSchedulingBounds(bounds);
		    
			transformGroup.addChild(c);
			// add ColorCube to SceneGraph
			_scene.addChild(transformGroup);
			_scene.compile();
			_simple_u.addBranchGraph(_scene);
		}

			
	}		


	private Panel3d _panel3d;
	private int _distance = 3;

	/**
	 * Constructor, with a reference to the global image stack as argument.
	 * @param slices a reference to the global image stack
	 */
	public Viewport3d() {
		super();
		
		this.setPreferredSize(new Dimension(DEF_WIDTH,DEF_HEIGHT));
		this.setLayout( new BorderLayout() );
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		_panel3d = new Panel3d( config );		
        this.add(_panel3d, BorderLayout.CENTER );        
	}

	/**
	 * calculates the 3d data structurs.
	 */
	public void update_view() {
		int w = _slices.getImageWidth();
		int h = _slices.getImageHeight();
		int num_images = _slices.getNumberOfImages();
		int counts = w*h*num_images;
		Point3f[] point3fs = new Point3f[counts];
//		_panel3d._scene.detach();
		TransformGroup transformGroup = new TransformGroup();
		for (String seg_name : _map_name_to_seg.keySet()) {
			Segment segment = _slices.getSegment(seg_name);
			Color3f color = new Color3f(new Color(segment.getColor()));
			ColoringAttributes color_ca = new ColoringAttributes(color,ColoringAttributes.FASTEST);
			int count = 0;
			for (int i = 0; i < _slices.getImageWidth(); i++) {
				if (i%_distance!=0) {
					continue;
				}
				for (int j = 0; j < _slices.getImageHeight(); j++) {
					if (j%_distance!=0) {
						continue;
					}
					for (int k = 0; k < _slices.getNumberOfImages(); k++) {
						if (k%_distance!=0) {
							continue;
						}
						if (segment.getMask(k).get(i, j)) {
							float x = (i - (float)w/2)/(float)w;
							float y = (j - (float)h/2)/(float)h;
							float z = (k - (float)num_images/2)/(float)num_images;
							Point3f p = new Point3f(x,y,z);
//							int index = h*num_images*i+num_images*j+k;
							point3fs[count]=p;
							count++;
						}
					}
				}
			}
			if (count==0) {
				System.out.println("no points");
				continue;
			}
			System.out.println(seg_name+": "+count+" points");
			PointArray points = new PointArray(count, PointArray.COORDINATES);
			for (int i = 0; i < count; i++) {
				points.setCoordinate(i, point3fs[i]);
			}
			Appearance appearance = new Appearance();
			appearance.setColoringAttributes(color_ca);
			javax.media.j3d.Shape3D shape3d = new javax.media.j3d.Shape3D(points, appearance);
			transformGroup.addChild(shape3d);
		}
		
	    MouseRotate mouseRotate = new MouseRotate();
	    MouseZoom mouseZoom = new MouseZoom();
	    MouseTranslate mouseTranslate = new MouseTranslate();
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		Bounds bounds = new BoundingBox();
		
	    mouseRotate.setTransformGroup(transformGroup);
	    transformGroup.addChild(mouseRotate);
	    mouseRotate.setSchedulingBounds(bounds);

	    mouseZoom.setTransformGroup(transformGroup);
	    transformGroup.addChild(mouseZoom);
	    mouseZoom.setSchedulingBounds(bounds);
	    
	    mouseTranslate.setTransformGroup(transformGroup);
	    transformGroup.addChild(mouseTranslate);
	    mouseTranslate.setSchedulingBounds(bounds);
	    
		_panel3d._scene.detach();
		_panel3d._scene = new BranchGroup();
		_panel3d._scene.setCapability(BranchGroup.ALLOW_DETACH);
		_panel3d._scene.addChild(transformGroup);
		_panel3d._simple_u.addBranchGraph(_panel3d._scene);
		
	}
	
	/**
	 * Implements the observer function update. Updates can be triggered by the global
	 * image stack.
	 */
	public void update(final MyObservable mo, final Message msg) {
		if (!EventQueue.isDispatchThread()) {
			// all swing thingies must be done in the AWT-EventQueue 
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					update(mo, msg);
				}
			});
			return;
		}

		if (msg._type == Message.M_SEG_CHANGED) {
			String seg_name = ((Segment)(msg._obj)).getName();
			boolean update_needed = _map_name_to_seg.containsKey(seg_name);
			if (update_needed) {
				update_view();
			}
		}
		if (msg._type == Message.M_DISTANCE_CHANGED) {
			_distance = (int)(msg._obj);
			update_view();
		}
	}
}
