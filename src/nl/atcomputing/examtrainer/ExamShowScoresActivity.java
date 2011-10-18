package nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.R;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author martijn brekhof
 *
 */
public class ExamShowScoresActivity extends Activity {
	private Button cancelButton;
	private ListView scoresList;
	private SimpleCursorAdapter adapter;
	private Cursor cursor;
	private long examId;
	private String examDate;
	public static final String TAG = "ExamShowScoresActivity";
	private ExaminationDbAdapter examinationDbHelper;

	private static final int DIALOG_SHOW_EXAM = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_scores);

		cancelButton = (Button) findViewById(R.id.show_scores_cancel);
		scoresList = (ListView) findViewById(R.id.show_scores_list);

		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		//Initialize
		examId = -1;
		
		examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open();
		cursor = examinationDbHelper.getScores();
		
		populateScoresList();

		setupListener();

	}

	protected void onDestroy() {
		super.onDestroy();
		examinationDbHelper.close();
	}
	
	private void setupListener() {
		scoresList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// When clicked, show a toast with the TextView text
				Cursor cursor = (Cursor) adapter.getCursor();
				int index = cursor.getColumnIndex(ExamTrainer.Scores._ID);
				examId = cursor.getLong(index);
				index = cursor.getColumnIndex(ExamTrainer.Scores.COLUMN_NAME_DATE);
				examDate = cursor.getString(index);
				Log.d(TAG, "ExamID: " + examId + " ExamDate: " + examDate);
				showDialog(DIALOG_SHOW_EXAM);
			}
		});
	}

	private void populateScoresList() {
		String[] fields = new String[] {
				ExamTrainer.Scores._ID,
				ExamTrainer.Scores.COLUMN_NAME_DATE,
				ExamTrainer.Scores.COLUMN_NAME_SCORE
		};
		adapter = new SimpleCursorAdapter(this, R.layout.show_scores_entry, cursor,
				fields, new int[] {
				R.id.scoreEntryExamID,
				R.id.scoreEntryDate,
				R.id.scoreEntryScore});
		scoresList.setAdapter(adapter);
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		switch(id) {
		case DIALOG_SHOW_EXAM:
			builder = new AlertDialog.Builder(this);
			builder.setCancelable(true)
			.setMessage(examId + " " + examDate )
			.setPositiveButton("Review exam", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent intent = new Intent(ExamShowScoresActivity.this, ExamReviewActivity.class);
					intent.putExtra("examId", examId);
					startActivity(intent);
				}
			})
			.setNegativeButton("Delete exam", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					examinationDbHelper.deleteScore(examId);
					cursor.requery();
					adapter.notifyDataSetChanged();
					dialog.dismiss();
				}
			});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}
}