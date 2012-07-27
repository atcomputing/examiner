package nl.atcomputing.examtrainer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.Time;

/**
 * @author martijn brekhof
 */
public final class ExamTrainer {

	public enum ExamTrainerMode {
		EXAM, REVIEW, ENDOFEXAM, SHOW_SCORE
	}

	public static String BROADCAST_ACTION_EXAMLIST_UPDATED = "nl.atcomputing.examtrainer.examlistupdated";
	
	private static String examTitle = "ExamTrainer";
	private static String examDatabaseName = null;
	private static ExamTrainerMode mode = ExamTrainerMode.EXAM;
	private static long examId = -1;
	private static long scoresId = -1;
	private static long answersCorrect = 0;
	private static long itemsNeededToPass = 0;
	private static long totalAmountOfItems = 0;
	private static final String questionNumber = "questionNumber";
	private static boolean KEEP_PROGRESS_DIALOG_RUNNING = false;
	private static long timeLimit = 0;
	private static long timeEnd = 0;
	private static long timerStart; 
	
	// This class cannot be instantiated
	private ExamTrainer() {
	}

	public static void setAnswersCorrect(long amount) {
		answersCorrect = amount;
	}
	
	public static long getAnswersCorrect() {
		return answersCorrect;
	}
	
	public static void setTimeLimit(long seconds) {
		timeLimit = seconds;
	}
	
	/**
	 * @return time limit in seconds
	 */
	public static long getTimeLimit() {
		return timeLimit;
	}
	
	public static void setTimer() {
		timerStart = System.currentTimeMillis();
		timeEnd = timerStart + (timeLimit * 1000);
	}
	
	public static boolean timeLimitExceeded() {
		long currentTime = System.currentTimeMillis();
		return currentTime > timeEnd;
	}
	
	public static long getTimerStart() {
		return timerStart;
	}
	
	/**
	 * @return end time in milliseconds
	 */
	public static long getTimeEnd() {
		return timeEnd;
	}
	
	public static void setTimeEnd(long milliseconds) {
		timeEnd = milliseconds;
	}
	
	public static void setExamId(long id) {
		examId = id;
	}

	public static long getExamId() {
		return examId;
	}

	public static void setScoresId(long id) {
		scoresId = id;
	}

	public static long getScoresId() {
		return scoresId;
	}
	
	public static void setItemsNeededToPass(long items) {
		itemsNeededToPass = items;
	}

	public static long getItemsNeededToPass() {
		return itemsNeededToPass;
	}

	public static void setExamDatabaseName(String examTitle, long date) {
		examDatabaseName = examTitle + "-" + date;
	}

	public static String getExamDatabaseName() {
		return examDatabaseName;
	}

	public static void setAmountOfItems(long n) {
		totalAmountOfItems = n;
	}
	
	public static long getAmountOfItems() {
		return totalAmountOfItems;
	}
	
	public static void setExamTitle(String title) {
		examTitle = title;
	}

	public static String getExamTitle() {
		return examTitle;
	}
	
	public static void setExamMode(ExamTrainerMode m) {
		mode = m;
	}
	
	public static ExamTrainerMode getExamMode() {
		return mode;
	}
	
	
	public static void setQuestionId(Intent intent, long id) {
		intent.putExtra(questionNumber, id);
	}

	public static long getQuestionId(Intent intent) {
		return intent.getLongExtra(questionNumber, 1);
	}

	public static String convertEpochToString(long epoch) {
		Time time = new Time();
		time.set(Long.valueOf(epoch));
		return time.format("%Y-%m-%d %H:%M");
	}
	
	public static void showError(Activity activity, String msg) {
		final Activity act = activity;
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(activity.getResources().getString(R.string.Error) + ": " + msg)
		       .setCancelable(false)
		       .setPositiveButton(activity.getResources().getString(R.string.ok), 
		    		   new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		        	   act.finish();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	
}