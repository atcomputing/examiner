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
public class ExamTrainerDatabaseHelper extends SQLiteOpenHelper {
	private static String DATABASE_NAME = "ExamTrainer.db";
	private static final int DATABASE_VERSION = 2;

	private static final String DATABASE_CREATE_EXAMS_TABLE = "CREATE TABLE "
															  + Exams.TABLE_NAME + " ("
															  + Exams._ID + " INTEGER PRIMARY KEY,"
															  + Exams.COLUMN_NAME_EXAMTITLE + " TEXT,"
															  + Exams.COLUMN_NAME_DATE + " INTEGER,"
															  + Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS + " INTEGER,"
															  + Exams.COLUMN_NAME_AMOUNTOFITEMS + " INTEGER,"
															  + Exams.COLUMN_NAME_INSTALLED + " TEXT,"
															  + Exams.COLUMN_NAME_URL + " TEXT,"
															  + Exams.COLUMN_NAME_COURSEURL + " TEXT,"
															  + Exams.COLUMN_NAME_AUTHOR + " TEXT,"
															  + Exams.COLUMN_NAME_CATEGORY + " TEXT,"
															  + Exams.COLUMN_NAME_TIMELIMIT + " INTEGER"
															  + ");";

	private static final String DATABASE_CREATE_USAGEDIALOGS_TABLE = "CREATE TABLE "
																	 + UsageDialogs.TABLE_NAME + " ("
																	 + UsageDialogs._ID + " INTEGER PRIMARY KEY,"
																	 + UsageDialogs.COLUMN_NAME_MSGID + " INTEGER,"
																	 + UsageDialogs.COLUMN_NAME_SHOW + " INTEGER"
																	 + ");";

	public static final class Exams implements BaseColumns {
		public static final String TABLE_NAME = "Exams";
		public static final String COLUMN_NAME_EXAMTITLE = "examTitle";
		public static final String COLUMN_NAME_DATE = "date";
		public static final String COLUMN_NAME_ITEMSNEEDEDTOPASS = "itemsNeededToPass";
		public static final String COLUMN_NAME_AMOUNTOFITEMS = "amountOfItems";
		public static final String COLUMN_NAME_INSTALLED = "installed";
		public static final String COLUMN_NAME_URL = "URL";
		public static final String COLUMN_NAME_COURSEURL = "courseURL";
		public static final String COLUMN_NAME_AUTHOR = "author";
		public static final String COLUMN_NAME_CATEGORY = "category";
		public static final String COLUMN_NAME_TIMELIMIT = "timelimit";

		private Exams() {}
	}

	public static final class UsageDialogs implements BaseColumns {
		public static final String TABLE_NAME = "UsageDialogs";
		public static final String COLUMN_NAME_MSGID = "messageResourceId";
		public static final String COLUMN_NAME_SHOW = "show";

		private UsageDialogs() {}
	}

	public ExamTrainerDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE_EXAMS_TABLE);
		db.execSQL(DATABASE_CREATE_USAGEDIALOGS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch( oldVersion ) {
			case 1:
				Log.w(ExamTrainerDatabaseHelper.class.getName(),
					  "Upgrading database from version 1 to 2");
				db.execSQL(DATABASE_CREATE_USAGEDIALOGS_TABLE);
				break;
			default:
		}
	}

	public String getDatabaseName() {
		return DATABASE_NAME;
	}
}