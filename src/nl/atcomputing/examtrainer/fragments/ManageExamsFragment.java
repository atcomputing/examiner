package nl.atcomputing.examtrainer.fragments;

import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.dialogs.TwoButtonDialog;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.activities.Exam;
import nl.atcomputing.examtrainer.activities.ExamTrainer;
import nl.atcomputing.examtrainer.adapters.ManageExamsAdapter;
import nl.atcomputing.examtrainer.adapters.ManageExamsAdapter.ManageExamsAdapterListener;
import nl.atcomputing.examtrainer.database.DatabaseManager;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.examparser.InstallExamAsyncTask;
import nl.atcomputing.examtrainer.examparser.UninstallExamAsyncTask;
import nl.atcomputing.examtrainer.examparser.XmlPullExamListParser;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author martijn brekhof
 *
 */

public class ManageExamsFragment extends AbstractFragment implements ManageExamsAdapterListener {
	private ManageExamsAdapter adap;
	static final int DIALOG_CONFIRMATION_ID = 0;
	private TextView noExamsAvailable;
	private TextView clickOnManageExams;
	private Cursor cursor;
	
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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();
	
		loadLocalExams();

		noExamsAvailable = (TextView) activity.findViewById(R.id.manageexams_no_exams_available);
		clickOnManageExams = (TextView) activity.findViewById(R.id.manageexams_click_on_manage_exams);		

	}

	public void onResume() {
		super.onResume();
		
		cleanupDatabaseStates();
		updateListView();
	}

	public void onPause() {
		super.onPause();

		if(this.cursor != null) {
			this.cursor.close();
		}
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
			loadLocalExams();
			updateListView();
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

	private void updateListView() {
		Activity activity = getActivity();
		
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
		examTrainerDbHelper.open();
		this.cursor = examTrainerDbHelper.getAllExams();

		if ( (cursor == null) || (cursor.getCount() == 0) ) {
			noExamsAvailable.setVisibility(View.VISIBLE);
			clickOnManageExams.setVisibility(View.VISIBLE);

		} else {
			//Remove exams not available text when there are exams installed
			noExamsAvailable.setVisibility(View.GONE);
			clickOnManageExams.setVisibility(View.GONE);
		}

		examTrainerDbHelper.close();

		this.adap = new ManageExamsAdapter(activity, this, R.layout.manageexams_entry, this.cursor);
		ListView lv = (ListView) activity.findViewById(R.id.manageexams_listview);
		Log.d("ManageExamsFragment", "updateListView: adap="+this.adap+", lv="+lv);
		lv.setAdapter(this.adap);
		
	}

	private void loadLocalExams() {
		Activity activity = getActivity();
		
		int file_index = 0;
		String[] filenames = null;

		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
		examTrainerDbHelper.open();
		AssetManager assetManager = activity.getAssets();

		if( assetManager != null ) {
			try {
				XmlPullExamListParser xmlPullExamListParser;
				filenames = assetManager.list("");
				int size = filenames.length;
				for( file_index = 0; file_index < size; file_index++) {
					String filename = filenames[file_index];
					if(filename.matches("list.xml")) {
						URL url = new URL("file:///"+filename);
						xmlPullExamListParser = new XmlPullExamListParser(activity, url);
						xmlPullExamListParser.parse();
						ArrayList<Exam> exams = xmlPullExamListParser.getExamList();
						
						/**
						 * if we are able to get a list of exams
						 * first delete old list of exams from database 
						 */
						if( exams.size() > 0 ) { 
							examTrainerDbHelper.deleteAllExams();
						}
						
						for ( Exam exam : exams ) {
							if ( ! examTrainerDbHelper.checkIfExamAlreadyInDatabase(exam) ) {
								exam.addToDatabase(activity);
							}
						}
					}
				}
			} catch (Exception e) {
				Log.d(this.getClass().getName() , "Updating exams failed: Error " + e.getMessage());
				Toast.makeText(activity, "Error: updating exam " + filenames[file_index] + " failed.", Toast.LENGTH_LONG).show();
			}
		}
		examTrainerDbHelper.close();
	}

	public void onButtonClick(Button button, final long examID) {
		final Activity activity = getActivity();
		
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
		examTrainerDbHelper.open();
		ExamTrainerDbAdapter.State state = examTrainerDbHelper.getInstallationState(examID);
		examTrainerDbHelper.close();
		if( state == ExamTrainerDbAdapter.State.INSTALLED ) {
			TwoButtonDialog twoButtonDialog = TwoButtonDialog.newInstance(R.string.are_you_sure_you_want_to_uninstall_this_exam);
			twoButtonDialog.setPositiveButton(R.string.uninstall, new Runnable() {

				public void run() {
//					holder.installUninstallButton.setEnabled(false);
//					holder.installUninstallButton.setText(R.string.Uninstalling_exam);
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
//			holder.installUninstallButton.setEnabled(false);
//			holder.installUninstallButton.setText(R.string.Installing_exam);
			if( ExamTrainer.getInstallExamAsyncTask(examID) == null ) {
				InstallExamAsyncTask installExam = new InstallExamAsyncTask(activity, (TextView) button, examID); 
				installExam.execute();
			}
		}
		
		//super.onButtonClickListener(this, examID);
	}

	@Override
	public void updateView() {
		updateListView();
	}
}
