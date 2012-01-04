package nl.atcomputing.examtrainer;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * @author martijn brekhof
 * 
 */

public class ShowScoreView extends View {

	private static final String TAG = "ShowScoreView";
	private static final int DELAY = 50;

	private static int balloonSizeX;
	private static int balloonSizeY;
	
	private static int windSpeedHorizontal = 0;
	private static final double windFactorHorizontal = 0.1;
	private static final double windFactorVertical = 0.1;
	
	//Convenience variable to prevent calculating
	//length of balloonArray
	private static int balloonCount;

	private ArrayList<Balloon> balloons = new ArrayList<Balloon>();

	private final Paint paint = new Paint();

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

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BalloonView);

		balloonSizeX = a.getInt(R.styleable.BalloonView_balloonSizeX, 64);
		balloonSizeY = a.getInt(R.styleable.BalloonView_balloonSizeY, 113);
		
		a.recycle();
		
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
        	int y = displayHeight;
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
			updateWind();
			updateBalloons();
			redrawHandler.sleep(DELAY);
		}
	}

	private void updateWind() {
		/**
		Randomly determine if a breeze comes up
			if so 
				set breeze to true
				determine randomly direction
				determine randomly duration
				determine randomly maxWindSpeed
				calculate incrementFactor
				calculate decrementFactor
		If breeze
				if ( windSpeed < maxWindSpeed )
					windSpeed = windSpeed * incrementFactor;
				else if ( duration < 0 )
					windSpeed = windSpeed * decrementFactor;
					if ( windSpeed == 0 )
						breeze = false;
			
		*/
		
		if((randomNumberGenerator.nextInt(10) > 7) && (!wind)) {
			wind = true;
			if(randomNumberGenerator.nextBool()) {
				windDirection = -1;
			} else {
				windDirection = 1;
			}
			
			
			maxWindSpeed = randomNumberGenerator.nextInt(MAX_WIND_SPEED);
			
			int incrementPeriod = randomNumberGenerator.nextInt(MAX_INCREMENT_PERIOD);
			incrementFactor = maxWindSpeed / incrementPeriod;
			
			int decrementPeriod = randomNumberGenerator.nextInt(MAX_INCREMENT_PERIOD);
			decrementFactor = maxWindSpeed / decrementPeriod;
			
			//in milliseconds
			windDuration = randomNumberGenerator.nextInt(MAX_WIND_DURATION) + 
					incrementPeriod + decrementPeriod;
		}
		
		if ( wind ) {
			if ( windSpeedHorizontal < maxWindSpeed )
				windSpeedHorizontal = windSpeed * incrementFactor;
			else if ( duration < 0 )
				windSpeedHorizontal = windSpeed * decrementFactor;
				if ( windSpeed == 0 )
					wind = false;
		}
	}
	
	private void updateBalloons() {
		
		// update coordinate of balloons
		for(int i = 0; i < amountOfBalloons; i++) {
			//Range from -2 to +2
        	int y = -20 + randomNumberGenerator.nextInt(5);
        	this.moveBalloon(i, windSpeedHorizontal, y);
        }
	}
	
	public Bitmap createBitmap(Drawable Balloon) {
		Bitmap bitmap = Bitmap.createBitmap(balloonSizeX, balloonSizeY, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Balloon.setBounds(0, 0, balloonSizeX, balloonSizeY);
		Balloon.draw(canvas);
		return bitmap;
	}

	protected void addBalloon(Balloon b) {
		balloons.add(b);
	}

	public void moveBalloon(int balloonNumber, int moveX, int moveY) {
		Balloon b = balloons.get(balloonNumber);
		b.move(moveX, moveY);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for ( Balloon b : balloons ) {
			canvas.drawBitmap(b.getBitmap(),
					b.getX(),
					b.getY(),
					paint);
		}
	}
}
