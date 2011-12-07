package nl.atcomputing.examtrainer;

import java.util.ArrayList;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.ExamTrainer.ExamTrainerMode;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

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
	private Button cancelButton;
	private long examId;
	

	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "Activity started");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review_exam);

		cancelButton = (Button) findViewById(R.id.review_exam_cancel);

		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		examId = ExamTrainer.getExamId();
		
		Log.d(TAG, "databaseName: " + ExamTrainer.getExamDatabaseName() + "examId: " + examId);

		examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		cursor = examinationDbHelper.getResultPerQuestion(examId);

		

		adapter = new ExamReviewAdapter(this, R.layout.review_exam_entry, cursor);
		scoresGrid = (GridView) findViewById(R.id.review_exam_grid);
		scoresGrid.setAdapter(adapter);
		
		setupListener();
	}

	protected void onDestroy() {
		super.onDestroy();
		cursor.close();
		examinationDbHelper.close();
	}

	private void setupListener() {
		scoresGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "Pos: " + position + " QuestionId: " + adapter.getItemId(position));
				Intent intent = new Intent(ExamReviewActivity.this, ExamQuestionsActivity.class);
				ExamTrainer.setMode(ExamTrainerMode.REVIEW);
				ExamTrainer.setQuestionNumber(intent, adapter.getItemId(position));
				startActivity(intent);
			}
		});
	}
}