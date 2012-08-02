package nl.atcomputing.examtrainer.manage;

import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.examtrainer.Exam;
import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.adapters.ManageExamsAdapter;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

public class ManageExamsActivity extends ListActivity {
	private ManageExamsAdapter adap;
	static final int DIALOG_CONFIRMATION_ID = 0;
	private TextView noExamsAvailable;
	private TextView clickOnManageExams;
	private Cursor cursor;
	private ReceiveBroadcast receiveBroadcast;
	
	private class ReceiveBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateListView();
		}
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manageexams);

		loadLocalExams();
		
		noExamsAvailable = (TextView) this.findViewById(R.id.manageexams_no_exams_available);
		clickOnManageExams = (TextView) this.findViewById(R.id.manageexams_click_on_manage_exams);		

		this.receiveBroadcast = new ReceiveBroadcast();
	}
	
	public void onResume() {
		super.onResume();
		
		updateListView();
		
		IntentFilter filter = new IntentFilter(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
	    this.registerReceiver(this.receiveBroadcast, filter);
	}
	
	public void onPause() {
		super.onPause();
		
		this.unregisterReceiver(this.receiveBroadcast);
		
		if(this.cursor != null) {
			this.cursor.close();
		}
	}
	
	protected void onDestroy() {
		super.onDestroy();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.manageexam_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.manageexam_menu_delete_all:
			showDialog(DIALOG_CONFIRMATION_ID);
			break;
		case R.id.manageexam_menu_get_new_exams:
			loadLocalExams();
			updateListView();
			break;
		case R.id.manageexam_menu_settings:
			intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		switch(id) {
		case DIALOG_CONFIRMATION_ID:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(this.getString(R.string.Are_you_sure_you_want_to_delete_all_exams))
			.setCancelable(false)
			.setPositiveButton(this.getString(R.string.delete_all_exams), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					deleteAllExams();
				}
			})
			.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	private void updateListView() {
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
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
		
		this.adap = new ManageExamsAdapter(this, R.layout.manageexams_entry, this.cursor);
		setListAdapter(this.adap);
	}
	
	private void deleteAllExams() {
		int index;
		long examId;
		long examDate;
		String examTitle;
		
		Log.d("ManageExamsActivity", "Deleting all exams");
		
		this.adap.cancelRunningInstallations();
		
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getAllExams();
		
		if( cursor.getCount() < 1 ) {
			cursor.close();
			examTrainerDbHelper.close();
			return;
		}
		
		do {
			index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
			examTitle = cursor.getString(index);
			index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
			examDate = cursor.getLong(index);
			index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
			examId = cursor.getLong(index);

			if( examinationDbHelper.delete(examTitle, examDate) )  {
				if( ! examTrainerDbHelper.deleteExam(examId) ) {
					Toast.makeText(this, this.getString(R.string.Failed_to_delete_exam) + 
							examTitle, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(this, this.getString(R.string.Could_not_remove_exam_database_file) + 
						examTitle, Toast.LENGTH_LONG).show();
			}
		} while(cursor.moveToNext());

		cursor.close();
		examTrainerDbHelper.close();
		
		updateListView();
	}

	private void loadLocalExams() {
		int file_index = 0;
		String[] filenames = null;

		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		AssetManager assetManager = getAssets();

		if( assetManager != null ) {
			try {
				XmlPullExamListParser xmlPullExamListParser;
				filenames = assetManager.list("");
				int size = filenames.length;
				for( file_index = 0; file_index < size; file_index++) {
					String filename = filenames[file_index];
					if(filename.matches("list.xml")) {
						URL url = new URL("file:///"+filename);
						xmlPullExamListParser = new XmlPullExamListParser(this, url);
						xmlPullExamListParser.parse();
						ArrayList<Exam> exams = xmlPullExamListParser.getExamList();
						for ( Exam exam : exams ) {
							if ( ! examTrainerDbHelper.checkIfExamAlreadyInDatabase(exam) ) {
								exam.addToDatabase(this);
							}
						}
					}
				}
			} catch (Exception e) {
				Log.d(this.getClass().getName() , "Updating exams failed: Error " + e.getMessage());
				Toast.makeText(this, "Error: updating exam " + filenames[file_index] + " failed.", Toast.LENGTH_LONG).show();
			}
		}
		examTrainerDbHelper.close();
	}
}
