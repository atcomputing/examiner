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
		Cursor cursor = db.query(true, ExamTrainer.Exams.TABLE_NAME, 
				new String[] {
				ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE,
		},
		ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE + "=" + "\"" + exam.getTitle() + "\"", 
		null, null, null, null, null);
		if ( cursor != null ) {
			return cursor.getCount() > 0;
		} else {
			return false;
		}
	}

	public long addExam(Exam exam) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE, exam.getTitle());
		values.put(ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS, exam.getItemsNeededToPass());
		values.put(ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS, exam.getNumberOfItems());
		values.put(ExamTrainer.Exams.COLUMN_NAME_AUTHOR, exam.getAuthor());
		values.put(ExamTrainer.Exams.COLUMN_NAME_CATEGORY, exam.getCategory());
		values.put(ExamTrainer.Exams.COLUMN_NAME_INSTALLED, 0);
		values.put(ExamTrainer.Exams.COLUMN_NAME_URL, exam.getURL());
		return db.insert(ExamTrainer.Exams.TABLE_NAME, null, values);
	}

	public boolean deleteExam(long rowId) {
		return db.delete(ExamTrainer.Exams.TABLE_NAME, 
				ExamTrainer.Exams._ID + "=" + rowId, null) > 0;
	}
	
	public boolean setInstalled(long rowId, String date, boolean installed) {
		ContentValues values = new ContentValues();
		if(installed) {
			values.put(ExamTrainer.Exams.COLUMN_NAME_INSTALLED, 1);
		} else {
			values.put(ExamTrainer.Exams.COLUMN_NAME_INSTALLED, 0);
		}
		values.put(ExamTrainer.Exams.COLUMN_NAME_DATE, date);
		return db.update(ExamTrainer.Exams.TABLE_NAME, values, 
				ExamTrainer.Exams._ID + "=\"" + rowId + "\"", null) > 0;
	}
	
	public Cursor getExam(long rowId) {
		Cursor cursor = db.query(true, ExamTrainer.Exams.TABLE_NAME,
				new String[] {
				ExamTrainer.Exams._ID,
				ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE,
				ExamTrainer.Exams.COLUMN_NAME_DATE,
				ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS,
				ExamTrainer.Exams.COLUMN_NAME_INSTALLED,
				ExamTrainer.Exams.COLUMN_NAME_URL,
				ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS,
				ExamTrainer.Exams.COLUMN_NAME_AUTHOR,
				ExamTrainer.Exams.COLUMN_NAME_CATEGORY
		},
		ExamTrainer.Exams._ID + "=" + rowId, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	public Cursor getAllExams() {
		Cursor cursor = db.query(true, ExamTrainer.Exams.TABLE_NAME,
				new String[] {
				ExamTrainer.Exams._ID,
				ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE,
				ExamTrainer.Exams.COLUMN_NAME_DATE,
				ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS,
				ExamTrainer.Exams.COLUMN_NAME_INSTALLED,
				ExamTrainer.Exams.COLUMN_NAME_URL,
				ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS,
				ExamTrainer.Exams.COLUMN_NAME_AUTHOR,
				ExamTrainer.Exams.COLUMN_NAME_CATEGORY
		}, null, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public Cursor getInstalledExams() {
		Cursor cursor = db.query(true, ExamTrainer.Exams.TABLE_NAME,
				new String[] {
				ExamTrainer.Exams._ID,
				ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE,
				ExamTrainer.Exams.COLUMN_NAME_DATE,
				ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS,
				ExamTrainer.Exams.COLUMN_NAME_INSTALLED,
				ExamTrainer.Exams.COLUMN_NAME_URL,
				ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS,
				ExamTrainer.Exams.COLUMN_NAME_AUTHOR,
				ExamTrainer.Exams.COLUMN_NAME_CATEGORY
		}, ExamTrainer.Exams.COLUMN_NAME_INSTALLED + "= 1",
		null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
}