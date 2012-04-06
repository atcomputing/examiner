package nl.atcomputing.examtrainer.exam.score;

import static android.opengl.GLES11.GL_MODELVIEW_MATRIX;
import static android.opengl.GLES11.GL_PROJECTION_MATRIX;
import static android.opengl.GLES11.glGetFloatv;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */

public class GLSurfaceViewRenderer extends GLSurfaceView implements Renderer {
	private final int PAUSE = 0;
	private final int RUN = 1;
	private int mode = PAUSE;
	private float zoom = -10f;
	private int amountOfBalloons;
	private Textures textures;
	private Balloon[] balloons;
	private Context context;
	private Wind wind;
	private float screenBoundaryTop;
	private float screenBoundaryRight;
	private float screenBoundaryLeft;
	private float screenBoundaryBottom;
	private float scaleFactor = 5f;
	
	public GLSurfaceViewRenderer(Context context) {
		super(context);
		this.context = context;

		//Set this as Renderer
		this.setRenderer(this);
		//Request focus
		this.requestFocus();
		this.setFocusableInTouchMode(true);

		this.wind = new Wind();
		this.wind.setWindChance(30);
		this.wind.setWindSpeedUpperLimit(0.5f);
	}

	public void onDrawFrame(GL10 gl) {
		//Clear Screen And Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	
		//gl.glEnable(GL10.GL_BLEND);			//Turn Blending On
		//gl.glDisable(GL10.GL_DEPTH_TEST);	//Turn Depth Testing Off

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();                      // reset the matrix to its default state
		GLU.gluLookAt(gl, 0f, 0f, this.zoom, 0f, 0f, 0f, 0f, -1.0f, 0f);
		if( this.mode == this.RUN ) {
			this.wind.update();
			for(Balloon b: this.balloons) {
				float windSpeedHorizontal = this.wind.getWind(-b.y);
				if(wind.getDirection() == Wind.Direction.LEFT ) {
					windSpeedHorizontal = -windSpeedHorizontal;
				}

				b.x += windSpeedHorizontal; 
				b.y -= b.getLift();
				
				if( b.y < this.screenBoundaryBottom ) {
					b.y = this.screenBoundaryTop;
				}
				if( b.x < this.screenBoundaryLeft ) {
					b.x = this.screenBoundaryRight;
				} else if ( b.x > this.screenBoundaryRight ) {
					b.x = this.screenBoundaryLeft;
				}
				
				gl.glPushMatrix();
				gl.glTranslatef(b.x, b.y, 0f);
				b.draw(gl);
				//gl.glTranslatef(-b.x, -b.y, 0f);
				gl.glPopMatrix();
			}
		}
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
		this.wind.setWindowSize(this.screenBoundaryTop, 
				this.screenBoundaryBottom, 
				this.screenBoundaryLeft,
				this.screenBoundaryRight);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{
		gl.glEnable(GL10.GL_TEXTURE_2D);					//Enable Texture Mapping

		gl.glEnable(GL10.GL_BLEND);							//Enable blending
		gl.glDisable(GL10.GL_DEPTH_TEST);					//Disable depth test
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.textures = new Textures(this.context);
		this.textures.loadTextures(gl);
		updateScreenBoundaries();
	}

	protected void showBalloons(int amount) {
		this.amountOfBalloons = amount;
		Random rng = new Random();
         
		this.balloons = new Balloon[this.amountOfBalloons];
		for( int i = 0; i < this.amountOfBalloons; i++ ) {
			int type = rng.nextInt(Balloon.AMOUNT_OF_TYPES);
			int resource_id = Balloon.getReferenceDrawable(type);
			Balloon b = new Balloon(this.context, type,
					this.textures.getTexture(resource_id), 
					this.textures.getWidth(resource_id),
					this.textures.getHeight(resource_id));
			b.x = this.screenBoundaryRight - (rng.nextFloat() * this.screenBoundaryRight * 2f);
			b.y = this.screenBoundaryTop;
			b.setLift(0.001f + rng.nextFloat()/8f);
			
			this.balloons[i] = b;
		}
		this.mode = this.RUN;
	}

	protected void updateScreenBoundaries() {
		Frustum f = new Frustum();
		f.extractFromOGL();

		this.screenBoundaryBottom = f.getWorldCoordinateBottom(this.zoom - 10);
		this.screenBoundaryTop = f.getWorldCoordinateTop(this.zoom - 10);
		this.screenBoundaryLeft = f.getWorldCoordinateLeft(this.zoom - 10);
		this.screenBoundaryRight = f.getWorldCoordinateRight(this.zoom - 10);
	}
}