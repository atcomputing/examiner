package nl.atcomputing.lpic1examtrainer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */
public class ExamTrainerDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "lpic101-102exam_trainer.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE_QUESTIONS_TABLE = "CREATE TABLE " 
	+ ExamTrainer.Questions.TABLE_NAME + " ("
    + ExamTrainer.Questions._ID + " INTEGER PRIMARY KEY,"
    + ExamTrainer.Questions.COLUMN_NAME_QUESTION + " TEXT,"
    + ExamTrainer.Questions.COLUMN_NAME_EXHIBIT + " TEXT,"
    + ExamTrainer.Questions.COLUMN_NAME_TYPE + " TEXT"
    + ");";
	
	private static final String DATABASE_CREATE_CHOICES_TABLE = "CREATE TABLE " 
		+ ExamTrainer.Choices.TABLE_NAME + " ("
		+ ExamTrainer.Choices._ID + " INTEGER PRIMARY KEY,"
	    + ExamTrainer.Choices.COLUMN_NAME_QUESTION_ID + " INTEGER,"
	    + ExamTrainer.Choices.COLUMN_NAME_CHOICE + " TEXT"
	    + ");";
	
	private static final String DATABASE_CREATE_CORRECT_ANSWERS_TABLE = "CREATE TABLE " 
		+ ExamTrainer.CorrectAnswers.TABLE_NAME + " ("
		+ ExamTrainer.CorrectAnswers._ID + " INTEGER PRIMARY KEY,"
	    + ExamTrainer.CorrectAnswers.COLUMN_NAME_QUESTION_ID + " INTEGER,"
	    + ExamTrainer.CorrectAnswers.COLUMN_NAME_ANSWER + " TEXT"
	    + ");";
	
	private static final String DATABASE_CREATE_ANSWERS_TABLE = "CREATE TABLE " 
		+ ExamTrainer.Answers.TABLE_NAME + " ("
		+ ExamTrainer.Answers._ID + " INTEGER PRIMARY KEY,"
	    + ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID + " INTEGER,"
	    + ExamTrainer.Answers.COLUMN_NAME_ANSWER + " TEXT"
	    + ");";
		
	private static final String DATABASE_CREATE_SCORES_ANSWERS_TABLE = "CREATE TABLE " 
		+ ExamTrainer.ScoresAnswers.TABLE_NAME + " ("
		+ ExamTrainer.ScoresAnswers._ID + " INTEGER PRIMARY KEY,"
	    + ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID + " INTEGER,"
	    + ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID + " INTEGER,"
	    + ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER + " TEXT"
	    + ");";

	private static final String DATABASE_CREATE_SCORES_TABLE = "CREATE TABLE " 
		+ ExamTrainer.Scores.TABLE_NAME + " ("
		+ ExamTrainer.Scores._ID + " INTEGER PRIMARY KEY,"
	    + ExamTrainer.Scores.COLUMN_NAME_SCORE + " INTEGER,"
	    + ExamTrainer.Scores.COLUMN_NAME_DATE + " TEXT"
	    + ");";

	
	public ExamTrainerDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_QUESTIONS_TABLE);
		db.execSQL(DATABASE_CREATE_ANSWERS_TABLE);
		db.execSQL(DATABASE_CREATE_CORRECT_ANSWERS_TABLE);
		db.execSQL(DATABASE_CREATE_CHOICES_TABLE);
		db.execSQL(DATABASE_CREATE_SCORES_ANSWERS_TABLE);
		db.execSQL(DATABASE_CREATE_SCORES_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ExamTrainerDatabaseHelper.class.getName(),
		// TODO Auto-generated method stub
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + ExamTrainer.Questions.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ExamTrainer.Answers.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ExamTrainer.Choices.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ExamTrainer.CorrectAnswers.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ExamTrainer.ScoresAnswers.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ExamTrainer.Scores.TABLE_NAME);
		onCreate(db);	
	}
	
}