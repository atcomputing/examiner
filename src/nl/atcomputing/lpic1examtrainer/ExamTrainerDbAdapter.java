package nl.atcomputing.lpic1examtrainer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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

	public long addQuestion(String title, String question, String exhibit, String type, 
			String answers, String correct_answers) {
		
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Questions.COLUMN_NAME_TITLE, title);
		values.put(ExamTrainer.Questions.COLUMN_NAME_QUESTION, question);
		values.put(ExamTrainer.Questions.COLUMN_NAME_EXHIBIT, exhibit);
		values.put(ExamTrainer.Questions.COLUMN_NAME_TYPE, type);
		values.put(ExamTrainer.Questions.COLUMN_NAME_ANSWERS, answers);
		values.put(ExamTrainer.Questions.COLUMN_NAME_CORRECT_ANSWERS, correct_answers);
		
		return db.insert(ExamTrainer.Questions.TABLE_NAME, null, values);
	}

	public boolean updateQuestion(long rowId, String title, String question, String exhibit, String type, 
			String answers, String correct_answers) {
		
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Questions.COLUMN_NAME_TITLE, title);
		values.put(ExamTrainer.Questions.COLUMN_NAME_QUESTION, question);
		values.put(ExamTrainer.Questions.COLUMN_NAME_EXHIBIT, exhibit);
		values.put(ExamTrainer.Questions.COLUMN_NAME_TYPE, type);
		values.put(ExamTrainer.Questions.COLUMN_NAME_ANSWERS, answers);
		values.put(ExamTrainer.Questions.COLUMN_NAME_CORRECT_ANSWERS, correct_answers);
		
		return db.update(ExamTrainer.Questions.TABLE_NAME, values, ExamTrainer.Questions._ID + "="
				+ rowId, null) > 0;
	}

	
	public boolean deleteQuestion(long rowId) {
		return db.delete(ExamTrainer.Questions.TABLE_NAME, ExamTrainer.Questions._ID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor positioned at the defined todo
	 */
	public Cursor getQuestion(long rowId) throws SQLException {
		Cursor mCursor = db.query(true, ExamTrainer.Questions.TABLE_NAME, 
				new String[] {
				ExamTrainer.Questions.COLUMN_NAME_QUESTION,
				ExamTrainer.Questions.COLUMN_NAME_TYPE,
				ExamTrainer.Questions.COLUMN_NAME_EXHIBIT,
				ExamTrainer.Questions.COLUMN_NAME_TITLE,
				ExamTrainer.Questions.COLUMN_NAME_ANSWERS,
				ExamTrainer.Questions.COLUMN_NAME_CORRECT_ANSWERS
				},
				ExamTrainer.Questions._ID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
}