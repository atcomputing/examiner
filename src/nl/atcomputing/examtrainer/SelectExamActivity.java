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

public class SelectExamActivity extends Activity {
	private SelectExamAdapter adap;
	private static Cursor cursor;
	private long examsRowId;
	private static final int DIALOG_SHOW_EXAM = 0;
	private TextView clickOnManageExams;
	private TextView noExamsAvailable;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("trace", "SelectExamActivity created");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.selectexam);
		
		ListView selectExam = (ListView) this.findViewById(R.id.select_exam_list);
		noExamsAvailable = (TextView) this.findViewById(R.id.selectexam_no_exams_available);
		clickOnManageExams = (TextView) this.findViewById(R.id.selectexam_click_on_manage_exams);

		adap = new SelectExamAdapter(this, R.layout.selectexam_entry, null);
		selectExam.setAdapter(adap);

		selectExam.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				examsRowId = id;
				showDialog(DIALOG_SHOW_EXAM);
			}
		});
	}

	protected void onResume() {
		super.onResume();
		Log.d("trace", "SelectExamActivity resumed");
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		cursor = examTrainerDbHelper.getInstalledExams();
		examTrainerDbHelper.close();
		if(cursor.getCount() > 0) {
			//Remove exams not available text when there are exams installed
			noExamsAvailable.setVisibility(View.GONE);
			clickOnManageExams.setVisibility(View.GONE);
		} else {
			noExamsAvailable.setVisibility(View.VISIBLE);
			clickOnManageExams.setVisibility(View.VISIBLE);
		}
		adap.changeCursor(cursor);
		adap.notifyDataSetChanged();
	}

	protected void onPause() {
		super.onPause();
		Log.d("trace", "SelectExamActivity paused");
		cursor.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.selectexam_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.selectexam_menu_settings:
			intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			break;
		case R.id.selectexam_menu_manage:
			startManageExams();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
		case DIALOG_SHOW_EXAM:
			ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
			examTrainerDbHelper.open();
			Cursor cursor = examTrainerDbHelper.getExam(examsRowId);
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
			Log.d("SelectExamActivity", "examAmountOfItems " + examAmountOfItems);
			((AlertDialog) dialog).setMessage( dialogMessage );
			cursor.close();
			break;
		default:
			break;
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		switch(id) {
		case DIALOG_SHOW_EXAM: 
			builder = new AlertDialog.Builder(this);
			builder.setCancelable(true)
			.setMessage("")
			.setPositiveButton(this.getString(R.string.Start_exam), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					startExam();
				}
			})
			.setNeutralButton(this.getString(R.string.show_history), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					showHistory();
				}
			})
			.setNegativeButton(this.getString(R.string.close), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
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
