package nl.atcomputing.examtrainer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.BaseColumns;
import android.text.format.Time;
import android.util.Log;

/**
 * @author martijn brekhof
 */
public final class ExamTrainer {

	public enum ExamTrainerMode {
		EXAM, REVIEW
	}

	private static String TAG = "ExamTrainer";
	private static String examTitle = "ExamTrainer";
	private static String examDatabaseName = null;
	private static ExamTrainerMode mode = ExamTrainerMode.EXAM;
	private static long examId = -1;
	private static long itemsNeededToPass = 0;
	private static final String questionNumber = "questionNumber";
	private static final String endOfExam = "endOfExam";
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

	public static void setExamTitle(String title) {
		examTitle = title;
	}

	public static String getExamTitle() {
		return examTitle;
	}

	public static void setMode(ExamTrainerMode m) {
		mode = m;
	}

	public static ExamTrainerMode getMode() {
		return mode;
	}

	public static Boolean checkEndOfExam(Intent intent) {
		return intent.getBooleanExtra(endOfExam, false);
	}

	public static void setEndOfExam(Intent intent) {
		intent.putExtra(endOfExam, true);
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
	
	public static void showProgressDialog(Context context) {
		if( KEEP_PROGRESS_DIALOG_RUNNING == true ) {
			//Only one Progress Dialog is allowed to run simultaneously
			Log.d(TAG, "Another Progress Dialog still seems to be active.\nForgot to call ExamTrainer.stopDialog()?");
			return;
		}
		KEEP_PROGRESS_DIALOG_RUNNING = true;
		final ProgressDialog dialog = ProgressDialog.show(context, "", 
				context.getString(R.string.Loading_Please_wait), true, false);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while( KEEP_PROGRESS_DIALOG_RUNNING == true ) {
                	try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						Log.d(TAG, "InterruptedException " + e.getMessage());
						KEEP_PROGRESS_DIALOG_RUNNING = false;
					}
                }
                dialog.dismiss();
            }
        });
        thread.start();
	}
	
	public static void stopProgressDialog() {
		KEEP_PROGRESS_DIALOG_RUNNING = false;
	}
	
	

	
}