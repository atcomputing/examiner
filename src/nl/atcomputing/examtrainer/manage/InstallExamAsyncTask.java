package nl.atcomputing.examtrainer.manage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.examtrainer.ExamQuestion;
import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.adapters.ManageExamsAdapter;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

public class InstallExamAsyncTask extends AsyncTask<String, Integer, String> {
	private Context context;
	private TextView textview;
	private int examID;
	private String examTitle;
	private long examDate;
	private ArrayList<ExamQuestion> questions;
	
	public InstallExamAsyncTask(Context context, TextView view, int examID, ArrayList<ExamQuestion> questions) {
		super();
		this.context = context;
		this.textview = view;
		this.examID = examID;
		this.questions = questions;
	}

	public void setView(TextView view) {
		this.textview = view;
	}

	protected void onPreExecute() {
		ExamTrainerDbAdapter examTrainerDbHelperAdapter = new ExamTrainerDbAdapter(context);
		examTrainerDbHelperAdapter.open();
		if(! examTrainerDbHelperAdapter.setInstallationState(this.examID, ExamTrainerDbAdapter.State.INSTALLING)) {
			Toast.makeText(context, "Failed to set exam " + this.examID + " to state installing.", Toast.LENGTH_LONG).show();
		}
		this.examTitle = examTrainerDbHelperAdapter.getExamTitle(this.examID);

		examTrainerDbHelperAdapter.close();

		examDate = System.currentTimeMillis();

		ExamTrainer.addInstallationThread(this.examID, this);
	}

	protected String doInBackground(String... dummy) {
		String returnMessage = "";
		int total = this.questions.size();
		int count = 0;
		int percentage = 0;
		try {

			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(context);
			examinationDbHelper.open(this.examTitle, examDate);

			for( ExamQuestion examQuestion: this.questions ) {
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
			returnMessage = context.getString(R.string.failed_to_install_exam);
			Log.d("ManageExamsAdapter", returnMessage+"\n"+e.getMessage());
		} catch (RuntimeException e) {
			returnMessage = context.getString(R.string.error_parsing_exam);
			Log.d("ManageExamsAdapter", returnMessage+"\n"+e.getMessage());
		}
		return returnMessage;
	}

	protected void onProgressUpdate(Integer... progress) {
		//Log.d("ManageExamsAdapter", "InstallExam: onProgressUpdate percentage="+progress[0]+", this.holder="+this.holder);
		//this.holder.installUninstallButton = (Button) this.view.findViewById(R.id.manageExamsDelete);
		this.textview.setText(progress[0] + "%");
	}

	protected void onPostExecute(String errorMessage) {
		if( errorMessage.contentEquals("") ) {

			ExamTrainerDbAdapter examTrainerDbHelperAdapter = new ExamTrainerDbAdapter(context);
			examTrainerDbHelperAdapter.open();
			if(! examTrainerDbHelperAdapter.setInstallationState(this.examID, ExamTrainerDbAdapter.State.INSTALLED)) {
				Toast.makeText(context, "Failed to set exam " + this.examTitle + " to installed.", Toast.LENGTH_LONG).show();
			}
			examTrainerDbHelperAdapter.close();

		} else {
			Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
		}

		ExamTrainer.removeInstallationThread(this.examID);

		//Sent notification to interested activities that examlist is updated
		Intent intent=new Intent();
		intent.setAction(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
		this.context.sendBroadcast(intent);

	}

	protected void onCancelled(String result) {
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(context);
		examinationDbHelper.delete(this.examTitle, examDate);

		ExamTrainerDbAdapter examTrainerDbHelperAdapter = new ExamTrainerDbAdapter(context);
		examTrainerDbHelperAdapter.open();
		if(! examTrainerDbHelperAdapter.setInstallationState(this.examID, ExamTrainerDbAdapter.State.NOT_INSTALLED)) {
			Toast.makeText(context, "Failed to set exam " + this.examTitle + " to installed.", Toast.LENGTH_LONG).show();
		}
		examTrainerDbHelperAdapter.close();
	}
}