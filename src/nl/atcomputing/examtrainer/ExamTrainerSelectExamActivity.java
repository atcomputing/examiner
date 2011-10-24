package nl.atcomputing.examtrainer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author martijn brekhof
 *
 */

//Example code on how to setup nice selectionbox: 
//http://www.codemobiles.com/forum/viewtopic.php?t=876

public class ExamTrainerSelectExamActivity extends Activity {
	public static final String TAG = "ExamTrainerSelectExamActivity";
	private ListView selectExamList;
	private Button cancelButton;
	private Cursor cursor;
	private SimpleCursorAdapter adapter;
	private ExamTrainerDbAdapter examTrainerDbHelper;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.selectexam);		
		
		cancelButton = (Button) findViewById(R.id.selectexam_cancel);
		selectExamList = (ListView) findViewById(R.id.selectexam_list);

		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		cursor = examTrainerDbHelper.getExams();
		
		populateExamList();

		setupListener();
	}
	
	protected void onDestroy() {
		super.onDestroy();
		examTrainerDbHelper.close();
	} 
	
	private void setupListener() {
		selectExamList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// When clicked, show a toast with the TextView text
				Cursor cursor = (Cursor) adapter.getCursor();
				int index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_DATE);
				String examDate = cursor.getString(index);
				index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
				String examTitle = cursor.getString(index);
				Log.d(TAG, "Starting exam: " + examTitle + "-" + examDate);
				Intent intent = new Intent(ExamTrainerSelectExamActivity.this, ExamQuestionsActivity.class);
				intent.putExtra("question", 1);
				intent.putExtra("databaseName", examTitle + "-" + examDate);
				startActivity(intent);
			}
		});
	}

	private void populateScoresList() {
		String[] fields = new String[] {
				ExamTrainer.Scores._ID,
				ExamTrainer.Scores.COLUMN_NAME_DATE,
				ExamTrainer.Scores.COLUMN_NAME_SCORE
		};
		adapter = new SimpleCursorAdapter(this, R.layout.show_scores_entry, cursor,
				fields, new int[] {
				R.id.selectexamEntryTitle,
				R.id.selectexamEntryDate,
				R.id.selectexamEntryAmountOfQuestions});
		selectExamList.setAdapter(adapter);
	}
}