package nl.atcomputing.examtrainer.exam.score;

import java.util.Date;
import java.util.Random;

import android.util.Log;

/**
 * @author martijn brekhof
 *
 */

public class Wind  {
	private static final String TAG = "Wind";

	//Times are in milliseconds
	private static final int MAX_INCREMENT_STEPS = 50;
	private static final int MAX_DECREMENT_STEPS = 50;
	private static final int MAX_WIND_PERIOD = 1000;

	protected enum Direction {
		LEFT, RIGHT
	}

	private static final Random randomNumberGenerator = new Random();

	private boolean blowing;
	private Direction direction;
	private float maxSpeed;
	private float incrementSteps;
	private float decrementSteps;
	private long windPeriod;
	private float speedHorizontal;
	private int chance;
	private float speedUpperLimit;
	private boolean buildingUp;
	private float displayBottom;
	private float displayRange;

	private static float SPEED_BREEZE = 0.02f;
	
	public Wind() {
		this.direction = Direction.LEFT;
		this.blowing = false;
		this.chance = 20;
		this.speedUpperLimit=20;
		this.displayRange = 1f;
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

	protected void setWindSpeedUpperLimit(float speed) {
		this.speedUpperLimit = speed;
	}

	/**
	 * Sets the window size used to calculate the wind height factor
	 * @param top top of frustum in world coordinates
	 * @param bottom bottom of frustum in world coordinates
	 * @param left left of frustum in world coordinates
	 * @param right right of frustum in world coordinates
	 */
	protected void setWindowSize(float top, float bottom, float left, float right) {
		this.displayBottom = bottom;
		this.displayRange = Math.abs(top - bottom); 
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
				this.maxSpeed = (randomNumberGenerator.nextFloat()%this.speedUpperLimit) + 0.001f;
			} else {
				//calculate a small wind to simulate no wind
				this.maxSpeed = (randomNumberGenerator.nextFloat()%SPEED_BREEZE) + 0.001f;
			}

			//Set direction
			if(randomNumberGenerator.nextBoolean()) {
				direction = Wind.Direction.LEFT;
			} else {
				direction = Wind.Direction.RIGHT;
			}

			//int incrementPeriod = randomNumberGenerator.nextInt(MAX_INCREMENT_PERIOD) + 1;
			int incrementPeriod = MAX_INCREMENT_STEPS;
			this.incrementSteps = this.maxSpeed / incrementPeriod;

			//int decrementPeriod = randomNumberGenerator.nextInt(MAX_DECREMENT_PERIOD) + 1;
			int decrementPeriod = MAX_DECREMENT_STEPS;
			this.decrementSteps = this.maxSpeed / decrementPeriod;

			this.speedHorizontal = 0.0f;
			this.buildingUp = true;
		} else {
			//first build up to max wind
			if ( this.buildingUp ) {
				if (this.speedHorizontal < this.maxSpeed){
					//				Log.d(TAG, "Incrementing speed="+this.speedHorizontal);
					this.speedHorizontal += this.incrementSteps;
				} else {
					this.buildingUp = false;
					Date date = new Date();
					this.windPeriod = date.getTime() + MAX_WIND_PERIOD;
				}
			} else {
				Date date = new Date();
				if (this.windPeriod < date.getTime()) {
					this.speedHorizontal -= decrementSteps;
					if( this.speedHorizontal < 0f ) {
						this.speedHorizontal = 0.0f;
						this.windPeriod = 0;
						this.blowing = false;
					}

				}
			}
		}
	}

	protected float getWind(float height) {
		float heightFactor = (height - this.displayBottom) / this.displayRange;
		//		
		//		Log.d("Wind", "heightFactor="+heightFactor+" windSpeed=" + this.speedHorizontal * heightFactor);
		//		
		//		return this.speedHorizontal * heightFactor;
		//Log.d("Wind", "windSpeed=" + this.speedHorizontal);
		return this.speedHorizontal * heightFactor;
	}
}
