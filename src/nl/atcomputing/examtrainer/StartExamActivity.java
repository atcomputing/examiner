package nl.atcomputing.examtrainer;

import nl.atcomputing.dialogs.DialogFactory;
import nl.atcomputing.dialogs.RunThreadWithProgressDialog;
import nl.atcomputing.examtrainer.adapters.HistoryAdapter;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.manage.InstallExamAsyncTask;
import nl.atcomputing.examtrainer.manage.PreferencesActivity;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

public class StartExamActivity extends Activity {
	private HistoryAdapter adapter;
	private Button buttonDeleteSelected;
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
		setContentView(R.layout.startexam);

		this.receiveBroadcast = new ReceiveBroadcast();

		this.buttonStartExam = (Button) findViewById(R.id.startexam_button_start_exam);
		this.buttonStartExam.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				startExam();
			}
		});

		this.buttonDeleteSelected = (Button) findViewById(R.id.startexam_history_button_delete_scores);

		this.adapter = new HistoryAdapter(
				StartExamActivity.this, 
				R.layout.history_entry, 
				null, 
				buttonDeleteSelected);

		final SparseBooleanArray itemsChecked = (SparseBooleanArray) getLastNonConfigurationInstance();
		if (itemsChecked != null) {
			this.adapter.itemChecked = itemsChecked;
		}

		this.buttonDeleteSelected.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				deleteSelectedFromDatabase();
			}
		});

		ListView scoresList = (ListView) findViewById(R.id.startexam_history_listview);
		scoresList.setAdapter(adapter);

		scoresList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(StartExamActivity.this, ExamReviewActivity.class);
				ExamTrainer.setScoresId(id);
				startActivity(intent);
			}
		});
	}

	public Object onRetainNonConfigurationInstance() {
		return this.adapter.itemChecked;
	}

	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
		this.registerReceiver(this.receiveBroadcast, filter);
		setupView();
	}

	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(this.receiveBroadcast);
	}
	protected void onDestroy() {
		super.onDestroy();
		Cursor cursor = adapter.getCursor();
		if ( cursor != null ) {
			cursor.close();
		}
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


	private void deleteSelectedFromDatabase() {
		RunThreadWithProgressDialog pd = new RunThreadWithProgressDialog(this, 
				new Thread(new Runnable() {
					public void run() {
						ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(StartExamActivity.this);
						examinationDbHelper.open(ExamTrainer.getExamDatabaseName()); 

						int size = adapter.itemChecked.size();
						for( int i = 0; i < size; i++ ) {
							int key = adapter.itemChecked.keyAt(i);
							if( adapter.itemChecked.get(key) ) {
								examinationDbHelper.deleteScore(key);
							}
						}

						examinationDbHelper.close();
					}
				}),
				new Runnable() {
			public void run() {
				ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(StartExamActivity.this);
				examinationDbHelper.open(ExamTrainer.getExamDatabaseName()); 

				Cursor cursor = examinationDbHelper.getScoresReversed();
				examinationDbHelper.close();

				adapter.itemChecked.clear();
				adapter.changeCursor(cursor);
				adapter.notifyDataSetChanged();

			}
		}
				);

		pd.run(getString(R.string.deleting_scores_please_wait_));

		this.buttonDeleteSelected.setVisibility(View.GONE);
	}

	private void setupView() {
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
		String localDate = "";
		if( examInstallationDate > 0 ) {
			localDate = ExamTrainer.convertEpochToString(examInstallationDate);
		}
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
		String state = cursor.getString(index);

		TextView tv = (TextView) findViewById(R.id.startexam_amount_of_items_value);
		tv.setText(Integer.toString(examAmountOfItems));
		tv = (TextView) findViewById(R.id.startexam_items_needed_to_pass_value);
		tv.setText(Integer.toString(examItemsNeededToPass));
		tv = (TextView) findViewById(R.id.startexam_installed_on_value);
		tv.setText(localDate);
		tv = (TextView) findViewById(R.id.startexam_examtitle_value);
		tv.setText(examTitle);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useTimeLimit = prefs.getBoolean(this.getResources().getString(R.string.pref_key_use_timelimits), false);

		tv = (TextView) findViewById(R.id.startexam_timelimit_value);
		if ( ( useTimeLimit ) && ( examTimeLimit > 0 ) ) {
			tv.setText(Long.toString(examTimeLimit));
			ExamTrainer.setTimeLimit(examTimeLimit * 60);
			Dialog dialog = DialogFactory.createUsageDialog(this, R.string.Usage_Dialog_Time_limit_is_activated_for_this_exam);
			if( dialog != null ) {
				dialog.show();
			}
		} else {
			tv.setText(getString(R.string.No_time_limit));
			ExamTrainer.setTimeLimit(0);
		}


		ExamTrainer.setExamDatabaseName(examTitle, examInstallationDate);
		ExamTrainer.setItemsNeededToPass(examItemsNeededToPass);
		ExamTrainer.setExamTitle(examTitle);
		ExamTrainer.setAmountOfItems(examAmountOfItems);
		cursor.close();

		tv = (TextView) findViewById(R.id.startexam_history_textview_noscoresavailable);
		tv.setVisibility(View.GONE);
		TextView progress = (TextView) findViewById(R.id.startexam_history_textview_progress);
		progress.setVisibility(View.GONE);
		LinearLayout ll = (LinearLayout) findViewById(R.id.startexam_history_header);
		ListView lv = (ListView) findViewById(R.id.startexam_history_listview);
		ll.setVisibility(View.GONE);
		lv.setVisibility(View.GONE);

		if( state.contentEquals(ExamTrainerDbAdapter.State.INSTALLING.name()) ) {
			tv.setText(R.string.Installing_exam);
			tv.setVisibility(View.VISIBLE);
			InstallExamAsyncTask task = ExamTrainer.getInstallExamAsyncTask(ExamTrainer.getExamId());
			if( task != null ) {
				task.setProgressTextView(progress);
				progress.setVisibility(View.VISIBLE);
			}
			this.buttonStartExam.setEnabled(false);
		} else if( state.contentEquals(ExamTrainerDbAdapter.State.INSTALLED.name()) ) {
			this.buttonStartExam.setText(R.string.start_a_new_exam);
			this.buttonStartExam.setEnabled(true);
			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
			examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
			cursor = examinationDbHelper.getScoresReversed();
			examinationDbHelper.close();

			if( cursor.getCount() > 0 ) {
				tv.setVisibility(View.GONE);
				ll.setVisibility(View.VISIBLE);
				lv.setVisibility(View.VISIBLE);
			} else {
				ll.setVisibility(View.GONE);
				lv.setVisibility(View.GONE);
				tv.setVisibility(View.VISIBLE);
				tv.setText(R.string.no_previous_scores_available);
			}

			adapter.changeCursor(cursor);
			adapter.notifyDataSetChanged();
		} else if( state.contentEquals(ExamTrainerDbAdapter.State.NOT_INSTALLED.name()) ) {
			tv.setText(R.string.Exam_not_installed);
			tv.setVisibility(View.VISIBLE);
			this.buttonStartExam.setEnabled(false);
		}

	}
}
