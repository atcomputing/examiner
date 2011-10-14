package nl.atcomputing.lpic1examtrainer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;

/**
 * @author martijn brekhof
 *
 */
public class ExamReviewActivity extends Activity {
	public static final String TAG = "ExamReviewActivity";
	private ExamTrainerDbAdapter dbHelper;
	private SimpleCursorAdapter adapter;
	private GridView scoresGrid;
	private Cursor cursor;
	private Button cancelButton;
	
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "Activity started");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review_exam);
		
		cancelButton = (Button) findViewById(R.id.review_exam_cancel);
		scoresGrid = (GridView) findViewById(R.id.review_exam_grid);
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		Intent intent = getIntent();
		long examId = intent.getLongExtra("examId", 1);
		
		Log.d(TAG, "examId: " + examId);
		
		dbHelper = new ExamTrainerDbAdapter(this);
		dbHelper.open();
		cursor = dbHelper.getScoresAnswers(examId);
		
		populateScoresAnswersGrid();
	}
	
	private void populateScoresAnswersGrid() {
		String[] fields = new String[] {
				ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID,
				ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER
		};
		adapter = new SimpleCursorAdapter(this, R.layout.review_exam_entry, cursor,
				fields, new int[] {
				R.id.reviewExamQuestionID,
				R.id.reviewExamAnswer});
		scoresGrid.setAdapter(adapter);
	}
}