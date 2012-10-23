package nl.atcomputing.examtrainer;

import java.io.Serializable;
import java.util.HashMap;

import nl.atcomputing.dialogs.DialogFactory;
import nl.atcomputing.dialogs.RunThreadWithProgressDialog;
import nl.atcomputing.examtrainer.adapters.HistoryAdapter;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.examparser.InstallExamAsyncTask;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
/**
 * @author martijn brekhof
 *
 */

public class ExamOverviewFragment extends SherlockFragment {
	private final String KEY_ITEMSCHECKED = "itemsChecked";

	private HistoryAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.examoverviewfragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		this.adapter = new HistoryAdapter(
				getActivity(), 
				R.layout.history_entry, 
				null);

		if( savedInstanceState != null ) {
			Serializable object = savedInstanceState.getSerializable(KEY_ITEMSCHECKED);
			if( object instanceof HashMap<?, ?> ) {
				@SuppressWarnings("unchecked")
				HashMap<Integer, Boolean> itemsChecked = (HashMap<Integer, Boolean>) object;
				this.adapter.setItemsChecked(itemsChecked);
			}
		}

		setupView();

	}


	@Override
	public void onResume() {
		super.onResume();
		updateView();
	}

	public void onDestroy() {
		super.onDestroy();
		Cursor cursor = adapter.getCursor();
		if ( cursor != null ) {
			cursor.close();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.examoverviewfragment_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_delete:
			deleteSelectedFromDatabase();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void deleteSelectedFromDatabase() {
		RunThreadWithProgressDialog pd = new RunThreadWithProgressDialog(getActivity(), 
				new Thread(new Runnable() {
					public void run() {
						ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(getActivity());
						examinationDbHelper.open(ExamTrainer.getExamDatabaseName()); 

						HashMap<Integer, Boolean> itemsChecked = adapter.getItemsChecked();
						for( Integer key : itemsChecked.keySet() ) {
							if( itemsChecked.get(key) ) {
								examinationDbHelper.deleteScore(key);
							}
						}

						examinationDbHelper.close();
					}
				}),
				new Runnable() {
			public void run() {
				ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(getActivity());
				examinationDbHelper.open(ExamTrainer.getExamDatabaseName()); 

				Cursor cursor = examinationDbHelper.getScoresReversed();
				examinationDbHelper.close();

				adapter.changeCursor(cursor);
				adapter.notifyDataSetChanged();

			}
		}
				);

		pd.run(getString(R.string.deleting_scores_please_wait_));
	}

	private void setupView() {
		final Activity activity = getActivity();

		ListView scoresList = (ListView) activity.findViewById(R.id.startexam_history_listview);
		scoresList.setAdapter(adapter);

		scoresList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(activity, ExamReviewActivity.class);
				ExamTrainer.setScoresId(id);
				startActivity(intent);
			}
		});

		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getExam(ExamTrainer.getExamId());
		examTrainerDbHelper.close();

		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		String examTitle = cursor.getString(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS);
		int examAmountOfItems = cursor.getInt(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
		int examItemsNeededToPass = cursor.getInt(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
		long examInstallationDate = cursor.getLong(index);
		String localDate = "";
		if( examInstallationDate > 0 ) {
			localDate = ExamTrainer.convertEpochToString(examInstallationDate);
		}

		TextView tv = (TextView) activity.findViewById(R.id.startexam_amount_of_items_value);
		tv.setText(Integer.toString(examAmountOfItems));
		tv = (TextView) activity.findViewById(R.id.startexam_items_needed_to_pass_value);
		tv.setText(Integer.toString(examItemsNeededToPass));
		tv = (TextView) activity.findViewById(R.id.startexam_installed_on_value);
		tv.setText(localDate);
		tv = (TextView) activity.findViewById(R.id.startexam_examtitle_value);
		tv.setText(examTitle);

		ExamTrainer.setExamDatabaseName(examTitle, examInstallationDate);
		ExamTrainer.setItemsNeededToPass(examItemsNeededToPass);
		ExamTrainer.setExamTitle(examTitle);
		ExamTrainer.setAmountOfItems(examAmountOfItems);
		cursor.close();
	}

	public void updateView() {
		Activity activity = getActivity();

		TextView tv = (TextView) activity.findViewById(R.id.startexam_history_textview_noscoresavailable);
		tv.setVisibility(View.GONE);
		TextView progress = (TextView) activity.findViewById(R.id.startexam_history_textview_progress);
		progress.setVisibility(View.GONE);
		LinearLayout ll = (LinearLayout) activity.findViewById(R.id.startexam_history_header);
		ListView lv = (ListView) activity.findViewById(R.id.startexam_history_listview);
		ll.setVisibility(View.GONE);
		lv.setVisibility(View.GONE);

		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
		examTrainerDbHelper.open();
		Cursor examTrainerCursor = examTrainerDbHelper.getExam(ExamTrainer.getExamId());
		examTrainerDbHelper.close();

		int index = examTrainerCursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
		String state = examTrainerCursor.getString(index);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		boolean useTimeLimit = prefs.getBoolean(this.getResources().getString(R.string.pref_key_use_timelimits), false);

		index = examTrainerCursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT);
		long examTimeLimit = examTrainerCursor.getLong(index);

		tv = (TextView) activity.findViewById(R.id.startexam_timelimit_value);
		if ( ( useTimeLimit ) && ( examTimeLimit > 0 ) ) {
			tv.setText(Long.toString(examTimeLimit));
			ExamTrainer.setTimeLimit(examTimeLimit * 60);
			Dialog dialog = DialogFactory.createUsageDialog(activity, R.string.Usage_Dialog_Time_limit_is_activated_for_this_exam);
			if( dialog != null ) {
				dialog.show();
			}
		} else {
			tv.setText(getString(R.string.No_time_limit));
			ExamTrainer.setTimeLimit(0);
		}
		
		if( state.contentEquals(ExamTrainerDbAdapter.State.INSTALLING.name()) ) {
			tv.setText(R.string.Installing_exam);
			tv.setVisibility(View.VISIBLE);
			InstallExamAsyncTask task = ExamTrainer.getInstallExamAsyncTask(ExamTrainer.getExamId());
			if( task != null ) {
				task.setProgressTextView(progress);
				progress.setVisibility(View.VISIBLE);
			}
		} else if( state.contentEquals(ExamTrainerDbAdapter.State.INSTALLED.name()) ) {
			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(activity);
			examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
			Cursor examinationCursor = examinationDbHelper.getScoresReversed();
			examinationDbHelper.close();

			if( examinationCursor.getCount() > 0 ) {
				tv.setVisibility(View.GONE);
				ll.setVisibility(View.VISIBLE);
				lv.setVisibility(View.VISIBLE);
			} else {
				ll.setVisibility(View.GONE);
				lv.setVisibility(View.GONE);
				tv.setVisibility(View.VISIBLE);
				tv.setText(R.string.no_previous_scores_available);
			}

			adapter.changeCursor(examinationCursor);
			adapter.notifyDataSetChanged();
		} else if( state.contentEquals(ExamTrainerDbAdapter.State.NOT_INSTALLED.name()) ) {
			tv.setText(R.string.Exam_not_installed);
			tv.setVisibility(View.VISIBLE);
		}

		
	}
}
