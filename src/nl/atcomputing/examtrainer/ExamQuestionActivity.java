package nl.atcomputing.examtrainer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import nl.atcomputing.dialogs.DialogFactory;
import nl.atcomputing.examtrainer.database.ExaminationDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.scorecalculation.ShowScoreActivity;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
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
	private static TextView timeLimitTextView;
	private ArrayList <View> multipleChoices;
	private static final int DIALOG_ENDOFEXAM_ID = 1;
	private static final int DIALOG_SHOW_HINT_ID = 2;
	private static final int DIALOG_QUITEXAM_ID = 3;
	private static final int DIALOG_TIMELIMITREACHED_ID = 4;
	private static final String HANDLER_MESSAGE_KEY = "handler_update_timer"; 
	private static final int HANDLER_MESSAGE_VALUE_UPDATE_TIMER = 0;
	private static final int HANDLER_MESSAGE_VALUE_TIMELIMITREACHED = 1;
	private static final String HANDLER_KEY_CURRENT_TIME = "handler_current_time"; 

	private Timer timer;
	private static MyHandler myHandler;

	private ExamQuestion examQuestion;

	private static class MyHandler extends Handler {
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("HH:mm:ss");
		Activity activity;
		
		public MyHandler(Activity activity) {
			super();
			this.activity = activity;
		}
		
		public void handleMessage(Message msg) {
			int key = msg.getData().getInt(HANDLER_MESSAGE_KEY);
			if( key == HANDLER_MESSAGE_VALUE_UPDATE_TIMER ) {
				long currentTime = msg.getData().getLong(HANDLER_KEY_CURRENT_TIME);
				Date date = new Date(ExamTrainer.getTimeEnd() - currentTime);
				dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));		
				String timeLeft = dateFormatGmt.format(date);
				timeLimitTextView.setText(timeLeft);
			} else if ( key == HANDLER_MESSAGE_VALUE_TIMELIMITREACHED ) {
				activity.showDialog(DIALOG_TIMELIMITREACHED_ID);
			}

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myHandler = new MyHandler(this);

		if ( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.ENDOFEXAM ) {
			finish();
		}

		Intent intent = getIntent();

		this.questionId = ExamTrainer.getQuestionId(intent);
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
	}

	protected void onResume() {
		super.onResume();
		if ( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.ENDOFEXAM ) {
			finish();
		}
		setTitle(ExamTrainer.getExamTitle());
		
		setupTimer();
		
		updateLayout();
		
		if( this.questionId == 1 ) {
			Dialog dialog = DialogFactory.createUsageDialog(this, R.string.Usage_Dialog_Press_menu_to_quit_the_exam_or_show_a_hint_if_available);
			if( dialog != null ) {
				dialog.show();
			}
		}
	}

	protected void onPause() {
		super.onPause();
		if ( timer != null ) {
			timer.cancel();
		}
		if( ExamTrainer.getExamMode() != ExamTrainer.ExamTrainerMode.REVIEW ) {
			saveScore();
		}
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
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.question_menu_get_hint:
			showDialog(DIALOG_SHOW_HINT_ID);
			return true;
		case R.id.question_menu_quit_exam:
			showDialog(DIALOG_QUITEXAM_ID);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_ENDOFEXAM_ID:
			final Dialog d1 = DialogFactory.createTwoButtonDialog(this, R.string.end_of_exam_message, 
					R.string.yes, new Runnable() {

				public void run() {
					if( ExamTrainer.getExamMode() != ExamTrainer.ExamTrainerMode.REVIEW ) {
						startShowScoreActivity();
					} 
					else {
						stopExam();
					}
				}
			},
			R.string.no, new Runnable() {

				public void run() {
				}
			});
			return d1;
		case DIALOG_TIMELIMITREACHED_ID:
			final Dialog d2 = DialogFactory.createTwoButtonDialog(this, R.string.time_s_up_, 
					R.string.calculate_score, new Runnable() {

				public void run() {
					startShowScoreActivity();
				}
			}, 
			R.string.Quit, new Runnable() {

				public void run() {
					stopExam();
				}
			});
			return d2;
		case DIALOG_QUITEXAM_ID:
			final Dialog d3 = DialogFactory.createTwoButtonDialog(this, R.string.quit_exam_message, 
					R.string.Quit, new Runnable() {

				public void run() {
					stopExam();
				}
			}, 
			R.string.resume, new Runnable() {

				public void run() {

				}
			});
			return d3;
		case DIALOG_SHOW_HINT_ID:
			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
			examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
			String message = examinationDbHelper.getHint(questionId);
			examinationDbHelper.close();
			if( message == null ) {
				message = getString(R.string.hint_not_available);
			}
			final Dialog d4 = DialogFactory.createHintDialog(this, message);
			return d4;
		}
		return null;
	}

	private void setupTimer() {
		if ( ( ExamTrainer.getTimeLimit() > 0 ) && 
				( ExamTrainer.getExamMode() != ExamTrainer.ExamTrainerMode.REVIEW ) ) {
			this.timer = new Timer();
			this.timer.scheduleAtFixedRate(new TimerTask() {

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
		} else {
			this.timer = null;
		}
	}
	
	private void showAnswers() {
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getAnswers(this.questionId);
		examinationDbHelper.close();
		if ( cursor.getCount() < 1 ) {
			return;
		}
		int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Answers.COLUMN_NAME_ANSWER);

		if(examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_MULTIPLE_CHOICE)) {
			do {
				String answer = Html.fromHtml(cursor.getString(index)).toString();
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
				String answer = Html.fromHtml(cursor.getString(index)).toString();
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
			String userAnswer = this.editText.getText().toString();
			examinationDbHelper.setScoresAnswersOpen(ExamTrainer.getScoresId(), questionId,
					userAnswer);
			Cursor correctAnswers = examinationDbHelper.getAnswers(this.questionId);
			if( correctAnswers.getCount() < 1 ) {
				examinationDbHelper.close();
				return;
			}
			int index = correctAnswers.getColumnIndex(ExaminationDatabaseHelper.Answers.COLUMN_NAME_ANSWER);
			do {
				String correctAnswer = correctAnswers.getString(index);
				String answerWithoutMarkup = Html.fromHtml(correctAnswer).toString();
				if( answerWithoutMarkup.contentEquals(userAnswer) ) {
					answerCorrect = true;
				}
			} while( correctAnswers.moveToNext() );
		} else {
			answerCorrect = examinationDbHelper.checkScoresAnswersMultipleChoice(questionId, ExamTrainer.getScoresId());
		}
		examinationDbHelper.setResultPerQuestion(ExamTrainer.getScoresId(), questionId, answerCorrect);

		examinationDbHelper.close();
	}

	private void createChoicesLayout(LinearLayout layout) {
		CheckBox cbox; 
		this.multipleChoices = new ArrayList<View>();
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
	}


	private void createOpenQuestionLayout() {
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getScoresAnswers(ExamTrainer.getScoresId(), questionId);
		examinationDbHelper.close();
		if ( cursor.getCount() > 0 ) {
			int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Answers.COLUMN_NAME_ANSWER);
			this.editText.setText(cursor.getString(index));
		}
	}

	private void updateLayout() {
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());

		Cursor scoresAnswersCursor = examinationDbHelper.getScoresAnswers(ExamTrainer.getScoresId(), questionId);
		if ( scoresAnswersCursor.getCount() < 1 ) {
			examinationDbHelper.close();
			return;
		}

		if( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.REVIEW ) {
			showAnswers();
		}
		
		int index = scoresAnswersCursor.getColumnIndex(ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_ANSWER);

		if( this.examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_OPEN)) {
			this.editText.setText(scoresAnswersCursor.getString(index));
		} else {
			for( View view : this.multipleChoices ) {
				
				TextView tv = (TextView) view.findViewById(R.id.choiceTextView);
				String tvText = tv.getText().toString();
				CheckBox cbox = (CheckBox) view.findViewById(R.id.choiceCheckBox);
				cbox.setChecked(false);
				if( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.REVIEW ) {
					cbox.setClickable(false);
				}
				
				//Check if choice was selected by user previously
				scoresAnswersCursor.moveToFirst();
				do {
					String answer = Html.fromHtml(scoresAnswersCursor.getString(index)).toString();			
					if( answer.contentEquals(tvText)) {
						cbox.setChecked(true);
					}
				} while( scoresAnswersCursor.moveToNext() );
			}
		}
		examinationDbHelper.close();
	}

	private void setupLayout() {
		String text;

		setContentView(R.layout.question);

		timeLimitTextView = (TextView) findViewById(R.id.textExamTime);

		LinearLayout layout = (LinearLayout) findViewById(R.id.question_layout);

		TextView question = (TextView) findViewById(R.id.textQuestionNumber);
		question.setText(Long.toString(questionId));

		text = this.examQuestion.getExhibit();
		TextView exhibit = (TextView) findViewById(R.id.textExhibit);
		if( text != null ) {
			exhibit.setText(Html.fromHtml(text));
		} else {
			HorizontalScrollView viewExhibit = (HorizontalScrollView) findViewById(R.id.horizontalScrollViewExhibit);
			layout.removeView(viewExhibit);
		}


		text = this.examQuestion.getQuestion();
		TextView question_textview = (TextView) findViewById(R.id.textQuestion);
		question_textview.setText(Html.fromHtml(text));

		if( this.examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_MULTIPLE_CHOICE)) {
			LinearLayout v_layout = (LinearLayout) findViewById(R.id.question_multiplechoice_linear_layout);
			createChoicesLayout(v_layout);
			HorizontalScrollView sv = (HorizontalScrollView) findViewById(R.id.question_multiplechoice_horizontalScrollView);
			sv.setVisibility(View.VISIBLE);
		} else if ( examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_OPEN)) {
			this.editText = (EditText) findViewById(R.id.question_open_answer_edittext);
			createOpenQuestionLayout();
			this.editText.setVisibility(View.VISIBLE);
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
