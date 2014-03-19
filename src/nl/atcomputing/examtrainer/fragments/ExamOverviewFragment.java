package nl.atcomputing.examtrainer.fragments;

import java.io.Serializable;
import java.util.HashMap;

import nl.atcomputing.dialogs.RunThreadWithProgressDialog;
import nl.atcomputing.dialogs.TwoButtonDialog;
import nl.atcomputing.dialogs.UsageDialog;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.activities.PreferencesActivity;
import nl.atcomputing.examtrainer.adapters.HistoryAdapter;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.examparser.InstallExamAsyncTask;
import nl.atcomputing.examtrainer.main.Exam;
import nl.atcomputing.examtrainer.main.ExamTrainer;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
/**
 * @author martijn brekhof
 *
 */

public class ExamOverviewFragment extends AbstractFragment implements OnClickListener {
	private final String KEY_ITEMSCHECKED = "itemsChecked";

	private HistoryAdapter adapter;

	private Exam exam;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.examoverviewfragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Activity activity = getActivity();

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

		this.exam = Exam.newInstance(activity, ExamTrainer.getExamId());
		
		setupView();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		boolean useTimeLimit = prefs.getBoolean(this.getResources().getString(R.string.pref_key_use_timelimits), false);

		if( useTimeLimit && ( this.exam.getTimeLimit() > 0 ) ) {
			UsageDialog usageDialog = UsageDialog.newInstance(activity, R.string.Usage_Dialog_Time_limit_is_activated_for_this_exam);
			if( usageDialog != null ) {
				usageDialog.show(getFragmentManager(), "UsageDialog");
			}
		}
		
	}


	@Override
	public void onResume() {
		super.onResume();
		updateView();
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
			final HashMap<Integer, Boolean> itemsChecked = adapter.getItemsChecked();
			if( itemsChecked.size() == 0 ) {
				Toast.makeText(getActivity(), R.string.No_exams_selected_to_delete, Toast.LENGTH_SHORT).show();
			} else {
				TwoButtonDialog confirmDialog = TwoButtonDialog.newInstance(R.string.Delete_selected_scores_);
				confirmDialog.setPositiveButton(R.string.ok, new Runnable() {
					
					@Override
					public void run() {
						deleteSelectedFromDatabase(itemsChecked);
					}
				});
				confirmDialog.show(getFragmentManager(), "ConfirmDeleteDialog");
			}
			
			break;
		case R.id.menu_preferences:
			Intent intent = new Intent(getActivity(), PreferencesActivity.class);
			startActivity(intent);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	public void updateView() {
		this.exam = Exam.newInstance(getActivity(), ExamTrainer.getExamId());
		updateHistoryView();
		updateExamInfoView();
		updateButton();
	}
	
	@Override
	public String getTitle() {
		return ExamTrainer.getExamTitle();
	}
	
	private void deleteSelectedFromDatabase(final HashMap<Integer, Boolean> itemsChecked) {
		RunThreadWithProgressDialog pd = new RunThreadWithProgressDialog(getActivity(), 
				new Thread(new Runnable() {
					public void run() {
						ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(getActivity());
						examinationDbHelper.open(ExamTrainer.getExamDatabaseName()); 
				
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

		Button buttonStartExam = (Button) activity.findViewById(R.id.button_start_exam);
		buttonStartExam.setOnClickListener(this);
		
		Button buttonEnroll = (Button) getActivity().findViewById(R.id.button_enroll);
		buttonEnroll.setOnClickListener(this);
		
		String courseURL = this.exam.getCourseURL();
		if( courseURL == null ) {
			buttonEnroll.setVisibility(View.GONE);
		}
		
		setupHistoryView();
		setupExamInfoView();
	}

	private void setupHistoryView() {
		final Activity activity = getActivity();

		ListView scoresList = (ListView) activity.findViewById(R.id.startexam_history_listview);
		scoresList.setAdapter(this.adapter);

		scoresList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				abstractFragmentListener.onItemClickListener(id);
			}
		});
	}

	private void setupExamInfoView() {
		Activity activity = getActivity();
		
		TextView tv = (TextView) activity.findViewById(R.id.startexam_amount_of_items_value);
		tv.setText(Integer.toString(this.exam.getNumberOfItems()));
		tv = (TextView) activity.findViewById(R.id.startexam_items_needed_to_pass_value);
		tv.setText(Integer.toString(this.exam.getItemsNeededToPass()));

		ExamTrainer.setExamDatabaseName(this.exam.getTitle(), this.exam.getInstallationDate());
		ExamTrainer.setItemsNeededToPass(this.exam.getItemsNeededToPass());
		ExamTrainer.setExamTitle(this.exam.getTitle());
		ExamTrainer.setAmountOfItems(this.exam.getNumberOfItems());
	}
	
	private void updateButton() {
		Exam.State state = this.exam.getInstallationState();

		Button buttonStart = (Button) getActivity().findViewById(R.id.button_start_exam);
		
		if( state == Exam.State.INSTALLED ) {
			buttonStart.setEnabled(true);
		} else {
			buttonStart.setEnabled(false);
		}
	}

	private void updateHistoryView() {
		Activity activity = getActivity();

		LinearLayout linearLayoutHistoryHeader = (LinearLayout) activity.findViewById(R.id.startexam_history_header);
		ListView listViewHistory = (ListView) activity.findViewById(R.id.startexam_history_listview);
		linearLayoutHistoryHeader.setVisibility(View.GONE);
		listViewHistory.setVisibility(View.GONE);

		TextView textViewHistoryMessage = (TextView) activity.findViewById(R.id.startexam_history_textview_message);
		textViewHistoryMessage.setVisibility(View.GONE);
		TextView textViewHistoryMessageValue = (TextView) activity.findViewById(R.id.startexam_history_textview_message_value);
		textViewHistoryMessageValue.setVisibility(View.GONE);

		Exam.State state = this.exam.getInstallationState();

		if( state == Exam.State.INSTALLING ) {
			textViewHistoryMessage.setText(R.string.Installing_exam);
			textViewHistoryMessage.setVisibility(View.VISIBLE);
			InstallExamAsyncTask task = ExamTrainer.getInstallExamAsyncTask(this.exam.getExamID());
			if( task != null ) {
				task.setProgressTextView(textViewHistoryMessageValue);
				textViewHistoryMessageValue.setVisibility(View.VISIBLE);
			}
		} else if( state == Exam.State.INSTALLED ) {
			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(activity);
			examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
			Cursor examinationCursor = examinationDbHelper.getScoresReversed();
			examinationDbHelper.close();

			if( examinationCursor.getCount() > 0 ) {
				textViewHistoryMessage.setVisibility(View.GONE);
				linearLayoutHistoryHeader.setVisibility(View.VISIBLE);
				listViewHistory.setVisibility(View.VISIBLE);
			} else {
				linearLayoutHistoryHeader.setVisibility(View.GONE);
				listViewHistory.setVisibility(View.GONE);
				textViewHistoryMessage.setVisibility(View.VISIBLE);
				textViewHistoryMessage.setText(R.string.no_previous_scores_available);
			}

			adapter.changeCursor(examinationCursor);
			adapter.notifyDataSetChanged();
		} else if( state == Exam.State.NOT_INSTALLED ) {
			textViewHistoryMessage.setText(R.string.Exam_not_installed);
			textViewHistoryMessage.setVisibility(View.VISIBLE);
		}
	}

	private void updateExamInfoView() {
		Activity activity = getActivity();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		boolean useTimeLimit = prefs.getBoolean(this.getResources().getString(R.string.pref_key_use_timelimits), false);

		long examTimeLimit = this.exam.getTimeLimit();

		TextView textViewTimeLimitValue = (TextView) activity.findViewById(R.id.startexam_timelimit_value);
		if ( ( useTimeLimit ) && ( examTimeLimit > 0 ) ) {
			textViewTimeLimitValue.setText(Long.toString(examTimeLimit));
			ExamTrainer.setTimeLimit(examTimeLimit * 60);
		} else {
			textViewTimeLimitValue.setText(getString(R.string.No_time_limit));
			ExamTrainer.setTimeLimit(0);
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_enroll:
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.exam.getCourseURL()));
			startActivity(browserIntent);
			break;
		case R.id.button_start_exam:
			abstractFragmentListener.onButtonClickListener(ExamOverviewFragment.this, this.exam.getExamID());
			break;
		}
	}
}
