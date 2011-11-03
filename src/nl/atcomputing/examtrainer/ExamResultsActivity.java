package nl.atcomputing.examtrainer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
	
	private static final int DIALOG_SHOW_SCORE_ID = 1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        Intent intent = this.getIntent();
        boolean calculateScore = intent.getBooleanExtra("calculateScore", false);
        if(calculateScore) {
        	showDialog(DIALOG_SHOW_SCORE_ID);
        }
        
        setContentView(R.layout.results);
        
        LayoutInflater li = getLayoutInflater();
		li.inflate(R.layout.results, null);
		
		Button quit = (Button) findViewById(R.id.result_button_return_to_main_menu);
        quit.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamResultsActivity.this, ExamTrainerActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
        
        Button showScores = (Button) findViewById(R.id.result_button_show_scores);
        showScores.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamResultsActivity.this, ShowScoresActivity.class);
				startActivity(intent);
			}
		});
    }
    
    protected void onDestroy() {
    	super.onDestroy();
    }  

    protected Dialog onCreateDialog(int id) {
    	Dialog dialog = null;
    	AlertDialog.Builder builder;
    	switch(id) {
    	case DIALOG_SHOW_SCORE_ID:
    		int score = calculateScore();
    		builder = new AlertDialog.Builder(this);
    		String message = getString(R.string.you_scored) + " " + Integer.toString(score);
    		builder.setMessage(message)
    		.setCancelable(false)
    		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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

    private int calculateScore() {
    	
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    	Date date = new Date();
    	
    	ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.examDatabaseName);
    	long examId = examinationDbHelper.addScore(dateFormat.format(date), 0);
    	
    	//TODO: add all questions to the scoresanswer table first. Because the check...Answer
    	//methods will check the answers from the scoresanswer table.
    	List<Long> questionIDsList = examinationDbHelper.getAllQuestionIDs();
    	int amountOfQuestions = questionIDsList.size();
    	int answers_correct = 0;
    	for(int i = 0; i < amountOfQuestions; i++) {
    		long questionId = questionIDsList.get(i);
    		String questionType = examinationDbHelper.getQuestionType(questionId);
    		boolean answerCorrect = false;
    		if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
    			answerCorrect = examinationDbHelper.checkOpenAnswer(questionId, examId);
    		}
    		else {
    			answerCorrect = examinationDbHelper.checkMultipleChoiceAnswer(questionId, examId);
    		}
    		
    		if ( answerCorrect ) {
    			answers_correct++;
    		}
    		
//    		Cursor answerCursor = examinationDbHelper.getAnswers(questionId);
//    		if ( answerCursor != null ) {
//    			int index = answerCursor.getColumnIndex(ExamTrainer.Answers.COLUMN_NAME_ANSWER);
//    			do {
//    				String answer = answerCursor.getString(index);
//    				examinationDbHelper.addScoresAnswers(examId, questionId, answer);
//    				if ( examinationDbHelper.checkAnswer(answer, questionId)) {
//    					Log.d(this.getClass().getName(), "calculateScore: answer " + answer + " is correct");
//    					answers_correct++;
//    				}
//    				else {
//    					Log.d(this.getClass().getName(), "calculateScore: answer " + answer + " is wrong");
//    				}
//    			} while( answerCursor.moveToNext() );
//    			answerCursor.close();
//    		}
    	}
    	
    	examinationDbHelper.updateScore(examId, answers_correct);
    	
    	return answers_correct;
    }
}