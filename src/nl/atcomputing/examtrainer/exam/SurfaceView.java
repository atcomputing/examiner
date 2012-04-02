package nl.atcomputing.examtrainer.exam;

import nl.popdaballoons.games.Game;
import android.content.Context;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;

public class SurfaceView extends GLSurfaceView {

	private static int MODE_NONE = 0; 
	private static int MODE_SINGLE_TOUCH = 1;
	private static int MODE_DOUBLE_TOUCH = 2;
	private int mode = MODE_NONE;
	private GLSurfaceViewRenderer renderer;
	private float prevDistanceBetweenFingers = 0;
	
    public SurfaceView(Context context, Game game) {
        super(context);
        SensorManager sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        this.renderer = new GLSurfaceViewRenderer(context, sensorManager, game);
        setRenderer(this.renderer);  
    }
    
    public void onPause() {
    	super.onPause();
    	this.renderer.stop();
    }

    public void onResume() {
    	super.onResume();
    	this.renderer.start();
    }
    
    public boolean onTouchEvent(MotionEvent e) {
        //dumpEvent(e);
        switch (e.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_MOVE:
        	if( mode == MODE_DOUBLE_TOUCH ) {
        		float dx = e.getX(0) - e.getX(1);
            	float dy = e.getY(0) - e.getY(1);
            	float sum = dx*dx + dy*dy;
            	if ( sum > this.prevDistanceBetweenFingers ) {
            		this.renderer.zoomIn();
            	} else {
            		this.renderer.zoomOut();
            	}
            	this.prevDistanceBetweenFingers = sum;
        	}
        	break;
        case MotionEvent.ACTION_DOWN:
        	//first finger touched
        	mode = MODE_SINGLE_TOUCH;
        	break;
        case MotionEvent.ACTION_POINTER_DOWN:
        	//second finger touched
        	mode = MODE_DOUBLE_TOUCH;
        	this.prevDistanceBetweenFingers = 0;
        	break;
        default:
        	mode = MODE_NONE;
        }
        return true;
    }
    
    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
       String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
          "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
       StringBuilder sb = new StringBuilder();
       int action = event.getAction();
       int actionCode = action & MotionEvent.ACTION_MASK;
       sb.append("event ACTION_" ).append(names[actionCode]);
       if (actionCode == MotionEvent.ACTION_POINTER_DOWN
             || actionCode == MotionEvent.ACTION_POINTER_UP) {
          sb.append("(pid " ).append(
          action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
          sb.append(")" );
       }
       sb.append("[" );
       for (int i = 0; i < event.getPointerCount(); i++) {
          sb.append("#" ).append(i);
          sb.append("(pid " ).append(event.getPointerId(i));
          sb.append(")=" ).append((int) event.getX(i));
          sb.append("," ).append((int) event.getY(i));
          if (i + 1 < event.getPointerCount())
             sb.append(";" );
       }
       sb.append("]" );
       Log.d("SurfaceView", sb.toString());
    }
}