package nl.atcomputing.lpic1examtrainer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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

	public long addQuestion(ExamQuestion examQuestion) {
		
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Questions.COLUMN_NAME_QUESTION, examQuestion.getQuestion());
		values.put(ExamTrainer.Questions.COLUMN_NAME_EXHIBIT, examQuestion.getExhibit());
		values.put(ExamTrainer.Questions.COLUMN_NAME_TYPE, examQuestion.getType());
		values.put(ExamTrainer.Questions.COLUMN_NAME_ANSWERS, 
				examQuestion.convertArrayListToString(examQuestion.getAnswers()));
		values.put(ExamTrainer.Questions.COLUMN_NAME_CORRECT_ANSWERS, 
				examQuestion.convertArrayListToString(examQuestion.getCorrectAnswers()));
		
		return db.insert(ExamTrainer.Questions.TABLE_NAME, null, values);
	}

	public boolean updateQuestion(long rowId, String title, String question, String exhibit, String type, 
			String answers, String correct_answers) {
		
		ContentValues values = new ContentValues();
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
	 * Return a Cursor positioned at the defined question
	 */
	public Cursor getQuestion(long rowId) throws SQLException {
		Cursor mCursor = db.query(true, ExamTrainer.Questions.TABLE_NAME, 
				new String[] {
				ExamTrainer.Questions.COLUMN_NAME_QUESTION,
				ExamTrainer.Questions.COLUMN_NAME_TYPE,
				ExamTrainer.Questions.COLUMN_NAME_EXHIBIT,
				ExamTrainer.Questions.COLUMN_NAME_ANSWERS,
				ExamTrainer.Questions.COLUMN_NAME_CORRECT_ANSWERS
				},
				ExamTrainer.Questions._ID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/**
	 * @brief Retrieves answer from the score table for the given question ID.
	 * @param questionId The row number of the question in the question table
	 * @return Cursor that matched the SQL query. Note that Cursor can be null if no 
	 * row was found for the given questionId. 
	 * @throws SQLException
	 */
	public Cursor getAnswer(long questionId) throws SQLException {
		Cursor mCursor = db.query(true, ExamTrainer.Score.TABLE_NAME, 
				new String[] {
				ExamTrainer.Score.COLUMN_NAME_ANSWER_ID
				},
				ExamTrainer.Score.COLUMN_NAME_QUESTION_ID + "=" + questionId, 
				null, null, null, null, null);
		return mCursor;
	}
	
	public void printAnswers() {
		Cursor mCursor = db.query(true, ExamTrainer.Score.TABLE_NAME, 
				new String[] {
				ExamTrainer.Score._ID,
				ExamTrainer.Score.COLUMN_NAME_QUESTION_ID,
				ExamTrainer.Score.COLUMN_NAME_ANSWER_ID,
				}, 
				null, null, null, null, null, null);
		int answerIndex = mCursor.getColumnIndex(ExamTrainer.Score.COLUMN_NAME_ANSWER_ID);
		int questionIndex = mCursor.getColumnIndex(ExamTrainer.Score.COLUMN_NAME_QUESTION_ID);
		int idIndex = mCursor.getColumnIndex(ExamTrainer.Score._ID);
		if( mCursor.moveToFirst() ) {
			do {
				long answer_id = mCursor.getLong(answerIndex);
				long question_id = mCursor.getLong(questionIndex);
				long id = mCursor.getLong(idIndex);
				Log.d("ExamTrainerDbAdapter", "printAnswers: " + 
						ExamTrainer.Score._ID + "=" + id 
						+ ", " + 
						ExamTrainer.Score.COLUMN_NAME_QUESTION_ID + "=" + question_id
						+ ", " + 
						ExamTrainer.Score.COLUMN_NAME_ANSWER_ID + "=" + answer_id);
			} while (mCursor.moveToNext());
		}
	}
	public boolean checkIfAnswerInTable(long questionId, long answerId) 
													throws SQLException {
		Cursor mCursor = db.query(true, ExamTrainer.Score.TABLE_NAME, 
				new String[] {
				ExamTrainer.Score.COLUMN_NAME_ANSWER_ID
				},
				ExamTrainer.Score.COLUMN_NAME_QUESTION_ID + "=" + questionId
				+ " AND " + ExamTrainer.Score.COLUMN_NAME_ANSWER_ID + "=" + answerId, 
				null, null, null, null, null);
		
		return mCursor.getCount() > 0;
	}

	/**
	 * @brief Will update the database and set the answer for a specific multiple 
	 * choice question as answered by the user
	 * @param questionId	The rowId of the question answered
	 * @param answerId		The number of the answer chosen by the user
	 * @return true if succeeded, false otherwise
	 */
	public boolean setAnswer(long questionId, long answerId) {
		
		//Check if answer is already in the database
		if( ! checkIfAnswerInTable(questionId, answerId) ) {
			Log.d("DbAdapter", "setAnswer: questionId=" + questionId + 
					", answerId=" + answerId);
			ContentValues values = new ContentValues();
			values.put(ExamTrainer.Score.COLUMN_NAME_QUESTION_ID, questionId);
			values.put(ExamTrainer.Score.COLUMN_NAME_ANSWER_ID, answerId);

			return db.insert(ExamTrainer.Score.TABLE_NAME, null, values) != -1;
		}
		
		return true;
	}
}