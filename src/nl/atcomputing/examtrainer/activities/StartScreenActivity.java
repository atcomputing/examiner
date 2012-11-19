package nl.atcomputing.examtrainer.activities;

import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.examparser.XmlPullExamListParser;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;


/**
 * @author martijn brekhof
 * Copyright AT Computing 2012
 */
public class StartScreenActivity extends Activity {
	private Thread waitThread;
	private boolean cancelThread = false;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.startscreenactivity);		

		//Load default preference values
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		this.waitThread = new Thread(new Runnable() {

			@Override
			public void run() {
				loadLocalExams();
				try {
					int waited = 0;
					while ( (cancelThread == false) && (waited < 2000) ) {
						synchronized (this) {
							wait(100);
						}
						waited += 100;
					}
				} catch (InterruptedException e) {

				} finally {
					Intent intent = new Intent(StartScreenActivity.this, ExamActivity.class);
					startActivity(intent);
					finish();
				}
			}
		});

		this.waitThread.start();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.cancelThread = true;
		return true;
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
				Log.w(this.getClass().getName() , "Updating exams failed: Error " + e.getMessage());
				Toast.makeText(this, "Error: updating exam " + filenames[file_index] + " failed.", Toast.LENGTH_LONG).show();
			}
		}
		examTrainerDbHelper.close();
	}
}