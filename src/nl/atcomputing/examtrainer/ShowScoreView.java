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
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author martijn brekhof
 *
 */

public class ShowScoreView extends ShowScoreBalloonView {

    private static final String TAG = "ShowScoreBalloonView";

    private static final int DELAY = 100;
    
    private int currentMode = RUNNING;
    public static final int PAUSE = 0;
    public static final int RUNNING = 1;

    private TextView textView;

    private static final Random randomNumberGenerator = new Random();
    
    private RefreshHandler redrawHandler = new RefreshHandler();

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
        init();
   }

    private void init() {
    	Resources r = this.getContext().getResources();
        
        loadBalloon(0, r.getDrawable(R.drawable.aj_balloon_blue_64));
        loadBalloon(1, r.getDrawable(R.drawable.aj_balloon_red_64));
    }

    protected Bundle saveState() {
    	Bundle bundle = new Bundle();
    	return bundle;
    }
    
    protected void restoreState(Bundle bundle) {
        //We are restored from stopped state
    	//We need to get the balloon coordinates from the bundle
    }
    
    protected void setTextView(TextView view) {
        textView = view;
    }

    protected void setMode(int mode) {
    	currentMode = mode;
    }
    
    protected void update() {
        if (currentMode == RUNNING) {
        	updateBalloons();
            redrawHandler.sleep(DELAY);
        }

    }
    
    private void updateBalloons() {
    	//update coordinate of balloons
    }
}
