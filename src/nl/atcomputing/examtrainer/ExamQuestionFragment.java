package nl.atcomputing.examtrainer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import nl.atcomputing.dialogs.HintDialog;
import nl.atcomputing.dialogs.TwoButtonDialog;
import nl.atcomputing.dialogs.UsageDialog;
import nl.atcomputing.examtrainer.database.ExaminationDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
/**
 * @author martijn brekhof
 *
 */
public class ExamQuestionFragment extends SherlockFragment {
	public static final int DIALOG_ENDOFEXAM_ID = 1;
	public static final int DIALOG_SHOW_HINT_ID = 2;
	public static final int DIALOG_QUITEXAM_ID = 3;
	public static final int DIALOG_TIMELIMITREACHED_ID = 4;
	
	//private ExaminationDbAdapter examinationDbHelper;
	//private Cursor cursorQuestion;
	private long questionId = 1;
	private EditText editText;
	private static TextView timeLimitTextView;
	private ArrayList <View> multipleChoices;
	private static final String HANDLER_MESSAGE_KEY = "handler_update_timer"; 
	private static final int HANDLER_MESSAGE_VALUE_UPDATE_TIMER = 0;
	private static final int HANDLER_MESSAGE_VALUE_TIMELIMITREACHED = 1;
	private static final String HANDLER_KEY_CURRENT_TIME = "handler_current_time"; 

	private Timer timer;
	private static MyHandler myHandler;

	private ExamQuestion examQuestion;

	private static class MyHandler extends Handler {
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("HH:mm:ss");
		ExamQuestionFragment fragment;

