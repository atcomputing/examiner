package nl.atcomputing.lpic1examtrainer;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

/**
 * @author martijn brekhof
 *
 */
public class ExamResultsActivity extends Activity {
	public static final int END_EXAM = 1;
	public static final int NONE = 2;
	
	private ExamTrainerDbAdapter dbHelper;
	private int score;
	private static final int DIALOG_SHOW_SCORE = 1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.results);
        
        LayoutInflater li = getLayoutInflater();
		li.inflate(R.layout.results, null);
		
        Intent intent = getIntent();
		int action = intent.getIntExtra("action", 1);
		
        dbHelper = new ExamTrainerDbAdapter(this);
		dbHelper.open();
		
		if( action == END_EXAM ) {
			long examId = createScore();
			score = calculateScore(examId);
			showDialog(DIALOG_SHOW_SCORE);
		}
		
		
		dbHelper.close();
		
		Button quit = (Button) findViewById(R.id.result_button_return_to_main_menu);
        quit.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ExamResultsActivity.this, ExamTrainerActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
        
        Button showScores = (Button) findViewById(R.id.result_button_show_scores);
        showScores.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ExamResultsActivity.this, ExamShowScoresActivity.class);
				startActivity(intent);
			}
		});

    }
    
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case DIALOG_SHOW_SCORE:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("You scored " + score)
			.setCancelable(true)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
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

    private long createScore() {
    	Cursor cursor;
    	int index;
    	Date date = new Date();
    	
    	long examId = dbHelper.addScore(date.toString(), 0);
    	
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
    
    private int calculateScore(long examId) {
    	Cursor cursor;
    	
    	int score_correct = 0;
    	int score_wrong = 0;
    	cursor = dbHelper.getScoresAnswers(examId);
    	if ( cursor == null )
    		return 0;
    	
    		int answerIndex = cursor.getColumnIndex(ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER);
    		int questionIdIndex = cursor.getColumnIndex(ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID);
			do {
				String answer = cursor.getString(answerIndex);
				long questionId = cursor.getLong(questionIdIndex);
				if ( dbHelper.checkAnswer(answer, questionId)) {
					Log.d(this.getClass().getName(), "calculateScore: answer " + answer + " is correct");
					score_correct++;
				}
				else {
					Log.d(this.getClass().getName(), "calculateScore: answer " + answer + " is wrong");
					score_wrong++;
				}
					
			} while( cursor.moveToNext() );
			
			int total = score_correct + score_wrong;
			
	    	double percentage = 0.0;
			
	    	if( total > 0 ) {
				percentage = 100 * (score_correct / (double) total);	
			}
			
			return (int) percentage;
			
    }

}