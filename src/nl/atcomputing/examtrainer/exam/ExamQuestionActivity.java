package nl.atcomputing.examtrainer.exam;

import java.util.ArrayList;

import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.ExamTrainerActivity;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.R.color;
import nl.atcomputing.examtrainer.R.id;
import nl.atcomputing.examtrainer.R.layout;
import nl.atcomputing.examtrainer.R.menu;
import nl.atcomputing.examtrainer.R.string;
import nl.atcomputing.examtrainer.database.ExaminationDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.review.HistoryActivity;

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
	private EditText editText;
	private ArrayList <CheckBox> cboxes;
	private static final int DIALOG_ENDOFEXAM_ID = 1;
	private static final int DIALOG_SHOW_HINT_ID = 2;
	private static final int DIALOG_QUITEXAM_ID = 3;
	
	private static final String TAG = "ExamQuestionActivity";

	private ExamQuestion examQuestion;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("trace", "ExamQuestionActivity created");
		if ( ExamTrainer.endOfExam() ) {
			finish();
		}

		Intent intent = getIntent();

		questionNumber = ExamTrainer.getQuestionNumber(intent);
		if ( ( questionNumber < 1 ) || ( ExamTrainer.getExamDatabaseName() == null ) ) {
			this.finish();
		}

		this.examQuestion = new ExamQuestion(this);
		try {
			this.examQuestion.fillFromDatabase(ExamTrainer.getExamDatabaseName(), questionNumber);
		} catch (SQLiteException e) {
			ExamTrainer.showError(this, this.getResources().getString(R.string.Exam_is_empty) + "\n" +
					this.getResources().getString(R.string.Try_reinstalling_the_exam));
		}
		setupLayout();
	}

	protected void onResume() {
		super.onResume();
		if ( ExamTrainer.endOfExam() ) {
			finish();
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.question_menu, menu);

		if( ExamTrainer.review() ) {
			MenuItem item = menu.findItem(R.id.question_menu_get_hint);
			item.setTitle(R.string.Show_Answers);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.question_menu_get_hint:
			if(ExamTrainer.review()) {
				showAnswers();
			}
			else {
				showDialog(DIALOG_SHOW_HINT_ID);
			}
			return true;
		case R.id.question_menu_quit_exam:
			showDialog(DIALOG_QUITEXAM_ID);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		String message;
		int messageId;
		AlertDialog.Builder builder;
		switch(id) {
		case DIALOG_ENDOFEXAM_ID:
			builder = new AlertDialog.Builder(this);
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
		case DIALOG_QUITEXAM_ID:
			builder = new AlertDialog.Builder(this);
			messageId = R.string.quit_exam_message;		
			builder.setMessage(messageId)
			.setCancelable(false)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					stopExam();
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

		if(examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_MULTIPLE_CHOICE)) {
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

	private void createChoicesLayout(LinearLayout layout) {
		CheckBox cbox; 
		cboxes = new ArrayList<CheckBox>();
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());

		ArrayList<String> choices = this.examQuestion.getChoices();
		Log.d("ExamQuestionActivity", "choices.size()"+choices.size());
		for( String choice : choices ) {
			Log.d("ExamQuestionActivity", "choice: "+ choice);
			cbox = new CheckBox(this);
			cbox.setText(choice);

			if ( examinationDbHelper.scoresAnswerPresent(ExamTrainer.getExamId(), 
					questionNumber, choice) ) {
				cbox.setChecked(true);
			}

			final String answer = choice;
			cbox.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (((CheckBox) v).isChecked()) {
						ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
						examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
						examinationDbHelper.setScoresAnswersMultipleChoice(ExamTrainer.getExamId(), questionNumber, answer);
						examinationDbHelper.close();

					} else {
						ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
						examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
						examinationDbHelper.deleteScoresAnswer(ExamTrainer.getExamId(), questionNumber, answer);
						examinationDbHelper.close();
					}

				}
			});

			layout.addView(cbox);
			cboxes.add(cbox);
		}
		examinationDbHelper.close();
	}


	private void createOpenQuestionLayout(LinearLayout layout) {
		this.editText = new EditText(this);
		
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getScoresAnswers(ExamTrainer.getExamId(), questionNumber);
		if ( cursor.getCount() > 0 ) {
			int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Answers.COLUMN_NAME_ANSWER);
			editText.setText(cursor.getString(index));
		}
		layout.addView(editText);
	}
	
	private void setupLayout() {
		String text;

		setContentView(R.layout.question);

		LinearLayout layout = (LinearLayout) findViewById(R.id.question_layout);

		TextView title = (TextView) findViewById(R.id.textExamTitle);
		title.setText(ExamTrainer.getExamTitle());

		TextView question = (TextView) findViewById(R.id.textQuestionNumber);
		question.setText(this.getString(R.string.Question) + ": " + Long.toString(questionNumber));

		text = this.examQuestion.getExhibit();
		if( text != null ) {
			TextView exhibit = (TextView) findViewById(R.id.textExhibit);
			exhibit.setText(text);
		} else {
			HorizontalScrollView viewExhibit = (HorizontalScrollView) findViewById(R.id.horizontalScrollViewExhibit);
			layout.removeView(viewExhibit);
		}


		text = this.examQuestion.getQuestion();
		TextView question_textview = (TextView) findViewById(R.id.textQuestion);
		question_textview.setText(text);

		LinearLayout v_layout = (LinearLayout) findViewById(R.id.answerLayout);

		if( this.examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_MULTIPLE_CHOICE)) {
			createChoicesLayout(v_layout);
		} else if ( examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_OPEN)) {
			createOpenQuestionLayout(v_layout);
		}

		Button button_prev_question = (Button) findViewById(R.id.button_prev);
		if( questionNumber == 1 ) {
			button_prev_question.setEnabled(false);
		} else {
			button_prev_question.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
					if( examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
						ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
						examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
						examinationDbHelper.setScoresAnswersOpen(ExamTrainer.getExamId(), questionNumber,
								editText.getText().toString());
						examinationDbHelper.close();
					}
					Intent intent = new Intent(ExamQuestionActivity.this, ExamQuestionActivity.class);
					ExamTrainer.setQuestionNumber(intent, questionNumber - 1);
					startActivity(intent);
				}
			});
		}

		Button button_next_question = (Button) findViewById(R.id.button_next);
		if( questionNumber >= ExamTrainer.getAmountOfItems() ) {
			if (ExamTrainer.review()) {
				button_next_question.setText(R.string.End_review);
			} else {
				button_next_question.setText(R.string.End_exam);
			}
		}

		button_next_question.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				if( examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
					ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
					examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
					examinationDbHelper.setScoresAnswersOpen(ExamTrainer.getExamId(), questionNumber,
							editText.getText().toString());
					examinationDbHelper.close();
				}

				if ( questionNumber >= ExamTrainer.getAmountOfItems() ) {
					if(ExamTrainer.review()) {
						Intent intent = new Intent(ExamQuestionActivity.this, HistoryActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
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
