package nl.atcomputing.examtrainer;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.text.format.Time;
import android.util.Log;

/**
 * @author martijn brekhof
 */
public final class ExamTrainer {

	public enum ExamTrainerMode {
		EXAM, REVIEW, ENDOFEXAM
	}

	private static String TAG = "ExamTrainer";
	private static String examTitle = "ExamTrainer";
	private static String examDatabaseName = null;
	private static ExamTrainerMode mode = ExamTrainerMode.EXAM;
	private static long examId = -1;
	private static long itemsNeededToPass = 0;
	private static long totalAmountOfItems = 0;
	private static final String questionNumber = "questionNumber";
	private static boolean KEEP_PROGRESS_DIALOG_RUNNING = false;
	private static long timeLimit = 0;
	private static long timerStart; 
	
	// This class cannot be instantiated
	private ExamTrainer() {
	}

	public static void setTimeLimit(long minutes) {
		timeLimit = minutes;
	}
	
	public static long getTimeLimit() {
		return timeLimit;
	}
	
	public static void setTimer() {
		timerStart = System.currentTimeMillis();
	}
	
	public static long getTimerStart() {
		return timerStart;
	}
	
	public static void setExamId(long id) {
		examId = id;
	}

	public static long getExamId() {
		return examId;
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
	
	public static void setEndOfExam() {
		mode = ExamTrainerMode.ENDOFEXAM;
	}
	
	public static boolean endOfExam() {
		return mode == ExamTrainerMode.ENDOFEXAM;
	}
	
	public static void setReview() {
		mode = ExamTrainerMode.REVIEW;
	}
	
	public static boolean review() {
		return mode == ExamTrainerMode.REVIEW;
	}
	
	public static void setStartExam() {
		mode = ExamTrainerMode.EXAM;
	}
	
	public static boolean startExam() {
		return mode == ExamTrainerMode.EXAM;
	}
	
	public static void setQuestionNumber(Intent intent, long number) {
		intent.putExtra(questionNumber, number);
	}

	public static long getQuestionNumber(Intent intent) {
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