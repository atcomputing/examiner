package nl.atcomputing.examtrainer.review;

import java.util.ArrayList;
import java.util.Iterator;

import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

/**
 * @author martijn brekhof
 *
 * TODO FIX: Second item in list points to first question
 */
public class HistoryActivity extends Activity {
	private HistoryAdapter adapter;
	private ArrayList<Integer> examIdsSelected = new ArrayList<Integer>();
	private Button deleteSelectedButton;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);        
		Log.d("trace", "HistoryActivity created");
		
		setContentView(R.layout.history);
		
		this.deleteSelectedButton = (Button) findViewById(R.id.history_button_delete_scores);
		this.deleteSelectedButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				deleteSelectedFromDatabase();
			}
		});
        
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(HistoryActivity.this);
        examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
        Cursor cursor = examinationDbHelper.getScoresReversed();
        examinationDbHelper.close();
        
        adapter = new HistoryAdapter(
        		HistoryActivity.this, 
        		R.layout.history_entry, 
        		cursor, 
        		deleteSelectedButton);

        ListView scoresList = (ListView) findViewById(R.id.history_listview);
        scoresList.setAdapter(adapter);
        
        scoresList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(HistoryActivity.this, ExamReviewActivity.class);
				ExamTrainer.setExamId(id);
				startActivity(intent);
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
	
	protected void addItemToDeletionList(int id) {
		this.examIdsSelected.add(new Integer(id));
		this.deleteSelectedButton.setEnabled(true);
	}
	
	protected void removeItemFromDeletionList(int id) {
		Iterator<Integer> itr = this.examIdsSelected.iterator();
		while( itr.hasNext() ) {
			Integer examId = itr.next();
			if( examId.intValue() == id ) {
				this.examIdsSelected.remove(examId);
				break;
			}
		}
		
		if( this.examIdsSelected.isEmpty() ) {
			this.deleteSelectedButton.setEnabled(false);
		}
		
	}
	
	private void deleteSelectedFromDatabase() {
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(HistoryActivity.this);
        examinationDbHelper.open(ExamTrainer.getExamDatabaseName());    
		for( Integer examId : this.examIdsSelected ) {
			examinationDbHelper.deleteScore(examId.intValue());
		}
		Cursor cursor = examinationDbHelper.getScoresReversed();
		examinationDbHelper.close();
		adapter.changeCursor(cursor);
		adapter.notifyDataSetChanged();
		
		this.examIdsSelected.clear();
		this.deleteSelectedButton.setEnabled(false);
	}
}
