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
	private XmlPullExamParser xmlPullFeedParser;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);		
		
		Button startExam = (Button) findViewById(R.id.button_start_exam);
		startExam.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, ExamTrainerSelectExamActivity.class);
				startActivity(intent);
			}
		});
		
		Button updateExam = (Button) findViewById(R.id.button_get_updates);
		updateExam.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				checkForUpdates();
			}
		});
		
		Button reviewPreviousExam = (Button) findViewById(R.id.button_show_results);
		reviewPreviousExam.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamTrainerActivity.this, ExamResultsActivity.class);
				intent.putExtra("action", ExamResultsActivity.NONE);
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
	
	private void checkForUpdates() {
		int file_index = 0;
		String[] filenames = null;
		//retrieveExam();
		//For testing purposes
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		examTrainerDbHelper.upgrade();
		examTrainerDbHelper.close();
		
		AssetManager assetManager = getAssets();
		
		if( assetManager != null ) {
			try {
				filenames = assetManager.list("");
				int size = filenames.length;
				for( file_index = 0; file_index < size; file_index++) {
					String filename = filenames[file_index];
					if(filename.matches("exam..*.xml")) {
						Log.d(TAG, "Found databasefile " + filename);
						InputStream raw = getApplicationContext().getAssets().open(filename);
						xmlPullFeedParser = new XmlPullExamParser(this, raw);
						xmlPullFeedParser.parse();
						if ( xmlPullFeedParser.checkIfExamInDatabase() ) {
							//Exam found in database. Ask user what to do.
							Log.d(TAG, "Included Exam already in database: " + filename);
						}
						else {
							Log.d(TAG, "Included Exam not in database:  " + filename);
							xmlPullFeedParser.addExam();
						}
					}
				}
			} catch (Exception e) {
				Log.d(this.getClass().getName() , "Updating exams failed: Error " + e.getMessage());
				Toast.makeText(this, "Error: updating exam " + filenames[file_index] + " failed.", Toast.LENGTH_LONG).show();
			}
		}
	}
}