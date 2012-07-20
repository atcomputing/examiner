package nl.atcomputing.examtrainer.review;

import nl.atcomputing.adapters.ExamReviewAdapter;
import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.exam.ExamQuestionActivity;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;

/**
 * @author martijn brekhof
 *
 */
public class ExamReviewActivity extends Activity {
	public static final String TAG = "ExamReviewActivity";
	private GridView scoresGrid;
	private ExamReviewAdapter adapter; 
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("trace", "ExamReviewActivity created");
			
		setContentView(R.layout.review_exam);
	}

	protected void onResume() {
		super.onResume();
		
		long examId = ExamTrainer.getScoresId();
		
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getResultPerQuestion(examId);
		//Log.d(TAG,"Cursor: " + cursor);
		
		adapter = new ExamReviewAdapter(this, R.layout.review_exam_entry, cursor);
		scoresGrid = (GridView) findViewById(R.id.review_exam_grid);
		scoresGrid.setAdapter(adapter);
		
		scoresGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(ExamReviewActivity.this, ExamQuestionActivity.class);
				ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.REVIEW);
				ExamTrainer.setQuestionId(intent, adapter.getItemId(position));
				startActivity(intent);
			}
		});
		
		final int amountOfQuestionsAnswered = adapter.getCount();
		int amountOfQuestions = examinationDbHelper.getQuestionsCount();
		if( amountOfQuestionsAnswered < amountOfQuestions ) {
			Button button = (Button) findViewById(R.id.review_exam_resume_button);
			button.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					Intent intent = new Intent(ExamReviewActivity.this, ExamQuestionActivity.class);
					ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.REVIEW);
					ExamTrainer.setQuestionId(intent, adapter.getItemId(amountOfQuestionsAnswered));
					startActivity(intent);
				}
			});
			button.setVisibility(View.VISIBLE);
		}
		
		examinationDbHelper.close();
	}
	
	protected void onDestroy() {
		super.onDestroy();
		Cursor cursor = adapter.getCursor();
		if ( cursor != null ) { 
			cursor.close();
		}
	}
}
