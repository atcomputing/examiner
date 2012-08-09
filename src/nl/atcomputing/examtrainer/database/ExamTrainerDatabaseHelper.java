package nl.atcomputing.examtrainer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */
public class ExamTrainerDatabaseHelper extends SQLiteOpenHelper {
	private static String DATABASE_NAME = "ExamTrainer.db";
	private static final int DATABASE_VERSION = 1;
	
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
		public static final String COLUMN_NAME_TIMELIMIT = "timelimit";
	}
	
	private static final String DATABASE_CREATE_EXAMS_TABLE = "CREATE TABLE " 
	+ Exams.TABLE_NAME + " ("
    + Exams._ID + " INTEGER PRIMARY KEY,"
    + Exams.COLUMN_NAME_EXAMTITLE + " TEXT,"
    + Exams.COLUMN_NAME_DATE + " INTEGER,"
    + Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS + " INTEGER,"
    + Exams.COLUMN_NAME_AMOUNTOFITEMS + " INTEGER,"
    + Exams.COLUMN_NAME_INSTALLED + " TEXT,"
    + Exams.COLUMN_NAME_URL + " TEXT,"
    + Exams.COLUMN_NAME_AUTHOR + " TEXT,"
    + Exams.COLUMN_NAME_CATEGORY + " TEXT,"
    + Exams.COLUMN_NAME_TIMELIMIT + " INTEGER"
    + ");";
	
	public static final class UsageDialogs implements BaseColumns {
		private UsageDialogs() {}

		public static final String TABLE_NAME = "UsageDialogs";
		public static final String COLUMN_NAME_MSGID = "messageResourceId";
		public static final String COLUMN_NAME_SHOW = "show";
	}
	
	private static final String DATABASE_CREATE_USAGEDIALOGS_TABLE = "CREATE TABLE " 
	+ UsageDialogs.TABLE_NAME + " ("
    + UsageDialogs._ID + " INTEGER PRIMARY KEY,"
    + UsageDialogs.COLUMN_NAME_MSGID + " INTEGER,"
    + UsageDialogs.COLUMN_NAME_SHOW + " INTEGER"
    + ");";
	
	public ExamTrainerDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_EXAMS_TABLE);
		db.execSQL(DATABASE_CREATE_USAGEDIALOGS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ExamTrainerDatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + Exams.TABLE_NAME);
		onCreate(db);	
	}
	
	public String getDatabaseName() {
		return DATABASE_NAME;
	}
}