package nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
/**
 * @author martijn brekhof
 *
 */

public class ExamActivity extends SherlockFragmentActivity {
	private Button buttonStartExam;
	private ReceiveBroadcast receiveBroadcast;

	private class ReceiveBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			setupView();
		}
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exam);
		
		this.receiveBroadcast = new ReceiveBroadcast();
		
		this.buttonStartExam = (Button) findViewById(R.id.button_start_exam);
		this.buttonStartExam.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				startExam();
			}
		});
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		ExamOverviewFragment fragment = new ExamOverviewFragment();
		fragmentTransaction.add(R.id.exam_fragment_holder, fragment);
		fragmentTransaction.commit();
	}

	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
		this.registerReceiver(this.receiveBroadcast, filter);
		setupView();
		setTitle(ExamTrainer.getExamTitle());
	}

	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(this.receiveBroadcast);
	}
	
	private void startExam() {
		Intent intent = new Intent(this, ExamQuestionActivity.class);
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		long scoresId = examinationDbHelper.createNewScore();
		examinationDbHelper.close();

		if( scoresId == -1 ) {
			Toast.makeText(this, this.getString(R.string.failed_to_create_a_new_score_for_the_exam), Toast.LENGTH_LONG).show();
		} else {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			boolean useTimelimit = prefs.getBoolean(this.getResources().getString(R.string.pref_key_use_timelimits), false);
			if( useTimelimit ) {
				ExamTrainer.setTimer();
			}
			ExamTrainer.setScoresId(scoresId);
			ExamTrainer.setQuestionId(intent, 1);
			ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM);
			startActivity(intent);
		}
	}
	
	private void setupView() {
		
	}
}
