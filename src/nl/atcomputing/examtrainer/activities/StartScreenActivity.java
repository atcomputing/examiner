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

		cleanupDatabaseStates();
		loadLocalExams();
		
		Thread waitThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int waited = 0; // wait a maximum amount of time to prevent indefinite loop
					while ( (cancelThread == false) && (waited < 15) ) {
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
		waitThread.start();
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
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();

		try {
			XmlPullExamListParser xmlPullExamListParser;
			URL url = new URL("file:///list.xml");
			xmlPullExamListParser = new XmlPullExamListParser(this, url);
			xmlPullExamListParser.parse();
			ArrayList<Exam> exams = xmlPullExamListParser.getExamList();

			for ( Exam exam : exams ) {
				if ( examTrainerDbHelper.getRowId(exam) == -1 ) {
					exam.addToDatabase(this);
				}
			}
		} catch (Exception e) {
			Log.w(this.getClass().getName() , "Updating exams failed: Error " + e.getMessage());
			Toast.makeText(this, "Error: updating exams failed.", Toast.LENGTH_LONG).show();
		}
		examTrainerDbHelper.close();
		
		
		InstallExamAsyncTask task = new InstallExamAsyncTask(this, null, null);
		task.execute();
	}
}