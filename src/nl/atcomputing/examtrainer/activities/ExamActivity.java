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
import nl.atcomputing.examtrainer.fragments.ManageExamsFragment;
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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
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
	private ManageExamsFragment manageExamsFragment;
	private AbstractFragment activeFragment;

	private class ReceiveBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d("ExamACtivity", "ReceiveBroadcast: action="+action);
			if( action.contentEquals(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED) ) {
				activeFragment.updateView();
			}
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.examactivity);

		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		
		this.receiveBroadcast = new ReceiveBroadcast();

		if( savedInstanceState == null ) {
			showSelectExamFragment(true);
		}
	}

	public void onResume() {
		super.onResume();

		IntentFilter filter = new IntentFilter(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
		this.registerReceiver(this.receiveBroadcast, filter);

		FragmentManager fm = getSupportFragmentManager();
		fm.addOnBackStackChangedListener(this);

		int currentBackStackEntryCount = fm.getBackStackEntryCount();

		if( currentBackStackEntryCount == 0 ) {
			return;
		}

		BackStackEntry bse = fm.getBackStackEntryAt(currentBackStackEntryCount-1);
		String fragmentName = bse.getName();
		Fragment fragment = fm.findFragmentByTag(fragmentName);
		setActiveFragment(fragment);

		updateActionBar();
	}

	public void onPause() {
		super.onPause();
		this.unregisterReceiver(this.receiveBroadcast);
		FragmentManager fm = getSupportFragmentManager();
		fm.removeOnBackStackChangedListener(this);
	}

	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();

		int currentBackStackEntryCount = fm.getBackStackEntryCount();

		if( currentBackStackEntryCount == 1 ) {
			finish();
			return;
		}

		if ( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.EXAM ) {
			examQuestionFragment.showDialog(ExamQuestionFragment.DIALOG_QUITEXAM_ID);
			return;
		}

		if ( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.EXAM_REVIEW ) {
			fm.popBackStack(ExamTrainer.ExamTrainerMode.EXAM_REVIEW.name(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		} else {
			fm.popBackStack();
		}


	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.selectexam_menu_manage:
			showManageExamsFragment(true);
			break;
		case android.R.id.home:
			if( this.activeFragment != this.examSelectFragment ) {
				onBackPressed();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	public void onStopExam() {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack(ExamTrainer.ExamTrainerMode.EXAM.name(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		fm.popBackStack(ExamTrainer.ExamTrainerMode.EXAM_REVIEW.name(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM_OVERVIEW);
	}

	public void onExamEnd() {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack(ExamTrainer.ExamTrainerMode.EXAM.name(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		fm.popBackStack(ExamTrainer.ExamTrainerMode.EXAM_REVIEW.name(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
		startCalculateScoreActivity();
	}

	public void onItemClickListener(long id) {
		if (ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.SHOW_EXAM_REVIEW) {		
			ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM_REVIEW);
			showExamQuestionFragment(id, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		} else if (ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.EXAM_OVERVIEW) {
			showExamReviewFragment(id, true);
		} else if (ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.SELECT_EXAM) {
			ExamTrainer.setExamId(id);
			showExamOverviewFragment(true);
		}
	}

	public void onButtonClickListener(AbstractFragment fragment, long id) {
		if( fragment instanceof ExamOverviewFragment ) {
			startExam();
		} else if ( fragment instanceof ExamQuestionFragment ) {
			long questionId = ((ExamQuestionFragment) fragment).getQuestionId();
			if( id > questionId ) { // if new question number is larger than current question number
				showExamQuestionFragment(id, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			} else {
				showExamQuestionFragment(id, FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
			}
		} else if ( fragment instanceof ExamReviewFragment ) {
			startExam();
		} else if ( fragment instanceof ManageExamsFragment ) {
			this.manageExamsFragment.updateView();
		}
	}

	public void onBackStackChanged() {
		FragmentManager fm = getSupportFragmentManager();
		int currentBackStackEntryCount = fm.getBackStackEntryCount();

		if( currentBackStackEntryCount == 0 ) {
			return;
		}

		BackStackEntry bse = fm.getBackStackEntryAt(currentBackStackEntryCount-1);
		String fragmentName = bse.getName();
		Fragment fragment = fm.findFragmentByTag(fragmentName);

		setActiveFragment(fragment);
		updateActionBar();
	}

	private void updateActionBar() {
		String title = this.activeFragment.getTitle();
		if( title != null ) {
			setTitle(title);
		}
		
		ActionBar actionBar = getSupportActionBar();
		if( this.activeFragment != this.examSelectFragment ) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		} else {
			actionBar.setDisplayHomeAsUpEnabled(false);
		}
	}

	private void setActiveFragment(Fragment fragment) {
		if( fragment instanceof AbstractFragment ) {
			this.activeFragment = (AbstractFragment) fragment;
		} else {
			Log.e("ExamActivity", "Error: fragment "+fragment+" is not of type AbstractFragment");
		}

		if( fragment instanceof ExamQuestionFragment ) {
			this.examQuestionFragment = (ExamQuestionFragment) fragment;
		} else if ( fragment instanceof ExamReviewFragment ) {
			this.examReviewFragment = (ExamReviewFragment) fragment;
			ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.SHOW_EXAM_REVIEW);
		} else if ( fragment instanceof ExamOverviewFragment ) {
			this.examOverviewFragment = (ExamOverviewFragment) fragment;
			ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM_OVERVIEW);
		} else if ( fragment instanceof ExamSelectFragment ) {
			this.examSelectFragment = (ExamSelectFragment) fragment;
			ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.SELECT_EXAM);
		} else  if ( fragment instanceof ManageExamsFragment ) {
			this.manageExamsFragment = (ManageExamsFragment) fragment;
			ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.MANAGE_EXAMS);
		}
	}

	private void startCalculateScoreActivity() {
		Intent intent = new Intent(ExamActivity.this, ShowScoreActivity.class);
		startActivity(intent);
	}

	private void startExam() {
		long scoresId;
		long questionId = 1;
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());

		if( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.EXAM_OVERVIEW ) {
			scoresId = examinationDbHelper.createNewScore();
			if( scoresId == -1 ) {
				Toast.makeText(this, this.getString(R.string.failed_to_create_a_new_score_for_the_exam), Toast.LENGTH_LONG).show();
				examinationDbHelper.close();
				return;
			}
		} else {
			scoresId = ExamTrainer.getScoresId();
			questionId = examinationDbHelper.getLastAnsweredQuestionId(scoresId);
			if( questionId == -1 ) {
				Toast.makeText(this, this.getString(R.string.Failed_to_resume_exam), Toast.LENGTH_LONG).show();
				examinationDbHelper.close();
				return;
			} 
		}
		
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useTimelimit = prefs.getBoolean(this.getResources().getString(R.string.pref_key_use_timelimits), false);
		if( useTimelimit ) {
			ExamTrainer.setTimer(examinationDbHelper.getDate(scoresId));
		}
		examinationDbHelper.close();
		
		ExamTrainer.setScoresId(scoresId);
		ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM);
		
		showExamQuestionFragment(questionId, FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	}

	private void doFragmentTransation(AbstractFragment fragment, String mode, boolean addToBackStack, int fragmentTransition) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.exam_fragment_holder, fragment, mode);
		fragmentTransaction.setTransition(fragmentTransition);
		if( addToBackStack) {
			fragmentTransaction.addToBackStack(mode);
		}
		fragmentTransaction.commit();
		this.activeFragment = fragment;
	}

	private void showExamQuestionFragment(long number, int fragmentTransition) {
		ExamTrainer.ExamTrainerMode mode = ExamTrainer.getExamMode();
		this.examQuestionFragment = new ExamQuestionFragment();
		this.examQuestionFragment.setQuestionId(number);
		doFragmentTransation(this.examQuestionFragment, mode.name(), true, fragmentTransition);
	}

	private void showSelectExamFragment(boolean addToBackStack) {
		ExamTrainer.ExamTrainerMode mode = ExamTrainer.ExamTrainerMode.SELECT_EXAM;
		this.examSelectFragment = new ExamSelectFragment();
		doFragmentTransation(this.examSelectFragment, mode.name(), addToBackStack, FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ExamTrainer.setExamMode(mode);
	}

	private void showExamOverviewFragment(boolean addToBackStack) {
		ExamTrainer.ExamTrainerMode mode = ExamTrainer.ExamTrainerMode.EXAM_OVERVIEW;
		this.examOverviewFragment = new ExamOverviewFragment();
		doFragmentTransation(this.examOverviewFragment, mode.name(), addToBackStack, FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ExamTrainer.setExamMode(mode);
	}

	private void showExamReviewFragment(long id, boolean addToBackStack) {
		ExamTrainer.ExamTrainerMode mode = ExamTrainer.ExamTrainerMode.SHOW_EXAM_REVIEW;
		ExamTrainer.setScoresId(id);
		this.examReviewFragment = new ExamReviewFragment();
		doFragmentTransation(this.examReviewFragment, mode.name(), addToBackStack, FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ExamTrainer.setExamMode(mode);
	}

	private void showManageExamsFragment(boolean addToBackStack) {
		ExamTrainer.ExamTrainerMode mode = ExamTrainer.ExamTrainerMode.MANAGE_EXAMS;
		this.manageExamsFragment = new ManageExamsFragment();
		doFragmentTransation(this.manageExamsFragment, mode.name(), addToBackStack, FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ExamTrainer.setExamMode(mode);
	}
}
