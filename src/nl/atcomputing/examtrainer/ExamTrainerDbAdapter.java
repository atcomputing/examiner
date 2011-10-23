package nl.atcomputing.examtrainer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author martijn brekhof
 *
 */
public class ExamTrainerDbAdapter {
	private final Context context;
	private SQLiteDatabase db;
	private ExamTrainerDatabaseHelper dbHelper;

	public ExamTrainerDbAdapter(Context context) {
		this.context = context;
	}

	public ExamTrainerDbAdapter open() {
		dbHelper = new ExamTrainerDatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
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

	public long addExam(String examTitle, String date, int itemsNeededToPass) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE, examTitle);
		values.put(ExamTrainer.Exams.COLUMN_NAME_DATE, date);
		values.put(ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS, itemsNeededToPass);
		return db.insert(ExamTrainer.Exams.TABLE_NAME, null, values);
	}

	public boolean deleteExam(long rowId) {
		return db.delete(ExamTrainer.Exams.TABLE_NAME, 
				ExamTrainer.Exams._ID + "=" + rowId, null) > 0;
	}

	public Cursor getExam(long rowId) {
		Cursor cursor = db.query(true, ExamTrainer.Exams.TABLE_NAME,
				new String[] {
				ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE,
				ExamTrainer.Exams.COLUMN_NAME_DATE,
				ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS
		},
		ExamTrainer.Exams._ID + "=" + rowId, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

}