package nl.atcomputing.lpic1examtrainer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ExamTrainerDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "lpic101-102exam_trainer.db";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_CREATE_QUESTIONS_TABLE = "CREATE TABLE " 
	+ ExamTrainer.Questions.TABLE_NAME + " ("
    + ExamTrainer.Questions._ID + " INTEGER PRIMARY KEY,"
    + ExamTrainer.Questions.COLUMN_NAME_QUESTION + " TEXT,"
    + ExamTrainer.Questions.COLUMN_NAME_EXHIBIT + " TEXT,"
    + ExamTrainer.Questions.COLUMN_NAME_TYPE + " TEXT,"
    + ExamTrainer.Questions.COLUMN_NAME_ANSWERS + " TEXT,"
    + ExamTrainer.Questions.COLUMN_NAME_CORRECT_ANSWERS + " TEXT"
    + ");";
	
	private static final String DATABASE_CREATE_ANSWERS_TABLE = "CREATE TABLE " 
		+ ExamTrainer.Answers.TABLE_NAME + " ("
		+ ExamTrainer.Answers._ID + " INTEGER PRIMARY KEY,"
	    + ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID + " TEXT,"
	    + ExamTrainer.Answers.COLUMN_NAME_ANSWER + " TEXT"
	    + ");";
		
	public ExamTrainerDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_QUESTIONS_TABLE);
		db.execSQL(DATABASE_CREATE_ANSWERS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ExamTrainerDatabaseHelper.class.getName(),
		// TODO Auto-generated method stub
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + ExamTrainer.Questions.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ExamTrainer.Answers.TABLE_NAME);
		onCreate(db);	
	}
	
}