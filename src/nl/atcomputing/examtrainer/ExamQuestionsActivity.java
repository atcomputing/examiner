package nl.atcomputing.examtrainer;

import java.util.ArrayList;
import java.util.List;

import nl.atcomputing.examtrainer.ExamTrainer.ExamTrainerMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */
public class ExamQuestionsActivity extends Activity {
	private ExaminationDbAdapter examinationDbHelper;
	private Cursor cursorQuestion;
	private long questionNumber;
	private String questionType;
	private EditText editText;
	private ArrayList <CheckBox> cboxes;
	private static final int DIALOG_ENDOFEXAM_ID = 1;
	private static final int DIALOG_SHOW_HINT_ID = 2;
	private static final int DIALOG_SHOW_SCORE_ID = 3;
	
	private static final String TAG = "ExamQuestionsActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		
		examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		
		questionNumber = ExamTrainer.getQuestionNumber(intent);
		if ( ( questionNumber < 1 ) || ( ExamTrainer.getExamDatabaseName() == null ) ) {
			this.finish();
		}
		else {
		cursorQuestion = examinationDbHelper.getQuestion(questionNumber);
		
			int index = cursorQuestion.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_TYPE);
			questionType = cursorQuestion.getString(index);

			setupLayout();
			cursorQuestion.close();
		}
	}
	
	protected void onDestroy() {
		super.onDestroy();
		examinationDbHelper.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.question_menu, menu);
		
		if(ExamTrainer.getMode() == ExamTrainerMode.REVIEW) {
			MenuItem item = menu.findItem(R.id.menu_get_hint);
			item.setTitle(R.string.show_answers);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_stop_exam:
			if(ExamTrainer.getMode() == ExamTrainerMode.EXAM) {
				examinationDbHelper.deleteScore(ExamTrainer.getExamId());
			}
			stopExam();
			return true;
		case R.id.menu_leave_comment:

			return true;
		case R.id.menu_get_hint:
			if(ExamTrainer.getMode() == ExamTrainerMode.REVIEW) {
				showAnswers();
			}
			else {
				showDialog(DIALOG_SHOW_HINT_ID);
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		String message;
		AlertDialog.Builder builder;
		switch(id) {
		case DIALOG_SHOW_SCORE_ID:
    		int score = calculateScore();
    		builder = new AlertDialog.Builder(this);
    		
    		message = getString(R.string.Score) + ": " + Integer.toString(score);
    		builder.setMessage(message)
    		.setCancelable(false)
    		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    				stopExam();
    				dialog.dismiss();
    			}
    		});
    		dialog = builder.create();
    		break;
		case DIALOG_ENDOFEXAM_ID:
			builder = new AlertDialog.Builder(this);
			int messageId;
	
			messageId = R.string.end_of_exam_message;		
			builder.setMessage(messageId)
			.setCancelable(false)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					if( ExamTrainer.getMode() == ExamTrainerMode.EXAM ) {
						showDialog(DIALOG_SHOW_SCORE_ID);
					} 
					else {
						stopExam();
					}
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			dialog = builder.create();
			break;
		case DIALOG_SHOW_HINT_ID:
			builder = new AlertDialog.Builder(this);
				message = examinationDbHelper.getHint(questionNumber);
				if( message == null ) {
					message = getString(R.string.hint_not_available);
				}
				builder.setMessage(message)
				.setCancelable(false)
				.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
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

	private void showAnswers() {
		Cursor cursor = examinationDbHelper.getAnswers(questionNumber);
		if ( cursor == null ) {
			Log.d(TAG, "Oi, cursor is nulllll");
			return;
		}
		int index = cursor.getColumnIndex(ExamTrainer.Answers.COLUMN_NAME_ANSWER);

		if(questionType.equalsIgnoreCase(ExamQuestion.TYPE_MULTIPLE_CHOICE)) {
			do {
				String answer = cursor.getString(index);
				for(int i = 0; i < cboxes.size(); i++) {
					if(cboxes.get(i).getText().toString().equals(answer)) {
						cboxes.get(i).setTextColor(getResources().getColor(
								R.color.correct_answer));
					}
				}
			} while(cursor.moveToNext());
		}
		else {
			StringBuffer str = new StringBuffer();
			do {
				String answer = cursor.getString(index);
				str.append(answer + "\n");
			} while(cursor.moveToNext());
			Toast.makeText(ExamQuestionsActivity.this, 
					getResources().getString(R.string.correct_answers) + ":\n\n" +
					str.toString(), Toast.LENGTH_LONG).show();
		}
		cursor.close();
	}

	private void stopExam() {
		Intent intent = new Intent(ExamQuestionsActivity.this, ExamTrainerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	private void createChoices(LinearLayout layout) {
		CheckBox cbox; 
		cboxes = new ArrayList<CheckBox>();
		
		Cursor cursor = examinationDbHelper.getChoices(questionNumber);
		if ( cursor != null ) {
			int index = cursor.getColumnIndex(ExamTrainer.Choices.COLUMN_NAME_CHOICE);
			do {
				final String choice = cursor.getString(index);
				cbox = new CheckBox(this);
				cbox.setText(choice);
				
				if ( examinationDbHelper.scoresAnswerPresent(ExamTrainer.getExamId(), 
						questionNumber, choice) ) {
					cbox.setChecked(true);
				}
				
				cbox.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (((CheckBox) v).isChecked()) {
							examinationDbHelper.setScoresAnswersMultipleChoice(ExamTrainer.getExamId(), questionNumber, choice);
						} else {
							examinationDbHelper.deleteScoresAnswer(ExamTrainer.getExamId(), questionNumber, choice);
						}

					}
				});
				
				layout.addView(cbox);
				cboxes.add(cbox);
			} while( cursor.moveToNext() );
			cursor.close();
		}
	}

	private void setupLayout() {
		int index;
		String text;

		setContentView(R.layout.question);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.question_layout);
		
		TextView title = (TextView) findViewById(R.id.textExamTitle);
		title.setText(ExamTrainer.getExamTitle());
		
		TextView question = (TextView) findViewById(R.id.textQuestionNumber);
		question.setText(this.getString(R.string.Question) + ": " + Long.toString(questionNumber));
		
		index = cursorQuestion.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_EXHIBIT);
		text = cursorQuestion.getString(index);
		TextView exhibit = (TextView) findViewById(R.id.textExhibit);
		if( text != null ) {
			exhibit.setText(text);
		} else {
			layout.removeView(exhibit);
		}
		

		index = cursorQuestion.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_QUESTION);
		text = cursorQuestion.getString(index);
		TextView question_textview = (TextView) findViewById(R.id.textQuestion);
		question_textview.setText(text);
		
		LinearLayout v_layout = (LinearLayout) findViewById(R.id.answerLayout);
		
		if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_MULTIPLE_CHOICE)) {
			createChoices(v_layout);
		} else if ( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN)) {
			editText = new EditText(this);
			Cursor aCursor = examinationDbHelper.getScoresAnswers(ExamTrainer.getExamId(), questionNumber);
			if ( aCursor != null ) {
				index = aCursor.getColumnIndex(ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER);
				text = aCursor.getString(index);
				editText.setText(text.toString());
				aCursor.close();
			}
			v_layout.addView(editText);
		}

		Button button_prev_question = (Button) findViewById(R.id.button_prev);
		button_prev_question.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
					examinationDbHelper.setScoresAnswersOpen(ExamTrainer.getExamId(), questionNumber, editText.getText().toString());
				}
				Intent intent = new Intent(ExamQuestionsActivity.this, ExamQuestionsActivity.class);
				ExamTrainer.setQuestionNumber(intent, questionNumber - 1);
				startActivity(intent);
				finish();
			}
		});
		Button button_next_question = (Button) findViewById(R.id.button_next);
		button_next_question.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
					examinationDbHelper.setScoresAnswersOpen(ExamTrainer.getExamId(), questionNumber, editText.getText().toString());
				}
				
				if ( questionNumber >= examinationDbHelper.getQuestionsCount() ) {
					if(ExamTrainer.getMode() == ExamTrainerMode.REVIEW) {
						finish();
					}
					else {
						showDialog(DIALOG_ENDOFEXAM_ID);
					}
				}
				else {
					Intent intent = new Intent(ExamQuestionsActivity.this, ExamQuestionsActivity.class);
					ExamTrainer.setQuestionNumber(intent, questionNumber + 1);
					startActivity(intent);
					finish();
				}
			}
		});
	}
	
private int calculateScore() {
    	long examId = ExamTrainer.getExamId();
    	
    	List<Long> questionIDsList = examinationDbHelper.getAllQuestionIDs();
    	int amountOfQuestions = questionIDsList.size();
    	int answers_correct = 0;
    	for(int i = 0; i < amountOfQuestions; i++) {
    		long questionId = questionIDsList.get(i);
    		
    		String questionType = examinationDbHelper.getQuestionType(questionId);
    		boolean answerCorrect = false;
    		if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
    			answerCorrect = examinationDbHelper.checkScoresAnswersOpen(questionId, examId);
    		}
    		else {
    			answerCorrect = examinationDbHelper.checkScoresAnswersMultipleChoice(questionId, examId);
    		}
    		
    		if ( answerCorrect ) {
    			answers_correct++;
    			examinationDbHelper.addResultPerQuestion(examId, questionId, true);
    		}
    		else {
    			examinationDbHelper.addResultPerQuestion(examId, questionId, false);
    		}
    		
    	}
    	
    	examinationDbHelper.updateScore(examId, answers_correct);
    	return answers_correct;
    }
}
