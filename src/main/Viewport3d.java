package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.PointArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture2D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.View;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;

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
		public TransformGroup _transformGroup;
		public Transform3D _transform3d;
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
			_transformGroup = transformGroup;
			_transform3d = new Transform3D();
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
	private Viewport2d _view2d;
	private Shape3D[] _planes = null;
	private int _rendering_mode = 0;
	private int _step = 2;

	/**
	 * Constructor, with a reference to the global image stack as argument.
	 * @param slices a reference to the global image stack
	 */
	public Viewport3d() {
		super();
		_view2d = LabMed.get_v2d();
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
		int mode = _view2d.getViewMode();
		TransformGroup transformGroup = new TransformGroup();
		if (_planes == null) {
			update_planes();
		}
		//Textur basiertes Volume Rendering
		for (int i = 0; i < _planes.length; i++) {
			Shape3D shape3d = new Shape3D(_planes[i].getGeometry(), _planes[i].getAppearance());
			transformGroup.addChild(shape3d);
		}
		//shape plane ortho slice
		int active_img_id = _slices.getActiveImageID();
		Shape3D shape3d_plane = createShapePlane(mode, active_img_id, 0x80);
		transformGroup.addChild(shape3d_plane);
		//Segment Darstellung
		for (String seg_name : _map_name_to_seg.keySet()) {
			Shape3D shape3d = computeSegmentShape(seg_name);
			if (shape3d == null) {
				continue;
			}
			transformGroup.addChild(shape3d);
		}
		
		//update
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
	    
	    _panel3d._transformGroup.getTransform(_panel3d._transform3d);
	    transformGroup.setTransform(_panel3d._transform3d);
		_panel3d._scene.detach();
		_panel3d._scene = new BranchGroup();
		_panel3d._scene.setCapability(BranchGroup.ALLOW_DETACH);
		_panel3d._transformGroup = transformGroup;
		_panel3d._scene.addChild(_panel3d._transformGroup);
		_panel3d._scene.compile();
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
		if (msg._type == Message.M_NEW_ACTIVE_IMAGE) {
			update_view();
		}
		if (msg._type == Message.M_WINDOW_CHANGED) {
			update_planes();
//			update_view();
		}
	}
	
	
	public Shape3D computeSegmentShape(String seg_name) {
		Segment segment = _slices.getSegment(seg_name);
		int count = 0;
		int w = _slices.getImageWidth();
		int h = _slices.getImageHeight();
		int num_images = _slices.getNumberOfImages();
		int counts = w*h*num_images;
		Point3f[] point3fs = new Point3f[counts];
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
						point3fs[count]=p;
						count++;
					}
				}
			}
		}
		if (count==0) {
			System.out.println("no points for"+seg_name);
			return null;
		}
		System.out.println(seg_name+": "+count+" points");
		PointArray points = new PointArray(count, PointArray.COORDINATES);
		for (int i = 0; i < count; i++) {
			points.setCoordinate(i, point3fs[i]);
		}
		
		Color3f color = new Color3f(new Color(segment.getColor()));
		ColoringAttributes color_ca = new ColoringAttributes(color,ColoringAttributes.FASTEST);
		Appearance appearance_segment = new Appearance();
		appearance_segment.setColoringAttributes(color_ca);
		Shape3D shape3d = new Shape3D(points, appearance_segment);
		return shape3d;
	}
	public void update_planes() {
		int counts = 0;
		switch (_rendering_mode) {
		case 0:
			counts = (int) Math.floor((float)(_slices.getNumberOfImages())/_step);
			_planes = new Shape3D[counts];
			for (int i = 0; i < _planes.length; i++) {
				Shape3D plane = createShapePlane(0, i*_step,-1);
				_planes[i] = plane;
			}
			break;
		case 1:
			counts = (int) Math.floor((float)(_slices.getImageWidth())/_step);
			_planes = new Shape3D[counts];
			for (int i = 0; i < _planes.length; i++) {
				Shape3D plane = createShapePlane(1, i*_step,-1);
				_planes[i] = plane;
			}
			break;
		case 2:
			counts = (int) Math.floor((float)(_slices.getImageHeight())/_step);
			_planes = new Shape3D[counts];
			for (int i = 0; i < _planes.length; i++) {
				Shape3D plane = createShapePlane(2, i*_step,-1);
				_planes[i] = plane;
			}
			break;
		case 3:
			int counts0 = (int) Math.floor((float)(_slices.getNumberOfImages())/_step);
			int counts1 = (int) Math.floor((float)(_slices.getImageWidth())/_step);
			int counts2 = (int) Math.floor((float)(_slices.getImageHeight())/_step);
			counts = counts0+counts1+counts2;
			_planes = new Shape3D[counts];
			int i = 0;
			for (i = 0; i < counts0; i++) {
				Shape3D plane = createShapePlane(0, i*_step,-1);
				_planes[i] = plane;
			}
			for (; i < counts0+counts1; i++) {
				Shape3D plane = createShapePlane(1, (i-counts0)*_step,-1);
				_planes[i] = plane;
			}
			for (; i < counts; i++) {
				Shape3D plane = createShapePlane(2, (i-counts0-counts1)*_step,-1);
				_planes[i] = plane;
			}
			break;
		default:
			break;
		}
		System.out.println(counts);
	}
	public Shape3D createShapePlane(int mode,int active_img_id,int alpha) {
		int num_images=0;
		QuadArray qa = new QuadArray(4, QuadArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);
		Point3d a = null,b = null,c = null,d = null;
		switch (mode) {
		case 0:
			num_images = _slices.getNumberOfImages();
			a = new Point3d(0.5f,-0.5f,(active_img_id-(float)num_images/2)/(float)num_images);
			b = new Point3d(-0.5f,-0.5f,(active_img_id-(float)num_images/2)/(float)num_images);
			c = new Point3d(-0.5f,0.5f,(active_img_id-(float)num_images/2)/(float)num_images);
			d = new Point3d(0.5f,0.5f,(active_img_id-(float)num_images/2)/(float)num_images);
			break;
		case 1:
			num_images = _slices.getImageWidth();
			a = new Point3d((active_img_id-(float)num_images/2)/(float)num_images,0.5f,-0.5f);
			b = new Point3d((active_img_id-(float)num_images/2)/(float)num_images,-0.5f,-0.5f);
			c = new Point3d((active_img_id-(float)num_images/2)/(float)num_images,-0.5f,0.5f);
			d = new Point3d((active_img_id-(float)num_images/2)/(float)num_images,0.5f,0.5f);
			break;
		case 2:
			num_images = _slices.getImageHeight();
			a = new Point3d(0.5f,(active_img_id-(float)num_images/2)/(float)num_images,-0.5f);
			b = new Point3d(-0.5f,(active_img_id-(float)num_images/2)/(float)num_images,-0.5f);
			c = new Point3d(-0.5f,(active_img_id-(float)num_images/2)/(float)num_images,0.5f);
			d = new Point3d(0.5f,(active_img_id-(float)num_images/2)/(float)num_images,0.5f);
			break;
		default:
			break;
		}
		qa.setCoordinate(0, c);
		qa.setCoordinate(1, d);
		qa.setCoordinate(2, a);
		qa.setCoordinate(3, b);
		qa.setTextureCoordinate(0, 0, new TexCoord2f(0.0f,0.0f));
		qa.setTextureCoordinate(0, 1, new TexCoord2f(1.0f,0.0f));
		qa.setTextureCoordinate(0, 2, new TexCoord2f(1.0f,1.0f));
		qa.setTextureCoordinate(0, 3, new TexCoord2f(0.0f,1.0f));
		
		//Texture
		BufferedImage image = _view2d.getBGImage(mode, active_img_id,alpha);
		ImageComponent2D i2d = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, image);
		Texture2D tex = new Texture2D(Texture2D.BASE_LEVEL,Texture2D.RGBA,image.getWidth(),image.getHeight());
		tex.setImage(0, i2d);
		Appearance ap_plane = new Appearance();
		PolygonAttributes pa = new PolygonAttributes();
		pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		ap_plane.setPolygonAttributes(pa);
		ap_plane.setTexture(tex);
		TransparencyAttributes trans = new TransparencyAttributes();
		trans.setTransparencyMode(TransparencyAttributes.BLEND_SRC_ALPHA);
//		trans.setTransparency(0.0f);
		ap_plane.setTransparencyAttributes(trans);
		
		Shape3D shape3d_plane = new Shape3D(qa,ap_plane);
		return shape3d_plane;
	}

	public void setRenderingMode(int i) {
		// TODO Auto-generated method stub
		_rendering_mode  = i;
		update_planes();
		update_view();
	}
}
