package nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.ExamTrainer.ExamTrainerMode;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * @author martijn brekhof
 *
 */
public class ExamTrainerActivity extends Activity {
	private final String TAG = this.getClass().getName();
	private LinearLayout about_layout;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);		

		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		examTrainerDbHelper.upgrade();
		examTrainerDbHelper.close();
		
		about_layout = (LinearLayout) findViewById(R.id.about_window);
		about_layout.setVisibility(View.INVISIBLE);
		
		Button startExam = (Button) findViewById(R.id.button_start);
		startExam.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, SelectExamActivity.class);
				ExamTrainer.setMode(ExamTrainerMode.EXAM);
				startActivity(intent);
			}
		});
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
		case R.id.main_menu_about:
			if( about_layout.getVisibility() == View.INVISIBLE ) {
				about_layout.setVisibility(View.VISIBLE);
			} else {
				about_layout.setVisibility(View.INVISIBLE);
			}
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
	
	protected void retrieveExam() {
		Intent intent = new Intent(this, RetrieveExamQuestions.class);
		startService(intent);
	}
	
	
}