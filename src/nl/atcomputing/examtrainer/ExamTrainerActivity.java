package nl.atcomputing.examtrainer;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */
public class ExamTrainerActivity extends Activity {
	private final String TAG = this.getClass().getName();
	
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);		
		
		Button startExam = (Button) findViewById(R.id.button_select_exam);
		startExam.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, SelectExamActivity.class);
				startActivity(intent);
			}
		});
		
		Button manageExams = (Button) findViewById(R.id.button_manage_exams);
		manageExams.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, ManageExamsActivity.class);
				startActivity(intent);
			}
		});
		
		Button reviewPreviousExam = (Button) findViewById(R.id.button_show_results);
		reviewPreviousExam.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, ExamResultsActivity.class);
				startActivity(intent);
			}
		});
		
		Button quitExamTrainer = (Button) findViewById(R.id.button_quit);
		quitExamTrainer.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		
	}
	
	protected void onDestroy() {
		super.onDestroy();
	} 
	
	protected void retrieveExam() {
		Intent intent = new Intent(this, RetrieveExamQuestions.class);
		startService(intent);
	}
	
	
}