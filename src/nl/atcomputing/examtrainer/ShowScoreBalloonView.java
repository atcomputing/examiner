/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.atcomputing.examtrainer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class ShowScoreBalloonView extends View {

	class Coordinates {
		int x;
		int y;
	}

	protected static int balloonSize;

	//Convenience variable to prevent calculating
	//length of balloonArray
	protected static int balloonCount;

	//Holds references to the Drawables.
	private Bitmap[] balloonArray; 

	private Coordinates[] balloonCoordinates;

	private final Paint paint = new Paint();

	public ShowScoreBalloonView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BalloonView);

		balloonSize = a.getInt(R.styleable.BalloonView_balloonSize, 12);

		a.recycle();
	}

	public ShowScoreBalloonView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BalloonView);

		balloonSize = a.getInt(R.styleable.BalloonView_balloonSize, 12);

		a.recycle();
	}

	public void loadBalloon(int key, Drawable Balloon) {
		Bitmap bitmap = Bitmap.createBitmap(balloonSize, balloonSize, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Balloon.setBounds(0, 0, balloonSize, balloonSize);
		Balloon.draw(canvas);

		balloonArray[key] = bitmap;
	}

	public void setBalloonCoords(int index, int x, int y) {
		balloonCoordinates[index].x = x;
		balloonCoordinates[index].y = y;
	}


	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < balloonCount; i++) {
			canvas.drawBitmap(balloonArray[i], 
					balloonCoordinates[i].x * balloonSize,
					balloonCoordinates[i].y * balloonSize,
					paint);

		}
	}
}
