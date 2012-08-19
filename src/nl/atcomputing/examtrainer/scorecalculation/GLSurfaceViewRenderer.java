package nl.atcomputing.examtrainer.scorecalculation;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

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
	private boolean showBalloons;
	private ShowScoreActivity activity;
	private Wind wind;
	private float screenBoundaryTop;
	private float screenBoundaryRight;
	private float screenBoundaryLeft;
	private float screenBoundaryBottom;
//	private long startTime;
//	private final int framerate = 33; //framerate is milliseconds per update
	
	/**
	 * Prevent a nasty bug that calls onSurfaceChanged twice when returning
	 * after another app was active.
	 */
	private boolean onSurfaceChangedAlreadyCalled = false;
	
	public GLSurfaceViewRenderer(ShowScoreActivity activity) {
		super(activity);
		this.activity = activity;

		//Set this as Renderer
		this.setRenderer(this);
		//Request focus
		this.requestFocus();
		this.setFocusableInTouchMode(true);

		this.wind = new Wind();
		this.wind.setWindChance(30);
		this.wind.setWindSpeedUpperLimit(0.5f);

		this.showBalloons = false;
	}

	public void onPause() {
		this.mode = this.PAUSE;
	}

	public void onResume() {
		this.mode = this.RUN;
		this.onSurfaceChangedAlreadyCalled = false;
	}

	public void onDrawFrame(GL10 gl) {
		if( this.mode != this.RUN ) {
			return;
		}
//	    
//		long endTime = System.currentTimeMillis();
//	    long dt = endTime - this.startTime;
//	    if (dt < framerate) {
//			try {
//				Thread.sleep(framerate - dt);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//	    }
//	    this.startTime = System.currentTimeMillis();
	    
		//Clear Screen And Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();                      // reset the matrix to its default state
		GLU.gluLookAt(gl, 0f, 0f, this.zoom, 0f, 0f, 0f, 0f, -1.0f, 0f);
		if(this.showBalloons) {
			this.wind.update();
			for(Balloon b: this.balloons) {
				float windSpeedHorizontal = this.wind.getWind(-b.y);
				if(wind.getDirection() == Wind.Direction.LEFT ) {
					windSpeedHorizontal = -windSpeedHorizontal;
				}

				b.x += windSpeedHorizontal; 
				b.y -= b.getLift();

				if( (b.y + 1.5f) < this.screenBoundaryBottom ) {
					b.y = this.screenBoundaryTop + 1.5f;
				}
				if( (b.x + 1.5f) < this.screenBoundaryLeft ) {
					b.x = this.screenBoundaryRight + 1.5f;
				} else if ( (b.x - 1.5f) > this.screenBoundaryRight ) {
					b.x = this.screenBoundaryLeft - 1.5f;
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
		if( this.onSurfaceChangedAlreadyCalled ) {
			return;
		}
		
		gl.glViewport(0, 0, width, height);

		// make adjustments for screen ratio
		float hratio = (float) width / height;
		float vratio = 1.0f;
		if( height < width ) {
			vratio = (float) height / width;
			hratio = 1.0f;
		}
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();                        // reset the matrix to its default state
		gl.glFrustumf(-hratio , hratio, -vratio, vratio, 1.0f, 250f);
		updateScreenBoundaries();
		this.wind.setWindowSize(this.screenBoundaryTop, 
				this.screenBoundaryBottom, 
				this.screenBoundaryLeft,
				this.screenBoundaryRight);

		this.activity.setGLSurfaceReady();
		
		this.onSurfaceChangedAlreadyCalled = true;
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{
		gl.glEnable(GL10.GL_TEXTURE_2D);					//Enable Texture Mapping
		gl.glDisable(GL10.GL_DITHER);
		gl.glEnable(GL10.GL_BLEND);							//Enable blending
		gl.glDisable(GL10.GL_DEPTH_TEST);					//Disable depth test
		gl.glCullFace(GL10.GL_FRONT);						//only draw front of polygon
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		this.textures = new Textures(this.activity);
		this.textures.loadTextures(gl);
	}


	protected void showBalloons(int amount) {
		this.amountOfBalloons = amount;
		Random rng = new Random();

		int resource_id = Balloon.getReferenceDrawable(Balloon.TYPE_BLUE);
		
		this.balloons = new Balloon[this.amountOfBalloons];
		for( int i = 0; i < this.amountOfBalloons; i++ ) {
			int type = rng.nextInt(Balloon.AMOUNT_OF_TYPES);
			resource_id = Balloon.getReferenceDrawable(type);
			Balloon b = new Balloon(this.activity, type,
					this.textures.getTexture(resource_id));
			b.x = this.screenBoundaryRight - (rng.nextFloat() * this.screenBoundaryRight * 2f);
			b.y = this.screenBoundaryTop;
			b.setLift(0.005f + rng.nextFloat()/8f);

			this.balloons[i] = b;
		}
		this.mode = this.RUN;
		this.showBalloons = true;
	}

	protected void updateScreenBoundaries() {
		Frustum f = new Frustum();
		f.extractFromOGL();

		this.screenBoundaryBottom = f.getWorldCoordinateBottom(this.zoom) - 0.5f;
		this.screenBoundaryTop = f.getWorldCoordinateTop(this.zoom) + 0.5f;
		this.screenBoundaryLeft = f.getWorldCoordinateLeft(this.zoom) - 0.5f;
		this.screenBoundaryRight = f.getWorldCoordinateRight(this.zoom) + 0.5f;
	}
}