package nl.atcomputing.examtrainer.activities;

import java.util.Collection;
import java.util.HashMap;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.examparser.InstallExamAsyncTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.format.Time;

/**
 * @author martijn brekhof
 */
public final class ExamTrainer {

	public enum ExamTrainerMode {
		SELECT_EXAM, 
		EXAM, 
		EXAM_REVIEW, 
		ENDOFEXAM, 
		CALCULATING_SCORE, 
		EXAM_OVERVIEW, 
		SHOW_EXAM_REVIEW,
		MANAGE_EXAMS
	}

	public static String BROADCAST_ACTION_EXAMLIST_UPDATED = "nl.atcomputing.examtrainer.examlistupdated";
	
	private static String examTitle = "ExamTrainer";
	private static String examDatabaseName = null;
	private static ExamTrainerMode mode = ExamTrainerMode.SELECT_EXAM;
	private static long examId = -1;
	private static long scoresId = -1;
	private static long answersCorrect = 0;
	private static long itemsNeededToPass = 0;
	private static long totalAmountOfItems = 0;
	private static long timeLimit = 0;
	private static long timeEnd = 0;
	private static long timerStart; 
	
	private static HashMap<Long, Integer> examInstallationProgression = new HashMap<Long, Integer>();
	private static HashMap<Long, InstallExamAsyncTask> installationThreads = new HashMap<Long, InstallExamAsyncTask>();
	
	// This class cannot be instantiated
	private ExamTrainer() {
	}

	public static void addInstallationThread(long id, InstallExamAsyncTask task) {
		installationThreads.put(id, task);
	}
	
	public static void removeInstallationThread(long id) {
		installationThreads.remove(id);
	}
	
	public static void cancelAllInstallationThreads() {
		Collection<InstallExamAsyncTask> values = ExamTrainer.getAllInstallExamAsyncTasks();
		for( InstallExamAsyncTask task : values ) {
			if( ! task.cancel(true) ) {
				removeInstallationThread(task.getExamID());
			}
		}
	}
	
	public static InstallExamAsyncTask getInstallExamAsyncTask(long id) {
		return installationThreads.get(id);
	}
	
	public static Collection<InstallExamAsyncTask> getAllInstallExamAsyncTasks() {
		return installationThreads.values();
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
	
	/**
	 * Sets the timer to the current time
	 */
	public static void setTimer() {
		timerStart = System.currentTimeMillis();
		timeEnd = timerStart + (timeLimit * 1000);
	}
	
	/**
	 * Sets the timer
	 * @param startTime epoch time in milliseconds
	 */
	public static void setTimer(long startTime) {
		timerStart = startTime;
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
	
	
//	public static void setQuestionId(Intent intent, long id) {
//		intent.putExtra(questionNumber, id);
//	}
//
//	public static long getQuestionId(Intent intent) {
//		return intent.getLongExtra(questionNumber, 1);
//	}

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
	
	public static void setExamInstallationProgression(long id, int percentage) {
		examInstallationProgression.put(id, percentage);
	}
	
	public static int getExamInstallationProgression(long id) {
		return examInstallationProgression.get(id);
	}
}