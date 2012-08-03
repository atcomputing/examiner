package nl.atcomputing.examtrainer.database;

import nl.atcomputing.examtrainer.Exam;
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

	public static enum State {
		INSTALLING, INSTALLED, NOT_INSTALLED
	}
	
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
			Log.d(TAG, "Could not get writable database");
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
	
	public boolean setInstallationState(long rowId, State state) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED, state.name());
		return db.update(ExamTrainerDatabaseHelper.Exams.TABLE_NAME, values, 
				ExamTrainerDatabaseHelper.Exams._ID + "=\"" + rowId + "\"", null) > 0;
	}
	
	public State getInstallationState(long rowId) {
		State state = State.NOT_INSTALLED;
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				new String[] {ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED},
		ExamTrainerDatabaseHelper.Exams._ID + "=" + rowId, null, null, null, null, null);
		if( cursor.moveToFirst() ) {
			String stateName = cursor.getString(cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED));
			if( stateName.contentEquals(State.INSTALLED.name()) ) {
				state = State.INSTALLED;
			} else if ( stateName.contentEquals(State.INSTALLING.name()) ) {
				state = State.INSTALLING;
			}
		}
		return state;
	}
	
	public Cursor getExam(long rowId) {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				this.allRows,
		ExamTrainerDatabaseHelper.Exams._ID + "=" + rowId, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public boolean setExamInstallationDate(long rowId, long epochseconds) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE, epochseconds);
		return db.update(ExamTrainerDatabaseHelper.Exams.TABLE_NAME, values, 
				ExamTrainerDatabaseHelper.Exams._ID + "=\"" + rowId + "\"", null) > 0;
	}
	
	public String getExamTitle(long rowId) {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				new String[] {ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE},
		ExamTrainerDatabaseHelper.Exams._ID + "=" + rowId, null, null, null, null, null);
		if ( cursor.moveToFirst() ) {
			return cursor.getString(cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE));
		}
		return null;
	}

	public Cursor getAllExams() {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				this.allRows, null, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public Cursor getInstalledAndInstallingExams() {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				this.allRows, ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED + "=\'" + 
		ExamTrainerDbAdapter.State.INSTALLED.name() +"\' OR " +
		ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED + "=\'" + 
		ExamTrainerDbAdapter.State.INSTALLING.name() +"\'",
		null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
}
