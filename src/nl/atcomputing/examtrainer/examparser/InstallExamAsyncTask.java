package nl.atcomputing.examtrainer.examparser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.activities.ExamQuestion;
import nl.atcomputing.examtrainer.activities.ExamTrainer;
import nl.atcomputing.examtrainer.database.DatabaseManager;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.content.Context;
import android.content.Intent;
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
	private long examID;
	private String examTitle;
	private long examDate;
	private TextView tvProgress;
	private String url;

	public InstallExamAsyncTask(Context context, TextView progress, long examID) {
		super();
		this.examID = examID;
		this.context = context;
		this.tvProgress = progress;
	}

	public long getExamID() {
		return this.examID;
	}

	public void setProgressTextView(TextView tv) {
		this.tvProgress = tv;
	}

	protected void onPreExecute() {
		if( this.tvProgress != null ) {
			this.tvProgress.setText(R.string.Installing_exam);
		}

		ExamTrainerDbAdapter examTrainerDbHelperAdapter = new ExamTrainerDbAdapter(this.context);
		examTrainerDbHelperAdapter.open();
		if(! examTrainerDbHelperAdapter.setInstallationState(this.examID, ExamTrainerDbAdapter.State.INSTALLING)) {
			Toast.makeText(this.context, "Failed to set exam " + this.examID + " to state installing.", Toast.LENGTH_LONG).show();
		}
		this.examDate = System.currentTimeMillis();

		this.examTitle = examTrainerDbHelperAdapter.getExamTitle(this.examID);

		this.url = examTrainerDbHelperAdapter.getURL(this.examID);

		examTrainerDbHelperAdapter.setExamInstallationDate(this.examID, this.examDate);
		examTrainerDbHelperAdapter.close();

		ExamTrainer.addInstallationThread(this.examID, this);
	}

	protected String doInBackground(String... dummy) {
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
				percentage = (int) (100 * (count/(double )total));
				ExamTrainer.setExamInstallationProgression(this.examID, percentage);
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
		if( errorMessage.contentEquals("") ) {

			ExamTrainerDbAdapter examTrainerDbHelperAdapter = new ExamTrainerDbAdapter(this.context);
			examTrainerDbHelperAdapter.open();
			if(! examTrainerDbHelperAdapter.setInstallationState(this.examID, ExamTrainerDbAdapter.State.INSTALLED)) {
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

	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		DatabaseManager dm = new DatabaseManager(this.context);
		dm.deleteExam(this.examID);

		ExamTrainer.removeInstallationThread(this.examID);
	}

}
