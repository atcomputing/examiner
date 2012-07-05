package nl.atcomputing.examtrainer.exam.score;

import java.util.List;

import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.app.Activity;
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
	private int amountOfBalloons;
	private GLSurfaceViewRenderer renderer;
	private CalculateScore calculateScore;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("trace", "ShowScoreActivity created");

		setContentView(R.layout.show_score);

		GLSurfaceView view = (GLSurfaceView) findViewById(R.id.show_score_glsurfaceview);
		this.renderer = new GLSurfaceViewRenderer(this);
		view.setRenderer(this.renderer);

		if( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.SHOW_SCORE ) {
			showResult();
		} else {
			this.calculateScore = (CalculateScore) getLastNonConfigurationInstance();
			if( ( this.calculateScore != null ) && ( this.calculateScore.getStatus() != AsyncTask.Status.FINISHED) ) {
				this.calculateScore.setContext(this);
			} else {
				calculateScore();
			}
		}
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

	protected int calculateAmountOfBalloons(int score) {
		long totalAmountOfItems = ExamTrainer.getAmountOfItems();
		long itemsRequiredToPass = ExamTrainer.getItemsNeededToPass();
		//determine amount of balloons
		long amountOfWrongAnswers = totalAmountOfItems - score;
		if ( amountOfWrongAnswers < 1 ) {
			amountOfWrongAnswers = 1;
		}
		return Math.round((totalAmountOfItems - itemsRequiredToPass) / amountOfWrongAnswers) * 2;
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
			Log.d("ShowScoreActivity", e.getMessage());
			Toast.makeText(this, "Database Error: Could not get score", Toast.LENGTH_LONG).show();
			examinationDbHelper.close();
			return;
		}
		
		long totalAmountOfItems = ExamTrainer.getAmountOfItems();
		long itemsRequiredToPass = ExamTrainer.getItemsNeededToPass();
		Resources r = this.getResources();
		String text = "";

		if( score >= itemsRequiredToPass) {
			text = r.getString(R.string.Gongratulations) + "\n" + 
					r.getString(R.string.You_passed) + "\n" +
					r.getString(R.string.You_scored) + " " + score + " " +
					r.getString(R.string.out_of) + " " + totalAmountOfItems;
		} else {
			text = r.getString(R.string.You_failed) + "\n" +
					r.getString(R.string.You_scored) + " " + score + " " +
					r.getString(R.string.out_of) + " " + totalAmountOfItems + " " +
					r.getString(R.string.but_you_needed) + " " + itemsRequiredToPass;
		}
		TextView tv = (TextView) findViewById(R.id.show_score_text);
		tv.setText(text);
		tv.setVisibility(View.VISIBLE);

		this.amountOfBalloons = calculateAmountOfBalloons(score);
		Log.d("ShowScoreActivity", "amountOfBalloons"+this.amountOfBalloons);
		//if( this.amountOfBalloons > 0 ) {
		this.renderer.showBalloons(50);
		this.renderer.requestRender();
		//}
	}
}
