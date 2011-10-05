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
	private int questionNumber;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);
        
        Intent intent = getIntent();
		questionNumber = intent.getIntExtra("question", 1);
		
        dbHelper = new ExamTrainerDbAdapter(this);
		dbHelper.open();
		
		if( questionNumber > 0 ) {
			int examId = createScore();
			int score = calculateScore(examId);
			showScore(score);
		}
		
		
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
    
    private int createScore() {
    	Cursor cursor;
    	int index;
    	
    	int examId = dbHelper.addScore(date, null);
    	
    	List<Long> idList = dbHelper.getAllQuestionIDs();
    	for(int i = 0; i < idList.size(); i++) {
    		long questionId = idList.get(i);
    		cursor = dbHelper.getAnswers(idList.get(i));
    		if ( cursor != null ) {
    			index = cursor.getColumnIndex(ExamTrainer.Answers.COLUMN_NAME_ANSWER);
    			do {
    				String answer = cursor.getString(index);
    				dbHelper.addScoresAnswers(examId, questionId, answer);
    			} while( cursor.moveToNext() );
    		}	
    	}
    	return examId;
    }
    
    private int calculateScore(int examId) {
    	Cursor cursor;
    	int index;
    	int score;
    	
    	score = 0;
    	cursor = dbHelper.getScoresAnswers(examId);
    	if ( cursor == null )
    		return 0;
    	
    		int answerIndex = cursor.getColumnIndex(ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER);
    		int questionIdIndex = cursor.getColumnIndex(ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID);
			do {
				String answer = cursor.getString(index);
				long questionId = cursor.getLong(questionIdIndex);
				if ( dbHelper.checkAnswer(answer, questionId)) {
					score++;
				}
			} while( cursor.moveToNext() );
			
			return score;
			
    }
    
    private void showScore(int score) {
    	
    }
}