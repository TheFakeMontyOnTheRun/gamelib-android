package br.odb.gamelib.android.geometry;
 
import android.util.Log;
import br.odb.libstrip.IndexedSetFace;
import br.odb.libstrip.Mesh;
import br.odb.utils.Color;
import br.odb.utils.math.Vec3;
import br.odb.libstrip.GeneralTriangle;

public class GLESMesh extends Mesh {
	
	public GLESMesh(Mesh other) {
		IndexedSetFace face;
		Color color;
		Vec3 v;

		Log.d("bzk3", "about to copy " + other.faces.size() + " faces");

		for (int c = 0; c < other.getTotalItems(); ++c) {
			Log.d("bzk3", "copying faces:" + ((c * 100) / other.faces.size()));
			face = other.getFace(c);
			try {
				switch (face.getTotalIndexes()) {
				case 3:
					if (face instanceof GLES1Triangle) {
						Log.d("bzk3", "makecopy do triangulo");
						addFace(face.makeCopy());
					} else {
						GLES1Triangle t = new GLES1Triangle();
						color = face.getColor();
						Log.d("bzk3", "munhecando um triangulo. t=" + t
								+ " face=" + face + " color=" + color);
						t.r = color.getR();
						t.g = color.getG();
						t.b = color.getB();
						t.a = color.getA();
						v = face.getVertex(face.getIndex(0));
						t.x0 = v.x;
						t.y0 = v.y;
						t.z0 = v.z;
						v = face.getVertex(face.getIndex(1));
						t.x1 = v.x;
						t.y1 = v.y;
						t.z1 = v.z;
						v = face.getVertex(face.getIndex(2));
						t.x2 = v.x;
						t.y2 = v.y;
						t.z2 = v.z;
						t.flushToGLES();
						addFace(t);
					}
					break;
				case 4:
					if (face instanceof GLES1Square) {
						Log.d("bzk3", "makeCopy do quadrado");
						addFace(face.makeCopy());
					} else {
						Log.d("bzk3", "munhecando um quadrado");
						GLES1Square s = new GLES1Square();
						v = face.getVertex(face.getIndex(0));
						s.vertices[0] = v.x;
						s.vertices[1] = v.y;
						s.vertices[2] = v.z;
						v = face.getVertex(face.getIndex(1));
						s.vertices[3] = v.x;
						s.vertices[4] = v.y;
						s.vertices[5] = v.z;
						v = face.getVertex(face.getIndex(2));
						s.vertices[6] = v.x;
						s.vertices[7] = v.y;
						s.vertices[8] = v.z;
						v = face.getVertex(face.getIndex(3));
						s.vertices[9] = v.x;
						s.vertices[10] = v.y;
						s.vertices[11] = v.z;

						color = face.getColor();
						s.color[0] = color.getR() / 255.0f;
						s.color[1] = color.getG() / 255.0f;
						s.color[2] = color.getB() / 255.0f;
						s.color[3] = color.getA() / 255.0f;
						
						for ( int d = 0; d < s.color.length; ++d ) {
							s.colorBits[ d ] = Float.floatToRawIntBits( s.color[ d ] );
						}
						
						s.flushToGLES();
					}
					break;
				default:
					Log.d("bzk3", "poligono gen??????????????????rico");
					addFace(face.makeCopy());
				}
			} catch (Exception e) {

			}
		}
	}
	
	public GLESMesh() {
		super();
	}

	public GLESMesh(String name) {
		super( name );
	}

	public void preBuffer() {
		
		if ( manager != null )
			return;
		
		manager = new GLESVertexArrayManager();
		manager.init( faces.size() );
		
		for ( IndexedSetFace trig : faces ) {
			
			( ( GeneralTriangle ) trig ).flush();
			
			if ( trig instanceof GLES1Triangle )
				manager.pushIntoFrameAsStatic( ( ( GLES1Triangle ) trig ).verticesBits, ( ( GLES1Triangle ) trig ).colorBits );
			else if ( trig instanceof GLES1Square )
					manager.pushIntoFrameAsStatic( ( ( GLES1Square ) trig ).verticesBits, ( ( GLES1Square ) trig ).colorBits );
			else
				manager.pushIntoFrameAsStatic( ( ( GeneralTriangle ) trig ).getVertexData(), ( ( GeneralTriangle ) trig ).singleColorData() );
		}
	}
}
