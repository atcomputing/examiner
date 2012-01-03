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

import java.util.ArrayList;

import com.example.android.snake.SnakeView.Coordinate;

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

	public void loadBalloon(int balloonKey, Drawable Balloon) {
		Bitmap bitmap = Bitmap.createBitmap(balloonSize, balloonSize, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Balloon.setBounds(0, 0, balloonSize, balloonSize);
		Balloon.draw(canvas);

		balloonArray[balloonKey] = bitmap;
	}

	protected int addBalloon(int balloonKey) {
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
		for (int i = 0; i < balloonCount; i++) {
			canvas.drawBitmap(balloonArray[i], 
					balloonCoordinates[i].x * balloonSize,
					balloonCoordinates[i].y * balloonSize,
					paint);

		}
	}
}
