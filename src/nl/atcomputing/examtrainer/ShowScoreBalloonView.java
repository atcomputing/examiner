
package nl.atcomputing.examtrainer;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @author martijn brekhof
 *
 */

public class ShowScoreBalloonView extends View {
	private static final String TAG = "ShowScoreBalloonView";
	protected static int balloonSize;

	//Convenience variable to prevent calculating
	//length of balloonArray
	protected static int balloonCount;

	private ArrayList<Balloon> balloons = new ArrayList<Balloon>();

	private final Paint paint = new Paint();

	public ShowScoreBalloonView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BalloonView);

		balloonSize = a.getInt(R.styleable.BalloonView_balloonSize, 24);

		a.recycle();
	}

	public Bitmap createBitmap(Drawable Balloon) {
		Bitmap bitmap = Bitmap.createBitmap(balloonSize, balloonSize * 2, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Balloon.setBounds(0, 0, balloonSize, balloonSize * 2);
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
