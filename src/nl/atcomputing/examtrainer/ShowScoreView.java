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
    }

	private void setupWind() {
		//setup wind
		this.wind = new Wind(this.context);
		this.wind.setWindSpeedUpperLimit(20);
		this.wind.setWindChance(100);
	}
	
	private void setupBalloons() {
		Resources r = this.getContext().getResources();
		amountOfBalloonBitmaps = 2;
		this.balloonBitmaps = new Bitmap[amountOfBalloonBitmaps];
	
		this.balloonBitmaps[0] = createBitmap(r.getDrawable(R.drawable.aj_balloon_blue_64));
		this.balloonBitmaps[1] = createBitmap(r.getDrawable(R.drawable.aj_balloon_red_64));
    
		int verticalRange = amountOfBalloons * 10;
		for(int i = 0; i < amountOfBalloons; i++) {
			Bitmap bitmap = balloonBitmaps[randomNumberGenerator.nextInt(2)];
			int x = randomNumberGenerator.nextInt(displayWidth);
			int y = displayHeight + randomNumberGenerator.nextInt(verticalRange);;
			Balloon b = new Balloon(x, y, bitmap);
			this.balloons.add(b);
		}
	}
	
	protected void setTextView(TextView view) {
		this.textView = view;
	}

	protected void setMode(int mode) {
		this.mode = mode;
	}
	
	protected void start() {
		long score = 0;
    	
    	try{
    		//score = ExamTrainer.calculateScore(context);
    	} catch (SQLiteException e) {
    		ExamTrainer.stopProgressDialog();
    		//Oops failed to calculate score
    	}
    	
		score = 15;
    	
    	this.amountOfBalloons = 0;
    	//long scoreNeededToPass = ExamTrainer.getItemsNeededToPass();
    	long scoreNeededToPass = 7;
    	if( score >= scoreNeededToPass ) {
    		textView.setText(this.getResources().getString(R.string.Gongratulations) + "\n" + 
    				this.getResources().getString(R.string.You_passed));
    		//determine amount of balloons
    		//long totalAmountOfItems = ExamTrainer.getAmountOfItems();
    		long totalAmountOfItems = 15;
    		long amountOfWrongAnswers = totalAmountOfItems - score;
    		if ( amountOfWrongAnswers < 1 ) {
    			amountOfWrongAnswers = 1;
    		}
    		this.amountOfBalloons = Math.round((totalAmountOfItems - scoreNeededToPass) / amountOfWrongAnswers) * 2;
    	
    		setupWind();
    		setupBalloons();
    		
    	} else {
    		textView.setText(this.getResources().getString(R.string.You_failed));
    	}
    	
    	
    	textView.setVisibility(View.VISIBLE);
    	
    	update();
	}
	
	protected void update() {
		if( mode == RUNNING ) {
			if( balloons.size() > 0 ) {
				updateBalloons();
				wind.update();
			}
			redrawHandler.sleep(DELAY);
		}
	}

	
	
	private void updateBalloons() {
		
		int size = balloons.size();
		for(int i = 0; i < size; i++) {
			
        	if( ! this.moveBalloon(i) ) {
        		this.balloons.remove(i);
        		i--;
        		size--;
        	}
        }
	}
	
	public Bitmap createBitmap(Drawable Balloon) {
		Bitmap bitmap = Bitmap.createBitmap(balloonSizeX, balloonSizeY, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Balloon.setBounds(0, 0, balloonSizeX, balloonSizeY);
		Balloon.draw(canvas);
		return bitmap;
	}

	/**
	 * @param balloonNumber
	 * @param moveX
	 * @param moveY
	 * @return true if balloon was moved, false if balloon reached top of screen
	 */
	public boolean moveBalloon(int balloonNumber) {
		Balloon b = balloons.get(balloonNumber);
		
		if(( b.getY() + balloonSizeY ) < 0) {
			return false;
		} else if(( b.getX() + balloonSizeX ) < 0) {
                b.setCoords(this.displayWidth, b.getY());
        } else if(b.getX() > this.displayWidth) {
                b.setCoords(0 - balloonSizeX, b.getY());
        }

		//Range from -2 to +2
    	//int y = -10 + randomNumberGenerator.nextInt(5);
		int yIncrement = -6;
		
		int windSpeedHorizontal = wind.getWind(this.displayHeight - (b.getY() + yIncrement));
		if(wind.getDirection() == Wind.Direction.LEFT ) {
			windSpeedHorizontal = -windSpeedHorizontal;
		}
		
		b.move(windSpeedHorizontal, yIncrement);
		return true;
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
