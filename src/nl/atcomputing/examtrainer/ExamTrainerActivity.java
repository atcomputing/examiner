package nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.manage.RetrieveExamQuestions;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * @author martijn brekhof
 *
 */
public class ExamTrainerActivity extends Activity {
	private LinearLayout about_layout;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);		
		
		//Load default preference values
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		this.about_layout = (LinearLayout) findViewById(R.id.about_window);
		this.about_layout.setVisibility(View.INVISIBLE);

		Button startExam = (Button) findViewById(R.id.button_start);
		startExam.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, SelectExamActivity.class);
				ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM);
				startActivity(intent);
			}
		});
		
		ImageView logo = (ImageView) findViewById(R.id.logo);
		logo.setOnClickListener( new View.OnClickListener() {
			
			public void onClick(View v) {
				showHideInfo();	
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
		switch (item.getItemId()) {
		case R.id.main_menu_about:
			showHideInfo();
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
	
	private void showHideInfo() {
		if( this.about_layout.getVisibility() == View.INVISIBLE ) {
			this.about_layout.setVisibility(View.VISIBLE);
		} else {
			this.about_layout.setVisibility(View.INVISIBLE);
		}
	}
}