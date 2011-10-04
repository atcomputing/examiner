package nl.atcomputing.lpic1examtrainer;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author mbrekhof
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
	
	public List<Integer> getAllQuestionIDs() throws SQLException {
		List<Integer> list = new ArrayList<Integer>();
		
		Cursor mCursor = db.query(true, ExamTrainer.Questions.TABLE_NAME, 
				new String[] {
				ExamTrainer.Questions._ID
				},
				null, null, null, null, null, null);
		if (mCursor == null) {
			return null;
		}
		
		int index = mCursor.getColumnIndex(ExamTrainer.Questions._ID);
		
		if(mCursor.moveToFirst()) {
			do {
				int questionId = mCursor.getInt(index);
				list.add(questionId);
			} while (mCursor.moveToNext());
		}
		
		return list;
	}
	
	/**
	 * @brief Retrieves answer from the score table for the given question ID.
	 * @param questionId The row number of the question in the question table
	 * @return Cursor that matched the SQL query. Note that Cursor can be null if no 
	 * row was found for the given questionId. 
	 * @throws SQLException
	 */
	public Cursor getAnswer(long questionId) throws SQLException {
		Cursor mCursor = db.query(true, ExamTrainer.Answers.TABLE_NAME, 
				new String[] {
				ExamTrainer.Answers.COLUMN_NAME_ANSWER
				},
				ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID + "=" + questionId, 
				null, null, null, null, null);
		
		if(mCursor.moveToFirst())
			return mCursor;
		
		return null;
	}
	
	public Cursor getCorrectAnswers(long questionId) throws SQLException {
		Cursor mCursor = db.query(true, ExamTrainer.Questions.TABLE_NAME, 
				new String[] {
				ExamTrainer.Questions.COLUMN_NAME_CORRECT_ANSWERS
				},
				ExamTrainer.Questions._ID + "=" + questionId, 
				null, null, null, null, null);
		
		if(mCursor.moveToFirst())
			return mCursor;
		
		return null;
	}
	
	/**
	 * @brief Checks if an answer with given questionId and answer is present in the database.
	 * @param questionId
	 * @param answer
	 * @return true if answer is in table, false otherwise
	 * @throws SQLException
	 */
	public boolean checkIfAnswerInTable(long questionId, String answer) 
													throws SQLException {
		String whereClause = "";
		
		Log.d(this.getClass().getName(), "checkIfAnswerInTable questionId = " + 
				questionId + " answer = " + answer);
			
			whereClause = ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID + "=" + questionId
				+ " AND " + ExamTrainer.Answers.COLUMN_NAME_ANSWER + "=" + 
				"\"" + answer + "\"";
			
		Cursor mCursor = db.query(true, ExamTrainer.Answers.TABLE_NAME, 
				new String[] {
				ExamTrainer.Answers.COLUMN_NAME_ANSWER
				},
				whereClause, 
				null, null, null, null, null);
		
		return mCursor.getCount() > 0;
	}
	
	/**
	 * @brief Checks if an answer with given questionId is present in the database.
	 * @param questionId
	 * @return true if answer is in table, false otherwise
	 * @throws SQLException
	 */
	public boolean checkIfAnswerInTable(long questionId) 
	throws SQLException {
		Log.d(this.getClass().getName(), "checkIfAnswerInTable questionId = " + 
				questionId);

		String whereClause = ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID + "=" + questionId;

		Cursor mCursor = db.query(true, ExamTrainer.Answers.TABLE_NAME, 
				new String[] {
				ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID,
				ExamTrainer.Answers.COLUMN_NAME_ANSWER
				}, whereClause, null, null, null, null, null);

		Log.d(this.getClass().getName(), "checkIfAnswerInTable mCursor.getCount = " + mCursor.getCount());
		return mCursor.getCount() > 0;
	}

	public boolean updateAnswer(long questionId, String answer) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Answers.COLUMN_NAME_ANSWER, answer);
		return db.update(ExamTrainer.Answers.TABLE_NAME, values, ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID + "="
				+ questionId, null) > 0;
	}
	
	public boolean insertAnswer(long questionId, String answer) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID, questionId);
		values.put(ExamTrainer.Answers.COLUMN_NAME_ANSWER, answer);
		Log.d(this.getClass().getName(), "insertAnswer values: "+ values.toString());
		return db.insert(ExamTrainer.Answers.TABLE_NAME, null, values) != -1;
	}
	
	public boolean deleteAnswer(long questionId, String answer) {
		return db.delete(ExamTrainer.Answers.TABLE_NAME, 
				ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID + "=" + questionId 
				+ " AND " +
				ExamTrainer.Answers.COLUMN_NAME_ANSWER + "=" + "\"" + answer + "\""
				, null) > 0;
	}
	
	/**
	 * @brief Will update the database and set the answer
	 * @param questionId	The rowId of the question answered
	 * @param answer		The answer
	 * @return true if succeeded, false otherwise
	 */
	public boolean setMultipleChoiceAnswer(long questionId, String answer) {	
		//Check if answer is already in the database
		if( ! checkIfAnswerInTable(questionId, answer) ) {
			return insertAnswer(questionId, answer);
		}
		
		return true;
	}
	
	public boolean setOpenAnswer(long questionId, String answer) {
		Log.d(this.getClass().getName(), "setOpenAnswer: questionId=" + questionId + 
				" answer=" + answer);
		if( checkIfAnswerInTable(questionId) ) {
			return updateAnswer(questionId, answer);
		} else {	
			return insertAnswer(questionId, answer);
		}
	}
}