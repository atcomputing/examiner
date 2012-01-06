package nl.atcomputing.examtrainer;

import java.util.Random;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * @author martijn brekhof
 *
 */

public class Wind  {
	private static final String TAG = "Wind";
	
	//Times are in milliseconds
	private static final int MAX_INCREMENT_STEPS = 20;
	private static final int MAX_DECREMENT_STEPS = 20;
	private static final int MAX_INCREMENT_PERIOD = 500;
	private static final int MAX_DECREMENT_PERIOD = 500;
	private static final int MAX_WIND_PERIOD = 1000;
	
	protected enum Direction {
		LEFT, RIGHT
	}
	
	private static final Random randomNumberGenerator = new Random();

	private boolean blowing;
	private boolean blowPeriodPassed;
	private int blowPeriod;
	private Direction direction;
	private double maxSpeed;
	private double incrementSteps;
	private double decrementSteps;
	private double speedHorizontal;
	private int chance;
	private int speedUpperLimit;
	private int displayHeight;
	private int windIncreaseFactor;
	
	public Wind(Context context) {
		direction = Direction.LEFT;
		blowing = false;
		chance = 20;
		speedUpperLimit=20;
		
		Display display = ((WindowManager) 
				context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		displayHeight = display.getHeight();
		windIncreaseFactor = 5;
	}
	
	protected boolean isBlowing() {
		return blowing;
	}
	
	protected Direction getDirection() {
		return direction;
	}
	
	protected void setWindChance(int percentage) {
		this.chance = percentage;
	}
	
	protected void setWindSpeedUpperLimit(int speed) {
		this.speedUpperLimit = speed;
	}
	
	protected void setWindHeightIncreaseFactor(int factor) {
		this.windIncreaseFactor = factor;
	}
	
	protected void update() {
		
		if( !this.blowing ) {
			//Calculate new wind
			setBlowing(true);
			
			if(randomNumberGenerator.nextInt(100) < this.chance) {
				this.maxSpeed = randomNumberGenerator.nextInt(this.speedUpperLimit) + 1;
			} else {
				//calculate a small wind to simulate no wind
				this.maxSpeed = randomNumberGenerator.nextInt(3) + 1;
			}
			
			//Set direction
			if(randomNumberGenerator.nextBoolean()) {
				direction = Wind.Direction.LEFT;
			} else {
				direction = Wind.Direction.RIGHT;
			}
			
			int incrementPeriod = randomNumberGenerator.nextInt(MAX_INCREMENT_PERIOD) + 1;
			this.incrementSteps = incrementPeriod / MAX_INCREMENT_STEPS;
			
			int decrementPeriod = randomNumberGenerator.nextInt(MAX_DECREMENT_PERIOD) + 1;
			this.decrementSteps = this.maxSpeed / decrementPeriod;
			
			this.blowPeriod = randomNumberGenerator.nextInt(MAX_WIND_PERIOD) + 
					incrementPeriod + decrementPeriod;
			
			Log.d(TAG, "blowPeriod: "+ blowPeriod +
					"\nmaxSpeed: " + this.maxSpeed +
					"\nincrementPeriod: " + incrementPeriod +
					"\ndecrementPeriod: " + decrementPeriod +
					"\nincrementFactor: " + this.incrementSteps +
					"\ndecrementFactor: " + this.decrementSteps +
					"\ndirection: " + this.direction
					);
			
			
			  //Setup timer to increase wind
			  new CountDownTimer(incrementPeriod, (int) this.incrementSteps) {
					
					public void onTick(long millisUntilFinished) {
						incrementWind();
				    }
					
				     public void onFinish() {
				    	//We setup a timer for the blowPeriod
				    	 setupBlowPeriodTimer();
				     }
				  }.start();
		} else {
			if (this.blowPeriodPassed ) {
				this.speedHorizontal -= decrementSteps;
				Log.d(TAG, "Decrementing speed="+this.speedHorizontal);
				if ( this.speedHorizontal < 0.0 ) {
					this.speedHorizontal = 0.0;
					this.blowing = false;
				}
			}
		}
	}
	
	protected int getWind(int height) {
		//int normalizedHeightFactor = (int) ((height/(double) this.displayHeight) 
		//		* this.windIncreaseFactor);
		int normalizedHeightFactor = 0;
		int relativeSpeedHorizontal = ((int) this.speedHorizontal) + normalizedHeightFactor;
		
		//Log.d(TAG, "Blowing: height: " + height + " speed="+this.speedHorizontal);
		return relativeSpeedHorizontal;
	}
	
	private void setBlowing(boolean b) {
		this.blowing = b;
		Log.d(TAG, "blow period passed");
	}
	
	private void setupBlowPeriodTimer() {
		new CountDownTimer(this.blowPeriod, this.blowPeriod) {
		
		public void onTick(long millisUntilFinished) {
	    }
		
	     public void onFinish() {
	    	 setupDecrementWindTimer();
	     }
	  }.start();
	}
	
	
	private void incrementWind() {
		this.speedHorizontal += this.incrementSteps;
	}
	
	private void setupDecrementWindTimer() {
		new CountDownTimer(this.blowPeriod, this.blowPeriod) {
		
		public void onTick(long millisUntilFinished) {
			decrementWind();
	    }
		
	     public void onFinish() {
	    	 setBlowing(false);
	     }
	  }.start();
	}
	
	private void decrementWind() {
		this.speedHorizontal -= this.decrementSteps;
	}
}
