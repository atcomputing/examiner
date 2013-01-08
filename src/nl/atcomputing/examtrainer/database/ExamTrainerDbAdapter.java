package nl.atcomputing.examtrainer.database;

import java.util.ArrayList;
import java.util.List;

import nl.atcomputing.examtrainer.activities.Exam;
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
			ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_COURSEURL,
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
			Log.w(TAG, "Could not get writable database");
			throw e;
		}
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	/**
	 * Searches table entries with given exam title and author
	 * @param exam used to get the exam title and author
	 * @return -1 if no row was found, primary key for the row otherwise.
	 */
	public long getRowId(Exam exam) {
		long rowId = -1;
		
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME, 
				new String[] {
				ExamTrainerDatabaseHelper.Exams._ID,
		},
		ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE + "=" + "\"" + exam.getTitle() + "\"" +
				" AND " + ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR + "=" + "\"" + exam.getAuthor() + "\"", 
				null, null, null, null, null);
		if ( cursor.moveToFirst() ) {
			int columnIndex = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
			rowId = cursor.getLong(columnIndex);
		}
		cursor.close();
		return rowId;
	}

	/**
	 * Searches table entries with given exam title and author
	 * @param cursor the row for which you want the ExamId returned
	 * @return -1 if no examId was found, examId otherwise.
	 */
	public long getExamId(Cursor cursor) {
		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
		if( index != -1 ) { 
			return cursor.getLong(index);
		} else {
			return -1;
		}
	}
	
	public long addExam(Exam exam) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE, exam.getTitle());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS, exam.getItemsNeededToPass());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS, exam.getNumberOfItems());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR, exam.getAuthor());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_CATEGORY, exam.getCategory());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED, Exam.State.NOT_INSTALLED.name());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_URL, exam.getURL());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT, exam.getTimeLimit());
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_COURSEURL, exam.getCourseURL());
		return db.insert(ExamTrainerDatabaseHelper.Exams.TABLE_NAME, null, values);
	}

	public boolean deleteExam(long rowId) {
		return db.delete(ExamTrainerDatabaseHelper.Exams.TABLE_NAME, 
				ExamTrainerDatabaseHelper.Exams._ID + "=" + rowId, null) > 0;
	}

	public boolean deleteAllExams() {
		return db.delete(ExamTrainerDatabaseHelper.Exams.TABLE_NAME, 
				null, null) > 0;
	}

	public boolean setInstallationState(long rowId, Exam.State state) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED, state.name());
		return db.update(ExamTrainerDatabaseHelper.Exams.TABLE_NAME, values, 
				ExamTrainerDatabaseHelper.Exams._ID + "=\"" + rowId + "\"", null) > 0;
	}

	public Exam.State getInstallationState(long rowId) {
		Exam.State state = Exam.State.NOT_INSTALLED;
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				new String[] {ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED},
				ExamTrainerDatabaseHelper.Exams._ID + "=" + rowId, null, null, null, null, null);
		if( cursor.moveToFirst() ) {
			String stateName = cursor.getString(cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED));
			state = Exam.State.valueOf(stateName);
		}
		cursor.close();
		return state;
	}

	public Cursor getExam(long rowId) {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				this.allRows,
				ExamTrainerDatabaseHelper.Exams._ID + "=" + rowId, null, null, null, null, null);
		return cursor;
	}

	public boolean setExamInstallationDate(long rowId, long epochseconds) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE, epochseconds);
		return db.update(ExamTrainerDatabaseHelper.Exams.TABLE_NAME, values, 
				ExamTrainerDatabaseHelper.Exams._ID + "=\"" + rowId + "\"", null) > 0;
	}

	public String getExamTitle(long rowId) {
		String title = null;
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				new String[] {ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE},
				ExamTrainerDatabaseHelper.Exams._ID + "=" + rowId, null, null, null, null, null);
		if ( cursor.moveToFirst() ) {
			title = cursor.getString(cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE));
		}
		cursor.close();
		return title;
	}

	public String getURL(long rowId) {
		String url = null;
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				new String[] {ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_URL},
				ExamTrainerDatabaseHelper.Exams._ID + "=" + rowId, null, null, null, null, null);
		if ( cursor.moveToFirst() ) {
			url = cursor.getString(cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_URL));
		}
		cursor.close();
		return url;
	}


	public Cursor getAllExams() {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				this.allRows, null, null, null, null, ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	public Cursor getNotInstalledExams() {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				this.allRows, ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED + "=\'" + 
						Exam.State.NOT_INSTALLED.name() + "\'",
						null, null, null, ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE, null);
		
		return cursor;
	}
	
	public Cursor getInstalledAndInstallingExams() {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				this.allRows, ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED + "=\'" + 
						Exam.State.INSTALLED.name() +"\' OR " +
						ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED + "=\'" + 
						Exam.State.INSTALLING.name() +"\'",
						null, null, null, ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE, null);
		return cursor;
	}

	public Cursor getInstallingExams() {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.Exams.TABLE_NAME,
				this.allRows, ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED + "=\'" + 
						Exam.State.INSTALLING.name() +"\'",
						null, null, null, null, null);
		cursor.moveToFirst();
		return cursor;
	}

	/**
	 * Queries the database if the usage dialog for message 
	 * with messageResourceId should be displayed or not.
	 * @param messageResourceId
	 * @return true if message should be displayed, false otherwise
	 */
	public boolean showMessage(int messageResourceId) {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.UsageDialogs.TABLE_NAME, 
				new String[] {
				ExamTrainerDatabaseHelper.UsageDialogs.COLUMN_NAME_SHOW,
		},
		ExamTrainerDatabaseHelper.UsageDialogs.COLUMN_NAME_MSGID + "=" + messageResourceId, 
		null, null, null, null, null);
		if ( cursor.moveToFirst() ) {
			int res = cursor.getInt(cursor.getColumnIndex(ExamTrainerDatabaseHelper.UsageDialogs.COLUMN_NAME_SHOW));
			cursor.close();
			if( res == 0 ) {
				return false;
			} else {
				return true;
			}
		} else {
			cursor.close();
			return true;
		}
	}

	/**
	 * Use this to set if the message with messageResourceId should be shown or not.
	 * @param messageResourceId
	 * @param state one of UsageDialogDbAdapter.State enumerations
	 * @return amount of rows inserted or updated
	 */
	public long setShowDialog(int messageResourceId, boolean bool) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainerDatabaseHelper.UsageDialogs.COLUMN_NAME_MSGID, messageResourceId);
		values.put(ExamTrainerDatabaseHelper.UsageDialogs.COLUMN_NAME_SHOW, bool);

		if( isPresentInUsageDialogTable(messageResourceId) ) {
			return db.update(ExamTrainerDatabaseHelper.UsageDialogs.TABLE_NAME, values, 
					ExamTrainerDatabaseHelper.UsageDialogs.COLUMN_NAME_MSGID + " = " + messageResourceId, null);
		} else {
			return db.insert(ExamTrainerDatabaseHelper.UsageDialogs.TABLE_NAME, null, values);
		}
	}

	private boolean isPresentInUsageDialogTable(int messageResourceId) {
		Cursor cursor = db.query(true, ExamTrainerDatabaseHelper.UsageDialogs.TABLE_NAME, 
				new String[] {
				ExamTrainerDatabaseHelper.UsageDialogs.COLUMN_NAME_SHOW,
		},
		ExamTrainerDatabaseHelper.UsageDialogs.COLUMN_NAME_MSGID + "=" + messageResourceId, 
		null, null, null, null, null);
		int count = cursor.getCount();
		cursor.close();
		return count > 0;
	}
}
