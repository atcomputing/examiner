package nl.atcomputing.examtrainer.fragments;

import nl.atcomputing.dialogs.TwoButtonDialog;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.activities.ExamTrainer;
import nl.atcomputing.examtrainer.adapters.ManageExamsAdapter;
import nl.atcomputing.examtrainer.adapters.ManageExamsAdapter.ManageExamsAdapterListener;
import nl.atcomputing.examtrainer.database.DatabaseManager;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.examparser.InstallExamAsyncTask;
import nl.atcomputing.examtrainer.examparser.UninstallExamAsyncTask;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author martijn brekhof
 *
 */

public class ManageExamsFragment extends AbstractFragment implements ManageExamsAdapterListener {
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.manageexams, container, false);
	}

	public void onResume() {
		super.onResume();
		
		cleanupDatabaseStates();
		setupView();
	}

	@Override
	public String getTitle() {
		return "Manage Exams";
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.manageexam_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.manageexam_menu_get_new_exams:
			updateView();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void cleanupDatabaseStates() {
		Activity activity = getActivity();
		
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
		examTrainerDbHelper.open();

		Cursor cursor = examTrainerDbHelper.getInstallingExams();
		if( cursor.getCount() < 1 ) {
			examTrainerDbHelper.close();
			return;
		}

		DatabaseManager dm = new DatabaseManager(activity);

		//Delete all exams that have state INSTALLING but no install thread associated
		do {
			long examID = cursor.getLong(cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID));

			InstallExamAsyncTask task = ExamTrainer.getInstallExamAsyncTask(examID);
			if( task == null ) { 
				dm.deleteExam(examID);
			}
		} while(cursor.moveToNext());

		examTrainerDbHelper.close();
	}

	private void setupView() {
		Activity activity = getActivity();

		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getAllExams();
		examTrainerDbHelper.close();
		ManageExamsAdapter adap = new ManageExamsAdapter(activity, this, R.layout.manageexams_entry, cursor);
		ListView lv = (ListView) activity.findViewById(R.id.manageexams_listview);
		lv.setAdapter(adap);	
	}

	

	public void onButtonClick(final View v, final long examID) {
		final Activity activity = getActivity();

		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
		examTrainerDbHelper.open();
		ExamTrainerDbAdapter.State state = examTrainerDbHelper.getInstallationState(examID);
		examTrainerDbHelper.close();
		if( state == ExamTrainerDbAdapter.State.INSTALLED ) {
			TwoButtonDialog twoButtonDialog = TwoButtonDialog.newInstance(R.string.are_you_sure_you_want_to_uninstall_this_exam);
			twoButtonDialog.setPositiveButton(R.string.uninstall, new Runnable() {

				public void run() {
					v.setEnabled(false);
					UninstallExamAsyncTask task = new UninstallExamAsyncTask(activity, examID);
					task.execute();
				}
			});
			twoButtonDialog.setNegativeButton(R.string.cancel, new Runnable() {

				public void run() {

				}
			});
			twoButtonDialog.show(getFragmentManager(), "ConfirmationDialog");
		} else {
			v.setEnabled(false);
			if( ExamTrainer.getInstallExamAsyncTask(examID) == null ) {
				InstallExamAsyncTask installExam = new InstallExamAsyncTask(activity, (TextView) v, examID); 
				installExam.execute();
				Log.d("ManageExamsFragment", "onButtonClick: installing exam view="+v.getClass().getSimpleName()+
						" with id="+v+" for examID="+examID+" on task="+installExam);
			}
		}
	}

	@Override
	public void updateView() {
		setupView();
	}
}
