
package nl.atcomputing.examtrainer;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author martijn brekhof
 *
 */

public class ShowScoreView extends ShowScoreBalloonView {

    private static final String TAG = "ShowScoreBalloonView";
    private static final int DELAY = 100;
    
    private int currentMode = RUNNING;
    protected static final int PAUSE = 0;
    protected static final int RUNNING = 1;
    
    private final int RED_BALLOON = 0;
    private final int BLUE_BALLOON = 1;

    private TextView textView;

    private static final Random randomNumberGenerator = new Random();
    
    private RefreshHandler redrawHandler = new RefreshHandler();

    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            ShowScoreView.this.update();
            ShowScoreView.this.invalidate();
        }

        public void sleep(long delayMillis) {
        	this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };

    public ShowScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
   }

    private void init() {
    	Resources r = this.getContext().getResources();
        
        loadBalloon(BLUE_BALLOON, r.getDrawable(R.drawable.aj_balloon_blue_64));
        loadBalloon(RED_BALLOON, r.getDrawable(R.drawable.aj_balloon_red_64));
        
        addBalloon(RED_BALLOON);
        addBalloon(BLUE_BALLOON);
        
        setBalloonCoords();
    }

    protected Bundle saveState() {
    	Bundle bundle = new Bundle();
    	return bundle;
    }
    
    protected void restoreState(Bundle bundle) {
        //We are restored from stopped state
    	//We need to get the balloon coordinates from the bundle
    }
    
    protected void setTextView(TextView view) {
        textView = view;
    }

    protected void setMode(int mode) {
    	currentMode = mode;
    }
    
    protected void update() {
        if (currentMode == RUNNING) {
        	updateBalloons();
            redrawHandler.sleep(DELAY);
        }

    }
    
    private void updateBalloons() {
    	//update coordinate of balloons
    }
}
