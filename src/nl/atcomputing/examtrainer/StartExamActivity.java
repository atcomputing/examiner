package nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.R.id;
import nl.atcomputing.examtrainer.R.layout;
import nl.atcomputing.examtrainer.R.menu;
import nl.atcomputing.examtrainer.R.string;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.exam.ExamQuestionActivity;
import nl.atcomputing.examtrainer.manage.ManageExamsActivity;
import nl.atcomputing.examtrainer.manage.PreferencesActivity;
import nl.atcomputing.examtrainer.review.HistoryActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

public class StartExamActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectexam);
		
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getExam(ExamTrainer.getExamId());
		examTrainerDbHelper.close();
		
		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		String examTitle = cursor.getString(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS);
		int examAmountOfItems = cursor.getInt(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
		int examItemsNeededToPass = cursor.getInt(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT);
		long examTimeLimit = cursor.getLong(index);

		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
		long examInstallationDate = cursor.getLong(index);
		String localDate = ExamTrainer.convertEpochToString(examInstallationDate);

		StringBuffer dialogMessage = new StringBuffer();
		dialogMessage.append(examTitle + "\n\n" +
				this.getString(R.string.installed_on) + 
				" " + localDate + "\n" +
				this.getString(R.string.questions) + 
				": " +  examAmountOfItems + "\n" +
				this.getString(R.string.correct_answer_required_to_pass) +
				": " +  examItemsNeededToPass + "\n");

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useTimeLimit = prefs.getBoolean(this.getResources().getString(R.string.pref_key_use_timelimits), false);

		if ( useTimeLimit ) {
			dialogMessage.append(this.getString(R.string.Time_limit) 
					+ ": " + examTimeLimit + " " + this.getString(R.string.minutes) + "\n");
		}

		ExamTrainer.setExamDatabaseName(examTitle, examInstallationDate);
		ExamTrainer.setItemsNeededToPass(examItemsNeededToPass);
		ExamTrainer.setExamTitle(examTitle);
		ExamTrainer.setTimeLimit(examTimeLimit);
		ExamTrainer.setAmountOfItems(examAmountOfItems);
		cursor.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.startexam_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.startexam_menu_preferences:
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void showHistory() {
		Intent intent = new Intent(this, HistoryActivity.class);
		startActivity(intent);
	}

	private void startManageExams() {
		Intent intent = new Intent(this, ManageExamsActivity.class);
		startActivity(intent);
	}

	private void startExam() {
		Intent intent = new Intent(this, ExamQuestionActivity.class);
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		long examId = examinationDbHelper.createNewScore();
		examinationDbHelper.close();
		if( examId == -1 ) {
			Toast.makeText(this, this.getString(R.string.failed_to_create_a_new_score_for_the_exam), Toast.LENGTH_LONG);
		} else {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			boolean useTimelimit = prefs.getBoolean(this.getResources().getString(R.string.pref_key_use_timelimits), false);
			ExamTrainer.setTimer();
			if( useTimelimit ) {
				//startTimer();
			}
			ExamTrainer.setExamId(examId);
			ExamTrainer.setQuestionNumber(intent, 1);
			ExamTrainer.setStartExam();
			startActivity(intent);
		}
	}
}
