package nl.atcomputing.examtrainer.adapters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import nl.atcomputing.dialogs.DialogFactory;
import nl.atcomputing.examtrainer.ExamQuestion;
import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.manage.InstallExamAsyncTask;
import nl.atcomputing.examtrainer.manage.XmlPullExamParser;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

public class ManageExamsAdapter extends BaseAdapter  {
	private Context context;
	private int layout;
	private Cursor cursor;

	public ManageExamsAdapter(Context context, int layout, Cursor c) {

		this.context = context;

		this.layout = layout;

		this.cursor = c;
	}

	public int getCount() {
		if( this.cursor != null ) {
			return cursor.getCount();
		} else {
			return 0;
		}
	}

	public Object getItem(int position) {
		if( (cursor.moveToPosition(position) ) && ( ! cursor.isClosed() ) ){
			return cursor;
		} else {
			return null;
		}
	}

	public long getItemId(int position) {
		if( ( cursor.move(position) )  && ( ! cursor.isClosed() ) ) {
			int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
			return cursor.getLong(index);
		} else {
			return 0;
		}
	}

	public View getView(int position, View view, ViewGroup parent) {
		Log.d("ManageExamsAdapter", "getView("+position+", "+view+", "+parent+")");
		final ViewHolder holder = new ViewHolder();

		if ( view == null )
		{  
			final LayoutInflater mInflater = LayoutInflater.from(this.context);
			view = (View) mInflater.inflate(this.layout, parent, false);
		}

		holder.examTitleView = (TextView) view.findViewById(R.id.manageExamsEntryTitle);
		holder.examAuthorView = (TextView) view.findViewById(R.id.manageExamsEntryAuthor);
		holder.installUninstallButton = (Button) view.findViewById(R.id.manageExamsDelete);

		InstallExamAsyncTask installExam = ExamTrainer.getInstallExamAsyncTask(holder.examID);
		if( installExam != null ) {
			Log.d("ManageExamsAdapter", "Connecting existing thread to view");
			installExam.setView(holder.installUninstallButton);
		}
		
		Cursor row = (Cursor) getItem(position);
		if( cursor == null ) {
			Log.d("ManageExamsAdapter", "Cursor null");
			return view;
		}

		int index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		holder.examTitle = row.getString(index);
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
		holder.examDate = row.getLong(index);
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
		holder.examID = row.getLong(index);
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_URL);
		holder.url = row.getString(index);
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS);
		holder.examAmountOfItems = row.getInt(index);
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
		holder.examItemsNeededToPass = row.getInt(index);
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR);
		holder.author = row.getString(index);
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT);
		holder.timeLimit = row.getLong(index);


		holder.examTitleView.setText(holder.examTitle);

		holder.examAuthorView.setText(holder.author);

		holder.installUninstallButton.setEnabled(true);

		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
		String state = row.getString(index);
		if( state.contentEquals(ExamTrainerDbAdapter.State.NOT_INSTALLED.name()) ) {
			holder.installUninstallButton.setText(R.string.install);
		} else if ( state.contentEquals(ExamTrainerDbAdapter.State.INSTALLED.name()) ) {
			holder.installUninstallButton.setText(R.string.uninstall);
		} else if ( state.contentEquals(ExamTrainerDbAdapter.State.INSTALLING.name()) ) {
			holder.installUninstallButton.setText(R.string.Installing_exam);
			holder.installUninstallButton.setEnabled(false);
		} else {
			holder.installUninstallButton.setText(R.string.install);
		}

		holder.installUninstallButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				handleButtonClick(holder);
			}
		});

		view.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				StringBuffer strBuf = new StringBuffer();

				strBuf.append(holder.examTitle + "\n");

				strBuf.append(context.getString(R.string.questions) + 
						": " +  holder.examAmountOfItems + "\n" +
						context.getString(R.string.correct_answer_required_to_pass) +
						": " +  holder.examItemsNeededToPass + "\n");

				if ( holder.timeLimit == 0 ) {
					strBuf.append(context.getString(R.string.No_time_limit));
				} else {
					strBuf.append(context.getString(R.string.Time_limit_in_minutes) + ": " + holder.timeLimit
							+ " " + context.getString(R.string.minutes));
				}

				Toast.makeText(context,  strBuf.toString(), Toast.LENGTH_LONG).show();
			}
		});

		return view;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void cancelRunningInstallations() {
		Collection<InstallExamAsyncTask> values = ExamTrainer.getAllgetInstallExamAsyncTasks();
		for( InstallExamAsyncTask task : values ) {
			task.cancel(false);
		}
	}

	private void handleButtonClick(final ViewHolder holder) {
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
		examTrainerDbHelper.open();
		ExamTrainerDbAdapter.State state = examTrainerDbHelper.getInstallationState(holder.examID);
		examTrainerDbHelper.close();
		if( state == ExamTrainerDbAdapter.State.INSTALLED ) {
			Dialog dialog = DialogFactory.createTwoButtonDialog(this.context, 
					R.string.are_you_sure_you_want_to_uninstall_this_exam, 
					R.string.uninstall, new Runnable() {

				public void run() {
					holder.installUninstallButton.setEnabled(false);
					new UninstallExam(holder, context).execute();
				}
			}, R.string.cancel, new Runnable() {

				public void run() {

				}
			});
			dialog.show();
		} else {
			holder.installUninstallButton.setEnabled(false);
			if( ExamTrainer.getInstallExamAsyncTask(holder.examID) == null ) {
				
				try {
					URL url = new URL(holder.url);
					XmlPullExamParser xmlPullFeedParser = new XmlPullExamParser(context, url);
					xmlPullFeedParser.parseExam();
					ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(context);
					examinationDbHelper.open(holder.examTitle, holder.examDate);

					ArrayList<ExamQuestion> examQuestions = xmlPullFeedParser.getExam();

					InstallExamAsyncTask installExam = new InstallExamAsyncTask(context, 
							holder.installUninstallButton, (int) holder.examID, examQuestions);
					ExamTrainer.addInstallationThread(holder.examID, installExam);
					installExam.execute();
					examinationDbHelper.close();
				}
				catch (MalformedURLException e) {
					String message = context.getString(R.string.error_url_is_not_correct) + " " + holder.url;
					Log.d("ManageExamsAdapter", message+"\n"+e.getMessage());
				}		
				
			}
		}
	}

	class ViewHolder {
		String examTitle;
		long examID;
		long examDate;
		String url;
		String author;
		int examAmountOfItems;
		int examItemsNeededToPass;
		long timeLimit;
		TextView examTitleView;
		TextView examAuthorView;
		Button installUninstallButton;
		View view;
	}



	private class UninstallExam extends AsyncTask<ViewHolder, Integer, Boolean> {
		private ViewHolder holder;
		private Context context;

		public UninstallExam(ViewHolder viewHolder, Context context) {
			super();
			this.holder = viewHolder;
			this.context = context;
		}

		protected void onPreExecute() {
			this.holder.installUninstallButton.setEnabled(false);
		}

		protected Boolean doInBackground(ViewHolder... holders) {
			ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
			examTrainerDbHelper.open();
			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(context);
			if( examinationDbHelper.delete(this.holder.examTitle, this.holder.examDate) )  {
				if( ! examTrainerDbHelper.setInstallationState(this.holder.examID, ExamTrainerDbAdapter.State.NOT_INSTALLED) ) {
					Toast.makeText(context, context.getString(R.string.Failed_to_uninstall_exam) + 
							this.holder.examTitle, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(context, context.getString(R.string.Could_not_remove_exam_database_file) + 
						this.holder.examTitle, Toast.LENGTH_LONG).show();
			}
			examTrainerDbHelper.close();
			return true;
		}

		protected void onProgressUpdate(Integer... progress) {
			//setProgressPercent(progress[0]);
		}

		protected void onPostExecute(Boolean result) {
			this.holder.installUninstallButton.setEnabled(true);
			//updateView();
			ManageExamsAdapter.this.notifyDataSetChanged();
			//Sent notification to interested activities that examlist is updated
			Intent intent=new Intent();
			intent.setAction(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
			this.context.sendBroadcast(intent);
		}
	}


}
