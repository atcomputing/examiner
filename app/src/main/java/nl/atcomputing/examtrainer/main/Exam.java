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

package nl.atcomputing.examtrainer.main;

import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import android.content.Context;
import android.database.Cursor;


/**
 * @author martijn brekhof
 *
 */
public class Exam {
	
	public static enum State {
		INSTALLING, INSTALLED, NOT_INSTALLED
	}
	
	private String title;
	private String category;
	private String author;
	private String URL;
	private String courseURL;
	private int numberofitems;
	private int itemsneededtopass;
	private long timelimit;
	private long installationDate;
	private State installationState;
	private long examID;
	
	/**
	 * Creates a new Exam object from information available in the database
	 * @param context
	 * @param examID the identifier (_ID) of the row in the database holding the exam information
	 * @return null if exam with given examID is not in the database
	 */
	public static Exam newInstance(Context context, long examID) {
		Exam exam = new Exam();
		
		exam.setExamID(examID);
		
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getExam(examID);
		
		if( ! cursor.moveToFirst() ) {
			return null;
		}
		
		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		exam.setTitle(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_CATEGORY);
		exam.setCategory(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR);
		exam.setAuthor(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_URL);
		exam.setURL(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_COURSEURL);
		exam.setCourseURL(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS);
		exam.setNumberOfItems(cursor.getInt(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
		exam.setItemsNeededToPass(cursor.getInt(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT);
		exam.setTimeLimit(cursor.getLong(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
		exam.setInstallationDate(cursor.getLong(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
		String state = cursor.getString(index);
		exam.setInstallationState(State.valueOf(state));
		
		cursor.close();
		examTrainerDbHelper.close();
		
		return exam;
	}
	
	public static Exam newInstance(Cursor cursor) {
		Exam exam = new Exam();
		
		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
		exam.setExamID(cursor.getLong(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		exam.setTitle(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_CATEGORY);
		exam.setCategory(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR);
		exam.setAuthor(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_URL);
		exam.setURL(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_COURSEURL);
		exam.setCourseURL(cursor.getString(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS);
		exam.setNumberOfItems(cursor.getInt(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
		exam.setItemsNeededToPass(cursor.getInt(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT);
		exam.setTimeLimit(cursor.getLong(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
		exam.setInstallationDate(cursor.getLong(index));
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
		String state = cursor.getString(index);
		exam.setInstallationState(State.valueOf(state));
		
		return exam;
	}
	
	public Exam() {
		title = null;
		category = null;
		author = null;
		URL = null;
		courseURL = null;
		numberofitems = 0;
		itemsneededtopass = 0;
		timelimit = 0;
		installationDate = 0;
		examID = 0;
	}
	
	public long getExamID() {
		return examID;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getURL() {
		return URL;
	}
	
	public String getCourseURL() {
		return courseURL;
	}
	
	public int getNumberOfItems() {
		return numberofitems;
	}
	
	public int getItemsNeededToPass() {
		return itemsneededtopass;
	}
	
	public long getTimeLimit() {
		return timelimit;
	}
	
	public long getInstallationDate() {
		return installationDate;
	}
	
	public State getInstallationState() {
		return installationState;
	}
	
	public void setExamID(long examID) {
		this.examID = examID;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public void setURL(String url) {
		this.URL = url;
	}
	
	public void setCourseURL(String url) {
		this.courseURL = url;
	}
	
	public void setNumberOfItems( int n ) {
		this.numberofitems = n;
	}
	
	public void setItemsNeededToPass( int n ) {
		this.itemsneededtopass = n;
	}
	
	public void setTimeLimit(long t) {
		this.timelimit = t;
	}
	
	public void setInstallationDate(long epoch) {
		this.installationDate = epoch;
	}
	
	public void setInstallationState(State state) {
		this.installationState = state;
	}
	
	public long addToDatabase(Context context) {
		
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
		examTrainerDbHelper.open();
		
		long rowId = examTrainerDbHelper.addExam(this);

		examTrainerDbHelper.close();
		
		return rowId;
	}
}