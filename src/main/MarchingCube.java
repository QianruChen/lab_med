package main;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.print.Printable;
import java.util.Arrays;
import java.util.HashMap;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.PointArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleArray;
import javax.swing.JFrame;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.universe.SimpleUniverse;

import main.MarchingCube.Triangle;


public class MarchingCube {
	public class Triangle{
		private float[] _vertex0;
		private float[] _vertex1;
		private float[] _vertex2;
		public Triangle(float[] vertex0,float[] vertex1,float[] vertex2) {
			// TODO Auto-generated constructor stub
			_vertex0 = vertex0;
			_vertex1 = vertex1;
			_vertex2 = vertex2;
		}
		public String triangle2String() {
			return vertex2String(_vertex0)+vertex2String(_vertex1)+vertex2String(_vertex2);
		}
		public String vertex2String(float[] vertex) {
			if (Arrays.equals(vertex, A)) return "A";
			if (Arrays.equals(vertex, B)) return "B";
			if (Arrays.equals(vertex, C)) return "C";
			if (Arrays.equals(vertex, D)) return "D";
			if (Arrays.equals(vertex, E)) return "E";
			if (Arrays.equals(vertex, F)) return "F";
			if (Arrays.equals(vertex, G)) return "G";
			if (Arrays.equals(vertex, H)) return "H";
			if (Arrays.equals(vertex, I)) return "I";
			if (Arrays.equals(vertex, J)) return "J";
			if (Arrays.equals(vertex, K)) return "K";
			if (Arrays.equals(vertex, L)) return "L";
			return " ";
		}
		public float[] getVertex0() {
			return _vertex0;
		}
		public float[] getVertex1() {
			return _vertex1;
		}
		public float[] getVertex2() {
			return _vertex2;
		}
		public Triangle rotate_x_tirangle() {
			float[] rotated_v0 = rotate_x_point(_vertex0);
			float[] rotated_v1 = rotate_x_point(_vertex1);
			float[] rotated_v2 = rotate_x_point(_vertex2);
			Triangle rotated_Triangle = new Triangle(rotated_v0, rotated_v1, rotated_v2);
			return rotated_Triangle;
		}
		public Triangle rotate_y_tirangle() {
			float[] rotated_v0 = rotate_y_point(_vertex0);
			float[] rotated_v1 = rotate_y_point(_vertex1);
			float[] rotated_v2 = rotate_y_point(_vertex2);
			Triangle rotated_Triangle = new Triangle(rotated_v0, rotated_v1, rotated_v2);
			return rotated_Triangle;
		}
		public Triangle rotate_z_tirangle() {
			float[] rotated_v0 = rotate_z_point(_vertex0);
			float[] rotated_v1 = rotate_z_point(_vertex1);
			float[] rotated_v2 = rotate_z_point(_vertex2);
			Triangle rotated_Triangle = new Triangle(rotated_v0, rotated_v1, rotated_v2);
			return rotated_Triangle;
		}
		public float[] rotate_x_point(float[] point) {
			float[] rotated_point = new float[3];
			switch (vertex2String(point)) {
			case "A":
				rotated_point = H.clone();
				break;
			case "B":
				rotated_point = D.clone();
				break;
			case "C":
				rotated_point = G.clone();
				break;
			case "D":
				rotated_point = L.clone();
				break;
			case "E":
				rotated_point = A.clone();
				break;
			case "F":
				rotated_point = C.clone();
				break;
			case "G":
				rotated_point = K.clone();
				break;
			case "H":
				rotated_point = I.clone();
				break;
			case "I":
				rotated_point = E.clone();
				break;
			case "J":
				rotated_point = B.clone();
				break;
			case "K":
				rotated_point = F.clone();
				break;
			case "L":
				rotated_point = J.clone();
				break;
			default:
				break;
			}
			return rotated_point;
		}
		public float[] rotate_y_point(float[] point) {
			float[] rotated_point = new float[3];
			switch (vertex2String(point)) {
			case "A":
				rotated_point = I.clone();
				break;
			case "B":
				rotated_point = E.clone();
				break;
			case "C":
				rotated_point = A.clone();
				break;
			case "D":
				rotated_point = H.clone();
				break;
			case "E":
				rotated_point = J.clone();
				break;
			case "F":
				rotated_point = B.clone();
				break;
			case "G":
				rotated_point = D.clone();
				break;
			case "H":
				rotated_point = L.clone();
				break;
			case "I":
				rotated_point = K.clone();
				break;
			case "J":
				rotated_point = F.clone();
				break;
			case "K":
				rotated_point = C.clone();
				break;
			case "L":
				rotated_point = G.clone();
				break;
			default:
				break;
			}
			return rotated_point;
		}
		public float[] rotate_z_point(float[] point) {
			float[] rotated_point = new float[3];
			switch (vertex2String(point)) {
			case "A":
				rotated_point = B.clone();
				break;
			case "B":
				rotated_point = C.clone();
				break;
			case "C":
				rotated_point = D.clone();
				break;
			case "D":
				rotated_point = A.clone();
				break;
			case "E":
				rotated_point = F.clone();
				break;
			case "F":
				rotated_point = G.clone();
				break;
			case "G":
				rotated_point = H.clone();
				break;
			case "H":
				rotated_point = E.clone();
				break;
			case "I":
				rotated_point = J.clone();
				break;
			case "J":
				rotated_point = K.clone();
				break;
			case "K":
				rotated_point = L.clone();
				break;
			case "L":
				rotated_point = I.clone();
				break;
			default:
				break;
			}
			return rotated_point;
		}
		public Triangle get_symmetryTriangle() {
			float[] vertex0 = _vertex0.clone();
			float[] vertex1 = _vertex2.clone();
			float[] vertex2 = _vertex1.clone();
			Triangle symTriangle = new Triangle(vertex0, vertex1, vertex2);
			return symTriangle;
		}
	}
	public class CubeCase{
		private int _key;
		private Triangle[] _Triangles;
		private int[] _keys;
		public CubeCase(int key, Triangle[] Triangles) {
			_key = key;
			_Triangles = Triangles.clone();
			_keys = key2bits(key);
		}
		public CubeCase(int[] keys, Triangle[] Triangles) {
			_keys = keys;
			_Triangles = Triangles.clone();
			_key = bits2key(keys);
		}
		public CubeCase get_sym_cubeCase() {
			int[] sym_keys = get_sym_keys();
			Triangle[] triangles = null;
			if (_key == 0||_key == 0b11111111) {
				return new CubeCase(sym_keys, triangles);
			}
			triangles = new Triangle[_Triangles.length];
			for (int i = 0; i < _Triangles.length; i++) {
				triangles[i] = _Triangles[i].get_symmetryTriangle();
			}
			return new CubeCase(sym_keys, triangles);
		}
		public int[] get_sym_keys() {
			int[] sym_keys = new int[8];
			for (int i = 0; i < sym_keys.length; i++) {
				sym_keys[i] = (_keys[i]==1)?0:1;
			}
			return sym_keys;
		}
		public int[] key2bits(int key) {
			int[] keys = new int[8];
			for (int i = 0; i < keys.length; i++) {
				keys[i] = (key>>i)&1;
			}
			return keys;
		}
		public int bits2key(int[] keys) {
			int key = 0;
			for (int i = 0; i < keys.length; i++) {
				key = key + (keys[i]<<i);
			}
			return key;
		}
		public CubeCase get_rotate_cubeCase(int rot_x,int rot_y,int rot_z) {
			CubeCase case_before = new CubeCase(_key, _Triangles);
//			System.out.println(case_before.get_Triangles_String());
			for (int i = rot_x; i > 0; i--) {
				CubeCase case_after = case_before.get_rotate_x_cubeCase();
				case_before = case_after;
//				System.out.println(case_before.get_Triangles_String());
			}
			for (int j = rot_y; j > 0; j--) {
				CubeCase case_after = case_before.get_rotate_y_cubeCase();
				case_before = case_after;
//				System.out.println(case_before.get_Triangles_String());
			}			
			for (int k = rot_z; k > 0; k--) {
				CubeCase case_after = case_before.get_rotate_z_cubeCase();
				case_before = case_after;
//				System.out.println(case_before.get_Triangles_String());
			}
			return case_before;
		}
		public CubeCase get_rotate_x_cubeCase() {
			int rotated_key = get_rotate_x_key();
			Triangle[] rotatedTriangles = new Triangle[_Triangles.length];
			for (int i = 0; i < _Triangles.length; i++) {
				rotatedTriangles[i] = _Triangles[i].rotate_x_tirangle();
			}
			CubeCase rotatedCubeCase = new CubeCase(rotated_key, rotatedTriangles);
			return rotatedCubeCase;
		}
		public CubeCase get_rotate_y_cubeCase() {
			int rotated_key = get_rotate_y_key();
			Triangle[] rotatedTriangles = new Triangle[_Triangles.length];
			for (int i = 0; i < _Triangles.length; i++) {
				rotatedTriangles[i] = _Triangles[i].rotate_y_tirangle();
			}
			CubeCase rotatedCubeCase = new CubeCase(rotated_key, rotatedTriangles);
			return rotatedCubeCase;
		}
		public CubeCase get_rotate_z_cubeCase() {
			int rotated_key = get_rotate_z_key();
			Triangle[] rotatedTriangles = new Triangle[_Triangles.length];
			for (int i = 0; i < _Triangles.length; i++) {
				rotatedTriangles[i] = _Triangles[i].rotate_z_tirangle();
			}
			CubeCase rotatedCubeCase = new CubeCase(rotated_key, rotatedTriangles);
			return rotatedCubeCase;
		}
		public int get_rotate_x_key() {
			int[] rotated_keys = new int[8];
			rotated_keys[0] = _keys[4];
			rotated_keys[1] = _keys[5];
			rotated_keys[2] = _keys[1];
			rotated_keys[3] = _keys[0];
			rotated_keys[4] = _keys[7];
			rotated_keys[5] = _keys[6];
			rotated_keys[6] = _keys[2];
			rotated_keys[7] = _keys[3];
			return bits2key(rotated_keys);
		}
		public int get_rotate_y_key() {
			int[] rotated_keys = new int[8];
			rotated_keys[0] = _keys[1];
			rotated_keys[1] = _keys[5];
			rotated_keys[2] = _keys[6];
			rotated_keys[3] = _keys[2];
			rotated_keys[4] = _keys[0];
			rotated_keys[5] = _keys[4];
			rotated_keys[6] = _keys[7];
			rotated_keys[7] = _keys[3];
			return bits2key(rotated_keys);
		}
		public int get_rotate_z_key() {
			int[] rotated_keys = new int[8];
			rotated_keys[0] = _keys[3];
			rotated_keys[1] = _keys[0];
			rotated_keys[2] = _keys[1];
			rotated_keys[3] = _keys[2];
			rotated_keys[4] = _keys[7];
			rotated_keys[5] = _keys[4];
			rotated_keys[6] = _keys[5];
			rotated_keys[7] = _keys[6];
			return bits2key(rotated_keys);
		}
		public int get_key() {
			return _key;
		}
		public Triangle[] get_Triangles() {
			return _Triangles;
		}
		public String get_key_String() {
			String keyString = "";
			keyString = String.format("%8s", Integer.toBinaryString(_key)).replace(' ', '0');
			return keyString;
		}
		public String get_Triangles_String() {
			String string = "{";
			for (int i = 0; i < _Triangles.length; i++) {
				if (i!=0) {
					string = string+",";
				}
				string = string+_Triangles[i].triangle2String();
			}
			string = string+"}";
			return string;
		}
	}
	private static MarchingCube _instance = null;
	private HashMap<String, Triangle[]> _key_Triangles_map = new HashMap<>();
	public static float[] A = {0.0f,0.5f,0.0f};
	public static float[] B = {0.5f,0.0f,0.0f};
	public static float[] C = {1.0f,0.5f,0.0f};
	public static float[] D = {0.5f,1.0f,0.0f};
	
