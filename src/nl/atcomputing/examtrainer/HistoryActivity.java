package nl.atcomputing.examtrainer;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * @author martijn brekhof
 *
 * TODO FIX: Second item in list points to first question
 */
public class HistoryActivity extends Activity {
	private HistoryAdapter adapter;
	private long examId;
	private static final int DIALOG_SHOW_EXAM = 1;
	private static final int DIALOG_CONFIRMATION_ID = 2;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);        
		Log.d("trace", "HistoryActivity created");
		
		setContentView(R.layout.history);
		
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(HistoryActivity.this);
        examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
        Cursor cursor = examinationDbHelper.getScoresReversed();
        examinationDbHelper.close();
        adapter = new HistoryAdapter(HistoryActivity.this, R.layout.history_entry, cursor);

        ListView scoresList = (ListView) findViewById(R.id.show_scores_list);
        scoresList.setAdapter(adapter);
        
        scoresList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				examId = id;
				showDialog(DIALOG_SHOW_EXAM);
			}
		});
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.d("trace", "HistoryActivity destroyed");
		Cursor cursor = adapter.getCursor();
		if ( cursor != null ) {
			cursor.close();
		}
	}
	
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
		case DIALOG_SHOW_EXAM:
			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(HistoryActivity.this);
        	examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
			Cursor cursor = examinationDbHelper.getScore(examId);
			examinationDbHelper.close();
			int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Scores.COLUMN_NAME_DATE);
			String examDate = ExamTrainer.convertEpochToString(cursor.getLong(index));
			index = cursor.getColumnIndex(ExaminationDatabaseHelper.Scores.COLUMN_NAME_SCORE);
		    int examScore = cursor.getInt(index);
			cursor.close();	
			
			String pass = this.getResources().getString(R.string.no);
			if( examScore >= ExamTrainer.getItemsNeededToPass() ) { 
				pass = this.getResources().getString(R.string.yes);
			}
			((AlertDialog) dialog).setMessage(this.getString(R.string.ExamID) + ": " + examId + "\n" + 
					this.getString(R.string.Exam_date) + ": "+ examDate + "\n" +
					this.getString(R.string.Score) + ": " + examScore + "\n" +
					this.getString(R.string.Pass) + ": " + pass
			);
			break;
		default:
			break;
		}
		 
	}
	
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		switch(id) {
		case DIALOG_CONFIRMATION_ID:
	    	builder = new AlertDialog.Builder(this);
			builder.setMessage(this.getString(R.string.Are_you_sure_you_want_to_delete_this_score))
			.setCancelable(false)
			.setPositiveButton(this.getString(R.string.Yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(HistoryActivity.this);
			        examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
					examinationDbHelper.deleteScore(examId);
			        Cursor cursor = examinationDbHelper.getScoresReversed();
					examinationDbHelper.close();
					adapter.changeCursor(cursor);
					adapter.notifyDataSetChanged();
					dialog.dismiss();
				}
			})
			.setNegativeButton(this.getString(R.string.No), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			dialog = builder.create();
	        break;
		case DIALOG_SHOW_EXAM:
			builder = new AlertDialog.Builder(this);
			builder.setCancelable(true)
			.setMessage("")
			.setPositiveButton(this.getString(R.string.Review_Exam), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent intent = new Intent(HistoryActivity.this, ExamReviewActivity.class);
					ExamTrainer.setExamId(examId);
					startActivity(intent);
				}
			})
			.setNeutralButton(this.getString(R.string.Delete_score), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						showDialog(DIALOG_CONFIRMATION_ID);
						dialog.dismiss();
					}
			})
			.setNegativeButton(this.getString(R.string.Cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});			;
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}
	
	private void deleteSelected() {
		ArrayList<Integer> examIdsSelected = this.adapter.getExamIdsSelected();
		...
	}
}
