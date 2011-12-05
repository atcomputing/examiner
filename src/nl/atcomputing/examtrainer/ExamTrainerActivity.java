package nl.atcomputing.examtrainer;

import java.io.IOException;

import nl.atcomputing.examtrainer.ExamTrainer.ExamTrainerMode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

//		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
//		examTrainerDbHelper.open();
//		examTrainerDbHelper.upgrade();
//		examTrainerDbHelper.close();
		
		Button startExam = (Button) findViewById(R.id.button_start);
		startExam.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, SelectExamActivity.class);
				ExamTrainer.setMode(ExamTrainerMode.EXAM);
				startActivity(intent);
			}
		});
		
//		Button reviewPreviousExam = (Button) findViewById(R.id.button_show_history);
//		reviewPreviousExam.setOnClickListener( new View.OnClickListener() {
//			public void onClick(View v) {
//				Intent intent = new Intent(ExamTrainerActivity.this, SelectExamActivity.class);
//				ExamTrainer.setMode(ExamTrainerMode.REVIEW);
//				startActivity(intent);
//			}
//		});
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.main_menu_manage_exams:
			intent = new Intent(ExamTrainerActivity.this, ManageExamsActivity.class);
			startActivity(intent);
			break;
		case R.id.main_menu_settings:
			intent = new Intent(ExamTrainerActivity.this, PreferencesActivity.class);
			startActivity(intent);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	protected void onDestroy() {
		super.onDestroy();
	} 
	
	protected void retrieveExam() {
		Intent intent = new Intent(this, RetrieveExamQuestions.class);
		startService(intent);
	}
	
	
}