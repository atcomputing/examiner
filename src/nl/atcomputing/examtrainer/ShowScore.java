package nl.atcomputing.examtrainer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ShowScore extends Activity {
	private String KEY = "showscore-key";
	private ShowScoreView showScoreView;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.show_score);

        showScoreView = (ShowScoreView) findViewById(R.id.show_score);
        showScoreView.setTextView((TextView) findViewById(R.id.show_score_text));

        if (savedInstanceState == null) {
        	//Ah we are a new activity. We should
        	//calculate the score and show it
        } else {
            Bundle map = savedInstanceState.getBundle(KEY);
            if (map != null) {
            	showScoreView.restoreState(map);
            } else {
            	showScoreView.setMode(ShowScoreView.PAUSE);
            }
            showScoreView.setMode(ShowScoreView.RUNNING);
        }
    }
	
	@Override
	protected void onRestart() {
		showScoreView.setMode(ShowScoreView.RUNNING);
	}

    @Override
    protected void onStop() {
        super.onPause();
        showScoreView.setMode(ShowScoreView.PAUSE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle(KEY, showScoreView.saveState());
    }
}
