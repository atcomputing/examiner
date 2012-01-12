package nl.atcomputing.examtrainer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */
public class ExamTrainerDbAdapter {
	private static final String TAG = "ExamTrainerDbAdapter";
	private final Context context;
	private SQLiteDatabase db;
	private ExamTrainerDatabaseHelper dbHelper;

	private String[] allRows = new String[] {
			ExamTrainerDatabaseHelper.Exams._ID,
			ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE,
			ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE,
			ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS,
			ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED,
			ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_URL,
			ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS,
			ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR,
			ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_CATEGORY,
			ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT
			};
	
	public ExamTrainerDbAdapter(Context context) {
		this.context = context;
	}

	public ExamTrainerDbAdapter open() throws SQLiteException {
		dbHelper = new ExamTrainerDatabaseHelper(context);
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			Log.d(TAG, "Could not get writable database " + dbHelper.getDatabaseName());
			throw e;
		}
		return this;
	}

	public void upgrade() {
		dbHelper.onUpgrade(db, 1, 1);
	}

	public void close() {
		dbHelper.close();
	}
	
	public boolean checkIfExamAlreadyInDatabase(Exam exam) {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME, 
				new String[] {
				ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE,
		},
		ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE + "=" + "\"" + exam.getTitle() + "\"", 
		null, null, null, null, null);
		Log.d("ExamTrainedDbAdapter", "Cursor: "+ cursor);
		if ( cursor != null ) {
			boolean res = cursor.getCount() > 0;
			cursor.close();
			return res;
		} else {
			return false;
		}
	}

	public long addExam(Exam exam) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE, exam.getTitle());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS, exam.getItemsNeededToPass());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS, exam.getNumberOfItems());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR, exam.getAuthor());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_CATEGORY, exam.getCategory());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED, 0);
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_URL, exam.getURL());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT, exam.getTimeLimit());
		return db.insert(ExamTrainerDatabaseHelper.Exams.TABLE_NAME, null, values);
	}

	public boolean deleteExam(long rowId) {
		return db.delete(ExamTrainerDatabaseHelper.Exams.TABLE_NAME, 
				ExamTrainerDatabaseHelper.Exams._ID + "=" + rowId, null) > 0;
	}
	
	public boolean setInstalled(long rowId, long date, boolean installed) {
		ContentValues values = new ContentValues();
		if(installed) {
			values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED, 1);
		} else {
			values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED, 0);
		}
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE, date);
		return db.update(ExamTrainerDatabaseHelper.Exams.TABLE_NAME, values, 
				ExamTrainerDatabaseHelper.Exams._ID + "=\"" + rowId + "\"", null) > 0;
	}
	
	public Cursor getExam(long rowId) {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				allRows,
		ExamTrainerDatabaseHelper.Exams._ID + "=" + rowId, null, null, null, null, null);
		Log.d("ExamTrainedDbAdapter", "Cursor: "+ cursor);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	public Cursor getAllExams() {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				allRows, null, null, null, null, null, null);
		Log.d("ExamTrainedDbAdapter", "Cursor: "+ cursor);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public Cursor getInstalledExams() {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				allRows, ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED + "= 1",
		null, null, null, null, null);
		Log.d("ExamTrainedDbAdapter", "Cursor: "+ cursor);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
}
