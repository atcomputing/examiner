package nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.adapters.ExamReviewAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

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

		setContentView(R.layout.review_exam);
	}

	protected void onResume() {
		super.onResume();

		long examId = ExamTrainer.getScoresId();

		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getResultsPerQuestion(examId);

		adapter = new ExamReviewAdapter(this, R.layout.review_exam_entry, cursor);
		scoresGrid = (GridView) findViewById(R.id.review_exam_grid);
		scoresGrid.setAdapter(adapter);

		//Disable reviewing exam items when not all questions have been answered yet.
		final int amountOfQuestionsAnswered = adapter.getCount();
		final int amountOfQuestions = examinationDbHelper.getQuestionsCount();

		if( amountOfQuestionsAnswered < amountOfQuestions ) {
			Button button = (Button) findViewById(R.id.review_exam_resume_button);
			button.setOnClickListener(new OnClickListener() {				
				public void onClick(View v) {
					Intent intent = new Intent(ExamReviewActivity.this, ExamQuestionActivity.class);
					ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM);
					ExamTrainer.setQuestionId(intent, adapter.getItemId(amountOfQuestionsAnswered - 1));
					if( (ExamTrainer.getTimeLimit() > 0)  && ( ExamTrainer.timeLimitExceeded() ) ) {
						ExamTrainer.setTimeLimit(0);
						Toast.makeText(ExamReviewActivity.this, "Time limit exceeded. Disabling time limit", Toast.LENGTH_SHORT).show();
					} 
					startActivity(intent);
					finish();
				}
			});
			button.setVisibility(View.VISIBLE);
		}

		scoresGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if( amountOfQuestionsAnswered < amountOfQuestions ) {
					Toast.makeText(ExamReviewActivity.this, R.string.Reviewing_questions_is_only_available_after_completing_the_exam,
							Toast.LENGTH_SHORT).show();
				} else {
					Intent intent = new Intent(ExamReviewActivity.this, ExamQuestionActivity.class);
					ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.REVIEW);
					ExamTrainer.setQuestionId(intent, adapter.getItemId(position));
					startActivity(intent);
				}
			}
		});


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
