package nl.atcomputing.lpic1examtrainer;

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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

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
	private long examId;
	Drawable not_okImage;
	Drawable okImage;
	
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
		examId = intent.getLongExtra("examId", 1);
		
		Log.d(TAG, "examId: " + examId);
		
		dbHelper = new ExamTrainerDbAdapter(this);
		dbHelper.open();
		cursor = dbHelper.getScoresAnswers(examId);
		
		Resources res = this.getResources();
		not_okImage = res.getDrawable(R.drawable.not_ok_48x48);
		okImage = res.getDrawable(R.drawable.ok_48x48);
		
		//populateScoresAnswersGrid();
		scoresGrid.setAdapter(new ImageAdapter(this));
	}
	
	protected void onDestroy() {
		super.onDestroy();
		dbHelper.close();
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
	
	public class ImageAdapter extends BaseAdapter
	{
	      Context MyContext;
	      
	      public ImageAdapter(Context _MyContext)
	      {
	         MyContext = _MyContext;
	      }
	      
	      public int getCount() 
	      {
	         /* Set the number of element we want on the grid */
	    	 return dbHelper.getAmountOfScoreAnswers(examId);
	      }

	      public View getView(int position, View convertView, ViewGroup parent) 
	      {
	         View MyView = convertView;
	         Drawable icon;
	         
	         icon = not_okImage;
	         
	         if ( convertView == null )
	         {  
	            LayoutInflater li = getLayoutInflater();
	            MyView = li.inflate(R.layout.review_exam_entry, null);
	            
	            long questionId = 0;
	            
	            if( cursor.moveToNext() ) {
	            	int index = cursor.getColumnIndex(ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID);
	            	questionId = cursor.getLong(index);
	            	
	            	index = cursor.getColumnIndex(ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER);
	            	String answer = cursor.getString(index);
	            	
	            	if ( dbHelper.checkAnswer(answer, questionId) == true ) {
	            		icon = okImage;
	            	}
	            }
	            TextView tv = (TextView)MyView.findViewById(R.id.reviewExamQuestionID);
	            tv.setText(Long.toString(questionId));
	            
	            ImageView iv = (ImageView)MyView.findViewById(R.id.reviewExamAnswer);
	            iv.setImageDrawable(icon);
	         }
	         
	         return MyView;
	      }

	      public Object getItem(int arg0) {
	         // TODO Auto-generated method stub
	         return null;
	      }

	      public long getItemId(int arg0) {
	         // TODO Auto-generated method stub
	         return 0;
	      }
	   }
}