	public static float[] E = {0.0f,0.0f,0.5f};
	public static float[] F = {1.0f,0.0f,0.5f};
	public static float[] G = {1.0f,1.0f,0.5f};
	public static float[] H = {0.0f,1.0f,0.5f};
	
	public static float[] I = {0.0f,0.5f,1.0f};
	public static float[] J = {0.5f,0.0f,1.0f};
	public static float[] K = {1.0f,0.5f,1.0f};
	public static float[] L = {0.5f,1.0f,1.0f};
	public MarchingCube() {
	}
	public static MarchingCube getInstance() {
		if (_instance == null) {
			_instance = new MarchingCube();
		}
		return _instance;
	}
	public void create_lut() {
		CubeCase rotatedCase = null;
		CubeCase basicCube = null;
		CubeCase basicCube_sym = null;
		CubeCase rotatedCase_sym = null;
		for (int i = 1; i < 15; i++) {
			basicCube = basic_case(i);
			basicCube_sym = basicCube.get_sym_cubeCase();
			_key_Triangles_map.put(basicCube.get_key_String(), basicCube.get_Triangles());
			_key_Triangles_map.put(basicCube_sym.get_key_String(), basicCube_sym.get_Triangles());
			for (int rot_x = 0; rot_x < 4; rot_x++) {
				for (int rot_y = 0; rot_y < 4; rot_y++) {
					for (int rot_z = 0; rot_z < 4; rot_z++) {
						rotatedCase = basicCube.get_rotate_cubeCase(rot_x, rot_y, rot_z);
						_key_Triangles_map.put(rotatedCase.get_key_String(), rotatedCase.get_Triangles());
						rotatedCase_sym = rotatedCase.get_sym_cubeCase();
						_key_Triangles_map.put(rotatedCase_sym.get_key_String(), rotatedCase_sym.get_Triangles());
					}
				}
			}
		}
	}
	public CubeCase basic_case(int case_num) {
		CubeCase cubeCase = null;
		Triangle[] triangles = null;
		Triangle triangle = null;
		
		int[] keys = new int[8];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = 0;
		}
		switch (case_num) {
		case 0:
			break;
		case 1:
			keys[7] = 1;
			triangles = new Triangle[1];
			triangle = new Triangle(I, H, L);
			triangles[0] = triangle;
			break;
		case 2:
			keys[6] = 1;
			keys[7] = 1;
			triangles = new Triangle[2];
			triangle = new Triangle(I, H, G);
			triangles[0] = triangle;
			triangle = new Triangle(I, G, K);
			triangles[1] = triangle;
			break;
		case 3:
			keys[2] = 1;
			keys[7] = 1;
			triangles = new Triangle[2];
			triangle = new Triangle(I, H, L);
			triangles[0] = triangle;
			triangle = new Triangle(D, C, G);
			triangles[1] = triangle;
			break;
		case 4:
			keys[1] = 1;
			keys[7] = 1;
			triangles = new Triangle[2];
			triangle = new Triangle(I, H, L);
			triangles[0] = triangle;
			triangle = new Triangle(B, F, C);
			triangles[1] = triangle;
			break;
		case 5:
			keys[4] = 1;
			keys[5] = 1;
			keys[6] = 1;
			triangles = new Triangle[3];
			triangle = new Triangle(E, I, L);
			triangles[0] = triangle;
			triangle = new Triangle(E, L, G);
			triangles[1] = triangle;
			triangle = new Triangle(E, G, F);
			triangles[2] = triangle;
			break;
		case 6:
			keys[1] = 1;
			keys[6] = 1;
			keys[7] = 1;
			triangles = new Triangle[3];
			triangle = new Triangle(I, H, G);
			triangles[0] = triangle;
			triangle = new Triangle(I, G, K);
			triangles[1] = triangle;
			triangle = new Triangle(B, F, C);
			triangles[2] = triangle;
			break;
		case 7:
			keys[1] = 1;
			keys[3] = 1;
			keys[6] = 1;
			triangles = new Triangle[3];
			triangle = new Triangle(A, D, H);
			triangles[0] = triangle;
			triangle = new Triangle(B, F, C);
			triangles[1] = triangle;
			triangle = new Triangle(G, K, L);
			triangles[2] = triangle;
			break;
		case 8:
			keys[4] = 1;
			keys[5] = 1;
			keys[6] = 1;
			keys[7] = 1;
			triangles = new Triangle[2];
			triangle = new Triangle(E, H, F);
			triangles[0] = triangle;
			triangle = new Triangle(F, H, G);
			triangles[1] = triangle;
			break;
		case 9:
			keys[0] = 1;
			keys[4] = 1;
			keys[5] = 1;
			keys[7] = 1;
			triangles = new Triangle[4];
			triangle = new Triangle(A, H, B);
			triangles[0] = triangle;
			triangle = new Triangle(B, H, L);
			triangles[1] = triangle;
			triangle = new Triangle(B, L, F);
			triangles[2] = triangle;
			triangle = new Triangle(F, L, K);
			triangles[3] = triangle;
			break;
		case 10:
			keys[1] = 1;
			keys[3] = 1;
			keys[5] = 1;
			keys[7] = 1;
			triangles = new Triangle[4];
			triangle = new Triangle(A, D, I);
			triangles[0] = triangle;
			triangle = new Triangle(D, L, I);
			triangles[1] = triangle;
			triangle = new Triangle(C, B, J);
			triangles[2] = triangle;
			triangle = new Triangle(C, J, K);
			triangles[3] = triangle;
			break;
		case 11:
			keys[1] = 1;
			keys[4] = 1;
			keys[5] = 1;
			keys[7] = 1;
			triangles = new Triangle[4];
			triangle = new Triangle(E, H, L);
			triangles[0] = triangle;
			triangle = new Triangle(B, E, C);
			triangles[1] = triangle;
			triangle = new Triangle(C, E, L);
			triangles[2] = triangle;
			triangle = new Triangle(C, L, K);
			triangles[3] = triangle;
			break;
		case 12:
			keys[3] = 1;
			keys[4] = 1;
			keys[5] = 1;
			keys[6] = 1;
			triangles = new Triangle[4];
			triangle = new Triangle(A, D, H);
			triangles[0] = triangle;
			triangle = new Triangle(F, E, G);
			triangles[1] = triangle;
			triangle = new Triangle(E, I, G);
			triangles[2] = triangle;
			triangle = new Triangle(G, I, L);
			triangles[3] = triangle;
			break;
		case 13:
			keys[0] = 1;
			keys[2] = 1;
			keys[5] = 1;
			keys[7] = 1;
			triangles = new Triangle[4];
			triangle = new Triangle(A, E, B);
			triangles[0] = triangle;
			triangle = new Triangle(C, G, D);
			triangles[1] = triangle;
			triangle = new Triangle(F, J, K);
			triangles[2] = triangle;
			triangle = new Triangle(I, H, L);
			triangles[3] = triangle;
			break;
		case 14:
			keys[0] = 1;
			keys[4] = 1;
			keys[5] = 1;
			keys[6] = 1;
			triangles = new Triangle[4];
			triangle = new Triangle(A, F, B);
			triangles[0] = triangle;
			triangle = new Triangle(A, I, L);
			triangles[1] = triangle;
			triangle = new Triangle(A, L, F);
			triangles[2] = triangle;
			triangle = new Triangle(F, L, G);
			triangles[3] = triangle;
			break;
		default:
			break;
		}
		cubeCase = new CubeCase(keys, triangles);
		return cubeCase;
	}
	public Triangle[] get_Triangles(int key) {
		return _key_Triangles_map.get(String.format("%8s", Integer.toBinaryString(key)).replace(' ', '0'));
	}
	public int get_key_num() {
		return _key_Triangles_map.size();
	}
	public HashMap<String, Triangle[]> get_key_Triangles_map() {
		return _key_Triangles_map;
	}
	public void print_cube(int key) {
		Triangle[] triangles = get_Triangles(key);
		
		String string = String.format("%8s", Integer.toBinaryString(key)).replace(' ', '0')+": {";
		for (int i = 0; i < triangles.length; i++) {
			if (i!=0) {
				string = string+",";
			}
			string = string+triangles[i].triangle2String();
		}
		string = string+"}";
		System.out.println(string);
	}
	public void show_cube(int key) {
		Triangle[] triangles = get_Triangles(key);
		TriangleArray triangleArray = new TriangleArray(triangles.length*3, TriangleArray.COORDINATES|TriangleArray.NORMALS);
		String keyString = String.format("%8s", Integer.toBinaryString(key)).replace(' ', '0');
		int index = 0;
		Point3f trans = new Point3f(-0.5f, -0.5f, -0.5f);
		for (int i = 0; i < triangles.length; i++) {
			Triangle triangle = triangles[i];
			Point3f p0 = new Point3f(triangle.getVertex0());
			Point3f p1 = new Point3f(triangle.getVertex1());
			Point3f p2 = new Point3f(triangle.getVertex2());
			p0.add(trans);
			p1.add(trans);
			p2.add(trans);
			triangleArray.setCoordinate(index, p0);
			triangleArray.setCoordinate(index+1, p1);
			triangleArray.setCoordinate(index+2, p2);
			index = index + 3;
		}
		Appearance appearance = new Appearance();
		Color3f color = new Color3f(new Color(0xff0000));
		ColoringAttributes coloringAttributes = new ColoringAttributes(color, ColoringAttributes.FASTEST);
		appearance.setColoringAttributes(coloringAttributes);
		Shape3D shape3d = new Shape3D(triangleArray, appearance);
		
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		Canvas3D canvas3d = new Canvas3D(config);
		SimpleUniverse su = new SimpleUniverse(canvas3d);
		su.getViewingPlatform().setNominalViewingTransform();
		BranchGroup obj_root = new BranchGroup(); 
		TransformGroup transformGroup = new TransformGroup();
		
		transformGroup.addChild(shape3d);
		
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
		
		obj_root.addChild(transformGroup);
		obj_root.compile();
		su.addBranchGraph(obj_root);
		// create a window to show the whole thing
		JFrame window = new JFrame(keyString);
		window.add(canvas3d ); 
		window.setSize(256 ,256);
		window. setVisible(true);
	}
}
