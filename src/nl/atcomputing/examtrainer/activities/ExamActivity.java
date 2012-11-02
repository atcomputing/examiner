package nl.atcomputing.examtrainer.activities;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.fragments.ExamOverviewFragment;
import nl.atcomputing.examtrainer.fragments.ExamOverviewFragment.ExamOverviewListener;
import nl.atcomputing.examtrainer.fragments.ExamQuestionFragment;
import nl.atcomputing.examtrainer.fragments.ExamQuestionFragment.ExamQuestionListener;
import nl.atcomputing.examtrainer.fragments.ExamReviewFragment;
import nl.atcomputing.examtrainer.fragments.ExamReviewFragment.ExamReviewListener;
import nl.atcomputing.examtrainer.fragments.SelectExamFragment;
import nl.atcomputing.examtrainer.fragments.SelectExamFragment.SelectExamListener;
import nl.atcomputing.examtrainer.scorecalculation.ShowScoreActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
/**
 * @author martijn brekhof
 *
 */

public class ExamActivity extends SherlockFragmentActivity implements ExamQuestionListener, ExamReviewListener, ExamOverviewListener, SelectExamListener {
	private Button buttonStartExam;
	private Button buttonNextQuestion;
	private Button buttonPrevQuestion;

	private ReceiveBroadcast receiveBroadcast;
	private long questionId;
	private ExamQuestionFragment examQuestionFragment;
	private ExamOverviewFragment examOverviewFragment;
	private ExamReviewFragment examReviewFragment;
	private SelectExamFragment examSelectFragment;

	private class ReceiveBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.examactivity);

		this.receiveBroadcast = new ReceiveBroadcast();

		showSelectExamFragment();
	}

	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
		this.registerReceiver(this.receiveBroadcast, filter);
		setTitle(ExamTrainer.getExamTitle());

		//if mode is exam
		if( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.EXAM ) {
			//fragment.setupTimer();
		}
	}

	public void onPause() {
		super.onPause();
		this.unregisterReceiver(this.receiveBroadcast);
	}

	public void onBackPressed() {
		if ( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.EXAM ) {
			if( this.questionId == 1 ) {
				examQuestionFragment.showDialog(ExamQuestionFragment.DIALOG_QUITEXAM_ID);
			} else {
				showPrevQuestionFragment();
			}
		} else {
			FragmentManager fm = getSupportFragmentManager();
			int backStackEntryCount = fm.getBackStackEntryCount();
			if( backStackEntryCount < 2 ) {
				finish();
			} else {
				fm.popBackStack();
				BackStackEntry bse = fm.getBackStackEntryAt(backStackEntryCount - 2);
				String fragmentName = bse.getName();
				ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.valueOf(fragmentName));
			}
		}
	}
	
	private void startCalculateScoreActivity() {
		ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.ENDOFEXAM);
		Intent intent = new Intent(ExamActivity.this, ShowScoreActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	private void startExam() {
		long scoresId;
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());

		if( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.SHOW_EXAM_OVERVIEW ) {
			scoresId = examinationDbHelper.createNewScore();
			examinationDbHelper.close();
			if( scoresId == -1 ) {
				Toast.makeText(this, this.getString(R.string.failed_to_create_a_new_score_for_the_exam), Toast.LENGTH_LONG).show();
				return;
			} 
			this.questionId = 1;
		} else {
			scoresId = ExamTrainer.getScoresId();
			this.questionId = examinationDbHelper.getLastAnsweredQuestionId(scoresId);
			examinationDbHelper.close();
			if( this.questionId == -1 ) {
				Toast.makeText(this, this.getString(R.string.Failed_to_resume_exam), Toast.LENGTH_LONG).show();
				return;
			} 
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useTimelimit = prefs.getBoolean(this.getResources().getString(R.string.pref_key_use_timelimits), false);
		if( useTimelimit ) {
			ExamTrainer.setTimer();
		}
		ExamTrainer.setScoresId(scoresId);
		ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM);
		showQuestionFragment(this.questionId);
	}

	private void showNextQuestionFragment() {
		long number = this.questionId + 1;
		showQuestionFragment(number);
		this.questionId = number;
	}

	private void showPrevQuestionFragment() {
		long number = this.questionId - 1;
		showQuestionFragment(number);
		this.questionId = number;
	}

	private void showQuestionFragment(long number) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		this.examQuestionFragment = new ExamQuestionFragment();
		this.examQuestionFragment.setQuestionId(number);
		fragmentTransaction.replace(R.id.exam_fragment_holder, this.examQuestionFragment);
		if( number > this.questionId ) { 
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		} else {
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
		}
		fragmentTransaction.addToBackStack(ExamTrainer.ExamTrainerMode.EXAM.name());
		fragmentTransaction.commit();
	}

	private void showSelectExamFragment() {
		ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.SELECT_EXAM);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		this.examSelectFragment = new SelectExamFragment();
		fragmentTransaction.replace(R.id.exam_fragment_holder, this.examSelectFragment);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.addToBackStack(ExamTrainer.ExamTrainerMode.SELECT_EXAM.name());
		fragmentTransaction.commit();
	}

	private void showExamOverviewFragment() {
		ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.SHOW_EXAM_OVERVIEW);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		this.examOverviewFragment = new ExamOverviewFragment();
		fragmentTransaction.replace(R.id.exam_fragment_holder, this.examOverviewFragment);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.addToBackStack(ExamTrainer.ExamTrainerMode.SHOW_EXAM_OVERVIEW.name());
		fragmentTransaction.commit();
	}

	private void showExamReviewFragment(long id) {
		ExamTrainer.setScoresId(id);
		ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.SHOW_SCORE_OVERVIEW);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		this.examReviewFragment = new ExamReviewFragment();
		fragmentTransaction.replace(R.id.exam_fragment_holder, this.examReviewFragment);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.addToBackStack(ExamTrainer.ExamTrainerMode.SHOW_SCORE_OVERVIEW.name());
		fragmentTransaction.commit();
	}

	public void onStopExam() {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack(ExamTrainer.ExamTrainerMode.EXAM.name(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		showExamOverviewFragment();
	}

	public void onExamEnd() {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack(ExamTrainer.ExamTrainerMode.EXAM.name(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		startCalculateScoreActivity();
	}

	public void onItemClickListener(long id) {
		if (ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.EXAM_REVIEW) {
			int amountOfQuestionsAnswered = this.examReviewFragment.getAmountOfQuestionsAnswered();
			if( amountOfQuestionsAnswered < ExamTrainer.getAmountOfItems() ) {
				Toast.makeText(this, R.string.Reviewing_questions_is_only_available_after_completing_the_exam,
						Toast.LENGTH_SHORT).show();
			} else {
				showQuestionFragment(id);
			}
		} else if (ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.SHOW_EXAM_OVERVIEW) {
			showExamReviewFragment(id);
		} else if (ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.SELECT_EXAM) {
			ExamTrainer.setExamId(id);
			showExamOverviewFragment();
		}
	}

	public void onButtonClickListener(SherlockFragment fragment, long id) {
		Log.d("ExamActivity", "onButtonClickListener: " + fragment.getClass().getSimpleName());
		if( fragment instanceof ExamOverviewFragment ) {
			startExam();
		} else if ( fragment instanceof ExamQuestionFragment ) {
			showQuestionFragment(id);
		}
	}


}
