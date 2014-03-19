package nl.atcomputing.examtrainer.scorecalculation;

import java.util.List;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.main.ExamTrainer;
import android.app.Activity;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
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
		setContentView(R.layout.show_score);

		this.glView = (GLSurfaceView) findViewById(R.id.show_score_glsurfaceview);
		this.renderer = new GLSurfaceViewRenderer(this);
		this.glView.setRenderer(this.renderer);

		ShowScoreActivity.this.calculateScore = (CalculateScore) getLastNonConfigurationInstance();
		if( ShowScoreActivity.this.calculateScore != null ) { // we got a previous state
			if ( ShowScoreActivity.this.calculateScore.getStatus() != AsyncTask.Status.FINISHED) {
				//Reconnect running calculation thread
				ShowScoreActivity.this.calculateScore.setContext(ShowScoreActivity.this);
			} else {
				//Calculation thread already finished so we can show the result
				showResult();
			}
		} else {
			//no previous state available. We are called for the first time
			//so we calculate the score.
			calculateScore();
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
		ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.ENDOFEXAM);

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
		int color = r.getColor(R.color.exam_passed_text);
		if( score >= itemsRequiredToPass) {
			text = r.getString(R.string.Congratulations) + ".\n" + 
					r.getString(R.string.You_passed) + ".\n" +
					r.getString(R.string.You_scored) + " " + score + " " +
					r.getString(R.string.out_of) + " " + totalAmountOfItems + ".";
			this.waitForGLSurfaceReadyAsyncTask = new WaitForGLSurfaceReadyAsyncTask();
			this.waitForGLSurfaceReadyAsyncTask.execute(15);
		} else {
			text = r.getString(R.string.You_failed) + ".\n" +
					r.getString(R.string.You_scored) + " " + score + " " +
					r.getString(R.string.out_of) + " " + totalAmountOfItems + ", " +
					r.getString(R.string.but_you_needed) + " " + itemsRequiredToPass + " " +
					r.getString(R.string.to_pass) + ".";
			color = r.getColor(R.color.exam_failed_text);
		}
		TextView tv = (TextView) findViewById(R.id.show_score_text);
		tv.setText(text);
		tv.setTextColor(color);
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
