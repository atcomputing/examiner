package nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author martijn brekhof
 *
 */
public class ExamQuestionsActivity extends Activity {
	private ExaminationDbAdapter examinationDbHelper;
	private Cursor cursorQuestion;
	private int questionNumber;
	private String questionType;
	private EditText editText;
	private boolean review = false;
	private String examTitle = "";
	
	private static final int DIALOG_ENDOFEXAM_ID = 1;
	private static final int DIALOG_SHOW_HINT_ID = 2;
	
	private static final String TAG = "ExamQuestionsActivity";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

		questionNumber = intent.getIntExtra("question", 1);
		if ( questionNumber < 1 ) {
			finishActivity();
		}
		
		review = intent.getBooleanExtra("reviewExam", false);
		examTitle = intent.getStringExtra("examTitle");
		
		examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open();

		cursorQuestion = examinationDbHelper.getQuestion(questionNumber);
		
			int index = cursorQuestion.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_TYPE);
			questionType = cursorQuestion.getString(index);

			setupLayout();
		
	}

	protected void onDestroy() {
		super.onDestroy();
		examinationDbHelper.open();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		menu.getItem(R.id.menu_get_hint).setTitle("BLAH");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_stop_exam:
			stopExam();
			return true;
		case R.id.menu_leave_comment:

			return true;
		case R.id.menu_get_hint:
			showHint();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder;
		switch(id) {
		case DIALOG_ENDOFEXAM_ID:
			builder = new AlertDialog.Builder(this);
			int messageId;
			if(review) {
				messageId = R.string.end_of_review_message;
			} else {
				messageId = R.string.end_of_exam_message;
			}
			builder.setMessage(messageId)
			.setCancelable(false)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					showResults();
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
			if(review) {
				//showAnswers();
				break;
			} 
			else {
				String message = examinationDbHelper.getHint(questionNumber);
				builder.setMessage(message)
				.setCancelable(false)
				.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
				dialog = builder.create();
				break;
			}
		default:
			dialog = null;
		}
		return dialog;
	}

	protected void finishActivity() {
		examinationDbHelper.close();
		finish();
	}

	protected void stopExam() {
		examinationDbHelper.close();
		Intent intent = new Intent(ExamQuestionsActivity.this, ExamTrainerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	protected void showHint() {
		if( review ) {
			showDialog(DIALOG_SHOW_HINT_ID);
		}
	}
	
	protected void showResults() {
		examinationDbHelper.close();
		Intent intent = new Intent(ExamQuestionsActivity.this, ExamResultsActivity.class);
		intent.putExtra("action", ExamResultsActivity.END_EXAM);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	protected LinearLayout createChoices() {
		CheckBox cbox; 
		LinearLayout v_layout = new LinearLayout(this);
		v_layout.setOrientation(LinearLayout.VERTICAL);
		
		Cursor cursor = examinationDbHelper.getChoices(questionNumber);
		if ( cursor != null ) {
			int index = cursor.getColumnIndex(ExamTrainer.Choices.COLUMN_NAME_CHOICE);
			do {
				final String choice = cursor.getString(index);
				cbox = new CheckBox(this);
				cbox.setText(choice);
				
				if ( examinationDbHelper.answerPresent(questionNumber, choice) ) {
					cbox.setChecked(true);
				}
				
				cbox.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (((CheckBox) v).isChecked()) {
							examinationDbHelper.setMultipleChoiceAnswer(questionNumber, choice);
						} else {
							examinationDbHelper.deleteAnswer(questionNumber, choice);
						}

					}
				});
				
				v_layout.addView(cbox);
			} while( cursor.moveToNext() );
		}
		return v_layout;
	}

	protected void setupLayout() {
		int index;
		String text;

		setContentView(R.layout.question);

		TextView title = (TextView) findViewById(R.id.textExamTitle);
		title.setText(examTitle);
		
		index = cursorQuestion.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_EXHIBIT);
		text = cursorQuestion.getString(index);
		TextView exhibit = (TextView) findViewById(R.id.textExhibit);
		exhibit.setText(text);

		index = cursorQuestion.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_QUESTION);
		text = cursorQuestion.getString(index);
		TextView question_textview = (TextView) findViewById(R.id.textQuestion);
		question_textview.setText(text);

		LinearLayout v_layout = (LinearLayout) findViewById(R.id.question_layout);

		if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_MULTIPLE_CHOICE)) {
			LinearLayout layout = createChoices();
			v_layout.addView(layout);
		} else if ( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN)) {
			editText = new EditText(this);
			Cursor aCursor = examinationDbHelper.getAnswers(questionNumber);
			if ( aCursor != null ) {
				index = aCursor.getColumnIndex(ExamTrainer.Answers.COLUMN_NAME_ANSWER);
				text = aCursor.getString(index);
				editText.setText(text.toString());
			}
			v_layout.addView(editText);
		}

		LayoutInflater li = getLayoutInflater();
		li.inflate(R.layout.question_prev_next_buttons, v_layout);

		Button button_prev_question = (Button) findViewById(R.id.button_prev);
		button_prev_question.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
					examinationDbHelper.setOpenAnswer(questionNumber, editText.getText().toString());
				}
				finishActivity();
			}
		});
		Button button_next_question = (Button) findViewById(R.id.button_next);
		button_next_question.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
					examinationDbHelper.setOpenAnswer(questionNumber, editText.getText().toString());
				}
				
				if ( questionNumber >= examinationDbHelper.getAmountOfQuestions() ) {
					showDialog(DIALOG_ENDOFEXAM_ID);
				}
				else {
					Intent intent = new Intent(ExamQuestionsActivity.this, ExamQuestionsActivity.class);
					intent.putExtra("question", questionNumber + 1);
					intent.putExtra("review", review);
					intent.putExtra("examTitle", examTitle);
					startActivity(intent);
				}
			}
		});
	}
}
