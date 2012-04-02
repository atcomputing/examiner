package nl.atcomputing.examtrainer.exam;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;

// http://developer.android.com/guide/topics/sensors/sensors_overview.html
//See also com.example.android.apis.os/RotationVectorDemo.java
//http://android-developers.blogspot.com/2010/09/one-screen-turn-deserves-another.html
//http://www.anddev.org/acceleration_-_orientation_visual_reference-t11818.html
//http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
//http://developer.android.com/reference/android/hardware/Sensor.html#TYPE_ACCELEROMETER

// OpenGL Tutorial http://blog.jayway.com/2009/12/03/opengl-es-tutorial-for-android-part-i/
// http://insanitydesign.com/wp/projects/nehe-android-ports/
// http://www.droidnova.com/android-3d-game-tutorial-part-i,312.html
// http://developer.android.com/guide/topics/graphics/opengl.html
// http://www.41post.com/1540/programming/android-opengl-get-the-modelview-matrix-on-15-cupcake

public class GLSurfaceViewRenderer extends GLSurfaceView implements Renderer {
	private Context context;
	private int mode = this.RUNNING;
	private final int RUNNING = 0;
	private final int PAUSE = 1;
	private float zoom = -7f;
	
	public GLSurfaceViewRenderer(Context context) {
		super(context);
		//Set this as Renderer
		this.setRenderer(this);
		//Request focus
		this.requestFocus();
		this.setFocusableInTouchMode(true);

		this.context = context;
	}

	public void onDrawFrame(GL10 gl) {
		//Clear Screen And Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	
		gl.glEnable(GL10.GL_BLEND);			//Turn Blending On
		gl.glDisable(GL10.GL_DEPTH_TEST);	//Turn Depth Testing Off

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();                      // reset the matrix to its default state
		GLU.gluLookAt(gl, 0f, 0f, this.zoom, 0f, 0f, 0f, 0f, 1.0f, 0f);
		this.update();
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{
		gl.glViewport(0, 0, width, height);

		// make adjustments for screen ratio
		float ratio = (float) width / height;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();                        // reset the matrix to its default state
		gl.glFrustumf(-ratio, ratio, -1f, 1f, 1.0f, 250f);


		updateScreenBoundaries();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{
		gl.glEnable(GL10.GL_TEXTURE_2D);					//Enable Texture Mapping
		//gl.glShadeModel(GL10.GL_SMOOTH); 					//Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 			//Black Background
		//gl.glClearDepthf(1.0f); 							//Depth Buffer Setup

		gl.glEnable(GL10.GL_BLEND);							//Enable blending
		gl.glDisable(GL10.GL_DEPTH_TEST);					//Disable depth test
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);

	}

	
	public void start() {
		
		this.mode = this.RUNNING;
	
	}

	public void stop() {
		this.mode = this.PAUSE;
	}

	protected void updateScreenBoundaries() {
		Frustum f = new Frustum();
		f.extractFromOGL();

		f.getWorldCoordinateBottom((float) this.zoom);
		f.getWorldCoordinateTop((float) this.zoom);
		f.getWorldCoordinateLeft((float) this.zoom);
		f.getWorldCoordinateRight((float) this.zoom);
	}

	protected void update() {
		if( this.mode == this.RUNNING ) {
			
		} else {
			
		}
	}
}