package nl.atcomputing.examtrainer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * @author martijn brekhof
 *
 */

public class ShowScoreActivity extends Activity {
	private ShowScoreView showScoreView;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.show_score);

        showScoreView = (ShowScoreView) findViewById(R.id.show_score);
        showScoreView.setTextView((TextView) findViewById(R.id.show_score_text));
        
        showScoreView.update();
    }
	
	public void onPause() {
		super.onPause();
		showScoreView.setMode(ShowScoreView.PAUSE);
	}
	
	public void onResume() {
		super.onResume();
		showScoreView.setMode(ShowScoreView.RUNNING);
	}
}
