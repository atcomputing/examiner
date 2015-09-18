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

package nl.atcomputing.examtrainer.examparser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.DatabaseManager;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.main.Exam;
import nl.atcomputing.examtrainer.main.ExamQuestion;
import nl.atcomputing.examtrainer.main.ExamTrainer;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

public class InstallExamAsyncTask extends AsyncTask<String, Integer, String> {
	private Context context;
	private long examID = -1;
	private String examTitle;
	private long examDate;
	private TextView tvProgress;
	private String url;
	private Cursor cursor;

	/**
	 * Installs the exam as defined by the examsCursor which should be one or more rows from 
	 * the examTrainer database. 
	 * @param context
	 * @param progress textview to show progress information when installing an exam
	 * @param examID exam Identifier
	 */
	public InstallExamAsyncTask(Context context, TextView progress, long examID) {
		super();
		this.examID = examID;
		this.context = context;
		this.tvProgress = progress;
	}

	/**
	 * Installs the exam as defined by the examsCursor which should be one or more rows from 
	 * the examTrainer database. 
	 * @param context
	 * @param progress textview to show progress information when installing an exam
	 * @param examsCursor one or more rows from the examTrainer database. If null all not 
	 *        installed exams will be installed. Cursor must contain the examId.
	 */
	public InstallExamAsyncTask(Context context, TextView progress, Cursor examsCursor) {
		super();
		this.context = context;
		this.tvProgress = progress;
		this.cursor = examsCursor;
	}

	public long getExamID() {
		return this.examID;
	}

	public void setProgressTextView(TextView tv) {
		this.tvProgress = tv;
	}

	protected void onPreExecute() {
		ExamTrainerDbAdapter examTrainerDbHelperAdapter = new ExamTrainerDbAdapter(context);
		examTrainerDbHelperAdapter.open();

		//Check if we are called in cursor mode
		if( this.examID == -1 ) {
			if( this.cursor == null ) {
				this.cursor = examTrainerDbHelperAdapter.getNotInstalledExams();
			}

			if( this.cursor.getPosition() == -1 ) {
				if( this.cursor.moveToFirst() ) {
					this.examID = examTrainerDbHelperAdapter.getExamId(this.cursor);
				}
			} else {
				this.examID = examTrainerDbHelperAdapter.getExamId(this.cursor);
			}
		}

		if( this.examID == -1 ) {
			if( this.tvProgress != null ) {
				this.tvProgress.setText(R.string.failed_to_install_exam);
				examTrainerDbHelperAdapter.close();
				return;
			}
		} else {
			if( this.tvProgress != null ) {
				this.tvProgress.setText(R.string.Installing_exam);
			}
		}

		if(! examTrainerDbHelperAdapter.setInstallationState(this.examID, Exam.State.INSTALLING)) {
			Log.w("InstallExamAsyncTask", "Failed to set exam " + this.examID + " to state installing.");
		}
		this.examDate = System.currentTimeMillis();

		this.examTitle = examTrainerDbHelperAdapter.getExamTitle(this.examID);

		this.url = examTrainerDbHelperAdapter.getURL(this.examID);

		examTrainerDbHelperAdapter.setExamInstallationDate(this.examID, this.examDate);
		examTrainerDbHelperAdapter.close();

		ExamTrainer.addInstallationThread(this.examID, this);

		//Sent notification to interested activities that examlist is updated
		Intent intent=new Intent();
		intent.setAction(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
		this.context.sendBroadcast(intent);
	}

	protected String doInBackground(String... dummy) {
		if( this.examID == -1 ) {
			return "";
		}

		String returnMessage = "";

		try {
			URL url = new URL(this.url);
			XmlPullExamParser xmlPullFeedParser = new XmlPullExamParser(this.context, url);
			xmlPullFeedParser.parseExam();

			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this.context);
			examinationDbHelper.open(this.examTitle, this.examDate);

			ArrayList<ExamQuestion> examQuestions = xmlPullFeedParser.getExam();

			int total = examQuestions.size();
			int count = 0;
			int percentage = 0;

			for( ExamQuestion examQuestion: examQuestions ) {
				if( isCancelled() ) {
					break;
				}
				examQuestion.addToDatabase(examinationDbHelper);
				count++;
				percentage = (int) (100 * (count/(double)total));
				publishProgress(percentage);
			}

			examinationDbHelper.close();

		} catch (SQLiteException e) {
			returnMessage = this.context.getString(R.string.failed_to_install_exam);
			Log.w("InstallExamAsyncTask", returnMessage+"\n"+e.getMessage());
		} catch (RuntimeException e) {
			returnMessage = this.context.getString(R.string.error_parsing_exam);
			Log.w("InstallExamAsyncTask", returnMessage+"\n"+e.getMessage());
		} catch (MalformedURLException e) {
			returnMessage = context.getString(R.string.error_url_is_not_correct) + " " + this.url;
			Log.w("InstallExamAsyncTask", returnMessage+"\n"+e.getMessage());
		} catch (Exception e) {
			returnMessage = e.getMessage();
			Log.w("InstallExamAsyncTask", e.getMessage());
		}	
		return returnMessage;
	}

	protected void onProgressUpdate(Integer... progress) {
		if( this.tvProgress != null ) {
			this.tvProgress.setText(progress[0] + "%");
		}
	}

	protected void onPostExecute(String errorMessage) {
		if( this.examID == -1 ) {
			return;
		}
		if( errorMessage.contentEquals("") ) {

			ExamTrainerDbAdapter examTrainerDbHelperAdapter = new ExamTrainerDbAdapter(this.context);
			examTrainerDbHelperAdapter.open();
			if(! examTrainerDbHelperAdapter.setInstallationState(this.examID, Exam.State.INSTALLED)) {
				Toast.makeText(this.context, "Failed to set exam " + this.examTitle + " to installed.", Toast.LENGTH_LONG).show();
			}
			examTrainerDbHelperAdapter.close();

		} else {
			Toast.makeText(this.context, errorMessage, Toast.LENGTH_LONG).show();
			DatabaseManager dm = new DatabaseManager(this.context);
			dm.deleteExam(this.examID);
		}

		ExamTrainer.removeInstallationThread(this.examID);

		//Sent notification to interested activities that examlist is updated
		Intent intent=new Intent();
		intent.setAction(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
		this.context.sendBroadcast(intent);

		//Install next exam
		if( this.cursor.moveToNext() ) {
			InstallExamAsyncTask task = new InstallExamAsyncTask(this.context, null, this.cursor);
			task.execute();
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		DatabaseManager dm = new DatabaseManager(this.context);
		dm.deleteExam(this.examID);

		ExamTrainer.removeInstallationThread(this.examID);
	}

}
