/**
 * 
 * Copyright 2012 AT Computing BV
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
import android.database.Cursor;
import android.widget.Toast;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.main.Exam;

public class DatabaseManager {
	private Context context;
	
	public DatabaseManager(Context context) {
		this.context = context;
	}
	
	public void deleteExam(long examID) {
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this.context);
		examTrainerDbHelper.open();
		
		Cursor cursor = examTrainerDbHelper.getExam(examID);
		if( ! cursor.moveToFirst() ) {
			return;
		}
		
		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		String examTitle = cursor.getString(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
		Long examDate = cursor.getLong(index);
		
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this.context);
		
		if( examinationDbHelper.delete(examTitle, examDate) )  {
			if( ! examTrainerDbHelper.setInstallationState(examID, Exam.State.NOT_INSTALLED) ) {
				Toast.makeText(this.context, this.context.getString(R.string.Failed_to_uninstall_exam) + 
						examTitle, Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(context, context.getString(R.string.Could_not_remove_exam_database_file) + 
					examTitle, Toast.LENGTH_LONG).show();
		}
		examTrainerDbHelper.close();
	}
}
