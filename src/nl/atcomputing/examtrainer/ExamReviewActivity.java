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
	private GridView scoresGrid;
	private ExamReviewAdapter adapter; 
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("trace", "ExamReviewActivity created");
		
		ExamTrainer.showProgressDialog(this, this.getString(R.string.Loading_Please_wait));
		
		setContentView(R.layout.review_exam);
		
		long examId = ExamTrainer.getExamId();
		
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getResultPerQuestion(examId);
		Log.d(TAG,"Cursor: " + cursor);
		examinationDbHelper.close();
		
		adapter = new ExamReviewAdapter(this, R.layout.review_exam_entry, cursor);
		scoresGrid = (GridView) findViewById(R.id.review_exam_grid);
		scoresGrid.setAdapter(adapter);
		
		scoresGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(ExamReviewActivity.this, ExamQuestionActivity.class);
				ExamTrainer.setReview();
				ExamTrainer.setQuestionNumber(intent, adapter.getItemId(position));
				startActivity(intent);
			}
		});
		
		ExamTrainer.stopProgressDialog();
	}

	protected void onDestroy() {
		super.onDestroy();
		Cursor cursor = adapter.getCursor();
		if ( cursor != null ) { 
			cursor.close();
		}
	}
}
