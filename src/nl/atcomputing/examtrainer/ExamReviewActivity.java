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
	private ImageAdapter adapter; 
	private Cursor cursor;
	private Button cancelButton;
	private long examId;
	Drawable not_okImage;
	Drawable okImage;

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

		Resources res = this.getResources();
		not_okImage = res.getDrawable(R.drawable.not_ok);
		okImage = res.getDrawable(R.drawable.ok);

		adapter = new ImageAdapter(this);
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

	public class ImageAdapter extends BaseAdapter
	{
		Context MyContext;
		ArrayList<Long> questionIds = new ArrayList<Long>();
		
		public ImageAdapter(Context _MyContext)
		{
			MyContext = _MyContext;
		}

		public int getCount() 
		{
			/* Set the number of element we want on the grid */
			return cursor.getCount();
		}

		public View getView(int position, View convertView, ViewGroup parent) 
		{
			View MyView = convertView;
			long questionId = 0;
			
			Log.d(TAG, "position: "+ position + " convertView: " + convertView );
			if ( convertView == null )
			{  
				int answer = 0;
				
				LayoutInflater li = getLayoutInflater();
				MyView = li.inflate(R.layout.review_exam_entry, null);

				if( cursor.moveToPosition(position) ) {
					int index = cursor.getColumnIndex(ExamTrainer.ResultPerQuestion.COLUMN_NAME_QUESTION_ID);
					questionId = cursor.getLong(index);

					index = cursor.getColumnIndex(ExamTrainer.ResultPerQuestion.COLUMN_NAME_ANSWER_CORRECT);
					answer = cursor.getInt(index);
				}
				
				questionIds.add(questionId);
				
				TextView tv = (TextView)MyView.findViewById(R.id.reviewExamQuestionID);
				tv.setText(Long.toString(questionId));
				
				ImageView iv = (ImageView)MyView.findViewById(R.id.reviewExamAnswer);
				if( answer == 1 ) {
					iv.setImageDrawable(okImage);
				} 
				else {
					iv.setImageDrawable(not_okImage);
				}
					
			}

			return MyView;
		}

		public Object getItem(int position) {
			return 0;
		}

		public long getItemId(int position) {
			return questionIds.get(position);
		}
		
		
	}
}