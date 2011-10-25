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
		
		//retrieveExam();
		//For testing purposes
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		examTrainerDbHelper.upgrade();
		examTrainerDbHelper.close();
		
		AssetManager assetManager = getAssets();
		
		if( assetManager != null ) {
			try {
				String[] filenames = assetManager.list("");
				int size = filenames.length;
				for( int i = 0; i < size; i++) {
					if(filenames[i].matches("exam..*.xml")) {
						Log.d(TAG, "Found databasefile " + filenames[i]);
						InputStream raw = getApplicationContext().getAssets().open(filenames[i]);
						xmlPullFeedParser = new XmlPullExamParser(this, raw);
						if ( xmlPullFeedParser.checkIfExamInDatabase() ) {
							//Exam found in database. Ask user what to do.
							Log.d(TAG, "Included Exam already in database: " + filenames[i]);
						}
						else {
							Log.d(TAG, "Included Exam not in database:  " + filenames[i]);
							xmlPullFeedParser.addExam();
						}
					}
				}
			} catch (IOException e) {
				Log.d(this.getClass().getName() , e.getMessage());
			}
		}
	}
}