package nl.atcomputing.lpic1examtrainer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExamQuestionsActivity extends Activity {
	
	private ExamTrainerDbAdapter dbHelper;
	private Cursor cursor_question;
	private int question_number;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Intent intent = getIntent();
		
		final String action = intent.getAction();

        // For now we just abort if action is not ACTION_RUN
		// we might add other action types for reviewing afterwards
		// which means filling in the given answers
        if (Intent.ACTION_RUN.equals(action)) {
        	Log.e("ExamTrainer", "Unknown action, exiting");
        	finishActivity();
        }
        
        question_number = intent.getIntExtra("question", 1);
        
        if ( question_number < 1 ) {
        	finishActivity();
        }
        
        super.onCreate(savedInstanceState);
		dbHelper = new ExamTrainerDbAdapter(this);
		dbHelper.open();
		
		cursor_question = dbHelper.getQuestion(question_number);
		if ( cursor_question == null ) {
			//end of the exam
			finishActivity();
		}
		createView();
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
	
	protected void finishActivity() {
		dbHelper.close();
		finish();
	}
	
	protected void stopExam() {
		dbHelper.close();
		Intent intent = new Intent(ExamQuestionsActivity.this, ExamTrainerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}
	
	protected LinearLayout createAnswers(String text) {
		
		LinearLayout v_layout = new LinearLayout(this);
		v_layout.setOrientation(LinearLayout.VERTICAL);
		
		String[] answers = text.split(",");
		
		for (String answer : answers) {
			CheckBox cbox = new CheckBox(this);
			cbox.setText(answer);
			v_layout.addView(cbox);
		}
		
		return v_layout;
	}
	
	protected void createView() {

		int index;
		String text;
		LinearLayout v_layout = new LinearLayout(this);
		v_layout.setOrientation(LinearLayout.VERTICAL);

		index = cursor_question.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_EXHIBIT);
		text = cursor_question.getString(index);
		TextView exhibit = new TextView(this);
		exhibit.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		exhibit.setTextSize(1, 14);
		exhibit.setText(text);
		v_layout.addView(exhibit);

		index = cursor_question.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_QUESTION);
		text = cursor_question.getString(index);
		TextView question_textview = new TextView(this);
		question_textview.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		question_textview.setTextSize(1, 12);
		question_textview.setText(text);
		v_layout.addView(question_textview);

		index = cursor_question.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_ANSWERS);
		text = cursor_question.getString(index);
		LinearLayout layout = createAnswers(text);
		v_layout.addView(layout);

		LinearLayout h_layout = new LinearLayout(this);

		Button button_prev_question = (Button) findViewById(R.id.button_prev_question);
		button_prev_question.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_RUN, ExamTrainer.Questions.CONTENT_URI, ExamQuestionsActivity.this, ExamQuestionsActivity.class);
				intent.putExtra("question", question_number - 1);
				startActivity(intent);
			}
		});
		h_layout.addView(button_prev_question);
		
		Button button_next_question = (Button) findViewById(R.id.button_next_question);
		button_next_question.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_RUN, ExamTrainer.Questions.CONTENT_URI, ExamQuestionsActivity.this, ExamQuestionsActivity.class);
				intent.putExtra("question", question_number + 1);
				startActivity(intent);
			}
		});
		h_layout.addView(button_next_question);
		

		v_layout.addView(h_layout);
		setContentView(v_layout);
	}
}
