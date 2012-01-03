package nl.atcomputing.examtrainer;

import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * @author martijn brekhof
 * 
 */

public class ShowScoreView extends ShowScoreBalloonView {

	private static final String TAG = "ShowScoreView";
	private static final int DELAY = 100;

	private int mode = RUNNING;
	protected static final int RUNNING = 0;
	protected static final int PAUSE = 1;
	
	private int displayWidth;
	private int displayHeight;

	private int amountOfBalloons;
	private int amountOfBalloonBitmaps;
	private Bitmap[] balloonBitmaps;
	
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

		Display display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		displayWidth = display.getWidth();
		displayHeight = display.getHeight();

		init();
	}

	private void init() {
    	Resources r = this.getContext().getResources();
    	
    	amountOfBalloonBitmaps = 2;
    	
    	balloonBitmaps = new Bitmap[amountOfBalloonBitmaps];
    	
        balloonBitmaps[0] = createBitmap(r.getDrawable(R.drawable.aj_balloon_blue_64));
        balloonBitmaps[1] = createBitmap(r.getDrawable(R.drawable.aj_balloon_red_64));
        
        amountOfBalloons = 2;
        
        for(int i = 0; i < amountOfBalloons; i++) {
        	Bitmap bitmap = balloonBitmaps[randomNumberGenerator.nextInt(2)];
        	int x = randomNumberGenerator.nextInt(displayWidth);
        	int y = randomNumberGenerator.nextInt(displayHeight);
        	Log.d(TAG, "Balloon: " + x + "," + y );
        	Balloon b = new Balloon(x, y, bitmap);
        	addBalloon(b);
        }
    }

	protected void setTextView(TextView view) {
		//textView = view;
	}

	protected void setMode(int mode) {
		this.mode = mode;
	}
	
	protected void update() {
		if( mode == RUNNING ) {
			updateBalloons();
			redrawHandler.sleep(DELAY);
		}
	}

	private void updateBalloons() {
		
		// update coordinate of balloons
		for(int i = 0; i < amountOfBalloons; i++) {
			//Range from -2 to +2
        	int x = randomNumberGenerator.nextInt(11) - 5;
        	int y = randomNumberGenerator.nextInt(11) - 5;
        	Log.d(TAG, "updateBalloons: " + x + "," + y);
        	this.moveBalloon(i, x, y);
        }
	}
}
