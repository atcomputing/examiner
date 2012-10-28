package nl.atcomputing.examtrainer.activities;

import nl.atcomputing.examtrainer.R;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * @author martijn brekhof
 * Copyright AT Computing 2012
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
				ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.SELECT_EXAM);
				startActivity(intent);
			}
		});
		
		String version;
		try {
			PackageInfo info = getPackageManager().getPackageInfo("nl.atcomputing.examtrainer", 0);
			version = info.versionName;
		} catch (NameNotFoundException e) {
			version = getString(R.string.unknown);
		}
		TextView tv = (TextView) findViewById(R.id.about_version_number);
		if( tv != null ) {
			tv.setText(version);
		}
	}
}