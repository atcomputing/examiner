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

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.DatabaseManager;
import nl.atcomputing.examtrainer.main.ExamTrainer;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

public class UninstallExamAsyncTask extends AsyncTask<String, Integer, Boolean> {
	private Context context;
	private long examID;
	private boolean okToUninstall;

	public UninstallExamAsyncTask(Context context, long examID) {
		super();
		this.examID = examID;
		this.context = context;
		this.okToUninstall = false;
	}

	protected void onPreExecute() {
		int count = 0;
		InstallExamAsyncTask task = ExamTrainer.getInstallExamAsyncTask(this.examID);
		if( task != null ) {
			task.cancel(true);
			//Wait for installation thread to finish
			while( ( task.getStatus() == AsyncTask.Status.RUNNING ) && 
					( count++ < 10 ) ){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if( ( count == 10 ) && ( task.getStatus() == AsyncTask.Status.RUNNING ) ){
				Toast.makeText(this.context, R.string.Installation_proces_still_running_refusing_to_uninstall_exam, Toast.LENGTH_LONG).show();
			} else {
				this.okToUninstall = true;
			}
		} else {
			this.okToUninstall = true;
		}
	}

	protected Boolean doInBackground(String... dummy) {
		if( this.okToUninstall ) {
			DatabaseManager dm = new DatabaseManager(this.context);
			dm.deleteExam(this.examID);
			return true;
		}
		return false;
	}

	protected void onProgressUpdate(Integer... progress) {
		//setProgressPercent(progress[0]);
	}

	protected void onPostExecute(Boolean result) {
		Intent intent=new Intent();
		intent.setAction(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
		this.context.sendBroadcast(intent);
	}
}