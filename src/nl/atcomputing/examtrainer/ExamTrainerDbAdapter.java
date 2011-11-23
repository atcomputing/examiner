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
	
	public boolean checkIfExamAlreadyInDatabase(String title) {
		Cursor cursor = db.query(true, ExamTrainer.Exams.TABLE_NAME, 
				new String[] {
				ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE,
		},
		ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE + "=" + "\"" + title + "\"", 
		null, null, null, null, null);
		if ( cursor != null ) {
			return cursor.getCount() > 0;
		} else {
			return false;
		}
	}

	public long addExam(String examTitle, String date, int itemsNeededToPass, 
			int amountOfItems, boolean installed, String url) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE, examTitle);
		values.put(ExamTrainer.Exams.COLUMN_NAME_DATE, date);
		values.put(ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS, itemsNeededToPass);
		values.put(ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS, amountOfItems);
		if(installed) {
			values.put(ExamTrainer.Exams.COLUMN_NAME_INSTALLED, 1);
		}
		else {
			values.put(ExamTrainer.Exams.COLUMN_NAME_INSTALLED, 0);
		}
		values.put(ExamTrainer.Exams.COLUMN_NAME_URL, url);
		return db.insert(ExamTrainer.Exams.TABLE_NAME, null, values);
	}

	public boolean deleteExam(long rowId) {
		return db.delete(ExamTrainer.Exams.TABLE_NAME, 
				ExamTrainer.Exams._ID + "=" + rowId, null) > 0;
	}

	public boolean setInstalled(String title, String date, boolean installed) {
		ContentValues values = new ContentValues();
		if ( installed ) {
			values.put(ExamTrainer.Exams.COLUMN_NAME_INSTALLED, 1);
		} else {
			values.put(ExamTrainer.Exams.COLUMN_NAME_INSTALLED, 0);
		}
		return db.update(ExamTrainer.Exams.TABLE_NAME, values, 
				ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE + "=\"" + title + "\" AND " +
						ExamTrainer.Exams.COLUMN_NAME_DATE + "=\"" + date + "\""
						, null) > 0;
	}
	
	/**
	 * Updates an already existing exam entry
	 * @param rowId the row number that should be updated
	 * @param examTitle the new title or null if examTitle should not be updated
	 * @param date the new date or null if date should not be updated
	 * @param itemsNeededToPass the new amount of items needed to pass or negative to not update the amount
	 * @param amountOfItems the new amount of items in the exam or negative to not update the amount
	 * @return true if update succeeded, false otherwise
	 */
//	public boolean updateExam(long rowId, String examTitle, String date, int itemsNeededToPass, int amountOfItems) {
//		ContentValues values = new ContentValues();
//		if ( examTitle != null ) {
//			values.put(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE, examTitle);
//		}
//		if ( date != null ) {
//			values.put(ExamTrainer.Exams.COLUMN_NAME_DATE, date);
//		}
//		if ( itemsNeededToPass >= 0 ) {
//			values.put(ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS, itemsNeededToPass);
//		}
//		if ( amountOfItems >= 0 ) {
//			values.put(ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS, amountOfItems);
//		}
//		Log.d(TAG, ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE + ":" + examTitle
//				+ ExamTrainer.Exams.COLUMN_NAME_DATE + ":" + date
//				+ ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS + ":" + itemsNeededToPass
//				+ ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS + ":" + amountOfItems);
//		return db.update(ExamTrainer.Exams.TABLE_NAME, values, 
//				ExamTrainer.Exams._ID + "=" + rowId, null) > 0;
//	}
	
	public Cursor getExam(long rowId) {
		Cursor cursor = db.query(true, ExamTrainer.Exams.TABLE_NAME,
				new String[] {
				ExamTrainer.Exams._ID,
				ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE,
				ExamTrainer.Exams.COLUMN_NAME_DATE,
				ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS,
				ExamTrainer.Exams.COLUMN_NAME_INSTALLED,
				ExamTrainer.Exams.COLUMN_NAME_URL,
				ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS
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
				ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS
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
				ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS
		}, ExamTrainer.Exams.COLUMN_NAME_INSTALLED + "= 1",
		null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
}