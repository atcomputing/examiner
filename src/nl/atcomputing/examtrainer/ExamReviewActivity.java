package nl.atcomputing.examtrainer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

/**
 * @author martijn brekhof
 *
 */
public class ExamReviewActivity extends Activity {
	public static final String TAG = "ExamReviewActivity";
	private ExaminationDbAdapter examinationDbHelper;
	private GridView scoresGrid;
	private ExamReviewAdapter adapter; 
	private Cursor cursor;
	private long examId;
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("trace", "ExamReviewActivity created");
		
		ExamTrainer.showProgressDialog(this, this.getString(R.string.Loading_Please_wait));
		
		setContentView(R.layout.review_exam);
		
		examId = ExamTrainer.getExamId();
		
		examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		cursor = examinationDbHelper.getResultPerQuestion(examId);

		adapter = new ExamReviewAdapter(this, R.layout.review_exam_entry, cursor);
		scoresGrid = (GridView) findViewById(R.id.review_exam_grid);
		scoresGrid.setAdapter(adapter);
		
		setupListener();
		ExamTrainer.stopProgressDialog();
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.d("trace", "ExamReviewActivity destroyed");
		if( cursor != null ) {
			cursor.close();
		}
		examinationDbHelper.close();
	}

	private void setupListener() {
		scoresGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(ExamReviewActivity.this, ExamQuestionActivity.class);
				ExamTrainer.setReview();
				ExamTrainer.setQuestionNumber(intent, adapter.getItemId(position));
				startActivity(intent);
			}
		});
	}
}
