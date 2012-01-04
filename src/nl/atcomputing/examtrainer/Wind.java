package nl.atcomputing.examtrainer;

import java.util.Random;

import android.os.CountDownTimer;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */

public class Wind  {
	private static final String TAG = "Wind";
	
	//Times are in milliseconds
	private static final int MAX_INCREMENT_PERIOD = 200;
	private static final int MAX_WIND_PERIOD = 1000;
	
	protected enum Direction {
		LEFT, RIGHT
	}
	
	private static final Random randomNumberGenerator = new Random();

	private boolean blowing;
	private boolean blowPeriodPassed;
	private Direction direction;
	private double maxSpeed;
	private double incrementFactor;
	private double decrementFactor;
	private double speedHorizontal;
	private int chance;
	private int speedUpperLimit;
	
	public Wind() {
		direction = Direction.LEFT;
		blowing = false;
		chance = 90;
		speedUpperLimit=20;
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
	
	protected void setBlowPeriodPassed(boolean b) {
		this.blowPeriodPassed = b;
	}
	
	protected int getWind() {
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
		else return small random windSpeed
			
		*/

		
		if((randomNumberGenerator.nextInt(100) < this.chance) && (!this.blowing)) {
			//New wind
			this.blowing = true;
			
			//Set direction
			if(randomNumberGenerator.nextBoolean()) {
				direction = Wind.Direction.LEFT;
			} else {
				direction = Wind.Direction.RIGHT;
			}
			
			this.maxSpeed = randomNumberGenerator.nextInt(this.speedUpperLimit);
			
			int incrementPeriod = randomNumberGenerator.nextInt(MAX_INCREMENT_PERIOD);
			incrementFactor = this.maxSpeed / incrementPeriod;
			
			int decrementPeriod = randomNumberGenerator.nextInt(MAX_INCREMENT_PERIOD);
			decrementFactor = this.maxSpeed / decrementPeriod;
			
			int blowPeriod = randomNumberGenerator.nextInt(MAX_WIND_PERIOD) + 
					incrementPeriod + decrementPeriod;
			
			//We setup a timer for the blowPeriod
			setBlowPeriodPassed(false);
			new CountDownTimer(blowPeriod, blowPeriod) {
				
				public void onTick(long millisUntilFinished) {
			    }
				
			     public void onFinish() {
			    	 setBlowPeriodPassed(true);
			     }
			  }.start();
			
			//we start out no wind
			this.speedHorizontal = 0.0;
			
			return (int) this.speedHorizontal;
			  
		} else if ( this.blowing ) {
			if (this.blowPeriodPassed ) {
				this.speedHorizontal -= decrementFactor;
				if ( this.speedHorizontal < 1.0 ) {
					this.speedHorizontal = 0.0;
					blowing = false;
				}
			} else {
				if ( this.speedHorizontal < maxSpeed ) {
					this.speedHorizontal += incrementFactor;
				}
			}
			//Log.d(TAG, "Blowing: speed="+this.speedHorizontal);
			return (int) this.speedHorizontal;
		} else {
			//Log.d(TAG, "Not blowing");
			//No wind? Not possible so we return a small random speed
			return randomNumberGenerator.nextInt(2);
		}
	}
}
