package nl.atcomputing.examtrainer.examparser;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nl.atcomputing.examtrainer.activities.ExamTrainer;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */

public class InstallAllExamsAsyncTask extends AsyncTask<String, Integer, String> {
	private Context context;
	private Cursor cursor;

	public InstallAllExamsAsyncTask(Context context) {
		super();
		this.context = context;
	}

	protected void onPreExecute() {
		ExamTrainerDbAdapter examTrainerDbHelperAdapter = new ExamTrainerDbAdapter(this.context);
		examTrainerDbHelperAdapter.open();
		this.cursor = examTrainerDbHelperAdapter.getNotInstalledExams();
		Log.d("InstallAllExamsAsyncTask", "onPreExecute: this.cursor.getCount()="+this.cursor.getCount());
		examTrainerDbHelperAdapter.close();
	}

	protected String doInBackground(String... dummy) {
		while((cursor.moveToNext()) && (! isCancelled() ) ) {
			int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
			long examID = cursor.getLong(index);
			Log.d("InstallAllExamsAsyncTask", "doInBackground: installing examID="+examID);
			InstallExamAsyncTask task = new InstallExamAsyncTask(this.context, null, examID);
			task.execute();

			//Sent notification to interested activities that examlist is updated
			Intent intent=new Intent();
			intent.setAction(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
			this.context.sendBroadcast(intent);
			
			try {
				task.get();
			} catch (InterruptedException e) {
				break;
			} catch (ExecutionException e) {
				Log.d("InstallAllExamsAsyncTask", "Problem installing examID="+examID+"\n"+e.getMessage());
			}
		}

		return null;
	}
}
