package nl.atcomputing.examtrainer.fragments;

import java.net.URL;
import java.util.ArrayList;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import nl.atcomputing.dialogs.TwoButtonDialog;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.activities.Exam;
import nl.atcomputing.examtrainer.adapters.ManageExamsAdapter;
import nl.atcomputing.examtrainer.adapters.ManageExamsAdapter.ManageExamsAdapterListener;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.examparser.InstallExamAsyncTask;
import nl.atcomputing.examtrainer.examparser.UninstallExamAsyncTask;
import nl.atcomputing.examtrainer.examparser.XmlPullExamListParser;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

public class ManageExamsFragment extends AbstractFragment implements ManageExamsAdapterListener {
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.manageexamsfragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupListView();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setupListView();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.manageexams_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.manageexams_menu_reload:
			Activity activity = getActivity();
			ListView manageExams = (ListView) activity.findViewById(R.id.manageexams_listview);
			manageExams.setVisibility(View.INVISIBLE); // prevent user from installing exams when reloading
			reloadExams();
			setupListView();
			manageExams.setVisibility(View.VISIBLE);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	
	@Override
	public String getTitle() {
		return "Manage Exams";
	}
	
	private void setupListView() {
		final Activity activity = getActivity();
		ListView manageExams = (ListView) activity.findViewById(R.id.manageexams_listview);
	
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getAllExams();
		examTrainerDbHelper.close();
		
		ManageExamsAdapter adap = new ManageExamsAdapter(activity, R.layout.manageexams_entry, cursor, this);
		manageExams.setAdapter(adap);
	}

	@Override
	public void updateView() {
		setupListView();
	}

	public void onButtonClick(final View v, final long examID) {
		final Activity activity = getActivity();
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
		examTrainerDbHelper.open();
		Exam.State state = examTrainerDbHelper.getInstallationState(examID);
		examTrainerDbHelper.close();
		
		v.setEnabled(false);
		
		if( state == Exam.State.NOT_INSTALLED ) {
			InstallExamAsyncTask task = new InstallExamAsyncTask(activity, (TextView) v, examID);
			task.execute();
		} else  if ( state == Exam.State.INSTALLED ) {
			TwoButtonDialog dialog = TwoButtonDialog.newInstance(R.string.are_you_sure_you_want_to_uninstall_this_exam);
			dialog.setPositiveButton(R.string.yes, new Runnable() {
				
				@Override
				public void run() {
					UninstallExamAsyncTask task = new UninstallExamAsyncTask(activity, examID);
					task.execute();
				}
			});
			dialog.setNegativeButton(R.string.no, new Runnable() {
				
				@Override
				public void run() {
					v.setEnabled(true);
				}
			});
			dialog.show(getFragmentManager(), "ConfirmUninstallation");
		}
	}

	public void onItemClick(View v, long examID) {
		Activity activity = getActivity();

		Exam exam = Exam.newInstance(activity, examID);

		if( exam == null ) {
			return;
		}
		
		String examTitle = exam.getTitle();
		int examAmountOfItems = exam.getNumberOfItems();
		int examItemsNeededToPass = exam.getItemsNeededToPass();
		long timeLimit = exam.getTimeLimit();
		

		StringBuffer strBuf = new StringBuffer();
		strBuf.append(examTitle + "\n");
		strBuf.append(activity.getString(R.string.questions) + 
				": " +  examAmountOfItems + "\n" +
				activity.getString(R.string.correct_answer_required_to_pass) +
				": " +  examItemsNeededToPass + "\n");

		if ( timeLimit == 0 ) {
			strBuf.append(activity.getString(R.string.No_time_limit));
		} else {
			strBuf.append(activity.getString(R.string.Time_limit_in_minutes) + ": " + timeLimit
					+ " " + activity.getString(R.string.minutes));
		}

		Toast.makeText(activity,  strBuf.toString(), Toast.LENGTH_LONG).show();
	}
	
	private void reloadExams() {
		Activity activity = getActivity();
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
		examTrainerDbHelper.open();

		try {
			XmlPullExamListParser xmlPullExamListParser;
			URL url = new URL("file:///list.xml");
			xmlPullExamListParser = new XmlPullExamListParser(activity, url);
			xmlPullExamListParser.parse();
			ArrayList<Exam> exams = xmlPullExamListParser.getExamList();

			for ( Exam exam : exams ) {
				long rowId = examTrainerDbHelper.getRowId(exam);
				if ( rowId == -1 ) {
					exam.addToDatabase(activity);
				} else {
					Exam.State state = examTrainerDbHelper.getInstallationState(rowId);
					if ( state == Exam.State.NOT_INSTALLED ) {
						examTrainerDbHelper.deleteExam(rowId);
						exam.addToDatabase(activity);
					}
				}
			}
		} catch (Exception e) {
			Log.w(this.getClass().getName() , "Updating exams failed: Error " + e.getMessage());
			Toast.makeText(activity, "Error: updating exams failed.", Toast.LENGTH_LONG).show();
		}

		examTrainerDbHelper.close();
	}
}
