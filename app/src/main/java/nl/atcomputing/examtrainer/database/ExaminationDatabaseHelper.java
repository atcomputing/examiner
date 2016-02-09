/**
 * 
 * Copyright 2011 AT Computing BV
 *
 * This file is part of Examiner.
 *
 * Examiner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Examiner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Examiner.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.atcomputing.examtrainer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */
public class ExaminationDatabaseHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE_QUESTIONS_TABLE = "CREATE TABLE "
																  + Questions.TABLE_NAME + " ("
																  + Questions._ID + " INTEGER PRIMARY KEY,"
																  + Questions.COLUMN_NAME_QUESTION + " TEXT,"
																  + Questions.COLUMN_NAME_EXHIBIT + " TEXT,"
																  + Questions.COLUMN_NAME_TYPE + " TEXT,"
																  + Questions.COLUMN_NAME_HINT + " TEXT"
																  + ");";

	private static final String DATABASE_CREATE_CHOICES_TABLE = "CREATE TABLE "
																+ Choices.TABLE_NAME + " ("
																+ Choices._ID + " INTEGER PRIMARY KEY,"
																+ Choices.COLUMN_NAME_QUESTION_ID + " INTEGER,"
																+ Choices.COLUMN_NAME_CHOICE + " TEXT"
																+ ");";

	private static final String DATABASE_CREATE_ANSWERS_TABLE = "CREATE TABLE "
																+ Answers.TABLE_NAME + " ("
																+ Answers._ID + " INTEGER PRIMARY KEY,"
																+ Answers.COLUMN_NAME_QUESTION_ID + " INTEGER,"
																+ Answers.COLUMN_NAME_ANSWER + " TEXT"
																+ ");";

	private static final String DATABASE_CREATE_SCORESANSWERS_TABLE = "CREATE TABLE "
																	  + ScoresAnswers.TABLE_NAME + " ("
																	  + ScoresAnswers._ID + " INTEGER PRIMARY KEY,"
																	  + ScoresAnswers.COLUMN_NAME_SCORES_ID + " INTEGER,"
																	  + ScoresAnswers.COLUMN_NAME_QUESTION_ID + " INTEGER,"
																	  + ScoresAnswers.COLUMN_NAME_ANSWER + " TEXT"
																	  + ");";

	private static final String DATABASE_CREATE_SCORES_TABLE = "CREATE TABLE "
															   + Scores.TABLE_NAME + " ("
															   + Scores._ID + " INTEGER PRIMARY KEY,"
															   + Scores.COLUMN_NAME_SCORE + " INTEGER,"
															   + Scores.COLUMN_NAME_DATE + " INTEGER,"
															   + Scores.COLUMN_NAME_EXAM_START + " INTEGER"
															   + ");";

	private static final String DATABASE_CREATE_RESULTPERQUESTION_TABLE = "CREATE TABLE "
																		  + ResultPerQuestion.TABLE_NAME + " ("
																		  + ResultPerQuestion._ID + " INTEGER PRIMARY KEY,"
																		  + ResultPerQuestion.COLUMN_NAME_QUESTION_ID + " INTEGER,"
																		  + ResultPerQuestion.COLUMN_NAME_ANSWER_CORRECT + " INTEGER,"
																		  + ResultPerQuestion.COLUMN_NAME_SCORES_ID + " INTEGER"
																		  + ");";

	public static final class Questions implements BaseColumns {
		public static final String TABLE_NAME = "Questions";

		public static final String TYPE_OPEN = "open";
		public static final String TYPE_MULTIPLECHOICE = "multiplechoice";

		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_QUESTION = "question";
		public static final String COLUMN_NAME_EXHIBIT = "exhibit";
		public static final String COLUMN_NAME_HINT = "hint";

		// This class cannot be instantiated
		private Questions() {}
	}

	public static final class Choices implements BaseColumns {
		public static final String TABLE_NAME = "Choices";
		public static final String COLUMN_NAME_QUESTION_ID = "question_id";
		public static final String COLUMN_NAME_CHOICE = "choice";

		private Choices() {}
	}

	public static final class Answers implements BaseColumns {
		public static final String TABLE_NAME = "Answers";
		public static final String COLUMN_NAME_QUESTION_ID = "question_id";
		public static final String COLUMN_NAME_ANSWER = "answer";

		private Answers() {}

	}

	public static final class Scores implements BaseColumns {
		public static final String TABLE_NAME = "Scores";
		public static final String COLUMN_NAME_DATE = "date";
		public static final String COLUMN_NAME_SCORE = "score";
		public static final String COLUMN_NAME_EXAM_START = "exam_start";

		private Scores() {}

	}

	public static final class ScoresAnswers implements BaseColumns {
		public static final String TABLE_NAME = "ScoresAnswers";
		public static final String COLUMN_NAME_SCORES_ID = "exam_id";
		public static final String COLUMN_NAME_QUESTION_ID = "question_id";
		public static final String COLUMN_NAME_ANSWER = "answer";

		private ScoresAnswers() {}
	}

	public static final class ResultPerQuestion implements BaseColumns {
		public static final String TABLE_NAME = "ResultPerQuestion";
		public static final String COLUMN_NAME_SCORES_ID = "exam_id";
		public static final String COLUMN_NAME_QUESTION_ID = "question_id";
		public static final String COLUMN_NAME_ANSWER_CORRECT = "answered_correct";

		private ResultPerQuestion() {}
	}

	public ExaminationDatabaseHelper(Context context, String databaseName) {
		super(context, databaseName, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_QUESTIONS_TABLE);
		db.execSQL(DATABASE_CREATE_ANSWERS_TABLE);
		db.execSQL(DATABASE_CREATE_CHOICES_TABLE);
		db.execSQL(DATABASE_CREATE_SCORESANSWERS_TABLE);
		db.execSQL(DATABASE_CREATE_SCORES_TABLE);
		db.execSQL(DATABASE_CREATE_RESULTPERQUESTION_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ExaminationDatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + Questions.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Answers.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Choices.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ScoresAnswers.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Scores.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ResultPerQuestion.TABLE_NAME);
		this.onCreate(db);	
	}
}