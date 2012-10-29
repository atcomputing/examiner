package nl.atcomputing.examtrainer.database;

import java.util.ArrayList;
import java.util.List;

import nl.atcomputing.examtrainer.activities.ExamQuestion;
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
		dbHelper = new ExaminationDatabaseHelper(context, databaseName);
		try {
			db = dbHelper.getWritableDatabase();
		} catch (SQLiteException e) {
			Log.d(TAG, "Could not get writable database " + databaseName);
			throw e;
		}
		return this;
	}
	
	public ExaminationDbAdapter open(String title, long date) throws SQLiteException {
		String databaseName = createDataseName(title, date);
		ExaminationDbAdapter db = null;
		try {
			db = this.open(databaseName);
		} catch (SQLiteException e) {
			throw(e);
		}
		return db;
	}

	
	public void upgrade() {
		dbHelper.onUpgrade(db, 1, 1);
	}
	
	public void close() {
		dbHelper.close();
	}

	/**
	 * Deletes database file for a specific exam with database name title-date
	 * Note: deletion does not require the database to be open
	 * @param title
	 * @param date in seconds since 01/01/1970
	 * @return true if database was successfully removed. False otherwise
	 * @throws RuntimeException if database file does not exist.
	 */
	public boolean delete(String title, long date) {
		String databaseName = createDataseName(title, date);
		if( databaseExist(databaseName) ) {
			return context.deleteDatabase(databaseName);
		}
		return true;
	}
	
	public long addQuestion(ExamQuestion examQuestion) {
		
		ContentValues values = new ContentValues();
		values.put(ExaminationDatabaseHelper.Questions.COLUMN_NAME_QUESTION, examQuestion.getQuestion());
		values.put(ExaminationDatabaseHelper.Questions.COLUMN_NAME_EXHIBIT, examQuestion.getExhibit());
		values.put(ExaminationDatabaseHelper.Questions.COLUMN_NAME_TYPE, examQuestion.getType());
		values.put(ExaminationDatabaseHelper.Questions.COLUMN_NAME_HINT, examQuestion.getHint());
		return db.insert(ExaminationDatabaseHelper.Questions.TABLE_NAME, null, values);
	}

	public long addChoice(long questionId, String choice) {
		
		ContentValues values = new ContentValues();
		values.put(ExaminationDatabaseHelper.Choices.COLUMN_NAME_CHOICE, choice);
		values.put(ExaminationDatabaseHelper.Choices.COLUMN_NAME_QUESTION_ID, questionId);
		
		return db.insert(ExaminationDatabaseHelper.Choices.TABLE_NAME, null, values);
	}

	public long addAnswer(long questionId, String answer) {
	
		ContentValues values = new ContentValues();
		values.put(ExaminationDatabaseHelper.Answers.COLUMN_NAME_QUESTION_ID, questionId);
		values.put(ExaminationDatabaseHelper.Answers.COLUMN_NAME_ANSWER, answer);
		return db.insert(ExaminationDatabaseHelper.Answers.TABLE_NAME, null, values);
	}
	
	/**
	 * Adds a new row to Scores table with date set to current date in UTC
	 * and score set to 0
	 * @return _ID of the row that must be used as exam_id in the other tables
	 */
	public long createNewScore() {
		try {
			long date = System.currentTimeMillis();
	    	ContentValues values = new ContentValues();
			values.put(ExaminationDatabaseHelper.Scores.COLUMN_NAME_DATE, date);
			values.put(ExaminationDatabaseHelper.Scores.COLUMN_NAME_SCORE, 0);
			return db.insert(ExaminationDatabaseHelper.Scores.TABLE_NAME, null, values);
		} catch (NullPointerException e) {
			Log.d(TAG, "createNewScore null pointer exception: " + e.getMessage());
			return -1;
		} catch (IllegalArgumentException e) {
			Log.d(TAG, "createNewScore Illegal Argument: " + e.getMessage());
			return -1;
		}
	}
	
	
	/**
	 * updates the score for the exam with id
	 * @param id the exam Identification
	 * @param score the score for the specific exam
	 * @return boolean true if update was succesful, false otherwise
	 */
	public boolean updateScore(long scoresId, long score) {
		ContentValues values = new ContentValues();
		values.put(ExaminationDatabaseHelper.Scores.COLUMN_NAME_SCORE, score);
		return db.update(ExaminationDatabaseHelper.Scores.TABLE_NAME, values, 
				ExaminationDatabaseHelper.Scores._ID + "= ?", 
				new String[] { Long.toString(scoresId) }) > 0;
	}
	
	
	/**
	 * Deletes a complete score from the score table. This will delete both the score and
	 * the answers
	 * @param id
	 * @return true if rows were deleted, false if nothing was deleted.
	 */
	public boolean deleteScore(long scoresId) {
		db.delete(ExaminationDatabaseHelper.ScoresAnswers.TABLE_NAME, 
				ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_SCORES_ID + "= ?",
				new String[] { Long.toString(scoresId) });
		db.delete(ExaminationDatabaseHelper.ResultPerQuestion.TABLE_NAME,
				ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_SCORES_ID + "= ?",
				new String[] { Long.toString(scoresId) });
		return db.delete(ExaminationDatabaseHelper.Scores.TABLE_NAME, 
				ExaminationDatabaseHelper.Scores._ID + "= ?",
				new String[] { Long.toString(scoresId) }) > 0;
	}
	
	/**
	 * Return a Cursor positioned at the defined question
	 */
	public Cursor getQuestion(long rowId) throws SQLException {
		Cursor mCursor = db.query(true, ExaminationDatabaseHelper.Questions.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.Questions.COLUMN_NAME_QUESTION,
				ExaminationDatabaseHelper.Questions.COLUMN_NAME_TYPE,
				ExaminationDatabaseHelper.Questions.COLUMN_NAME_EXHIBIT,
				ExaminationDatabaseHelper.Questions.COLUMN_NAME_HINT
				},
				ExaminationDatabaseHelper.Questions._ID + "= ?", 
				new String[] { Long.toString(rowId) }, null, null, null, null);
		mCursor.moveToFirst();
		return mCursor;
	}
	
	public List<Long> getAllQuestionIDs() throws SQLException {
		List<Long> list = new ArrayList<Long>();
		
		Cursor mCursor = db.query(true, ExaminationDatabaseHelper.Questions.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.Questions._ID
				},
				null, null, null, null, null, null);
		
		if(mCursor.moveToFirst()) {
			int index = mCursor.getColumnIndex(ExaminationDatabaseHelper.Questions._ID);
			do {
				long questionId = mCursor.getLong(index);
				list.add(questionId);
			} while (mCursor.moveToNext());
		}
		
		mCursor.close();
		return list;
	}
	
	/**
	 * Returns the hint if specified for the question
	 * @param questionId _ID of the question 
	 * @return String or null if no hint was not found
	 */
	public String getHint(long questionId) {
		Cursor cursor = db.query(true, ExaminationDatabaseHelper.Questions.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.Questions.COLUMN_NAME_HINT
				},
				ExaminationDatabaseHelper.Questions._ID + "= ?",
				new String[] { Long.toString(questionId) },
				null, null, null, null);
		String hint = null;
		if(cursor.moveToFirst()) {
			int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Questions.COLUMN_NAME_HINT);
			hint = cursor.getString(index);
		}
		cursor.close();
		return hint;
	}
	
	public Cursor getAnswers(long questionId) {
		Cursor mCursor = db.query(true, ExaminationDatabaseHelper.Answers.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.Answers.COLUMN_NAME_ANSWER
				},
				ExaminationDatabaseHelper.Answers.COLUMN_NAME_QUESTION_ID + "= ?",
				new String[] { Long.toString(questionId) }, 
				null, null, null, null);
		
		mCursor.moveToFirst();
		return mCursor;
		
	}
	
	public Cursor getChoices(long questionId) throws SQLException {
		Cursor mCursor = db.query(true, ExaminationDatabaseHelper.Choices.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.Choices.COLUMN_NAME_CHOICE
				},
				ExaminationDatabaseHelper.Choices.COLUMN_NAME_QUESTION_ID + "= ?",
				new String[] { Long.toString(questionId) }, 
				null, null, null, null);
		
		mCursor.moveToFirst();
		return mCursor;
		
	}

	/**
	 * Returns the result of a question in the ResultPerQuestion table
	 * @param scoresId
	 * @param questionId
	 * @return -1 if result is not present, 0 if answer is wrong, 1 if answer is correct
	 */
	public int getResult(long scoresId, long questionId) {
		Cursor mCursor = db.query(true, ExaminationDatabaseHelper.ResultPerQuestion.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_ANSWER_CORRECT
				},
				ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_SCORES_ID + "= ? AND "+
						ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_QUESTION_ID + "= ?",
				new String[] { Long.toString(scoresId), Long.toString(questionId) }, 
				null, null, null, null);
		int answered_correct = -1;
		if(mCursor.moveToFirst()) {
			int index = mCursor.getColumnIndex(ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_ANSWER_CORRECT);
			answered_correct = mCursor.getInt(index);
		}
		mCursor.close();
		return answered_correct;
	}
	
	public Cursor getResultsPerQuestion(long scoresId) {
		Cursor mCursor = db.query(true, ExaminationDatabaseHelper.ResultPerQuestion.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_QUESTION_ID,
				ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_ANSWER_CORRECT,
				},
				ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_SCORES_ID + "= ?",
				new String[] { Long.toString(scoresId) }, 
				null, null, null, null);
		
		mCursor.moveToFirst();
		return mCursor;
	}
	
	public long getLastAnsweredQuestionId(long scoresId) {
		Cursor mCursor = db.query(true, ExaminationDatabaseHelper.ResultPerQuestion.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_QUESTION_ID
				},
				ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_SCORES_ID + "= ?",
				new String[] { Long.toString(scoresId) }, 
				null, null, null, null);
		
		long id = -1;
		
		if(mCursor.moveToFirst()) {
			id = mCursor.getLong(mCursor.getColumnIndex(ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_QUESTION_ID));
		}
		mCursor.close();
		return id;
	}
	
	public long addResultPerQuestion(long scoresId, long questionId, boolean answerCorrect) {
		ContentValues values = new ContentValues();
		values.put(ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_QUESTION_ID, questionId);
		values.put(ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_SCORES_ID, scoresId);
		if ( answerCorrect ) {
			values.put(ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_ANSWER_CORRECT, 1);
		} else {
			values.put(ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_ANSWER_CORRECT, 0);
		}
		return db.insert(ExaminationDatabaseHelper.ResultPerQuestion.TABLE_NAME, null, values);
	}
	
	public boolean updateResultPerQuestion(long scoresId, long questionId, boolean answerCorrect) {
		ContentValues values = new ContentValues();
		if ( answerCorrect ) {
			values.put(ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_ANSWER_CORRECT, 1);
		} else {
			values.put(ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_ANSWER_CORRECT, 0);
		}
		
		return db.update(ExaminationDatabaseHelper.ResultPerQuestion.TABLE_NAME, values, 
				ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_SCORES_ID + "= ? AND "+
						ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_QUESTION_ID + "= ?", 
				new String[] { Long.toString(scoresId), Long.toString(questionId) } ) > 0;
	}
	
	/**
	 * Adds or updates the result for a specific question in ResultPerQuestion table
	 * @param scoresId
	 * @param questionId
	 * @param answerCorrect
	 */
	public void setResultPerQuestion(long scoresId, long questionId, boolean answerCorrect) {
		int res = getResult(scoresId, questionId);
		if( res == -1 ) {
			addResultPerQuestion(scoresId, questionId, answerCorrect);
		} else {
			updateResultPerQuestion(scoresId, questionId, answerCorrect);
		}
	}
	
	/**
	 * Returns the answers a user previously provided for the given exam for a specific question
	 * @param scoresId the identifier for the exam you want the answers from
	 * @param questionId the identifier for the question you want the answers from
	 * @return Cursor containing the rows with answers for the given exam and question
	 */
	public Cursor getScoresAnswers(long scoresId, long questionId) {
		Cursor cursor = db.query(true, ExaminationDatabaseHelper.ScoresAnswers.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_ANSWER
				},
				ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_SCORES_ID + "= ?"
				+  " AND " +
				ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_QUESTION_ID + "= ?", 
				new String[] { Long.toString(scoresId), Long.toString(questionId) },
				null, null, null, null);
		cursor.moveToFirst();
		return cursor;
	}
	
	public Cursor getScoresReversed() {
		Cursor cursor = db.query(true, ExaminationDatabaseHelper.Scores.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.Scores._ID,
				ExaminationDatabaseHelper.Scores.COLUMN_NAME_DATE,
				ExaminationDatabaseHelper.Scores.COLUMN_NAME_SCORE
				},
				null, null, null, null, ExaminationDatabaseHelper.Scores.COLUMN_NAME_DATE + " DESC", null);
		cursor.moveToFirst();
		return cursor;
	}
	
	public int getScore(long id) {
		Cursor cursor = db.query(true, ExaminationDatabaseHelper.Scores.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.Scores.COLUMN_NAME_SCORE
				},
				ExaminationDatabaseHelper.Scores._ID + "= ?" , 
				new String[] { Long.toString(id) }, null, null, null, null);
		cursor.moveToFirst();
		int count = cursor.getInt(cursor.getColumnIndex(ExaminationDatabaseHelper.Scores.COLUMN_NAME_SCORE));
		cursor.close();
		return count;
	}
	
	/**
	 * @brief Checks if an answer with given questionId and answer is present in the database.
	 * @param questionId
	 * @param answer
	 * @return true if answer is in table, false otherwise
	 * @throws SQLException
	 */
	public boolean scoresAnswerPresent(long scoresId, long questionId, String answer) {
		 String selection = ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_QUESTION_ID + "= ?"
				+ " AND " + ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_ANSWER + "= ?" 
				+ " AND " + ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_SCORES_ID + "= ?";
		 
		 String[] selectionArgs = new String[] { Long.toString(questionId), answer, Long.toString(scoresId) };
		 
		Cursor mCursor = db.query(true, ExaminationDatabaseHelper.ScoresAnswers.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_ANSWER
				},
				selection, selectionArgs, 
				null, null, null, null);
		boolean res = mCursor.getCount() > 0;
		mCursor.close();
		return res;
	}
	
	/**
	 * @brief Checks if an answer with given questionId is present in the database.
	 * @param questionId
	 * @return true if answer is in table, false otherwise
	 * @throws SQLException
	 */
	public boolean scoresAnswerPresent(long scoresId, long questionId) {
		String selection = ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_QUESTION_ID + "= ?" 
		+ " AND " + ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_SCORES_ID + "= ?";
		String[] selectionArgs = new String[] { Long.toString(questionId), Long.toString(scoresId) };
		
		Cursor mCursor = db.query(true, ExaminationDatabaseHelper.ScoresAnswers.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_ANSWER
				}, selection, selectionArgs, null, null, null, null);
		boolean res = mCursor.getCount() > 0;
		mCursor.close();
		return res;
	}

	public boolean updateScoresAnswer(long scoresId, long questionId, String answer) {
		ContentValues values = new ContentValues();
		values.put(ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_ANSWER, answer);
		return db.update(ExaminationDatabaseHelper.ScoresAnswers.TABLE_NAME, values, 
				ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_QUESTION_ID + "= ?"
				+ " AND " +
				ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_SCORES_ID + "= ?", 
				new String[] { Long.toString(questionId), Long.toString(scoresId) } ) > 0;
	}
	
	public boolean insertScoresAnswer(long scoresId, long questionId, String answer) {
		ContentValues values = new ContentValues();
		values.put(ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_SCORES_ID, scoresId);
		values.put(ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_QUESTION_ID, questionId);
		values.put(ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_ANSWER, answer);
		return db.insert(ExaminationDatabaseHelper.ScoresAnswers.TABLE_NAME, null, values) != -1;
	}
	
	public boolean deleteScoresAnswer(long scoresId, long questionId, String answer) {
		return db.delete(ExaminationDatabaseHelper.ScoresAnswers.TABLE_NAME, 
				ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_QUESTION_ID + "= ?"
				+ " AND " +
				ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_ANSWER + "= ?"
				+ " AND " + 
				ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_SCORES_ID + "= ?"
				, new String[] {Long.toString(questionId), answer, Long.toString(scoresId) } ) > 0;
	}
	
	/**
	 * @brief Will update the database and set the answer
	 * @param questionId	The rowId of the question answered
	 * @param answer		The answer
	 * @return true if succeeded, false otherwise
	 */
	public boolean setScoresAnswersMultipleChoice(long scoresId, long questionId, String answer) {	
		//Check if answer is already in the database
		if( ! scoresAnswerPresent(scoresId, questionId, answer) ) {
			return insertScoresAnswer(scoresId, questionId, answer);
		}
		return true;
	}
	
	public boolean setScoresAnswersOpen(long scoresId, long questionId, String answer) {
		if( scoresAnswerPresent(scoresId, questionId) ) {
			return updateScoresAnswer(scoresId, questionId, answer);
		} else {	
			return insertScoresAnswer(scoresId, questionId, answer);
		}
	}
	
	/**
	 * Counts the amount of questions answered correctly in ResultPerQuestion table.
	 * @param scoresId  The ID of the score associated with the exam
	 * @return amount of correctly answered questions
	 */
	public int calculateScore(long scoresId) {
		String selection = ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_SCORES_ID + 
				" = ? AND " + ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_ANSWER_CORRECT + 
				" = ?";
		String[] selectionArgs = new String[] { Long.toString(scoresId), Long.toString(1) };
		
		Cursor mCursor = db.query(true, ExaminationDatabaseHelper.ResultPerQuestion.TABLE_NAME, 
				new String[] {
				ExaminationDatabaseHelper.ResultPerQuestion._ID
				}, selection, selectionArgs, null, null, null, null);
		int count = 0;
		if(mCursor.moveToFirst()) {
			count = mCursor.getCount();
		}
		mCursor.close();
		return count;
	}
	
	/**
	 * Checks if a multiple choice question was answered correctly using a SQL query
	 * with a JOIN over the scores answers table and correct answers table
	 * @param questionId ID of the question you want to check the answers for
	 * @param scoresId  The ID of the score associated with the exam
	 * @return true if all answers are correct, false otherwise
	 * 
	 * SELECT Answers.question_id FROM Answers, ScoresAnswers 
	 * WHERE Answers.question_id = ScoresAnswers.question_id 
	 * AND Answers.answer = ScoresAnswers.answer 
	 * AND ScoresAnswers.exam_id = scoresId
	 */
	public boolean checkScoresAnswersMultipleChoice(long questionId, long scoresId) {
		Cursor answersCountCursor = getScoresAnswers(scoresId, questionId);
		Cursor correctAnswersCountCursor = getAnswers(questionId);
		int answersCount = answersCountCursor.getCount();
		int correctAnswersCount = correctAnswersCountCursor.getCount();
		answersCountCursor.close();
		correctAnswersCountCursor.close();
		
		if ( answersCount == correctAnswersCount ) {
			String correctAnswersQuestionId = ExaminationDatabaseHelper.Answers.TABLE_NAME + "." + ExaminationDatabaseHelper.Answers.COLUMN_NAME_QUESTION_ID;
			String correctAnswersAnswer = ExaminationDatabaseHelper.Answers.TABLE_NAME + "." + ExaminationDatabaseHelper.Answers.COLUMN_NAME_ANSWER;
			String ScoresAnswersQuestionId = ExaminationDatabaseHelper.ScoresAnswers.TABLE_NAME + "." + ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_QUESTION_ID;
			String ScoresAnswersAnswer = ExaminationDatabaseHelper.ScoresAnswers.TABLE_NAME + "." + ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_ANSWER;
			String ScoresAnswersScoresId = ExaminationDatabaseHelper.ScoresAnswers.TABLE_NAME + "." + ExaminationDatabaseHelper.ScoresAnswers.COLUMN_NAME_SCORES_ID;
		String sqlQuery =  "SELECT * " + 
				//correctAnswersQuestionId + 
				" FROM " 
				+ ExaminationDatabaseHelper.Answers.TABLE_NAME + ", " + ExaminationDatabaseHelper.ScoresAnswers.TABLE_NAME +
				" WHERE "
				+ correctAnswersQuestionId + " = ?" +
				" AND "
				+ ScoresAnswersQuestionId + " = ?" +
				" AND "
				+ ScoresAnswersScoresId + " = ?" +
				" AND "
				+ correctAnswersAnswer + " = " + ScoresAnswersAnswer			
				;
			String[] sqlArgs = new String[] { Long.toString(questionId), Long.toString(questionId)
					, Long.toString(scoresId) };
		    Cursor mCursor = db.rawQuery(sqlQuery, sqlArgs);
		    int count = mCursor.getCount();
		    mCursor.close();
		    return count == correctAnswersCount;
		}
		
		return false;
	}
	
	
	private String createDataseName(String title, long date) {
		return title +"-"+ date;
	}
	
	private boolean databaseExist(String name ) {
		String[] databases = context.databaseList();
		for( int i =  0; i < databases.length; i++ ) {
			if( databases[i].contentEquals(name) ) {
				return true;
			}
		}
	    return false;
	}
}
