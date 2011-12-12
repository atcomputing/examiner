package nl.atcomputing.examtrainer;

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
import android.widget.Button;
import android.widget.ListView;

/**
 * @author martijn brekhof
 *
 */
public class ShowScoresActivity extends Activity {
	private final String TAG = this.getClass().getName();
	private ShowScoresAdapter adapter;
	private ExaminationDbAdapter examinationDbHelper;
	private long examId;
	private static final int DIALOG_SHOW_EXAM = 1;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_scores);

		Button cancelButton = (Button) findViewById(R.id.show_scores_cancel);
		ListView scoresList = (ListView) findViewById(R.id.show_scores_list);

		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getScoresReversed();
		adapter = new ShowScoresAdapter(this, R.layout.show_scores_entry, cursor);
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
		examinationDbHelper.close();
	}

	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
		case DIALOG_SHOW_EXAM:
			Cursor cursor = examinationDbHelper.getScore(examId);
			int index = cursor.getColumnIndex(ExamTrainer.Scores.COLUMN_NAME_DATE);
			String examDate = cursor.getString(index);
			index = cursor.getColumnIndex(ExamTrainer.Scores.COLUMN_NAME_SCORE);
		    int examScore = cursor.getInt(index);
			
			
			String pass = this.getResources().getString(R.string.no);
			if( examScore >= ExamTrainer.getItemsNeededToPass() ) { 
				pass = this.getResources().getString(R.string.yes);
			}
			((AlertDialog) dialog).setMessage("ExamID: "+ examId + "\n" + 
					"Exam date: " + examDate + "\n" +
					"Score: " + examScore + "\n" +
					"Pass: " + pass
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
		case DIALOG_SHOW_EXAM:
			builder = new AlertDialog.Builder(this);
			builder.setCancelable(true)
			.setMessage("")
			.setPositiveButton("Review exam", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent intent = new Intent(ShowScoresActivity.this, ExamReviewActivity.class);
					ExamTrainer.setExamId(examId);
					startActivity(intent);
				}
			})
			.setNeutralButton("Delete exam", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						examinationDbHelper.deleteScore(examId);
						adapter.getCursor().requery();
						adapter.notifyDataSetChanged();
						dialog.dismiss();
					}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
}
