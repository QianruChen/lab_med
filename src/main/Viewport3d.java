package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.Material;
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
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.universe.SimpleUniverse;

import main.MarchingCube.Triangle;
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
			Transform3D transform3d = new Transform3D();
			transformGroup.setTransform(transform3d);
			
			MouseRotate mouseRotate = new MouseRotate();
		    MouseZoom mouseZoom = new MouseZoom();
		    MouseTranslate mouseTranslate = new MouseTranslate();
			transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
			transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			_transformGroup = transformGroup;
			_transform3d = transform3d;
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
	private MarchingCube _marchingCube;
	private int _magic_size = 0;
	private HashMap<String, GeometryArray> _seg_GeoArray_map_1;
	private HashMap<String, GeometryArray> _seg_GeoArray_map_2;
	private HashMap<String, GeometryArray> _seg_GeoArray_map_3;
	private HashMap<String, GeometryArray> _seg_GeoArray_map_4;
	

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
        _marchingCube = MarchingCube.getInstance();
        _marchingCube.create_lut();
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
		
		if (_show_bg) {
			//Textur basiertes Volume Rendering
			for (int i = 0; i < _planes.length; i++) {
				Shape3D shape3d = new Shape3D(_planes[i].getGeometry(), _planes[i].getAppearance());
				transformGroup.addChild(shape3d);
			} 
			//shape plane ortho slice
			int active_img_id = _slices.getActiveImageID();
			Shape3D shape3d_plane = createShapePlane(mode, active_img_id, 0x80);
			transformGroup.addChild(shape3d_plane);
		}
		
		
		if (_magic_size==0) {
			//Segment Darstellung
			for (String seg_name : _map_name_to_seg.keySet()) {
				Shape3D shape3d = computeSegmentPointShape(seg_name);
					if (shape3d == null) {
						continue;
					}
			transformGroup.addChild(shape3d);
			}
		}else {
					//Marching cube Darstellung
			for (String seg_name : _map_name_to_seg.keySet()) {
				Shape3D shape3d = computeSegmentTriangleShape(seg_name);
				transformGroup.addChild(shape3d);
			}
		}

		
		//Licht Darstellung
		Color3f light1Color = new Color3f(0.4f,0.4f,0.4f);
		Vector3f light1Direction = new Vector3f(4.0f,-7.0f,-12.0f);
		light1Direction.normalize();
		Color3f light2Color = new Color3f(0.4f,0.4f,0.4f);
		Vector3f light2Direction = new Vector3f(-6.0f,-2.0f,-1.0f);
		light2Direction.normalize();
		Color3f ambientColor = new Color3f(1.0f,1.0f,1.0f);
		
	    BoundingSphere boundingSphere;
	    boundingSphere = new BoundingSphere(new Point3d(0.0d,0.0d,0.0d),Double.MAX_VALUE);
	    
		DirectionalLight d_light_1 = new DirectionalLight();
	    d_light_1 = new DirectionalLight();
	    d_light_1.setInfluencingBounds(boundingSphere);
	    d_light_1.setColor(light1Color);
	    d_light_1.setDirection(light1Direction);
	    
		DirectionalLight d_light_2 = new DirectionalLight();
	    d_light_2 = new DirectionalLight();
	    d_light_2.setInfluencingBounds(boundingSphere);
	    d_light_2.setColor(light2Color);
	    d_light_2.setDirection(light2Direction);
	    
	    AmbientLight ambientLight = new AmbientLight(ambientColor);
	    ambientLight.setInfluencingBounds(boundingSphere);
		
		//Mouse Control
	    MouseRotate mouseRotate = new MouseRotate();
	    MouseWheelZoom mouseZoom = new MouseWheelZoom();
	    MouseTranslate mouseTranslate = new MouseTranslate();
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		Bounds bounds = new BoundingBox();
	    mouseRotate.setTransformGroup(transformGroup);
	    mouseRotate.setSchedulingBounds(bounds);
	    mouseZoom.setTransformGroup(transformGroup);
	    mouseZoom.setSchedulingBounds(bounds);
	    mouseTranslate.setTransformGroup(transformGroup);
	    mouseTranslate.setSchedulingBounds(bounds);
	    
	    _panel3d._transformGroup.getTransform(_panel3d._transform3d);
	    transformGroup.setTransform(_panel3d._transform3d);
		_panel3d._scene.detach();
		_panel3d._scene = new BranchGroup();
		_panel3d._scene.setCapability(BranchGroup.ALLOW_DETACH);
		_panel3d._transformGroup = transformGroup;
		
		_panel3d._scene.addChild(mouseRotate);
		_panel3d._scene.addChild(mouseZoom);
		_panel3d._scene.addChild(mouseTranslate);
		
		if (_magic_size!=0) {
			_panel3d._scene.addChild(d_light_1);
			_panel3d._scene.addChild(d_light_2);
			_panel3d._scene.addChild(ambientLight);
		}
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
	
	public Shape3D computeSegmentTriangleShape(String seg_name) {
		Segment segment = _slices.getSegment(seg_name);
		HashMap<Point3f, LinkedList<Integer>> point_indexs_map = new HashMap<>();
		int index = 0;
		int w = _slices.getImageWidth();
		int h = _slices.getImageHeight();
		int num_images = _slices.getNumberOfImages();
		BitMask bitMask_up;
		BitMask bitMask_down;
				
		Point3f trans = new Point3f();
		for (int z = 0; z < num_images-_magic_size; z = z+_magic_size) {
			bitMask_up = segment.getMask(z);
			bitMask_down = segment.getMask(z+_magic_size);
			for (int x = 0; x < w - _magic_size; x = x+_magic_size) {
				for (int y = 0; y < h - _magic_size; y = y+_magic_size) {
					int key = calculate_key(x, y, bitMask_up, bitMask_down);
					if (key == 0 || key == 255) {
						continue;
					}
					Triangle[] triangles = _marchingCube.get_Triangles(key);
					for (int i = 0; i < triangles.length; i++) {
						trans = new Point3f(x,y,z);
						Point3f a = new Point3f(triangles[i].getVertex0());
						Point3f b = new Point3f(triangles[i].getVertex1());
						Point3f c = new Point3f(triangles[i].getVertex2());
						a.scale(_magic_size);
						b.scale(_magic_size);
						c.scale(_magic_size);
						a.add(trans);
						b.add(trans);
						c.add(trans);
						
						a.set(a.x/w-0.5f, a.y/h-0.5f, a.z/num_images-0.5f);
						b.set(b.x/w-0.5f, b.y/h-0.5f, b.z/num_images-0.5f);
						c.set(c.x/w-0.5f, c.y/h-0.5f, c.z/num_images-0.5f);
						a.set(0 - a.x , 0 - a.z, 0 - a.y );
						b.set(0 - b.x , 0 - b.z, 0 - b.y );
						c.set(0 - c.x , 0 - c.z, 0 - c.y );
						
//						seg_trias.add(a);
//						seg_trias.add(b);
//						seg_trias.add(c);
//						System.out.println("a: "+a+",b: "+b+",c: "+c);
						if (point_indexs_map.containsKey(a)) {
							point_indexs_map.get(a).add(index);
						}else {
							LinkedList<Integer> indexs = new LinkedList<>();
							indexs.add(index);
							point_indexs_map.put(a, indexs);
						}
						if (point_indexs_map.containsKey(b)) {
							point_indexs_map.get(b).add(index+1);
						}else {
							LinkedList<Integer> indexs = new LinkedList<>();
							indexs.add(index+1);
							point_indexs_map.put(b, indexs);
						}
						if (point_indexs_map.containsKey(c)) {
							point_indexs_map.get(c).add(index+2);
						}else {
							LinkedList<Integer> indexs = new LinkedList<>();
							indexs.add(index+2);
							point_indexs_map.put(c, indexs);
						}
						index = index + 3;
					}
				}
			}
		}
		System.out.println(index);
		
		IndexedTriangleArray itrias = new IndexedTriangleArray(point_indexs_map.size(), IndexedTriangleArray.COORDINATES|IndexedTriangleArray.NORMALS, index);
		index = 0;
		Iterator<Point3f> iterator = point_indexs_map.keySet().iterator();
		while (iterator.hasNext()) {
			Point3f point3f = iterator.next();
			itrias.setCoordinate(index, point3f);
			for (Integer integer : point_indexs_map.get(point3f)) {
				itrias.setCoordinateIndex(integer, index);
			}
			index++;
		}
		Color3f color = new Color3f(new Color(segment.getColor()));
		Material material = new Material();
		material.setDiffuseColor(color);
		Appearance appearance_segment = new Appearance();
		appearance_segment.setMaterial(material);
		GeometryInfo geometryInfo = new GeometryInfo(itrias);
		NormalGenerator ng = new NormalGenerator();
		ng.generateNormals(geometryInfo);
		GeometryArray result = geometryInfo.getGeometryArray();
		Shape3D shape3d = new Shape3D(result, appearance_segment);
		return shape3d;
	}
	public int calculate_key(int x, int y, BitMask bitMask_up, BitMask bitMask_down) {
		int key = 0;
		key = bitMask_up.get(x, y) ? (key+1):key;
		key = bitMask_up.get(x+_magic_size, y) ? (key+(1<<1)):key;
		key = bitMask_up.get(x+_magic_size, y+_magic_size) ? (key+(1<<2)):key;
		key = bitMask_up.get(x, y+_magic_size) ? (key+(1<<3)):key;
		key = bitMask_down.get(x, y) ? (key+(1<<4)):key;
		key = bitMask_down.get(x+_magic_size, y) ? (key+(1<<5)):key;
		key = bitMask_down.get(x+_magic_size, y+_magic_size) ? (key+(1<<6)):key;
		key = bitMask_down.get(x, y+_magic_size) ? (key+(1<<7)):key;
		return key;
	}
	
	public Shape3D computeSegmentPointShape(String seg_name) {
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
						float x = (0 - i + (float)w/2)/(float)w;
						float z = (0 - j + (float)h/2)/(float)h;
						float y = (0 - k + (float)num_images/2)/(float)num_images;
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
		a.set(0 - a.x , 0 - a.z, 0 - a.y );
		b.set(0 - b.x , 0 - b.z, 0 - b.y );
		c.set(0 - c.x , 0 - c.z, 0 - c.y );
		d.set(0 - d.x , 0 - d.z, 0 - d.y );

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
		_rendering_mode  = i;
		update_planes();
		if (_slices.getNumberOfImages()!=0) {
			update_view();
		}
	}

	public void setMagic(int i) {
		_magic_size  = i;
		if (_slices.getNumberOfImages()!=0) {
			update_view();
		}
	}
}
