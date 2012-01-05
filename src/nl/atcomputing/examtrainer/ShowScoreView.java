package nl.atcomputing.examtrainer;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * @author martijn brekhof
 * 
 */

public class ShowScoreView extends View {

	private Context context;
	
	private TextView textView;
	
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

	private Wind wind;
	
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

		this.context = context;
		
		Display display = ((WindowManager) 
				context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		displayWidth = display.getWidth();
		displayHeight = display.getHeight();

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BalloonView);

		balloonSizeX = a.getInt(R.styleable.BalloonView_balloonSizeX, 64);
		balloonSizeY = a.getInt(R.styleable.BalloonView_balloonSizeY, 113);
		
		a.recycle();
	
    	ExamTrainer.showProgressDialog(context, context.getString(R.string.Calculating_your_score));
    	long score = 0;
    	
    	try{
    		score = ExamTrainer.calculateScore(context);
    	} catch (SQLiteException e) {
    		//Oops failed to calculate score
    	}
    	
    	this.amountOfBalloons = 0;
    	long scoreNeededToPass = ExamTrainer.getItemsNeededToPass();
    	
    	if( score >= scoreNeededToPass ) {
    		textView.setText(this.getResources().getString(R.string.Gongratulations) + "\n" + 
    				this.getResources().getString(R.string.You_passed));
    		//determine amount of balloons
    		long totalAmountOfItems = ExamTrainer.getAmountOfItems();
    		long amountOfWrongAnswers = totalAmountOfItems - score;
    		if ( amountOfWrongAnswers < 1 ) {
    			amountOfWrongAnswers = 1;
    		}
    		this.amountOfBalloons = Math.round((totalAmountOfItems - scoreNeededToPass) / amountOfWrongAnswers);
    	
    		setupWind();
    		setupBalloons();
    		
    	} else {
    		textView.setText(this.getResources().getString(R.string.You_failed));
    	}
    	
    	ExamTrainer.stopProgressDialog();
    	textView.setVisibility(View.VISIBLE);
    }

	private void setupWind() {
		//setup wind
		this.wind = new Wind();
		this.wind.setWindSpeedUpperLimit(10);
		this.wind.setWindChance(4);
	}
	
	private void setupBalloons() {
		Resources r = this.getContext().getResources();
    	
		this.balloonBitmaps = new Bitmap[amountOfBalloonBitmaps];
	
		this.balloonBitmaps[0] = createBitmap(r.getDrawable(R.drawable.aj_balloon_blue_64));
		this.balloonBitmaps[1] = createBitmap(r.getDrawable(R.drawable.aj_balloon_red_64));
    
		for(int i = 0; i < amountOfBalloons; i++) {
			Bitmap bitmap = balloonBitmaps[randomNumberGenerator.nextInt(2)];
			int x = randomNumberGenerator.nextInt(displayWidth);
			int y = displayHeight + randomNumberGenerator.nextInt(20);;
			Balloon b = new Balloon(x, y, bitmap);
			this.balloons.add(b);
		}
	}
	
	protected void setTextView(TextView view) {
		textView = view;
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

	public void moveBalloon(int balloonNumber, int moveX, int moveY) {
		Balloon b = balloons.get(balloonNumber);
		b.move(moveX, moveY);
//		if(( b.getY() + balloonSizeY ) < 0) {
//			b.setCoords(b.getX(), this.displayHeight);
//		}
//		
//		if(( b.getX() + balloonSizeX ) < 0) {
//			b.setCoords(this.displayWidth + balloonSizeX, b.getY());
//		}
//		else if(b.getX() > this.displayWidth) {
//			b.setCoords(0 - balloonSizeX, b.getY());
//		}
		//Log.d(TAG, "Balloon at "+b.getX()+","+b.getY());
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
