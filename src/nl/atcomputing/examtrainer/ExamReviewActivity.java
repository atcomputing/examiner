package nl.atcomputing.examtrainer;

import java.util.ArrayList;

import nl.atcomputing.examtrainer.R;
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

		Intent intent = getIntent();
		examId = intent.getLongExtra("examId", 1);

		Log.d(TAG, "examId: " + examId);

		examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open();
		cursor = examinationDbHelper.getScoresAnswers(examId);

		Resources res = this.getResources();
		not_okImage = res.getDrawable(R.drawable.not_ok_48x48);
		okImage = res.getDrawable(R.drawable.ok_48x48);

		adapter = new ImageAdapter(this);
		scoresGrid = (GridView) findViewById(R.id.review_exam_grid);
		scoresGrid.setAdapter(adapter);
		
		setupListener();
	}

	protected void onDestroy() {
		super.onDestroy();
		examinationDbHelper.close();
	}

	private void setupListener() {
		scoresGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "Pos: " + position + " QuestionId: " + adapter.getItem(position).toString());
				Intent intent = new Intent(ExamReviewActivity.this, ExamQuestionsActivity.class);
				intent.putExtra("questionId", (long) adapter.getItemId(position));
				intent.putExtra("reviewExam", true);
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
			return examinationDbHelper.getAmountOfScoreAnswers(examId);
		}

		public View getView(int position, View convertView, ViewGroup parent) 
		{
			View MyView = convertView;
			long questionId = 0;
			
			if ( convertView == null )
			{  
				Drawable ok_notokImage;
				ok_notokImage = not_okImage;
				
				LayoutInflater li = getLayoutInflater();
				MyView = li.inflate(R.layout.review_exam_entry, null);

				

				if( cursor.moveToNext() ) {
					int index = cursor.getColumnIndex(ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID);
					questionId = cursor.getLong(index);

					index = cursor.getColumnIndex(ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER);
					String answer = cursor.getString(index);

					if ( examinationDbHelper.checkAnswer(answer, questionId) == true ) {
						ok_notokImage = okImage;
					}
				}
				
				questionIds.add(questionId);
				
				TextView tv = (TextView)MyView.findViewById(R.id.reviewExamQuestionID);
				tv.setText(Long.toString(questionId));
				
				ImageView iv = (ImageView)MyView.findViewById(R.id.reviewExamAnswer);
				iv.setImageDrawable(ok_notokImage);
			}

			return MyView;
		}

		public Object getItem(int position) {
			return questionIds.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
		
		
	}
}