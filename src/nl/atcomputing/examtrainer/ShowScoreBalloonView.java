
package nl.atcomputing.examtrainer;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author martijn brekhof
 *
 */

public class ShowScoreBalloonView extends View {

	class Balloon {
		int x;
		int y;
		int balloonArrayIndex;
	}

	protected static int balloonSize;

	//Convenience variable to prevent calculating
	//length of balloonArray
	protected static int balloonCount;

	//Holds references to the Drawables.
	private Bitmap[] balloonArray; 

	private ArrayList<Balloon> balloons = new ArrayList<Balloon>();

	private final Paint paint = new Paint();

	public ShowScoreBalloonView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BalloonView);

		balloonSize = a.getInt(R.styleable.BalloonView_balloonSize, 12);

		a.recycle();
	}

	public Bitmap loadBalloon(int balloonKey, Drawable Balloon) {
		Bitmap bitmap = Bitmap.createBitmap(balloonSize, balloonSize, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Balloon.setBounds(0, 0, balloonSize, balloonSize);
		Balloon.draw(canvas);

	}

	protected void addBalloon(int balloonKey) {
		Balloon b = new Balloon();
		b.x = 0;
		b.y = 0;
		b.balloonArrayIndex = balloonKey;
		balloons.add(b);
	}
	
	public void setBalloonCoords(int balloonNumber, int x, int y) {
		Balloon b = balloons.get(balloonNumber);
		b.x = x;
		b.y = y;
	}


	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for ( Balloon b : balloons ) {
			canvas.drawBitmap(balloonArray[b.balloonArrayIndex],
					b.x * balloonSize,
					b.y * balloonSize,
					paint);
		}
	}
}
