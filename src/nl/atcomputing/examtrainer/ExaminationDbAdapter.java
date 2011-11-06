package nl.atcomputing.examtrainer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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

	public long addAnswer(long questionId, String answer) {
	
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID, questionId);
		values.put(ExamTrainer.Answers.COLUMN_NAME_ANSWER, answer);
		return db.insert(ExamTrainer.Answers.TABLE_NAME, null, values);
	}
	
	/**
	 * Adds a new row to Scores table with date set to current date in UTC
	 * and score set to 0
	 * @return _ID of the row that must be used as exam_id in the other tables
	 */
	public long createNewScore() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    	String date = dateFormat.format(new Date());
    	
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.Scores.COLUMN_NAME_DATE, date);
		values.put(ExamTrainer.Scores.COLUMN_NAME_SCORE, 0);
		return db.insert(ExamTrainer.Scores.TABLE_NAME, null, values);
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
	
	public String getQuestionType(long rowId) throws SQLException {
		String type = null;
		Cursor mCursor = db.query(true, ExamTrainer.Questions.TABLE_NAME, 
				new String[] {
				ExamTrainer.Questions.COLUMN_NAME_TYPE
				},
				ExamTrainer.Questions._ID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
			int columnIndex = mCursor.getColumnIndex(ExamTrainer.Questions.COLUMN_NAME_TYPE);
			type = mCursor.getString(columnIndex);
		}
		return type;
	}
	
	public int getQuestionsCount() {
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
	public Cursor getAnswers(long questionId) {
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

	public long addScoresAnswers(long examId, long questionId, String answer) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID, questionId);
		values.put(ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER, answer);
		values.put(ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID, examId);
		return db.insert(ExamTrainer.ScoresAnswers.TABLE_NAME, null, values);
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
	
	/**
	 * Returns the hint if specified for the question
	 * @param questionId _ID of the question 
	 * @return String or null if no hint was not found
	 */
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
	public boolean scoresAnswerPresent(long examId, long questionId, String answer) {
		 String whereClause = ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID + "=" + questionId
				+ " AND " + ExamTrainer.Answers.COLUMN_NAME_ANSWER + "=" + 
				"\"" + answer + "\""
				+ ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID + "=" + examId;
			
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
	public boolean scoresAnswerPresent(long examId, long questionId) {
	
		String whereClause = ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID + "=" + 
		questionId + " AND " + ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID + "=" +
		examId;

		Cursor mCursor = db.query(true, ExamTrainer.ScoresAnswers.TABLE_NAME, 
				new String[] {
				ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER
				}, whereClause, null, null, null, null, null);

		return mCursor.getCount() > 0;
	}

	public boolean updateScoresAnswer(long examId, long questionId, String answer) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER, answer);
		return db.update(ExamTrainer.ScoresAnswers.TABLE_NAME, values, 
				ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID + "=" + questionId
				+ " AND " +
				ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID + "=" + examId, null) > 0;
	}
	
	public boolean insertScoresAnswer(long examId, long questionId, String answer) {
		ContentValues values = new ContentValues();
		values.put(ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID, examId);
		values.put(ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID, questionId);
		values.put(ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER, answer);
		//Log.d(this.getClass().getName(), "insertAnswer values: "+ values.toString());
		return db.insert(ExamTrainer.ScoresAnswers.TABLE_NAME, null, values) != -1;
	}
	
	public boolean deleteScoresAnswer(long examId, long questionId, String answer) {
		return db.delete(ExamTrainer.Answers.TABLE_NAME, 
				ExamTrainer.ScoresAnswers.COLUMN_NAME_QUESTION_ID + "=" + questionId 
				+ " AND " +
				ExamTrainer.ScoresAnswers.COLUMN_NAME_ANSWER + "=" + "\"" + answer + "\""
				+ " AND " + 
				ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID + "=" + examId
				, null) > 0;
	}
	
	/**
	 * @brief Will update the database and set the answer
	 * @param questionId	The rowId of the question answered
	 * @param answer		The answer
	 * @return true if succeeded, false otherwise
	 */
	public boolean setScoresAnswersMultipleChoice(long examId, long questionId, String answer) {	
		//Check if answer is already in the database
		if( ! scoresAnswerPresent(examId, questionId, answer) ) {
			return insertScoresAnswer(examId, questionId, answer);
		}
		return true;
	}
	
	public boolean setScoresAnswersOpen(long examId, long questionId, String answer) {
		//Log.d(this.getClass().getName(), "setOpenAnswer: questionId=" + questionId + 
		//		" answer=" + answer);
		if( scoresAnswerPresent(examId, questionId) ) {
			return updateScoresAnswer(examId, questionId, answer);
		} else {	
			return insertScoresAnswer(examId, questionId, answer);
		}
	}
	
	/**
	 * Checks if a open choice question was answered correctly using a SQL query
	 * with a JOIN over the scores answers table and correct answers table
	 * @param questionId ID of the question you want to check the answers for
	 * @param examId  The ID of the score associated with the exam
	 * @return true if all answers are correct, false otherwise
	 */
	public boolean checkScoresAnswerOpen(long questionId, long examId) {
		String correctAnswersQuestionId = ExamTrainer.Answers.TABLE_NAME + "." + ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID;
		String correctAnswersAnswer = ExamTrainer.Answers.TABLE_NAME + "." + ExamTrainer.Answers.COLUMN_NAME_ANSWER;
		String ScoresAnswersQuestionId = ExamTrainer.ScoresAnswers.TABLE_NAME + "." + ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID;
		String ScoresAnswersAnswer = ExamTrainer.ScoresAnswers.TABLE_NAME + "." + ExamTrainer.Answers.COLUMN_NAME_ANSWER;
		String sqlQuery = "SELECT " 
			+ correctAnswersQuestionId + 
			" FROM " 
			+ ExamTrainer.Answers.TABLE_NAME + ", " + ExamTrainer.ScoresAnswers.TABLE_NAME +
			" WHERE "
			+ correctAnswersQuestionId + 
			" = "
			+ ScoresAnswersQuestionId +
			" AND "
			+ correctAnswersAnswer +
			" = "
			+ ScoresAnswersAnswer +
			" AND "
			+ ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID + " = " + examId
			;
		Cursor mCursor = db.rawQuery(sqlQuery, null);
		if (mCursor != null) {
			int count = mCursor.getCount();
			mCursor.close();
			return count > 0;
		}
		return false;
	}
	
	/**
	 * Checks if a multiple choice question was answered correctly using a SQL query
	 * with a JOIN over the scores answers table and correct answers table
	 * @param questionId ID of the question you want to check the answers for
	 * @param examId  The ID of the score associated with the exam
	 * @return true if all answers are correct, false otherwise
	 * 
	 * SELECT Answers.question_id FROM Answers, ScoresAnswers 
	 * WHERE Answers.question_id = ScoresAnswers.question_id
	 * AND CorrectAnswers.answer =  ScoresAnswers.answer
	 * AND ScoresAnswers.exam_id = examId;
	 */
	public boolean checkScoresAnswersMultipleChoice(long questionId, long examId) {
		int answersCount = getAnswers(questionId).getCount();
		int correctAnswersCount = getAnswers(questionId).getCount();
		if ( answersCount == correctAnswersCount ) {
		String correctAnswersQuestionId = ExamTrainer.Answers.TABLE_NAME + "." + ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID;
		String correctAnswersAnswer = ExamTrainer.Answers.TABLE_NAME + "." + ExamTrainer.Answers.COLUMN_NAME_ANSWER;
		String ScoresAnswersQuestionId = ExamTrainer.ScoresAnswers.TABLE_NAME + "." + ExamTrainer.Answers.COLUMN_NAME_QUESTION_ID;
		String ScoresAnswersAnswer = ExamTrainer.ScoresAnswers.TABLE_NAME + "." + ExamTrainer.Answers.COLUMN_NAME_ANSWER;
		String sqlQuery = "SELECT " 
			+ correctAnswersQuestionId + 
			" FROM " 
			+ ExamTrainer.Answers.TABLE_NAME + ", " + ExamTrainer.ScoresAnswers.TABLE_NAME +
			" WHERE "
			+ correctAnswersQuestionId + 
			" = "
			+ ScoresAnswersQuestionId +
			" AND "
			+ correctAnswersAnswer +
			" = "
			+ ScoresAnswersAnswer +
			" AND "
			+ ExamTrainer.ScoresAnswers.COLUMN_NAME_EXAM_ID + " = " + examId
			;
		    Cursor mCursor = db.rawQuery(sqlQuery, null);
		    int count = mCursor.getCount();
		    mCursor.close();
		    return count == correctAnswersCount;
		}
		
		return false;
	}
	
	/**
	 * Returns the amount of saved answers for a exam with given ID 
	 * @param examId the _ID of the exam
	 * @return amount of answers for exam with ID examId
	 */
	public int getScoresAnswersCount(long examId) {
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