package nl.atcomputing.examtrainer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

/**
 * @author martijn brekhof
 *
 */
public class ExamTrainerActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);		

		//Load default preference values
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		Button startExam = (Button) findViewById(R.id.button_start);
		startExam.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, SelectExamActivity.class);
				ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM);
				startActivity(intent);
			}
		});
	}
}