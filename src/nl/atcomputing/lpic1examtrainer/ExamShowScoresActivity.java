package nl.atcomputing.lpic1examtrainer;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author martijn brekhof
 *
 */
public class ExamShowScoresActivity extends Activity {
	private Button cancelButton;
	private ListView scoresList;
	private SimpleCursorAdapter adapter;
	public static final String TAG = "ExamShowScoresActivity";
	private ExamTrainerDbAdapter dbHelper;
	
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "Activity started");
		super.onCreate(savedInstanceState);
        setContentView(R.layout.show_scores);
		
        cancelButton = (Button) findViewById(R.id.show_scores_cancel);
        scoresList = (ListView) findViewById(R.id.show_scores_list);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        populateScoresList();
        
        setupListener();
        
    }
	
	private void setupListener() {
		scoresList.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view,
		            int position, long id) {
		          // When clicked, show a toast with the TextView text
		    	  Cursor cursor = (Cursor) adapter.getCursor();
		    	  int index = cursor.getColumnIndex(ExamTrainer.Scores._ID);
		    	  int examId = cursor.getInt(index);
		    	  Log.d(TAG, "ExamID " + examId);
//		    	  Log.d(TAG, date);
//		          Toast.makeText(getApplicationContext(), date,
//		              Toast.LENGTH_SHORT).show();
		        }
		      });
	}
	
	private void populateScoresList() {
		dbHelper = new ExamTrainerDbAdapter(this);
		dbHelper.open();
		Cursor cursor = dbHelper.getScores();
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
        dbHelper.close();
	}
}