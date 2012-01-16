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

	private TextView showScoreTextView;
	private TextView calculateScoreTextView;
	
	private static final int DELAY = 50;

	private static int balloonSizeX;
	private static int balloonSizeY;

	private ArrayList<Balloon> balloons = new ArrayList<Balloon>();

	private final Paint paint = new Paint();

	private int mode = CALCULATING;
	protected static final int PAUSE = 0;
	protected static final int RUNNING = 1;
	protected static final int CALCULATING = 2;

	private int score;
	
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

		this.score = 0;
		
		a.recycle();
	}

	private void initBalloons() {
		Resources r = this.getContext().getResources();
		amountOfBalloonBitmaps = 2;
		this.balloonBitmaps = new Bitmap[amountOfBalloonBitmaps];

		this.balloonBitmaps[0] = createBitmap(r.getDrawable(R.drawable.aj_balloon_blue_64));
		this.balloonBitmaps[1] = createBitmap(r.getDrawable(R.drawable.aj_balloon_red_64));
	}

	protected void addBalloon() {
		Bitmap bitmap = balloonBitmaps[randomNumberGenerator.nextInt(2)];
		int x = randomNumberGenerator.nextInt(displayWidth);
		Balloon b = new Balloon(x, this.displayHeight, bitmap);
		this.balloons.add(b);
		this.amountOfBalloons++;
	}
	
	protected void popBalloon() {
		this.balloons.remove(0);
		//make popping sound?
	}
	
	protected int getAmountOfBalloons() {
		return this.amountOfBalloons;
	}
	
	protected void setShowScoreTextView(TextView view) {
		this.showScoreTextView = view;
	}

	protected TextView getShowScoreTextView() {
		return this.showScoreTextView;
	}
	
	protected void setCalculateScoreTextView(TextView view) {
		this.calculateScoreTextView = view;
	}
	
	protected TextView getCalculateScoreTextView() {
		return this.calculateScoreTextView;
	}
	
	protected void setMode(int mode) {
		this.mode = mode;
	}

	protected void start() {
		this.amountOfBalloons = 0;
		initBalloons();
		//setup wind
				this.wind = new Wind(this.context);
				this.wind.setWindSpeedUpperLimit(20);
				this.wind.setWindChance(100);
				
		update();
	}

	protected void setScore(int score) {
		this.score = score;
	}
	
	protected void calculateAmountOfBalloons() {
			long totalAmountOfItems = ExamTrainer.getAmountOfItems();
			long itemsRequiredToPass = ExamTrainer.getItemsNeededToPass();
			//determine amount of balloons
			long amountOfWrongAnswers = totalAmountOfItems - score;
			if ( amountOfWrongAnswers < 1 ) {
				amountOfWrongAnswers = 1;
			}
			this.amountOfBalloons = Math.round((totalAmountOfItems - itemsRequiredToPass) / amountOfWrongAnswers) * 2;
	}

	protected void showResult() {
		long totalAmountOfItems = ExamTrainer.getAmountOfItems();
		long itemsRequiredToPass = ExamTrainer.getItemsNeededToPass();
		
		if( score >= itemsRequiredToPass) {
			showScoreTextView.setText(this.getResources().getString(R.string.Gongratulations) + "\n" + 
					this.getResources().getString(R.string.You_passed));
			
					} else {
			Resources r = this.getResources();
			String text = r.getString(R.string.You_failed) + "\n" +
			r.getString(R.string.You_scored) + " " + score + " " +
			r.getString(R.string.out_of) + " " + totalAmountOfItems + " " +
			r.getString(R.string.but_you_needed) + " " + itemsRequiredToPass;
					
			showScoreTextView.setText(text);
		}
		
		showScoreTextView.setVisibility(View.VISIBLE);
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
