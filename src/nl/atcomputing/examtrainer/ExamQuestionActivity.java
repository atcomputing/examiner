package nl.atcomputing.examtrainer;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */
public class ExamQuestionActivity extends Activity {
	//private ExaminationDbAdapter examinationDbHelper;
	//private Cursor cursorQuestion;
	private long questionNumber;
	private String questionType;
	private EditText editText;
	private ArrayList <CheckBox> cboxes;
	private static final int DIALOG_ENDOFEXAM_ID = 1;
	private static final int DIALOG_SHOW_HINT_ID = 2;

	private static final String TAG = "ExamQuestionActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("trace", "ExamQuestionActivity created");
		if ( ExamTrainer.endOfExam() ) {
			finish();
		}
		
		Intent intent = getIntent();

		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		try {
			examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		} catch (SQLiteException e) {
			ExamTrainer.showError(this, 
					this.getResources().getString(R.string.Could_not_open_exam_database_file) + "\n" +
							this.getResources().getString(R.string.Try_reinstalling_the_exam));
			this.finish();
		}

		questionNumber = ExamTrainer.getQuestionNumber(intent);
		if ( ( questionNumber < 1 ) || ( ExamTrainer.getExamDatabaseName() == null ) ) {
			this.finish();
		}
		else {
			Cursor cursor = examinationDbHelper.getQuestion(questionNumber);
			Log.d("ExamQuestionActivity onCreate","Cursor: " + cursor);
			if ( cursor.getCount() < 1 ) {
				ExamTrainer.showError(this, this.getResources().getString(R.string.Exam_is_empty) + "\n" +
						this.getResources().getString(R.string.Try_reinstalling_the_exam));
			} else {

				int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Questions.COLUMN_NAME_TYPE);
				questionType = cursor.getString(index);

				setupLayout();
			}
			cursor.close();
		}
		examinationDbHelper.close();
	}

	public void onBackPressed() {
		if( this.questionNumber == 1 ) {
			showDialog(DIALOG_ENDOFEXAM_ID);
			Intent intent = new Intent(ExamQuestionActivity.this, SelectExamActivity.class);
			startActivity(intent);
			finish();
		}
		else {
			finish();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.question_menu, menu);

		if( ExamTrainer.review() ) {
			MenuItem item = menu.findItem(R.id.menu_get_hint);
			item.setTitle(R.string.Show_Answers);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_get_hint:
			if(ExamTrainer.review()) {
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
		case DIALOG_ENDOFEXAM_ID:
			builder = new AlertDialog.Builder(this);
			int messageId;

			messageId = R.string.end_of_exam_message;		
			builder.setMessage(messageId)
			.setCancelable(false)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					if( !ExamTrainer.review() ) {
						startShowScoreActivity();
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
			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
			examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
			message = examinationDbHelper.getHint(questionNumber);
			examinationDbHelper.close();
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
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getAnswers(questionNumber);
			Log.d("ExamQuestionActivity showAnswers","Cursor: " + cursor);
		examinationDbHelper.close();
		if ( cursor == null ) {
			Log.d(TAG, "Oi, cursor is nulllll");
			return;
		}
		int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Answers.COLUMN_NAME_ANSWER);

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
			Toast.makeText(ExamQuestionActivity.this, 
					getResources().getString(R.string.correct_answers) + ":\n\n" +
							str.toString(), Toast.LENGTH_LONG).show();
		}
		cursor.close();
	}

	private void startShowScoreActivity() {
		Intent intent = new Intent(ExamQuestionActivity.this, ShowScoreActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void stopExam() {
		Intent intent = new Intent(ExamQuestionActivity.this, ExamTrainerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void createChoices(LinearLayout layout) {
		CheckBox cbox; 
		cboxes = new ArrayList<CheckBox>();
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getChoices(questionNumber);
			Log.d("ExamQuestionActivity createChoices","Cursor: " + cursor);
		if ( cursor != null ) {
			int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Choices.COLUMN_NAME_CHOICE);
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
							ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
							examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
							examinationDbHelper.setScoresAnswersMultipleChoice(ExamTrainer.getExamId(), questionNumber, choice);
							examinationDbHelper.close();
							
						} else {
							ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
							examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
							examinationDbHelper.deleteScoresAnswer(ExamTrainer.getExamId(), questionNumber, choice);
							examinationDbHelper.close();
						}

					}
				});

				layout.addView(cbox);
				cboxes.add(cbox);
			} while( cursor.moveToNext() );
			cursor.close();
		} else {
			Log.d("ExamQuestionActivity", "cursor is null");
		}
		examinationDbHelper.close();
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

		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getQuestion(questionNumber);
		Log.d("ExamQuestionActivity setupLayout", "Cursor: "+ cursor);
		examinationDbHelper.close();
		
		if( cursor != null ) {
		index = cursor.getColumnIndex(ExaminationDatabaseHelper.Questions.COLUMN_NAME_EXHIBIT);
		text = cursor.getString(index);
		if( text != null ) {
			TextView exhibit = (TextView) findViewById(R.id.textExhibit);
			exhibit.setText(text);
		} else {
			HorizontalScrollView viewExhibit = (HorizontalScrollView) findViewById(R.id.horizontalScrollViewExhibit);
			layout.removeView(viewExhibit);
		}


		index = cursor.getColumnIndex(ExaminationDatabaseHelper.Questions.COLUMN_NAME_QUESTION);
		text = cursor.getString(index);
		TextView question_textview = (TextView) findViewById(R.id.textQuestion);
		question_textview.setText(text);
		cursor.close();
		}
		
		LinearLayout v_layout = (LinearLayout) findViewById(R.id.answerLayout);

		if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_MULTIPLE_CHOICE)) {
			createChoices(v_layout);
		} else if ( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN)) {
			editText = new EditText(this);
			examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
			examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
			cursor = examinationDbHelper.getScoresAnswers(ExamTrainer.getExamId(), questionNumber);
			Log.d("ExamQuestionActivity setupLayout", "Cursor: "+ cursor);
			examinationDbHelper.close();
			if ( cursor.getCount() > 0 ) {
				index = cursor.getColumnIndex(ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_ANSWER);
				text = cursor.getString(index);
				editText.setText(text.toString());
			}
			cursor.close();
			v_layout.addView(editText);
		}

		Button button_prev_question = (Button) findViewById(R.id.button_prev);
		if( questionNumber == 1 ) {
			button_prev_question.setEnabled(false);
		} else {
			button_prev_question.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
					if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
						ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
						examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
						examinationDbHelper.setScoresAnswersOpen(ExamTrainer.getExamId(), questionNumber, editText.getText().toString());
						examinationDbHelper.close();
					}
					Intent intent = new Intent(ExamQuestionActivity.this, ExamQuestionActivity.class);
					ExamTrainer.setQuestionNumber(intent, questionNumber - 1);
					startActivity(intent);
				}
			});
		}
		Button button_next_question = (Button) findViewById(R.id.button_next);
		examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		int questionsTotal = examinationDbHelper.getQuestionsCount();
		examinationDbHelper.close();
		if( questionNumber >= questionsTotal ) {
			if (ExamTrainer.review()) {
				button_next_question.setText(R.string.End_review);
			} else {
				button_next_question.setText(R.string.End_exam);
			}
		}
		button_next_question.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
				examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
				int questionsTotal = examinationDbHelper.getQuestionsCount();
				
				if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
					examinationDbHelper.setScoresAnswersOpen(ExamTrainer.getExamId(), questionNumber, editText.getText().toString());
				}
				
				examinationDbHelper.close();
				
				if ( questionNumber >= questionsTotal ) {
					if(ExamTrainer.review()) {
						finish();
					}
					else {
						showDialog(DIALOG_ENDOFEXAM_ID);
					}
				}
				else {
					Intent intent = new Intent(ExamQuestionActivity.this, ExamQuestionActivity.class);
					ExamTrainer.setQuestionNumber(intent, questionNumber + 1);
					startActivity(intent);
				}
			}
		});
	}
}
