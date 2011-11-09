package nl.atcomputing.examtrainer;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * @author martijn brekhof
 *
 */
public class ExamTrainerActivity extends Activity {
	private final String TAG = this.getClass().getName();
	
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);		

		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		examTrainerDbHelper.upgrade();
		examTrainerDbHelper.initialize();
		examTrainerDbHelper.close();
		
		Button startExam = (Button) findViewById(R.id.button_select_exam);
		startExam.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, SelectExamActivity.class);
				startActivity(intent);
			}
		});
		
		Button configureExamTrainer = (Button) findViewById(R.id.button_configure_examtrainer);
		configureExamTrainer.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, ConfigureActivity.class);
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