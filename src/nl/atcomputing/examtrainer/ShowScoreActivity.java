package nl.atcomputing.examtrainer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * @author martijn brekhof
 *
 */

public class ShowScoreActivity extends Activity {
	private static final String TAG = "ShowScoreActivity";
	private ShowScoreView showScoreView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("trace", "ShowScoreActivity created");
		
		ExamTrainer.setEndOfExam();
		
		setContentView(R.layout.show_score);
		
		
		showScoreView = (ShowScoreView) findViewById(R.id.show_score);
		showScoreView.setTextView((TextView) findViewById(R.id.show_score_text));

		showScoreView.start();
	}

	public void onPause() {
		super.onPause();
		Log.d("trace", "ShowScoreActivity paused");
		showScoreView.setMode(ShowScoreView.PAUSE);
	}

	public void onResume() {
		super.onResume();
		Log.d("trace", "ShowScoreActivity resumed");
		showScoreView.setMode(ShowScoreView.RUNNING);
	}
}
