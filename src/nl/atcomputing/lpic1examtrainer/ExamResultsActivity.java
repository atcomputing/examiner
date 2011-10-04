package nl.atcomputing.lpic1examtrainer;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
		
		Button quit = (Button) findViewById(R.id.button_quit);
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
    	List<Integer> idList = dbHelper.getAllQuestionIDs();
    	for(int i = 0; i < idList.size(); i++) {
    		
    		Cursor cursor = dbHelper.getCorrectAnswers(idList.get(i));
    		int index = cursor.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_CORRECT_ANSWERS);
    		String correct_answers = cursor.getString(index);
    		cursor.close();
    		
    		cursor = dbHelper.getAnswer(idList.get(i));
    		index = cursor.getColumnIndex(ExamTrainer.Answers.COLUMN_NAME_ANSWER);
    		String answers = cursor.getString(index);
    		cursor.close();
    		
    		Log.d(this.getClass().getName(), "calculateResults:\n" + 
    				"correct_answers: " + correct_answers + 
    				"answers: "+ answers);
    		
    	}
    }
}