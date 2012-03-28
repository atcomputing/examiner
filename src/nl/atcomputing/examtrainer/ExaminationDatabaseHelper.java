package nl.atcomputing.examtrainer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */
public class ExaminationDatabaseHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	
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

		public static final String COLUMN_NAME_SCORES_ID = "exam_id";
		public static final String COLUMN_NAME_QUESTION_ID = "question_id";
		public static final String COLUMN_NAME_ANSWER = "answer";
	}

	public static final class ResultPerQuestion implements BaseColumns {

		// This class cannot be instantiated
		private ResultPerQuestion() {}

		public static final String TABLE_NAME = "ResultPerQuestion";

		public static final String COLUMN_NAME_SCORES_ID = "exam_id";
		public static final String COLUMN_NAME_QUESTION_ID = "question_id";
		public static final String COLUMN_NAME_ANSWER_CORRECT = "answered_correct";
	}
	
	private static final String DATABASE_CREATE_QUESTIONS_TABLE = "CREATE TABLE " 
	+ Questions.TABLE_NAME + " ("
    + Questions._ID + " INTEGER PRIMARY KEY,"
    + Questions.COLUMN_NAME_QUESTION + " TEXT,"
    + Questions.COLUMN_NAME_EXHIBIT + " TEXT,"
    + Questions.COLUMN_NAME_TYPE + " TEXT,"
    + Questions.COLUMN_NAME_HINT + " TEXT"
    + ");";
	
	private static final String DATABASE_CREATE_CHOICES_TABLE = "CREATE TABLE " 
		+ Choices.TABLE_NAME + " ("
		+ Choices._ID + " INTEGER PRIMARY KEY,"
	    + Choices.COLUMN_NAME_QUESTION_ID + " INTEGER,"
	    + Choices.COLUMN_NAME_CHOICE + " TEXT"
	    + ");";
	
	private static final String DATABASE_CREATE_ANSWERS_TABLE = "CREATE TABLE " 
		+ Answers.TABLE_NAME + " ("
		+ Answers._ID + " INTEGER PRIMARY KEY,"
	    + Answers.COLUMN_NAME_QUESTION_ID + " INTEGER,"
	    + Answers.COLUMN_NAME_ANSWER + " TEXT"
	    + ");";
		
	private static final String DATABASE_CREATE_SCORESANSWERS_TABLE = "CREATE TABLE " 
		+ ScoresAnswers.TABLE_NAME + " ("
		+ ScoresAnswers._ID + " INTEGER PRIMARY KEY,"
	    + ScoresAnswers.COLUMN_NAME_SCORES_ID + " INTEGER,"
	    + ScoresAnswers.COLUMN_NAME_QUESTION_ID + " INTEGER,"
	    + ScoresAnswers.COLUMN_NAME_ANSWER + " TEXT"
	    + ");";

	private static final String DATABASE_CREATE_SCORES_TABLE = "CREATE TABLE " 
		+ Scores.TABLE_NAME + " ("
		+ Scores._ID + " INTEGER PRIMARY KEY,"
	    + Scores.COLUMN_NAME_SCORE + " INTEGER,"
	    + Scores.COLUMN_NAME_DATE + " INTEGER"
	    + ");";

	private static final String DATABASE_CREATE_RESULTPERQUESTION_TABLE = "CREATE TABLE " 
			+ ResultPerQuestion.TABLE_NAME + " ("
			+ ResultPerQuestion._ID + " INTEGER PRIMARY KEY,"
		    + ResultPerQuestion.COLUMN_NAME_QUESTION_ID + " INTEGER,"
		    + ResultPerQuestion.COLUMN_NAME_ANSWER_CORRECT + " INTEGER,"
		    + ResultPerQuestion.COLUMN_NAME_SCORES_ID + " INTEGER"
		    + ");";
	
	public ExaminationDatabaseHelper(Context context, String databaseName) {
		super(context, databaseName, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_QUESTIONS_TABLE);
		db.execSQL(DATABASE_CREATE_ANSWERS_TABLE);
		db.execSQL(DATABASE_CREATE_CHOICES_TABLE);
		db.execSQL(DATABASE_CREATE_SCORESANSWERS_TABLE);
		db.execSQL(DATABASE_CREATE_SCORES_TABLE);
		db.execSQL(DATABASE_CREATE_RESULTPERQUESTION_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ExaminationDatabaseHelper.class.getName(),
		// TODO Auto-generated method stub
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + Questions.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Answers.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Choices.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ScoresAnswers.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Scores.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ResultPerQuestion.TABLE_NAME);
		this.onCreate(db);	
	}
}