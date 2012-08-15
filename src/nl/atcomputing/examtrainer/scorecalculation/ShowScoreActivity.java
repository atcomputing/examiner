package nl.atcomputing.examtrainer.scorecalculation;

import java.util.List;

import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.SelectExamActivity;
import nl.atcomputing.examtrainer.StartExamActivity;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

public class ShowScoreActivity extends Activity {
	private GLSurfaceViewRenderer renderer;
	private GLSurfaceView glView;
	private CalculateScore calculateScore;
	private boolean glSurfaceReady = false;
	private WaitForGLSurfaceReadyAsyncTask waitForGLSurfaceReadyAsyncTask = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ShowScoreActivity", "onCreate");
		setContentView(R.layout.show_score);

		this.glView = (GLSurfaceView) findViewById(R.id.show_score_glsurfaceview);
		this.renderer = new GLSurfaceViewRenderer(this);
		this.glView.setRenderer(this.renderer);

		if( ExamTrainer.getExamMode() != ExamTrainer.ExamTrainerMode.SHOW_SCORE ) {
			ShowScoreActivity.this.calculateScore = (CalculateScore) getLastNonConfigurationInstance();
			if( ( ShowScoreActivity.this.calculateScore != null ) && 
					( ShowScoreActivity.this.calculateScore.getStatus() != AsyncTask.Status.FINISHED) ) {
				Log.d("ShowScoreActivity", "Attaching running calculateScore");
				ShowScoreActivity.this.calculateScore.setContext(ShowScoreActivity.this);
			} else {
				Log.d("ShowScoreActivity", "Creating new calculateScore");
				calculateScore();
			}
		} else {
			showResult();
		}
	}

	public void setGLSurfaceReady() {
		this.glSurfaceReady = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if( this.waitForGLSurfaceReadyAsyncTask != null ) { 
			this.waitForGLSurfaceReadyAsyncTask.cancel(true);
		}
		this.glView.onPause();
		this.renderer.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.glView.onResume();
		this.renderer.onResume();
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(ShowScoreActivity.this, StartExamActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
	
	public Object onRetainNonConfigurationInstance() {
		return this.calculateScore;
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
		examinationDbHelper.close();

		this.calculateScore = new CalculateScore(this);
		this.calculateScore.execute(questionIDsList.toArray());

	}

	protected void showResult() {
		int score = 0;
		ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.SHOW_SCORE);

		ExaminationDbAdapter examinationDbHelper;
		examinationDbHelper = new ExaminationDbAdapter(this);
		try {
			examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
			score = examinationDbHelper.getScore(ExamTrainer.getScoresId());
			examinationDbHelper.close();
		} catch (SQLiteException e) {
			Toast.makeText(this, "Database Error: Could not get score", Toast.LENGTH_LONG).show();
			examinationDbHelper.close();
			return;
		}

		long totalAmountOfItems = ExamTrainer.getAmountOfItems();
		long itemsRequiredToPass = ExamTrainer.getItemsNeededToPass();
		Resources r = this.getResources();
		String text = "";

		if( score >= itemsRequiredToPass) {
			text = r.getString(R.string.Congratulations) + ".\n" + 
					r.getString(R.string.You_passed) + ".\n" +
					r.getString(R.string.You_scored) + " " + score + " " +
					r.getString(R.string.out_of) + " " + totalAmountOfItems + ".";
			this.waitForGLSurfaceReadyAsyncTask = new WaitForGLSurfaceReadyAsyncTask();
			this.waitForGLSurfaceReadyAsyncTask.execute(score);
		} else {
			text = r.getString(R.string.You_failed) + ".\n" +
					r.getString(R.string.You_scored) + " " + score + " " +
					r.getString(R.string.out_of) + " " + totalAmountOfItems + ", " +
					r.getString(R.string.but_you_needed) + " " + itemsRequiredToPass + " " +
					r.getString(R.string.to_pass) + ".";
		}
		TextView tv = (TextView) findViewById(R.id.show_score_text);
		tv.setText(text);
		tv.setVisibility(View.VISIBLE);
	}
	
	private class WaitForGLSurfaceReadyAsyncTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {
			while( glSurfaceReady == false ) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return params[0];
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			renderer.showBalloons(result.intValue());
			renderer.requestRender();
		}
	}
}
