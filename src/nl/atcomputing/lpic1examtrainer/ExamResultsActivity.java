package nl.atcomputing.lpic1examtrainer;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * @author martijn brekhof
 *
 */
public class ExamResultsActivity extends Activity {
	private ExamTrainerDbAdapter dbHelper;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);
        
        dbHelper = new ExamTrainerDbAdapter(this);
		dbHelper.open();
		
		calculateResults();
		
		dbHelper.close();
		
		Button quit = (Button) findViewById(R.id.result_button_quit);
        quit.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ExamResultsActivity.this, ExamTrainerActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

    }
    
    private void calculateResults() {
    	Cursor cursor;
    	int index;
    	
    	List<Long> idList = dbHelper.getAllQuestionIDs();
    	for(int i = 0; i < idList.size(); i++) {
    		long questionId = idList.get(i);
    		cursor = dbHelper.getAnswers(idList.get(i));
    		if ( cursor != null ) {
    			index = cursor.getColumnIndex(ExamTrainer.Answers.COLUMN_NAME_ANSWER);
    			do {
    				String answer = cursor.getString(index);
    				if(dbHelper.checkAnswer(answer, questionId)) {
    					Log.d(this.getClass().getName(), answer + " is correct");
    				}
    				else {
    					Log.d(this.getClass().getName(), answer + " is wrong");
    				}
    			} while( cursor.moveToNext() );
    		}	
    	}
    }
    
    
}