		public MyHandler(ExamQuestionFragment fragment) {
			super();
			this.fragment = fragment;
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
				fragment.showDialog(DIALOG_TIMELIMITREACHED_ID);
			}
		}
	}

	private ExamQuestionListener listener;
	
	public interface ExamQuestionListener {
		/**
		 * Called when user quits a running exam
		 */
		public void onStopExam();
		
		/**
		 * Called when user answered last question 
		 */
		public void onExamEnd();
	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // Make sure activity implemented ExamQuestionListener
        try {
            this.listener = (ExamQuestionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ExamQuestionListener");
        }
        
        setHasOptionsMenu(true);
    }
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.question, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Activity activity = getActivity();

		myHandler = new MyHandler(this);

		if ( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.ENDOFEXAM ) {
			this.listener.onExamEnd();
		}

		if( this.questionId == 1 ) {
			UsageDialog usageDialog = UsageDialog.newInstance(activity, R.string.Usage_Dialog_Press_menu_to_quit_the_exam_or_show_a_hint_if_available);
			if( usageDialog != null ) {
				usageDialog.show(getFragmentManager(), "UsageDialog");
			}
		}

		this.examQuestion = new ExamQuestion(activity);
		try {
			this.examQuestion.fillFromDatabase(ExamTrainer.getExamDatabaseName(), questionId);
		} catch (SQLiteException e) {
			ExamTrainer.showError(activity, activity.getResources().getString(R.string.Exam_is_empty) + "\n" +
					this.getResources().getString(R.string.Try_reinstalling_the_exam));
		}
		setupLayout();

		/**
		 * Setup observer to be able to measure exhibit and choices width. We use this to determine if
		 * question contains horizontally scrollable text. If so, we show a usage dialog.
		 */
		final RelativeLayout layout = (RelativeLayout) activity.findViewById(R.id.question_toplayout_container);
		ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
			viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				public void onGlobalLayout() {
					Boolean showUsageDialog = false;
					layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					TextView exhibit = (TextView) activity.findViewById(R.id.textExhibit);
					if( exhibit != null ) {
						HorizontalScrollView hs = (HorizontalScrollView) activity.findViewById(R.id.horizontalScrollViewExhibit);
						if( exhibit.getMeasuredWidth() > hs.getMeasuredWidth() ) {
							showUsageDialog = true;
						}
					}
					HorizontalScrollView hs = (HorizontalScrollView) activity.findViewById(R.id.question_multiplechoice_horizontalScrollView);
					if( hs != null ) {
						LinearLayout ll = (LinearLayout) activity.findViewById(R.id.question_multiplechoice_linear_layout);
						if( ll.getMeasuredWidth() > hs.getMeasuredWidth() ) {
							showUsageDialog = true;
						}
					}
					if( showUsageDialog ) {
						UsageDialog usageDialog = UsageDialog.newInstance(activity, R.string.Usage_Dialog_Sometimes_the_text_in_the_exhibit_or_choices_is_larger_than_fits_on_screen);
						if( usageDialog != null ) {
							usageDialog.show(getFragmentManager(), "UsageDialog");
						}
					}
				}
			});
		}
	}

	public void onPause() {
		super.onPause();
		if ( timer != null ) {
			timer.cancel();
		}
		if( ExamTrainer.getExamMode() != ExamTrainer.ExamTrainerMode.REVIEW ) {
			saveScore();
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.question_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
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

	public void setQuestionId(long id) {
		this.questionId = id;
	}
	
	public void showDialog(int id) {
		final Activity activity = getActivity();
		switch(id) {
		case DIALOG_ENDOFEXAM_ID:
			TwoButtonDialog endOfExamDialog = TwoButtonDialog.newInstance(R.string.end_of_exam_message);
			endOfExamDialog.setPositiveButton(R.string.yes, new Runnable() {

				public void run() {
					if( ExamTrainer.getExamMode() != ExamTrainer.ExamTrainerMode.REVIEW ) {
						listener.onExamEnd();
					} 
					else {
						listener.onStopExam();
					}
				}
			});
			endOfExamDialog.setNegativeButton(R.string.no, new Runnable() {

				public void run() {
				}
			});
			endOfExamDialog.show(getFragmentManager(), "EndOfExamDialog");
			break;
		case DIALOG_TIMELIMITREACHED_ID:
			TwoButtonDialog timeLimitReachedDialog = TwoButtonDialog.newInstance(R.string.time_s_up_);
			timeLimitReachedDialog.setPositiveButton(R.string.calculate_score, new Runnable() {

				public void run() {
					listener.onExamEnd();
				}
			});
			timeLimitReachedDialog.setNegativeButton(R.string.Quit, new Runnable() {

				public void run() {
					listener.onStopExam();
				}
			});
			timeLimitReachedDialog.show(getFragmentManager(), "TimeLimitReachedDialog");
			break;
		case DIALOG_QUITEXAM_ID:
			TwoButtonDialog quitExamDialog = TwoButtonDialog.newInstance(R.string.quit_exam_message);
			quitExamDialog.setPositiveButton(R.string.Quit, new Runnable() {

				public void run() {
					listener.onStopExam();
				}
			});
			quitExamDialog.setNegativeButton(R.string.resume, new Runnable() {

				public void run() {

				}
			});
			quitExamDialog.show(getFragmentManager(), "QuitExamDialog");
			break;
		case DIALOG_SHOW_HINT_ID:
			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(activity);
			examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
			String message = examinationDbHelper.getHint(questionId);
			examinationDbHelper.close();
			if( message == null ) {
				message = getString(R.string.hint_not_available);
			}
			HintDialog hintDialog = HintDialog.newInstance(message);
			hintDialog.show(getFragmentManager(), "HintDialog");
			break;
		}
	}

	public void setupTimer() {
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
		Activity activity = getActivity();

		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(activity);
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
			Toast.makeText(activity, 
					getResources().getString(R.string.correct_answers) + ":\n\n" +
							str.toString(), Toast.LENGTH_LONG).show();
		}
		cursor.close();
	}


	private void saveScore() {
		boolean answerCorrect = false;
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(getActivity());
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
		final Activity activity = getActivity();
		CheckBox cbox; 
		this.multipleChoices = new ArrayList<View>();
		ArrayList<String> choices = this.examQuestion.getChoices();

		int amountOfMultilineChoices = 0;

		for( String choice : choices ) {
			View view = LayoutInflater.from(activity).inflate(R.layout.choice, null);

			TextView tv = (TextView) view.findViewById(R.id.choiceTextView);
			tv.setText(Html.fromHtml(choice));
			cbox = (CheckBox) view.findViewById(R.id.choiceCheckBox);

			//highlight and separate multi line choices
			if( choice.contains("<br/>") ) {
				amountOfMultilineChoices++;
			}

			final String answer = choice;
			cbox.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (((CheckBox) v).isChecked()) {
						ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(activity);
						examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
						examinationDbHelper.setScoresAnswersMultipleChoice(ExamTrainer.getScoresId(), questionId, answer);
						examinationDbHelper.close();

					} else {
						ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(activity);
						examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
						examinationDbHelper.deleteScoresAnswer(ExamTrainer.getScoresId(), questionId, answer);
						examinationDbHelper.close();
					}

				}
			});
			layout.addView(view);
			this.multipleChoices.add(view);
		}

		//Make sure multiline choices are distinguishable from eachother
		if( amountOfMultilineChoices > 1 ) {
			for( View view : this.multipleChoices ) {
				TextView tv = (TextView) view.findViewById(R.id.choiceTextView);
				tv.setBackgroundColor(getResources().getColor(R.color.choice));
				int padding = getResources().getDimensionPixelSize(R.dimen.paddingSmall);
				view.setPadding(0, padding, 0, padding);
			}
		}
	}


	private void createOpenQuestionLayout() {
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(getActivity());
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getScoresAnswers(ExamTrainer.getScoresId(), questionId);
		examinationDbHelper.close();
		if ( cursor.getCount() > 0 ) {
			int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Answers.COLUMN_NAME_ANSWER);
			this.editText.setText(cursor.getString(index));
		}
	}

	private void updateLayout() {
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(getActivity());
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());

		Cursor scoresAnswersCursor = examinationDbHelper.getScoresAnswers(ExamTrainer.getScoresId(), questionId);

		if( ExamTrainer.getExamMode() == ExamTrainer.ExamTrainerMode.REVIEW ) {
			showAnswers();
		} else {
			//No need to get previous selected choices during an exam if there are none
			if ( scoresAnswersCursor.getCount() < 1 ) {
				examinationDbHelper.close();
				return;
			}
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
				if ( scoresAnswersCursor.getCount() > 0 ) {
					scoresAnswersCursor.moveToFirst();
					do {
						String answer = Html.fromHtml(scoresAnswersCursor.getString(index)).toString();			
						if( answer.contentEquals(tvText)) {
							cbox.setChecked(true);
						}
					} while( scoresAnswersCursor.moveToNext() );
				}
			}
		}
		examinationDbHelper.close();
	}
	
	private void setupLayout() {
		Activity activity = getActivity();
		String text;
		
		timeLimitTextView = (TextView) activity.findViewById(R.id.textExamTime);

		LinearLayout layout = (LinearLayout) activity.findViewById(R.id.question_layout);

		TextView question = (TextView) activity.findViewById(R.id.textQuestionNumber);
		question.setText(Long.toString(questionId));

		text = this.examQuestion.getExhibit();
		TextView exhibit = (TextView) activity.findViewById(R.id.textExhibit);
		if( text != null ) {
			exhibit.setText(Html.fromHtml(text));
		} else {
			HorizontalScrollView viewExhibit = (HorizontalScrollView) activity.findViewById(R.id.horizontalScrollViewExhibit);
			layout.removeView(viewExhibit);
		}


		text = this.examQuestion.getQuestion();
		TextView question_textview = (TextView) activity.findViewById(R.id.textQuestion);
		question_textview.setText(Html.fromHtml(text));

		if( this.examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_MULTIPLE_CHOICE)) {
			LinearLayout v_layout = (LinearLayout) activity.findViewById(R.id.question_multiplechoice_linear_layout);
			createChoicesLayout(v_layout);
			HorizontalScrollView sv = (HorizontalScrollView) activity.findViewById(R.id.question_multiplechoice_horizontalScrollView);
			sv.setVisibility(View.VISIBLE);
		} else if ( examQuestion.getType().equalsIgnoreCase(ExamQuestion.TYPE_OPEN)) {
			this.editText = (EditText) activity.findViewById(R.id.question_open_answer_edittext);
			createOpenQuestionLayout();
			this.editText.setVisibility(View.VISIBLE);
		}
	}
}
