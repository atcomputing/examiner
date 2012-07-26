package nl.atcomputing.examtrainer.manage;

import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.adapters.ManageExamsAdapter;
import nl.atcomputing.examtrainer.Exam;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
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

//Example code on how to setup nice selectionbox: 
//http://www.codemobiles.com/forum/viewtopic.php?t=876

public class ManageExamsActivity extends ListActivity {
	private ManageExamsAdapter adap;
	static final int DIALOG_CONFIRMATION_ID = 0;
	private TextView noExamsAvailable;
	private TextView clickOnManageExams;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manageexams);

		loadLocalExams();
		
		noExamsAvailable = (TextView) this.findViewById(R.id.manageexams_no_exams_available);
		clickOnManageExams = (TextView) this.findViewById(R.id.manageexams_click_on_manage_exams);		

		this.adap = (ManageExamsAdapter) getLastNonConfigurationInstance();
		if( this.adap == null ) {
			this.adap = new ManageExamsAdapter(this, R.layout.manageexams_entry, null);
		}
		this.adap.setContext(this);
		setListAdapter(this.adap);
		updateView();
	}

	public Object onRetainNonConfigurationInstance() {
		return this.adap;
	}
	
	protected void onDestroy() {
		super.onDestroy();
		adap.getCursor().close();
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
			updateView();
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
			.setPositiveButton(this.getString(R.string.Yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					deleteAllExams();
				}
			})
			.setNegativeButton(this.getString(R.string.No), new DialogInterface.OnClickListener() {
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

	private void deleteAllExams() {
		int index;
		long examId;
		long examDate;
		String examTitle;

		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getAllExams();
		
		if( cursor.getCount() < 1 )
			return;
		
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
		
		updateView();
	}

	private void updateView() {
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getAllExams();
		
		if ( (cursor == null) || (cursor.getCount() == 0) ) {
			noExamsAvailable.setVisibility(View.VISIBLE);
			clickOnManageExams.setVisibility(View.VISIBLE);

		} else {
			//Remove exams not available text when there are exams installed
			noExamsAvailable.setVisibility(View.GONE);
			clickOnManageExams.setVisibility(View.GONE);
		}
		
		if(cursor != null) {
			cursor.close();
		}

		examTrainerDbHelper.close();
		adap.updateView();
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

	private void loadRemoteExams() {
		//retrieveExam();
	}
}
