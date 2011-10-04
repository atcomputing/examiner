package nl.atcomputing.lpic1examtrainer;

import java.util.ArrayList;

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

public class ExamQuestionsActivity extends Activity {
	private ArrayList<CheckBox> cboxes;
	private ExamTrainerDbAdapter dbHelper;
	private Cursor cursorQuestion;
	private int questionNumber;
	private String questionType;
	private EditText editText;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		cboxes = new ArrayList<CheckBox>();
		
        questionNumber = intent.getIntExtra("question", 1);
        if ( questionNumber < 1 ) {
        	finishActivity();
        }
        
        super.onCreate(savedInstanceState);
		dbHelper = new ExamTrainerDbAdapter(this);
		dbHelper.open();
		
		cursorQuestion = dbHelper.getQuestion(questionNumber);
		if ( cursorQuestion.getCount() < 1 ) {
			//showDialog(ExamTrainer.DIALOG_ENDOFEXAM_ID);
			showResults();
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
        case ExamTrainer.DIALOG_ENDOFEXAM_ID:
        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        		builder.setMessage("Are you sure you want to exit?")
        		       .setCancelable(false)
        		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        		           public void onClick(DialogInterface dialog, int id) {
        		                showResults();
        		           }
        		       })
        		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
        		           public void onClick(DialogInterface dialog, int id) {
        		                dialog.cancel();
        		           }
        		       });
        		dialog = builder.create();
            break;
        default:
            dialog = null;
        }
        return dialog;
    }
	
	protected void addCheckboxListeners() {
		for(int index = 0; index < cboxes.size(); index++) {
			CheckBox cbox = cboxes.get(index);
			final String answer = cbox.getText().toString();
			cbox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (((CheckBox) v).isChecked()) {
						dbHelper.setMultipleChoiceAnswer(questionNumber, answer);
			        } else {
			        	dbHelper.deleteAnswer(questionNumber, answer);
			        }
					
				}
			});
		}
	}
	
	protected void setCheckboxStatus() {
		Log.d("ExamQuestionsActivity", "setCheckboxStatus: size=" + cboxes.size());
		for(int index = 0; index < cboxes.size(); index++) {
			CheckBox cbox = cboxes.get(index);
			String answer = cbox.getText().toString();
			Cursor aCursor = dbHelper.getAnswer(questionNumber);
			if( aCursor != null ) {
				int cIndex = aCursor.getColumnIndex(ExamTrainer.Answers.COLUMN_NAME_ANSWER);
				do {
					Log.d("ExamQuestionsActivity", ExamTrainer.Answers.COLUMN_NAME_ANSWER +
							" = " + aCursor.getString(cIndex));
					if ( aCursor.getString(cIndex).equals(answer) ) {
						cbox.setChecked(true);
						break;
					}
				} while (aCursor.moveToNext());
			}
		}
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
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	protected LinearLayout createAnswers(String text) {
		CheckBox cbox; 
		LinearLayout v_layout = new LinearLayout(this);
		v_layout.setOrientation(LinearLayout.VERTICAL);
		
		String[] answers = text.split(", ");
		
		for (String answer : answers) {
			cbox = new CheckBox(this);
			cbox.setText(answer);
			v_layout.addView(cbox);
			cboxes.add(cbox);
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
			index = cursorQuestion.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_ANSWERS);
			text = cursorQuestion.getString(index);
			LinearLayout layout = createAnswers(text);
			v_layout.addView(layout);
			addCheckboxListeners();
			setCheckboxStatus();
		} else if ( questionType.equalsIgnoreCase(ExamQuestion.TYPE_OPEN)) {
			editText = new EditText(this);
			Cursor aCursor = dbHelper.getAnswer(questionNumber);
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
