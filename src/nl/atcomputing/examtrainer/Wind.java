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
	private static final int MAX_INCREMENT_PERIOD = 10;
	private static final int MAX_DECREMENT_PERIOD = 10;
	private static final int MAX_WIND_PERIOD = 50;
	
	protected enum Direction {
		LEFT, RIGHT
	}
	
	private static final Random randomNumberGenerator = new Random();

	private boolean blowing;
	private boolean blowPeriodPassed;
	private Direction direction;
	private double maxSpeed;
	private double incrementSteps;
	private double decrementSteps;
	private int windIterations;
	private double speedHorizontal;
	private int chance;
	private int speedUpperLimit;
	private boolean buildingUp;
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
				normalizedHeightFactor = height/displayHeight * windIncreaseFactor
				if ( windSpeed < maxWindSpeed )
					windSpeed = windSpeed * incrementFactor;
				else if ( duration < 0 )
					windSpeed = windSpeed * decrementFactor;
					if ( windSpeed == 0 )
						breeze = false;
		else return small random windSpeed
			
		*/

		
		if( !this.blowing ) {
			//Calculate new wind
			this.blowing = true;
			
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
			
			//int incrementPeriod = randomNumberGenerator.nextInt(MAX_INCREMENT_PERIOD) + 1;
			int incrementPeriod = MAX_INCREMENT_PERIOD;
			this.incrementSteps = this.maxSpeed / incrementPeriod;
			
			//int decrementPeriod = randomNumberGenerator.nextInt(MAX_DECREMENT_PERIOD) + 1;
			int decrementPeriod = MAX_DECREMENT_PERIOD;
			this.decrementSteps = this.maxSpeed / decrementPeriod;
			
			int blowPeriod = MAX_WIND_PERIOD + 
					incrementPeriod + decrementPeriod;
			windIterations = MAX_WIND_PERIOD;
			
			Log.d(TAG, "blowPeriod: "+ blowPeriod +
					"\nmaxSpeed: " + this.maxSpeed +
					"\nincrementPeriod: " + incrementPeriod +
					"\ndecrementPeriod: " + decrementPeriod +
					"\nincrementFactor: " + this.incrementSteps +
					"\ndecrementFactor: " + this.decrementSteps +
					"\ndirection: " + this.direction
					);
			
			  this.speedHorizontal = 0.0;
			  this.buildingUp = true;
		} else {
			//first build up to max wind
			if ( (this.speedHorizontal < this.maxSpeed) && this.buildingUp ){
				Log.d(TAG, "Incrementing speed="+this.speedHorizontal);
				this.speedHorizontal += this.incrementSteps;
			} else {
				this.buildingUp = false;
				//maintain max wind for windIterations
				if (this.windIterations < 0)
				this.speedHorizontal -= decrementSteps;
				Log.d(TAG, "Decrementing speed="+this.speedHorizontal);
				if ( this.speedHorizontal < 0.0 ) {
					this.speedHorizontal = 0.0;
					this.blowing = false;
				} else {
					this.windIterations--;
				}
				
			}
			
		}
	}
	
	protected int getWind(int height) {
		double normalizedHeightFactor = height/(double) this.displayHeight;
		int relativeSpeedHorizontal = (int) (Math.round(this.speedHorizontal) * normalizedHeightFactor);
		
//		Log.d(TAG, "Blowing: height: " + height + " speed="+this.speedHorizontal
//				+" normalizedHeightFactor=" + normalizedHeightFactor
//				+" relative speed="+relativeSpeedHorizontal);
		return relativeSpeedHorizontal;
//		return (int) Math.round(this.speedHorizontal);
	}
	
	private void setBlowPeriodPassed(boolean b) {
		this.blowPeriodPassed = b;
		Log.d(TAG, "blow period passed");
	}
}
