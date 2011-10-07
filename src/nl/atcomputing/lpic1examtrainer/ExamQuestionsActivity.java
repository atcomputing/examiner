package nl.atcomputing.lpic1examtrainer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
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
	private ExamTrainerDbAdapter dbHelper;
	private Cursor cursorQuestion;
	private int questionNumber;
	private String questionType;
	private EditText editText;

	private static final int DIALOG_ENDOFEXAM_ID = 1;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

		questionNumber = intent.getIntExtra("question", 1);
		if ( questionNumber < 1 ) {
			finishActivity();
		}
		
		dbHelper = new ExamTrainerDbAdapter(this);
		dbHelper.open();

		cursorQuestion = dbHelper.getQuestion(questionNumber);
		if ( cursorQuestion.getCount() < 1 ) {
			showDialog(DIALOG_ENDOFEXAM_ID);
		}
		else {
			int index = cursorQuestion.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_TYPE);
			questionType = cursorQuestion.getString(index);

			setupLayout();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.stop_exam:
			stopExam();
			return true;
		case R.id.leave_comment:

			return true;
		case R.id.get_hint:

			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case DIALOG_ENDOFEXAM_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("End of Exam.\nAre you sure you want to exit?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					showResults();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
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

	protected void finishActivity() {
		dbHelper.close();
		finish();
	}

	protected void stopExam() {
		dbHelper.close();
		Intent intent = new Intent(ExamQuestionsActivity.this, ExamTrainerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	protected void showResults() {
		dbHelper.close();
		Intent intent = new Intent(ExamQuestionsActivity.this, ExamResultsActivity.class);
		intent.putExtra("question", questionNumber);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	protected LinearLayout createChoices() {
		CheckBox cbox; 
		LinearLayout v_layout = new LinearLayout(this);
		v_layout.setOrientation(LinearLayout.VERTICAL);
		
		Cursor cursor = dbHelper.getChoices(questionNumber);
		if ( cursor != null ) {
			int index = cursor.getColumnIndex(ExamTrainer.Choices.COLUMN_NAME_CHOICE);
			do {
				final String choice = cursor.getString(index);
				cbox = new CheckBox(this);
				cbox.setText(choice);
				
				if ( dbHelper.answerPresent(questionNumber, choice) ) {
					cbox.setChecked(true);
				}
				
				cbox.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (((CheckBox) v).isChecked()) {
							dbHelper.setMultipleChoiceAnswer(questionNumber, choice);
						} else {
							dbHelper.deleteAnswer(questionNumber, choice);
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
			Cursor aCursor = dbHelper.getAnswers(questionNumber);
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
			@Override
			public void onClick(View v) {
				if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
					dbHelper.setOpenAnswer(questionNumber, editText.getText().toString());
				}
				finishActivity();
			}
		});
		Button button_next_question = (Button) findViewById(R.id.button_next);
		button_next_question.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
					dbHelper.setOpenAnswer(questionNumber, editText.getText().toString());
				}
				Intent intent = new Intent(ExamQuestionsActivity.this, ExamQuestionsActivity.class);
				intent.putExtra("question", questionNumber + 1);
				startActivity(intent);
			}
		});
	}
}
