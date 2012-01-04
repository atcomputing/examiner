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

	private Wind wind = new Wind();
	
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
			updateBalloons();
			redrawHandler.sleep(DELAY);
		}
	}

	
	
	private void updateBalloons() {
		
		int windSpeedHorizontal = wind.getWind();
		if(wind.getDirection() == Wind.Direction.LEFT ) {
			windSpeedHorizontal = -windSpeedHorizontal;
		}
		
		// update coordinate of balloons
		for(int i = 0; i < amountOfBalloons; i++) {
			//Range from -2 to +2
        	//int y = -10 + randomNumberGenerator.nextInt(5);
			int y = -2;
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
		if(b.getY() < 0) {
			b.setCoords(b.getX(), this.displayHeight);
		}
		if(Math.abs(b.getX()) > 1000) {
			Log.d(TAG, "Resetting balloon");
			b.setCoords(100, b.getY());
		}
		Log.d(TAG, "Balloon at "+b.getX()+","+b.getY());
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
