package nl.atcomputing.examtrainer.activities;

import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.examparser.XmlPullExamListParser;
import nl.atcomputing.examtrainer.fragments.ExamSelectFragment;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * @author martijn brekhof
 * Copyright AT Computing 2012
 */
public class StartScreenActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);		

		//Load default preference values
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		Button startExam = (Button) findViewById(R.id.button_start);
		startExam.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(StartScreenActivity.this, ExamActivity.class);
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
		
		loadLocalExams();
	}
	
	private void loadLocalExams() {
		int file_index = 0;
		String[] filenames = null;

		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		AssetManager assetManager = this.getAssets();

		if( assetManager != null ) {
			try {
				XmlPullExamListParser xmlPullExamListParser;
				filenames = assetManager.list("");
				int size = filenames.length;
				for( file_index = 0; file_index < size; file_index++) {
					String filename = filenames[file_index];
					if(filename.matches("list.xml")) {
						URL url = new URL("file:///"+filename);
						xmlPullExamListParser = new XmlPullExamListParser(this, url);
						xmlPullExamListParser.parse();
						ArrayList<Exam> exams = xmlPullExamListParser.getExamList();
												
						for ( Exam exam : exams ) {
							if ( ! examTrainerDbHelper.checkIfExamAlreadyInDatabase(exam) ) {
								exam.addToDatabase(this);
							}
						}
					}
				}
			} catch (Exception e) {
				Log.d(this.getClass().getName() , "Updating exams failed: Error " + e.getMessage());
				Toast.makeText(this, "Error: updating exam " + filenames[file_index] + " failed.", Toast.LENGTH_LONG).show();
			}
		}
		examTrainerDbHelper.close();
	}
}