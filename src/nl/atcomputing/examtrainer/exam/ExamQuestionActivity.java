package nl.atcomputing.examtrainer.exam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.StartExamActivity;
import nl.atcomputing.examtrainer.database.ExaminationDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.exam.score.ShowScoreActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
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
	private long questionId;
	private EditText editText;
	private TextView timeLimitTextView;
	private ArrayList <View> multipleChoices;
	private static final int DIALOG_ENDOFEXAM_ID = 1;
	private static final int DIALOG_SHOW_HINT_ID = 2;
	private static final int DIALOG_QUITEXAM_ID = 3;
	private static final int DIALOG_TIMELIMITREACHED_ID = 4;
	private static final String TAG = "ExamQuestionActivity";

	private Timer timer;

	private ExamQuestion examQuestion;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("trace", "ExamQuestionActivity created");
		if ( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.ENDOFEXAM ) {
			finish();
		}

		Intent intent = getIntent();

		questionId = ExamTrainer.getQuestionId(intent);
		if ( ( questionId < 1 ) || ( ExamTrainer.getExamDatabaseName() == null ) ) {
			this.finish();
		}

		this.examQuestion = new ExamQuestion(this);
		try {
			this.examQuestion.fillFromDatabase(ExamTrainer.getExamDatabaseName(), questionId);
		} catch (SQLiteException e) {
			ExamTrainer.showError(this, this.getResources().getString(R.string.Exam_is_empty) + "\n" +
					this.getResources().getString(R.string.Try_reinstalling_the_exam));
		}
		setupLayout();
		
		this.timeLimitTextView = (TextView) findViewById(R.id.textExamTime);
	}

	protected void onResume() {
		super.onResume();
		if ( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.ENDOFEXAM ) {
			finish();
		}
		setTitle(ExamTrainer.getExamTitle());
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				Log.d("ExamQuestionActivity", "timer run");
				//update time
				long currentTime = System.currentTimeMillis();
				if( currentTime > ExamTrainer.getTimeEnd() ) {
					//time limit reached 
					Log.d("ExamQuestionActivity", "out of time: currentTime="+currentTime+
							", timeEnd="+ExamTrainer.getTimeEnd());
					showDialog(DIALOG_TIMELIMITREACHED_ID);
					timer.cancel();
					timer.purge();
				} else {
					Date date = new Date(ExamTrainer.getTimeEnd() - currentTime);
					String timeLeft = new SimpleDateFormat("HH:mm:ss").format(date);
					timeLimitTextView.setText(timeLeft);
					Log.d("ExamQuestionActivity", "time left: " + timeLeft);
				}
			}
		}, 0, 1000);
	}
	
	protected void onPause() {
		super.onPause();
		timer.cancel();
	}

	public void onBackPressed() {
		if( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.REVIEW ) {
			super.onBackPressed();
		} else {
			if( this.questionId == 1 ) {
				showDialog(DIALOG_QUITEXAM_ID);
			} else {
				Intent intent = new Intent(ExamQuestionActivity.this, ExamQuestionActivity.class);
				ExamTrainer.setQuestionId(intent, questionId - 1);
				startActivity(intent);
				finish();
			}
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.question_menu, menu);

		if( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.REVIEW ) {
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
			if( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.REVIEW ) {
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
					if( ExamTrainer.getExamMode() != ExamTrainer.ExamTrainerMode.REVIEW ) {
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
		case DIALOG_TIMELIMITREACHED_ID:
			builder = new AlertDialog.Builder(this);
			messageId = R.string.time_s_up_;	
			builder.setMessage(messageId)
			.setCancelable(false)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					startShowScoreActivity();
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
			message = examinationDbHelper.getHint(questionId);
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
		Cursor cursor = examinationDbHelper.getAnswers(questionId);
		examinationDbHelper.close();
		if ( cursor == null ) {
			Log.d(TAG, "Oi, cursor is null");
			return;
		}
		int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Answers.COLUMN_NAME_ANSWER);

		if(examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_MULTIPLE_CHOICE)) {
			do {
				String answer = cursor.getString(index);
				for(int i = 0; i < multipleChoices.size(); i++) {
					View view = this.multipleChoices.get(i);
					TextView tv = (TextView) view.findViewById(R.id.choiceTextView);
					if(tv.getText().toString().equals(answer)) {
						tv.setTextColor(getResources().getColor(
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
		ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.ENDOFEXAM);
		Intent intent = new Intent(ExamQuestionActivity.this, ShowScoreActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	private void stopExam() {
		ExaminationDbAdapter db = new ExaminationDbAdapter(this);
		db.open(ExamTrainer.getExamDatabaseName());
		db.deleteScore(ExamTrainer.getScoresId());
		db.close();
		Intent intent = new Intent(ExamQuestionActivity.this, StartExamActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	private void saveScore() {
		boolean answerCorrect = false;
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		if( examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_OPEN) ) {
			examinationDbHelper.setScoresAnswersOpen(ExamTrainer.getScoresId(), questionId,
					editText.getText().toString());
			answerCorrect = examinationDbHelper.checkScoresAnswersOpen(questionId, ExamTrainer.getScoresId());
		} else {
			answerCorrect = examinationDbHelper.checkScoresAnswersMultipleChoice(questionId, ExamTrainer.getScoresId());
		}
		examinationDbHelper.addResultPerQuestion(ExamTrainer.getScoresId(), questionId, answerCorrect);
		
		examinationDbHelper.close();
	}

	private void createChoicesLayout(LinearLayout layout) {
		CheckBox cbox; 
		this.multipleChoices = new ArrayList<View>();
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());

		ArrayList<String> choices = this.examQuestion.getChoices();
		for( String choice : choices ) {
			View view = LayoutInflater.from(this).inflate(R.layout.choice, null);

			TextView tv = (TextView) view.findViewById(R.id.choiceTextView);
			tv.setText(Html.fromHtml(choice));

			cbox = (CheckBox) view.findViewById(R.id.choiceCheckBox);

			if ( examinationDbHelper.scoresAnswerPresent(ExamTrainer.getScoresId(), 
					questionId, choice) ) {
				cbox.setChecked(true);
			}

			final String answer = choice;
			cbox.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (((CheckBox) v).isChecked()) {
						ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
						examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
						examinationDbHelper.setScoresAnswersMultipleChoice(ExamTrainer.getScoresId(), questionId, answer);
						examinationDbHelper.close();

					} else {
						ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(ExamQuestionActivity.this);
						examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
						examinationDbHelper.deleteScoresAnswer(ExamTrainer.getScoresId(), questionId, answer);
						examinationDbHelper.close();
					}

				}
			});

			layout.addView(view);
			this.multipleChoices.add(view);
		}
		examinationDbHelper.close();
	}


	private void createOpenQuestionLayout(LinearLayout layout) {
		this.editText = new EditText(this);

		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getScoresAnswers(ExamTrainer.getScoresId(), questionId);
		examinationDbHelper.close();
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

		TextView question = (TextView) findViewById(R.id.textQuestionNumber);
		question.setText(Long.toString(questionId));

		text = this.examQuestion.getExhibit();
		if( text != null ) {
			TextView exhibit = (TextView) findViewById(R.id.textExhibit);
			exhibit.setText(Html.fromHtml(text));
		} else {
			HorizontalScrollView viewExhibit = (HorizontalScrollView) findViewById(R.id.horizontalScrollViewExhibit);
			layout.removeView(viewExhibit);
		}


		text = this.examQuestion.getQuestion();
		TextView question_textview = (TextView) findViewById(R.id.textQuestion);
		question_textview.setText(Html.fromHtml(text));

		LinearLayout v_layout = (LinearLayout) findViewById(R.id.answerLayout);

		if( this.examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_MULTIPLE_CHOICE)) {
			createChoicesLayout(v_layout);
		} else if ( examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_OPEN)) {
			createOpenQuestionLayout(v_layout);
		}

		Button button_prev_question = (Button) findViewById(R.id.button_prev);
		if( questionId == 1 ) {
			button_prev_question.setEnabled(false);
		} else {
			button_prev_question.setOnClickListener( new View.OnClickListener() {
				public void onClick(View v) {
					saveScore();
					Intent intent = new Intent(ExamQuestionActivity.this, ExamQuestionActivity.class);
					ExamTrainer.setQuestionId(intent, questionId - 1);
					startActivity(intent);
					finish();
				}
			});
		}

		Button button_next_question = (Button) findViewById(R.id.button_next);
		if( questionId >= ExamTrainer.getAmountOfItems() ) {
			if (ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.REVIEW) {
				button_next_question.setText(R.string.End_review);
			} else {
				button_next_question.setText(R.string.End_exam);
			}
		}

		button_next_question.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				saveScore();
				if ( questionId >= ExamTrainer.getAmountOfItems() ) {
					if(ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.REVIEW) {
						Intent intent = new Intent(ExamQuestionActivity.this, StartExamActivity.class);
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
					ExamTrainer.setQuestionId(intent, questionId + 1);
					startActivity(intent);
					finish();
				}
			}
		});
	}
}
