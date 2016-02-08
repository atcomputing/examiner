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

import java.util.ArrayList;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExaminationDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

/**
 * @author martijn brekhof
 *
 */
public class ExamQuestion {
	public static final String TYPE_OPEN = "open";
	public static final String TYPE_MULTIPLE_CHOICE = "multiple choice";
	
	/**
	 * Specifies the type of this question
	 */
	private String type;
	/**
	 * Specifies the category this question belongs to
	 */
	private String topic;
	/**
	 * The actual question
	 */
	private String question;
	/**
	 * A possible example the question refers too
	 */
	private String exhibit;
	/**
	 * A hint for the user regarding the question 
	 */
	private String hint;
	/**
	 * Correct answers to the question
	 */
	private ArrayList<String> answers;
	/**
	 * The choices when type is multiple choice
	 */
	private ArrayList<String> choices;
	
	private Context context;
	
	public ExamQuestion(Context context) {
		type = TYPE_MULTIPLE_CHOICE;
		topic = null;
		question = context.getString(R.string.No_question_available);
		answers = new ArrayList<String>();	//
		choices = new ArrayList<String>();
		hint = context.getString(R.string.hint_not_available);
		exhibit = null;
		
		this.context = context;
	}
	
	/**
	 * @brief Creates a new ExamQuestion object
	 * @param type Specifies the type of this question
	 * @param topic Specifies the category this question belongs to
	 * @param exhibit A possible example the question refers too
	 * @param question The actual question
	 * @param answers Correct answers to the question
	 * @param choices The choices when type is multiple choice
	 * @param hint A hint for the user regarding the question 
	 */
	public ExamQuestion(String type, String topic, String exhibit, String question, 
			ArrayList<String> answers, ArrayList<String> choices, String hint) {
		this.type = type;
		this.topic = topic;
		this.question = question;
		this.answers = answers;
		this.choices = choices;
		this.hint = hint;
		this.exhibit = exhibit;
	}
	
	public String convertArrayListToString(ArrayList<String> choices) {
		int size = choices.size();
		StringBuilder stringBuilder = new StringBuilder();
		
		for ( int i = 0; i < size; i++ ) {
			stringBuilder.append(choices.get(i) + ", ");
		}
		return stringBuilder.toString().replaceAll(", ^", "");
	}
	
	public String getType() {
		return type;
	}
	
	public String getTopic() {
		return topic;
	}
	
	public ArrayList<String> getChoices() {
		return choices;
	}
	
	/**
	 * @return the list of correct answers to this question
	 */
	public ArrayList<String> getAnswers() {
		return answers;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public String getExhibit() {
		return exhibit;
	}
	
	public String getHint() {
		return hint;
	}
	
	public void setType(String str) {
		type = str;
	}
	
	public void setTopic(String str) {
		topic = str;
	}
	
	public void setQuestion(String str) {
		question = str;
	}

	public void setExhibit(String str) {
		exhibit = str;
	}
	
	public void setHint(String str) {
		hint = str;
	}
	public void addChoice(String str) {
		choices.add(str);
	}
	
	public void addAnswer(String str) {
		answers.add(str);
	}
	
	public void addToDatabase(ExaminationDbAdapter examinationDbHelper) {
		ArrayList<String> arrayList;
		long questionId = examinationDbHelper.addQuestion(this);
		
		arrayList = this.getChoices();
		for( int i = 0; i < arrayList.size(); i++ ) {
			examinationDbHelper.addChoice(questionId, arrayList.get(i));
		}
		
		arrayList = this.getAnswers();
		for( int i = 0; i < arrayList.size(); i++ ) {
			examinationDbHelper.addAnswer(questionId, arrayList.get(i));
		}
	}
	
	public ExamQuestion fillFromDatabase(String databaseName, long questionId) 
			throws SQLiteException {
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this.context);
		try {
			examinationDbHelper.open(databaseName);
		} catch (SQLiteException e) {
			throw e;
		}
		
		Cursor cursor = examinationDbHelper.getQuestion(questionId);
		if( cursor.getCount() < 1 ) {
			cursor.close();
			return null;
		}
		
		int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Questions.COLUMN_NAME_TYPE);
		this.type = cursor.getString(index);
		index = cursor.getColumnIndex(ExaminationDatabaseHelper.Questions.COLUMN_NAME_EXHIBIT);
		this.exhibit = cursor.getString(index);
		index = cursor.getColumnIndex(ExaminationDatabaseHelper.Questions.COLUMN_NAME_QUESTION);
		this.question = cursor.getString(index);
		
		cursor.close();
		
		
		if( this.type.equalsIgnoreCase(TYPE_MULTIPLE_CHOICE) ) {
			cursor = examinationDbHelper.getChoices(questionId);
			if ( cursor.getCount() > 0 ) {
				index = cursor.getColumnIndex(ExaminationDatabaseHelper.Choices.COLUMN_NAME_CHOICE);
				do {
					this.choices.add(cursor.getString(index));
				} while( cursor.moveToNext() );
			}
		}
		cursor.close();
		
		examinationDbHelper.close();
		
		return this;
	}
	
	public String toString() {
		return this.question;
	}
}