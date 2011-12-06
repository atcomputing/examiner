package nl.atcomputing.examtrainer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.Intent;
import android.provider.BaseColumns;

/**
 * @author martijn brekhof
 *
 */
public final class ExamTrainer {

	public enum ExamTrainerMode {
		EXAM, REVIEW
	}

	private static String examTitle = "ExamTrainer";
	private static String examDatabaseName = null;
	private static ExamTrainerMode mode = ExamTrainerMode.EXAM;
	private static long examId = -1;
	private static long itemsNeededToPass = 0;
	private static final String questionNumber = "questionNumber";
	private static final String endOfExam = "endOfExam";

	// This class cannot be instantiated
	private ExamTrainer() {
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

	public static void setExamDatabaseName(String examTitle, String date) {
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

	public static String convertUTCtoLocal(String utcDate) throws ParseException {
		String localDate;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
		try {
			Date date = sdf.parse(utcDate);
			DateFormat df = DateFormat.getDateTimeInstance();
			localDate = df.format(date);
			return localDate;
		} catch (ParseException e) {
			throw e;
		}
	}
	
	public static final class Exams implements BaseColumns {
		private Exams() {}

		public static final String TABLE_NAME = "Exams";
		public static final String COLUMN_NAME_EXAMTITLE = "examTitle";
		public static final String COLUMN_NAME_DATE = "date";
		public static final String COLUMN_NAME_ITEMSNEEDEDTOPASS = "itemsNeededToPass";
		public static final String COLUMN_NAME_AMOUNTOFITEMS = "amountOfItems";
		public static final String COLUMN_NAME_INSTALLED = "installed";
		public static final String COLUMN_NAME_URL = "URL";
		public static final String COLUMN_NAME_AUTHOR = "author";
		public static final String COLUMN_NAME_CATEGORY = "category";
	}

	public static final class Questions implements BaseColumns {

		// This class cannot be instantiated
		private Questions() {}

		//public static int amount = 0;

		public static final String TABLE_NAME = "Questions";

		public static final String TYPE_OPEN = "open";
		public static final String TYPE_MULTIPLECHOICE = "multiplechoice";

		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_QUESTION = "question";
		public static final String COLUMN_NAME_EXHIBIT = "exhibit";
		public static final String COLUMN_NAME_HINT = "hint";

	}

	public static final class Choices implements BaseColumns {

		private Choices() {}

		public static final String TABLE_NAME = "Choices";
		public static final String COLUMN_NAME_QUESTION_ID = "question_id";
		public static final String COLUMN_NAME_CHOICE = "choice";
	}

	public static final class Answers implements BaseColumns {

		// This class cannot be instantiated
		private Answers() {}

		public static final String TABLE_NAME = "Answers";

		public static final String COLUMN_NAME_QUESTION_ID = "question_id";
		public static final String COLUMN_NAME_ANSWER = "answer";
	}



	public static final class Scores implements BaseColumns {

		// This class cannot be instantiated
		private Scores() {}

		public static final String TABLE_NAME = "Scores";

		public static final String COLUMN_NAME_DATE = "date";
		public static final String COLUMN_NAME_SCORE = "score";
	}

	public static final class ScoresAnswers implements BaseColumns {

		// This class cannot be instantiated
		private ScoresAnswers() {}

		public static final String TABLE_NAME = "ScoresAnswers";

		public static final String COLUMN_NAME_EXAM_ID = "exam_id";
		public static final String COLUMN_NAME_QUESTION_ID = "question_id";
		public static final String COLUMN_NAME_ANSWER = "answer";
	}

	public static final class ResultPerQuestion implements BaseColumns {

		// This class cannot be instantiated
		private ResultPerQuestion() {}

		public static final String TABLE_NAME = "ResultPerQuestion";

		public static final String COLUMN_NAME_EXAM_ID = "exam_id";
		public static final String COLUMN_NAME_QUESTION_ID = "question_id";
		public static final String COLUMN_NAME_ANSWER_CORRECT = "answered_correct";
	}
}