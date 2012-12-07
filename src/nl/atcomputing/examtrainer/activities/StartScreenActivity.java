package nl.atcomputing.examtrainer.activities;

import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.examparser.InstallExamAsyncTask;
import nl.atcomputing.examtrainer.examparser.XmlPullExamListParser;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;


/**
 * @author martijn brekhof
 * Copyright AT Computing 2012
 */
public class StartScreenActivity extends Activity {
	private final int FADEIN = 0;
	private int animationState = FADEIN;
	
	private ImageView imageView;
	private Animation animation;
	private boolean cancelThread = false;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.startscreenactivity);		

		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		
		//Load default preference values
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		this.imageView= (ImageView)findViewById(R.id.logo);
		this.animation = AnimationUtils.loadAnimation(this, R.anim.fadein);
		//this.animation.setAnimationListener(this);
		this.imageView.startAnimation(this.animation);

		Thread loadlLocalExamsThread = new Thread(new Runnable() {
			@Override
			public void run() {
				loadLocalExams();
				cleanupDatabaseStates();
				try {
					int waited = 0; // wait a maximum amount of time to prevent indefinite loop
					while ( (cancelThread == false) && (waited < 20) ) {
						synchronized (this) {
							wait(100);
						}
						waited += 1;
					}
				} catch (InterruptedException e) {
				} finally {
					Intent intent = new Intent(StartScreenActivity.this, ExamActivity.class);
					startActivity(intent);
					finish();
				}
			}
		});
		loadlLocalExamsThread.start();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if( ( event.getAction() == MotionEvent.ACTION_UP ) && ( this.animationState > -1 ) ) {
			this.animationState = -1;
			this.imageView.clearAnimation();
			this.cancelThread = true;
		}
		return true;
	}

	private void cleanupDatabaseStates() {
		ExamTrainerDbAdapter db = new ExamTrainerDbAdapter(this);
		db.open();
		
		//Remove exams with state installing that have no installation 
		//thread associated
		Cursor cursor = db.getInstallingExams();
		while( cursor.moveToNext() ) {
			int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
			long examID = cursor.getLong(index);
			InstallExamAsyncTask task = ExamTrainer.getInstallExamAsyncTask(examID);
			if( task == null ) {
				db.deleteExam(examID);
			}
		}
		
		cursor.close();
		db.close();
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