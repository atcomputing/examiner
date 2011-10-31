package nl.atcomputing.examtrainer;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */
public class ExaminationDbAdapter {
	private static final String TAG = "ExaminationDbAdapter";
	private final Context context;
	private SQLiteDatabase db;
	private ExaminationDatabaseHelper dbHelper;

	public ExaminationDbAdapter(Context context) {
		this.context = context;
	}
	
	public ExaminationDbAdapter open(String databaseName) throws SQLiteException {
		Log.d(TAG, "Opening " + databaseName);
		dbHelper = new ExaminationDatabaseHelper(context, databaseName);
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			Log.d(TAG, "Could not get writable database " + databaseName);
			throw e;
		}
		return this;
	}
	
	public ExaminationDbAdapter open(String title, String date) throws SQLiteException {
		String databaseName = createDataseName(title, date);
		return this.open(databaseName);
	}
	
	public void upgrade() {
		dbHelper.onUpgrade(db, 1, 1);
	}
	
	public void close() {
		dbHelper.close();
	}

	/**
	 * Deletes database file for a specific exam with database name title-date
	 * @param title
	 * @param date
	 * @return true if database was succesfully removed. False otherwiser
	 * @throws RuntimeException if database file does not exist.
	 */
	public boolean delete(String title, String date) {
		String databaseName = createDataseName(title, date);
		if ( databaseExist(databaseName) ) {
			return context.deleteDatabase(databaseName);
		}
		return true;
	}
	
	public long addQuestion(ExamQuestion examQuestion) {
		
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Questions.COLUMN_NAME_QUESTION, examQuestion.getQuestion());
		values.put(ExamTrainer.Questions.COLUMN_NAME_EXHIBIT, examQuestion.getExhibit());
		values.put(ExamTrainer.Questions.COLUMN_NAME_TYPE, examQuestion.getType());
		values.put(ExamTrainer.Questions.COLUMN_NAME_HINT, examQuestion.getHint());
		return db.insert(ExamTrainer.Questions.TABLE_NAME, null, values);
	}

	public long addChoice(long questionId, String choice) {
		
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Choices.COLUMN_NAME_CHOICE, choice);
		values.put(ExamTrainer.Choices.COLUMN_NAME_QUESTION_ID, questionId);
		
		//Log.d(this.getClass().getName(), "addChoice: " + values.toString() );
		
		return db.insert(ExamTrainer.Choices.TABLE_NAME, null, values);
	}

	public long addCorrectAnswers(long questionId, String answer) {
	
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.CorrectAnswers.COLUMN_NAME_QUESTION_ID, questionId);
		values.put(ExamTrainer.CorrectAnswers.COLUMN_NAME_ANSWER, answer);
		return db.insert(ExamTrainer.CorrectAnswers.TABLE_NAME, null, values);
	}
	
	public long addScore(String date, int score) {
		Log.d(TAG, "Adding score: " + date + " " + score);
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Scores.COLUMN_NAME_DATE, date);
		values.put(ExamTrainer.Scores.COLUMN_NAME_SCORE, score);
		return db.insert(ExamTrainer.Scores.TABLE_NAME, null, values);
	}
	
	public long addScoresAnswers(long examId, long questionId, String answer) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID, questionId);
		values.put(ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER, answer);
		values.put(ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID, examId);
		return db.insert(ExamTrainer.ScoresAnswers.TABLE_NAME, null, values);
	}
	
	
	/**
	 * updates the score for the exam with id
	 * @param id the exam Identification
	 * @param score the score for the specific exam
	 * @return boolean true if update was succesful, false otherwise
	 */
	public boolean updateScore(long id, int score) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Scores.COLUMN_NAME_SCORE, score);
		return db.update(ExamTrainer.Scores.TABLE_NAME, values, 
				ExamTrainer.Scores._ID + "=" + id, null) > 0;
	}
	
	
	/**
	 * Deletes a complete exam from the score table. This will delete both the score and
	 * the answers
	 * @param id
	 * @return true if rows were deleted, false if nothing was deleted.
	 */
	public boolean deleteScore(long id) {
		int status = db.delete(ExamTrainer.ScoresAnswers.TABLE_NAME, 
				ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID + "=" + id, null);
		//If ScoresAnswers rows were deleted, delete the corresponding Score as well
		if( status > 0 ) {
			return db.delete(ExamTrainer.Scores.TABLE_NAME, 
				ExamTrainer.Scores._ID + "=" + id, null) > 0;
		}
		return status > 0;
	}
	
	public boolean deleteQuestion(long rowId) {
		return db.delete(ExamTrainer.Questions.TABLE_NAME, 
				ExamTrainer.Questions._ID + "=" + rowId, null) > 0;
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
				ExamTrainer.Questions.COLUMN_NAME_HINT
				},
				ExamTrainer.Questions._ID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public List<Long> getAllQuestionIDs() throws SQLException {
		List<Long> list = new ArrayList<Long>();
		
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
				long questionId = mCursor.getLong(index);
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
	public Cursor getAnswers(long questionId) throws SQLException {
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
		Cursor mCursor = db.query(true, ExamTrainer.CorrectAnswers.TABLE_NAME, 
				new String[] {
				ExamTrainer.CorrectAnswers.COLUMN_NAME_ANSWER
				},
				ExamTrainer.CorrectAnswers.COLUMN_NAME_QUESTION_ID + "=" + questionId, 
				null, null, null, null, null);
		
		if(mCursor.moveToFirst())
			return mCursor;
		
		return null;
	}
	
	public Cursor getChoices(long questionId) throws SQLException {
		Cursor mCursor = db.query(true, ExamTrainer.Choices.TABLE_NAME, 
				new String[] {
				ExamTrainer.Choices.COLUMN_NAME_CHOICE
				},
				ExamTrainer.Choices.COLUMN_NAME_QUESTION_ID + "=" + questionId, 
				null, null, null, null, null);
		
		if(mCursor.moveToFirst())
			return mCursor;
		
		return null;
	}

	public Cursor getScoresAnswers(long examId) {
		Cursor cursor = db.query(true, ExamTrainer.ScoresAnswers.TABLE_NAME, 
				new String[] {
				ExamTrainer.ScoresAnswers._ID,
				ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER,
				ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID
				},
				ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID + "=" + examId, 
				null, null, null, null, null);
		
		if(cursor.moveToFirst())
			return cursor;
		
		return null;
	}
	
	public Cursor getScores() {
		Cursor cursor = db.query(true, ExamTrainer.Scores.TABLE_NAME, 
				new String[] {
				ExamTrainer.Scores._ID,
				ExamTrainer.Scores.COLUMN_NAME_DATE,
				ExamTrainer.Scores.COLUMN_NAME_SCORE
				},
				null, null, null, null, null, null);
		
		if(cursor.moveToFirst())
			return cursor;
		
		return null;
	}
	
	public String getHint(long questionId) {
		Cursor cursor = db.query(true, ExamTrainer.Questions.TABLE_NAME, 
				new String[] {
				ExamTrainer.Questions.COLUMN_NAME_HINT
				},
				ExamTrainer.Questions._ID + "=" + questionId,
				null, null, null, null, null);
		if(cursor.moveToFirst()) {
			int index = cursor.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_HINT);
			return cursor.getString(index);
		}
		return null;
	}
	
	/**
	 * @brief Checks if an answer with given questionId and answer is present in the database.
	 * @param questionId
	 * @param answer
	 * @return true if answer is in table, false otherwise
	 * @throws SQLException
	 */
	public boolean answerPresent(long questionId, String answer) 
													throws SQLException {
		String whereClause = "";
			
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
	
		String whereClause = ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID + "=" + questionId;

		Cursor mCursor = db.query(true, ExamTrainer.Answers.TABLE_NAME, 
				new String[] {
				ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID,
				ExamTrainer.Answers.COLUMN_NAME_ANSWER
				}, whereClause, null, null, null, null, null);

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
		//Log.d(this.getClass().getName(), "insertAnswer values: "+ values.toString());
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
		if( ! answerPresent(questionId, answer) ) {
			return insertAnswer(questionId, answer);
		}
		return true;
	}
	
	public boolean setOpenAnswer(long questionId, String answer) {
		//Log.d(this.getClass().getName(), "setOpenAnswer: questionId=" + questionId + 
		//		" answer=" + answer);
		if( checkIfAnswerInTable(questionId) ) {
			return updateAnswer(questionId, answer);
		} else {	
			return insertAnswer(questionId, answer);
		}
	}
	
	public boolean checkAnswer(String answer, long questionId) {
		Cursor cursor = this.getCorrectAnswers(questionId);
		if ( cursor != null ) {
			int index = cursor.getColumnIndex(ExamTrainer.CorrectAnswers.COLUMN_NAME_ANSWER);
			do {
				String correct_answer = cursor.getString(index);
				if(answer.equals(correct_answer)) {
					return true;
				}
			} while( cursor.moveToNext() );
		}
		return false;
	}
	
	public int getAmountOfQuestions() {
		Cursor mCursor = db.query(true, ExamTrainer.Questions.TABLE_NAME, 
				new String[] {
				ExamTrainer.Questions.COLUMN_NAME_QUESTION
				},
				null, null, null, null, null, null);
		if (mCursor != null) {
			return mCursor.getCount();
		}
		return 0;
	}
	
	public int getAmountOfScoreAnswers(long examId) {
		Cursor mCursor = db.query(true, ExamTrainer.ScoresAnswers.TABLE_NAME, 
				new String[] {
				ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID
				},
				ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID + "=" + examId,
				null, null, null, null, null);
		if (mCursor != null) {
			return mCursor.getCount();
		}
		return 0;
	}
	
	private String createDataseName(String title, String date) {
		return title +"-"+ date;
	}
	
	private boolean databaseExist(String name ) {
		SQLiteDatabase checkDB = null;
	    try {
	        checkDB = SQLiteDatabase.openDatabase(name, null,
	                SQLiteDatabase.OPEN_READONLY);
	        checkDB.close();
	    } catch (SQLiteException e) {
	        // database doesn't exist yet.
	    }
	    return checkDB != null ? true : false;
	}
}