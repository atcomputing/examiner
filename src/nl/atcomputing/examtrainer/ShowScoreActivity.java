package nl.atcomputing.examtrainer;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
		showScoreView.setShowScoreTextView((TextView) findViewById(R.id.show_score_text));
		showScoreView.setCalculateScoreTextView((TextView) findViewById(R.id.calculate_score_text));
		showScoreView.start();
		calculateScore();
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
	
	protected void calculateScore() {

		ExaminationDbAdapter examinationDbHelper;
		examinationDbHelper = new ExaminationDbAdapter(this);
		try {
			examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		} catch (SQLiteException e) {
			throw(e);
		}
		
		List<Long> questionIDsList = examinationDbHelper.getAllQuestionIDs();
		
		CalculateScore task = new CalculateScore(this, showScoreView);
		task.execute(questionIDsList.toArray());
		
		
	}

}
