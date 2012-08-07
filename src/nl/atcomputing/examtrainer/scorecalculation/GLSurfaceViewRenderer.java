package nl.atcomputing.examtrainer.scorecalculation;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
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
	private boolean showBalloons;
	private ShowScoreActivity activity;
	private Wind wind;
	private float screenBoundaryTop;
	private float screenBoundaryRight;
	private float screenBoundaryLeft;
	private float screenBoundaryBottom;
	private float screenWidth;
	private float screenHeight;
	
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
//		this.wind = new Wind();
//		this.wind.setWindChance(30);
//		this.wind.setWindSpeedUpperLimit(0.5f);
		this.mode = this.RUN;
	}
	
	public void onDrawFrame(GL10 gl) {
		if( this.mode != this.RUN ) {
			return;
		}
		
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
		gl.glViewport(0, 0, width, height);

		this.screenWidth = width;
		this.screenHeight = height;
		
		// make adjustments for screen ratio
		float ratio = (float) width / height;
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();                        // reset the matrix to its default state
		gl.glFrustumf(-ratio , ratio, -1f, 1f, 1.0f, 250f);
		updateScreenBoundaries();
		this.wind.setWindowSize(this.screenBoundaryTop, 
				this.screenBoundaryBottom, 
				this.screenBoundaryLeft,
				this.screenBoundaryRight);
		
		this.activity.startAnimation();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{
		Log.d("GLSurfaceViewRenderer", "onSurfaceCreated");
		
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
		int width = this.textures.getWidth(resource_id);
		int height = this.textures.getHeight(resource_id);
		if( this.screenWidth > this.screenHeight ) {
			double ratio = this.screenWidth / this.screenHeight;
			width *= ratio;
			height *= ratio;
		}
		
		this.balloons = new Balloon[this.amountOfBalloons];
		for( int i = 0; i < this.amountOfBalloons; i++ ) {
			int type = rng.nextInt(Balloon.AMOUNT_OF_TYPES);
			resource_id = Balloon.getReferenceDrawable(type);
			Balloon b = new Balloon(this.activity, type,
					this.textures.getTexture(resource_id), 
					width, height);
			b.x = this.screenBoundaryRight - (rng.nextFloat() * this.screenBoundaryRight * 2f);
			b.y = this.screenBoundaryTop;
			b.setLift(0.001f + rng.nextFloat()/8f);
			
			this.balloons[i] = b;
		}
		this.mode = this.RUN;
		this.showBalloons = true;
	}

	protected void updateScreenBoundaries() {
		Frustum f = new Frustum();
		f.extractFromOGL();

		this.screenBoundaryBottom = f.getWorldCoordinateBottom(this.zoom);
		this.screenBoundaryTop = f.getWorldCoordinateTop(this.zoom);
		this.screenBoundaryLeft = f.getWorldCoordinateLeft(this.zoom);
		this.screenBoundaryRight = f.getWorldCoordinateRight(this.zoom);
	}
}