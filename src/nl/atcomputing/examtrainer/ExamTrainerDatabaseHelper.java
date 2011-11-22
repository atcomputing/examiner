package nl.atcomputing.examtrainer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */
public class ExamTrainerDatabaseHelper extends SQLiteOpenHelper {
	private static String DATABASE_NAME = "ExamTrainer.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String DATABASE_CREATE_EXAMS_TABLE = "CREATE TABLE " 
	+ ExamTrainer.Exams.TABLE_NAME + " ("
    + ExamTrainer.Exams._ID + " INTEGER PRIMARY KEY,"
    + ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE + " TEXT,"
    + ExamTrainer.Exams.COLUMN_NAME_DATE + " TEXT,"
    + ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS + " INTEGER,"
    + ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS + " INTEGER,"
    + ExamTrainer.Exams.COLUMN_NAME_INSTALLED + " INTEGER,"
    + ExamTrainer.Exams.COLUMN_NAME_URL + " TEXT"
    + ");";
	
	private static final String DATABASE_CREATE_CONFIGURATION_TABLE = "CREATE TABLE "
		+ ExamTrainer.Configuration.TABLE_NAME + " ("
		+ ExamTrainer.Configuration._ID + " INTEGER PRIMARY KEY,"
		+ ExamTrainer.Configuration.COLUMN_NAME_CHECKFORUPDATES + " INTEGER,"
		+ ExamTrainer.Configuration.COLUMN_NAME_SENDSCORES + " INTEGER,"
		+ ExamTrainer.Configuration.COLUMN_NAME_USETIMELIMIT + " INTEGER,"
		+ ExamTrainer.Configuration.COLUMN_NAME_URL + " TEXT"
		+ ");";
	
	public ExamTrainerDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_EXAMS_TABLE);
		db.execSQL(DATABASE_CREATE_CONFIGURATION_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ExamTrainerDatabaseHelper.class.getName(),
		// TODO Auto-generated method stub
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + ExamTrainer.Exams.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ExamTrainer.Configuration.TABLE_NAME);
		onCreate(db);	
	}
	
	public String getDatabaseName() {
		return DATABASE_NAME;
	}
}