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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
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
	private static final String HANDLER_MESSAGE_KEY = "handler_update_timer"; 
	private static final int HANDLER_MESSAGE_VALUE_UPDATE_TIMER = 0;
	private static final int HANDLER_MESSAGE_VALUE_TIMELIMITREACHED = 1;
	private static final String HANDLER_KEY_CURRENT_TIME = "handler_current_time"; 

	private static final String TAG = "ExamQuestionActivity";

	private Timer timer;
	private MyHandler myHandler;

	private ExamQuestion examQuestion;

	private class MyHandler extends Handler {

		public void handleMessage(Message msg) {
			int key = msg.getData().getInt(HANDLER_MESSAGE_KEY);
			if( key == HANDLER_MESSAGE_VALUE_UPDATE_TIMER ) {
				long currentTime = msg.getData().getLong(HANDLER_KEY_CURRENT_TIME);
				Date date = new Date(ExamTrainer.getTimeEnd() - currentTime);
				String timeLeft = new SimpleDateFormat("HH:mm:ss").format(date);
				Log.d("ExamQuestionActivity", "timeLeft: " + timeLeft);
				timeLimitTextView.setText(timeLeft);
			} else if ( key == HANDLER_MESSAGE_VALUE_TIMELIMITREACHED ) {
				showDialog(DIALOG_TIMELIMITREACHED_ID);
			}

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.myHandler = new MyHandler();

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

		if ( ExamTrainer.getTimeLimit() > 0 ) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					//update time
					Message m = Message.obtain();
					long currentTime = System.currentTimeMillis();
					if( currentTime > ExamTrainer.getTimeEnd() ) {
						//time limit reached 
						Bundle b = new Bundle();
						b.putInt(HANDLER_MESSAGE_KEY, HANDLER_MESSAGE_VALUE_TIMELIMITREACHED);
						m.setData(b);
						myHandler.sendMessage(m);
						timer.cancel();
						timer.purge();
					} else {
						Bundle b = new Bundle();
						b.putInt(HANDLER_MESSAGE_KEY, HANDLER_MESSAGE_VALUE_UPDATE_TIMER);
						b.putLong(HANDLER_KEY_CURRENT_TIME, currentTime);
						m.setData(b);
						myHandler.sendMessage(m);
					}
				}
			}, 0, 1000);
		}

		updateLayout();
	}

	protected void onPause() {
		super.onPause();
		if ( ExamTrainer.getTimeLimit() > 0 ) {
			timer.cancel();
		}
		saveState();
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
			.setPositiveButton(R.string.calculate_score, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					startShowScoreActivity();
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.Quit, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					stopExam();
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
			.setPositiveButton(R.string.Quit, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					stopExam();
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.resume, new DialogInterface.OnClickListener() {
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

	private void saveState() {
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
			this.editText.setText(cursor.getString(index));
		}
		layout.addView(this.editText);
	}

	private void updateLayout() {
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());

		if( this.examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_OPEN)) {
			Cursor cursor = examinationDbHelper.getScoresAnswers(ExamTrainer.getScoresId(), questionId);
			if ( cursor.getCount() > 0 ) {
				int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Answers.COLUMN_NAME_ANSWER);
				this.editText.setText(cursor.getString(index));
			}
		} else {
			for( View view : this.multipleChoices ) {
				TextView tv = (TextView) view.findViewById(R.id.choiceTextView);
				CheckBox cbox = (CheckBox) view.findViewById(R.id.choiceCheckBox);

				if ( examinationDbHelper.scoresAnswerPresent(ExamTrainer.getScoresId(), 
						questionId, tv.getText().toString()) ) {
					cbox.setChecked(true);
				}
			}
		}
		examinationDbHelper.close();
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
