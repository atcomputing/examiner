package nl.atcomputing.examtrainer.activities;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.fragments.AbstractFragment;
import nl.atcomputing.examtrainer.fragments.AbstractFragment.FragmentListener;
import nl.atcomputing.examtrainer.fragments.ExamOverviewFragment;
import nl.atcomputing.examtrainer.fragments.ExamQuestionFragment;
import nl.atcomputing.examtrainer.fragments.ExamQuestionFragment.ExamQuestionListener;
import nl.atcomputing.examtrainer.fragments.ExamReviewFragment;
import nl.atcomputing.examtrainer.fragments.ExamSelectFragment;
import nl.atcomputing.examtrainer.scorecalculation.ShowScoreActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
/**
 * @author martijn brekhof
 *
 */

public class ExamActivity extends SherlockFragmentActivity 
implements FragmentListener, ExamQuestionListener, OnBackStackChangedListener {

	private ReceiveBroadcast receiveBroadcast;
	private ExamQuestionFragment examQuestionFragment;
	private ExamOverviewFragment examOverviewFragment;
	private ExamReviewFragment examReviewFragment;
	private ExamSelectFragment examSelectFragment;

	private class ReceiveBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if( action.contentEquals(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED) ) {
				examOverviewFragment.updateView();
			}
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

		FragmentManager fm = getSupportFragmentManager();
		fm.addOnBackStackChangedListener(this);
	}

	public void onPause() {
		super.onPause();
		this.unregisterReceiver(this.receiveBroadcast);
		FragmentManager fm = getSupportFragmentManager();
		fm.removeOnBackStackChangedListener(this);
	}

	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();

		if ( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.EXAM ) {
			examQuestionFragment.showDialog(ExamQuestionFragment.DIALOG_QUITEXAM_ID);
			return;
		}

		if ( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.EXAM_REVIEW ) {
			fm.popBackStack(ExamTrainer.ExamTrainerMode.EXAM_REVIEW.name(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		} else if ( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.CALCULATING_SCORE ) {
			fm.popBackStack();
		} else {
			fm.popBackStack();
		}
		
		
	}

	private void setActiveFragment(Fragment fragment) {
		if( fragment instanceof ExamQuestionFragment ) {
			this.examQuestionFragment = (ExamQuestionFragment) fragment;
		} else if ( fragment instanceof ExamReviewFragment ) {
			this.examReviewFragment = (ExamReviewFragment) fragment;
			ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.SHOW_EXAM_REVIEW);
		} else if ( fragment instanceof ExamOverviewFragment ) {
			this.examOverviewFragment = (ExamOverviewFragment) fragment;
			ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.SHOW_EXAM_OVERVIEW);
		} else if ( fragment instanceof ExamSelectFragment ) {
			this.examSelectFragment = (ExamSelectFragment) fragment;
			ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.SELECT_EXAM);
		}
	}

	private void startCalculateScoreActivity() {
		ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.CALCULATING_SCORE);
		Intent intent = new Intent(ExamActivity.this, ShowScoreActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void startExam() {
		long scoresId;
		long questionId = 1;
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());

		if( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.SHOW_EXAM_OVERVIEW ) {
			scoresId = examinationDbHelper.createNewScore();
			examinationDbHelper.close();
			if( scoresId == -1 ) {
				Toast.makeText(this, this.getString(R.string.failed_to_create_a_new_score_for_the_exam), Toast.LENGTH_LONG).show();
				return;
			}
		} else {
			scoresId = ExamTrainer.getScoresId();
			questionId = examinationDbHelper.getLastAnsweredQuestionId(scoresId);
			examinationDbHelper.close();
			if( questionId == -1 ) {
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
		showQuestionFragment(questionId);
	}
	
	private void showQuestionFragment(long number) {
		String mode = ExamTrainer.getExamMode().name();
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		this.examQuestionFragment = new ExamQuestionFragment();
		this.examQuestionFragment.setQuestionId(number);
		fragmentTransaction.replace(R.id.exam_fragment_holder, this.examQuestionFragment, mode);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fragmentTransaction.addToBackStack(mode);
		fragmentTransaction.commit();
	}

	private void showSelectExamFragment() {
		String mode = ExamTrainer.ExamTrainerMode.SELECT_EXAM.name();
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		this.examSelectFragment = new ExamSelectFragment();
		fragmentTransaction.replace(R.id.exam_fragment_holder, this.examSelectFragment, mode);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.addToBackStack(mode);
		fragmentTransaction.commit();
	}

	private void showExamOverviewFragment() {
		String mode = ExamTrainer.ExamTrainerMode.SHOW_EXAM_OVERVIEW.name();
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		this.examOverviewFragment = new ExamOverviewFragment();
		fragmentTransaction.replace(R.id.exam_fragment_holder, this.examOverviewFragment, mode);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.addToBackStack(mode);
		fragmentTransaction.commit();
	}

	private void showExamReviewFragment(long id) {
		String mode = ExamTrainer.ExamTrainerMode.SHOW_EXAM_REVIEW.name();
		ExamTrainer.setScoresId(id);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		this.examReviewFragment = new ExamReviewFragment();
		fragmentTransaction.replace(R.id.exam_fragment_holder, this.examReviewFragment, mode);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.addToBackStack(mode);
		fragmentTransaction.commit();
	}

	public void onStopExam() {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack(ExamTrainer.ExamTrainerMode.EXAM.name(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.SHOW_EXAM_OVERVIEW);
	}

	public void onExamEnd() {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack(ExamTrainer.ExamTrainerMode.EXAM.name(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		startCalculateScoreActivity();
	}

	public void onItemClickListener(long id) {
		Log.d("ExamActivity", "onItemClickListener: id="+id+", ExamMode="+ExamTrainer.getExamMode().name());
		if (ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.SHOW_EXAM_REVIEW) {
			int amountOfQuestionsAnswered = this.examReviewFragment.getAmountOfQuestionsAnswered();
			if( amountOfQuestionsAnswered < ExamTrainer.getAmountOfItems() ) {
				Toast.makeText(this, R.string.Reviewing_questions_is_only_available_after_completing_the_exam,
						Toast.LENGTH_SHORT).show();
			} else {
				ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM_REVIEW);
				showQuestionFragment(id);
			}
		} else if (ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.SHOW_EXAM_OVERVIEW) {
			showExamReviewFragment(id);
		} else if (ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.SELECT_EXAM) {
			ExamTrainer.setExamId(id);
			showExamOverviewFragment();
		}
	}

	public void onButtonClickListener(AbstractFragment fragment, long id) {
		if( fragment instanceof ExamOverviewFragment ) {
			startExam();
		} else if ( fragment instanceof ExamQuestionFragment ) {
			showQuestionFragment(id);
		} else if ( fragment instanceof ExamReviewFragment ) {
			startExam();
		}
	}

	public void onBackStackChanged() {
		FragmentManager fm = getSupportFragmentManager();
		int currentBackStackEntryCount = fm.getBackStackEntryCount();

		if( currentBackStackEntryCount == 0 ) {
			finish();
			return;
		}

		BackStackEntry bse = fm.getBackStackEntryAt(currentBackStackEntryCount-1);
		String fragmentName = bse.getName();
		setActiveFragment(fm.findFragmentByTag(fragmentName));
	}
}
