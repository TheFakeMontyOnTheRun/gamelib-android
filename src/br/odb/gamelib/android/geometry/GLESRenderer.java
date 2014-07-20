/**

 * @author Daniel Monteiro
 * */

package br.odb.gamelib.android.geometry;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import br.odb.libstrip.IndexedSetFace;
import br.odb.libstrip.Mesh;
import br.odb.utils.math.Vec3;

/*
 * Concerns:
 * - show the world from the chosen actor's view.
 * - do it in the most efficient way
 * - so, work to keep showing stuff efficiently
 * */

public class GLESRenderer implements GLSurfaceView.Renderer {
	// ////GLES2 stuff/////

	private int mProgram;
	private int maPositionHandle;
	private int colorHandle;
	private int muMVPMatrixHandle;
	private float[] mMVPMatrix = new float[16];
	private float[] mMMatrix = new float[16];
	private float[] mVMatrix = new float[16];
	private float[] mProjMatrix = new float[16];

	private int loadShader(int type, String shaderCode) {

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}

	// ///////////////////
	public final Vec3 camera = new Vec3();
	public float angle;

	private GLESVertexArrayManager fixedGeometryManager;

	final GLESVertexArrayManager manager = new GLESVertexArrayManager();
	final private ArrayList<GLESIndexedSetFace> sceneGeometryToRender;
	final public ArrayList<GLESIndexedSetFace> fixedScreenShapesToRender;
	final public ArrayList<GLESIndexedSetFace> screenShapesToRender;
	final private ArrayList<Mesh> meshes = new ArrayList<Mesh>();

	private boolean shouldCheckForBailingOut;
	private String vertexShaderCode;
	private String fragmentShaderCode;

	// ------------------------------------------------------------------------------------------------------------
	public GLESRenderer(int maxVisiblePolys, String vertexShader,
			String fragmentShader) {
		super();

		this.vertexShaderCode = vertexShader;
		this.fragmentShaderCode = fragmentShader;

		sceneGeometryToRender = new ArrayList<GLESIndexedSetFace>();
		screenShapesToRender = new ArrayList<GLESIndexedSetFace>();
		fixedScreenShapesToRender = new ArrayList<GLESIndexedSetFace>();

		manager.init(maxVisiblePolys);
		manager.flush();

	}

	// ------------------------------------------------------------------------------------------------------------

	public void addGeometryToScene(GLESIndexedSetFace isf) {
		sceneGeometryToRender.add(isf);
	}

	public void addGeometryToScreen(GLESIndexedSetFace s) {
		screenShapesToRender.add(s);

	}

	// ------------------------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------------------------

	// ------------------------------------------------------------------------------------------------------------
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		onSurfaceChangedGLES20(width, height);
	}

	public void onSurfaceChangedGLES20(int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		float ratio = (float) width / height;
		float xmin, xmax, ymin, ymax;

		ymax = (float) (0.1f * Math.tan(45.0f * Math.PI / 360.0));
		ymin = -ymax;
		xmin = ymin * ratio;
		xmax = ymax * ratio;

		Matrix.frustumM(mProjMatrix, 0, xmin, xmax, ymin, ymax, 0.1f, 1024.0f);
		muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
	}

	// ------------------------------------------------------------------------------------------------------------
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		onSurfaceCreatedGLES20(config);
	}

	public void onSurfaceCreatedGLES20(EGLConfig config) {

		// Set the background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glClearDepthf(1.0f);
		GLES20.glEnable(GLES10.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES10.GL_LEQUAL);
		GLES20.glHint(GLES10.GL_PERSPECTIVE_CORRECTION_HINT, GLES10.GL_FASTEST);

		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderCode);

		mProgram = GLES20.glCreateProgram(); // create empty OpenGL Program
		GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader
														// to program
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment
															// shader to program
		GLES20.glLinkProgram(mProgram); // creates OpenGL program executables

		// get handle to the vertex shader's vPosition member
		maPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		colorHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
	}

	// -------------------------------------------------------------------------------------------------
	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		renderSceneGLES20();
	}

	/**
	 * @param gl
	 * @param mesh
	 */
	private void drawMeshGLES2(Mesh mesh) {

		if (!mesh.isVisible())
			return;

		if (mesh.manager != null) {
			mesh.manager.flush();
			((GLESVertexArrayManager) mesh.manager).drawGLES2(maPositionHandle,
					colorHandle);
		} else {
			for (IndexedSetFace face : mesh.faces) {
				((GLESIndexedSetFace) face).draw();
			}
		}
	}

	// ------------------------------------------------------------------------------------------------------------
	public void setAngle(float Angle) {
		angle = Angle;
		this.needsToResetView(true);
	}

	private void setCamera() {
		mVMatrix = new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0,
				1, };

		// Matrix.rotateM(mVMatrix, 0, -7.5f + accelerometerOffset.x, 1.0f, 0,
		// 0);
		// Matrix.rotateM(mVMatrix, 0, 0.125f + accelerometerOffset.y, 0, 1.0f,
		// 0);

		Matrix.rotateM(mVMatrix, 0, angle, 0, 1.0f, 0);

		Matrix.translateM(mVMatrix, 0, -camera.x, -camera.y, -camera.z);

		Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

		// Apply a ModelView Projection transformation
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

	}

	private void renderSceneGLES20() {

		// Add program to OpenGL environment
		GLES20.glUseProgram(mProgram);

		GLES20.glEnable(GL10.GL_DEPTH_TEST);
		// ////////////////////////////
		if (shouldCheckForBailingOut) {

			return;
		}

		// ////////////////////////////

		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);

		setCamera();

		if (fixedGeometryManager != null) {

			fixedGeometryManager.flush();
			fixedGeometryManager.drawGLES2(maPositionHandle, colorHandle);
		}

		for (GLESIndexedSetFace face : sceneGeometryToRender) {

			face.drawGLES2(maPositionHandle, colorHandle);
		}

		for (Mesh mesh : meshes) {
			drawMeshGLES2(mesh);
		}

		manager.flush();
		manager.drawGLES2(maPositionHandle, colorHandle);
	}

	public void addToVA(GLESIndexedSetFace face) {
		manager.pushIntoFrameAsStatic(face.getVertexData(), face.getColorData());
	}

	// ------------------------------------------------------------------------------------------------------------a

	public synchronized void needsToResetView(boolean fastReset) {
		shouldCheckForBailingOut = fastReset;
	}

	public void addToFixedGeometryToScreen(GLES1Triangle[] graphic) {
		for (int c = 0; c < graphic.length; ++c) {

			graphic[c].flatten(-1.0f);
			graphic[c].flushToGLES();
			this.fixedScreenShapesToRender.add(graphic[c]);
		}
	}

	public void detach() {

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (fixedGeometryManager != null)
			fixedGeometryManager.reinit();

		angle = 0;
		manager.reinit();
		sceneGeometryToRender.clear();
		screenShapesToRender.clear();
		meshes.clear();
		fixedGeometryManager = null;
	}

	public void clearScreenGeometry() {
		screenShapesToRender.clear();
		manager.clear();
	}

	public void addToMovingGeometryToScreen(GLES1Triangle[] graphic) {

		for (int c = 0; c < graphic.length; ++c) {

			graphic[c].flatten(-1.0f);
			graphic[c].flushToGLES();
			this.addGeometryToScreen(graphic[c]);
		}
	}
}
