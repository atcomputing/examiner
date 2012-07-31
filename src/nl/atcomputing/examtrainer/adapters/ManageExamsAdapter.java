package nl.atcomputing.examtrainer.adapters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.dialogs.DialogFactory;
import nl.atcomputing.examtrainer.ExamQuestion;
import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.manage.XmlPullExamParser;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

public class ManageExamsAdapter extends CursorAdapter  {
	private Context gContext;
	private int layout;

	public ManageExamsAdapter(Context context, int layout, Cursor c) {
		super(context, c, false);
		this.gContext = context;
		this.layout = layout;
	}

	public void setContext(Context context) {
		this.gContext = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		setupView(view, cursor);	
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater mInflater = LayoutInflater.from(context);
		View view = (View) mInflater.inflate(layout, parent, false);
		
		//setupView(view, cursor);
		
		return view;
	}

	private void setupView(View view, Cursor cursor) {
		
		final ViewHolder holder = new ViewHolder();
		holder.examTitleView = (TextView) view.findViewById(R.id.manageExamsEntryTitle);
		holder.examAuthorView = (TextView) view.findViewById(R.id.manageExamsEntryAuthor);
		holder.installUninstallButton = (Button) view.findViewById(R.id.manageExamsDelete);

		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		holder.examTitle = cursor.getString(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
		holder.examDate = cursor.getLong(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
		holder.examID = cursor.getLong(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_URL);
		holder.url = cursor.getString(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS);
		holder.examAmountOfItems = cursor.getInt(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
		holder.examItemsNeededToPass = cursor.getInt(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR);
		holder.author = cursor.getString(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT);
		holder.timeLimit = cursor.getLong(index);


		holder.examTitleView.setText(holder.examTitle);

		holder.examAuthorView.setText(holder.author);

		holder.installUninstallButton.setEnabled(true);
		
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
		String state = cursor.getString(index);
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

				strBuf.append(gContext.getString(R.string.questions) + 
						": " +  holder.examAmountOfItems + "\n" +
						gContext.getString(R.string.correct_answer_required_to_pass) +
						": " +  holder.examItemsNeededToPass + "\n");

				if ( holder.timeLimit == 0 ) {
					strBuf.append(gContext.getString(R.string.No_time_limit));
				} else {
					strBuf.append(gContext.getString(R.string.Time_limit_in_minutes) + ": " + holder.timeLimit
							+ " " + gContext.getString(R.string.minutes));
				}

				Toast.makeText(gContext,  strBuf.toString(), Toast.LENGTH_LONG).show();
			}
		});
	}
	
	public void updateView() {
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(gContext);
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getAllExams();
		examTrainerDbHelper.close();
		this.changeCursor(cursor);
		this.notifyDataSetChanged();
	}
	
	private void handleButtonClick(final ViewHolder holder) {
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(gContext);
		examTrainerDbHelper.open();
		ExamTrainerDbAdapter.State state = examTrainerDbHelper.getInstallationState(holder.examID);
		examTrainerDbHelper.close();
		if( state == ExamTrainerDbAdapter.State.INSTALLED ) {
			Dialog dialog = DialogFactory.createTwoButtonDialog(this.gContext, 
					R.string.are_you_sure_you_want_to_uninstall_this_exam, 
					R.string.uninstall, new Runnable() {
						
						public void run() {
							new UninstallExam(holder, gContext).execute();
						}
					}, R.string.cancel, new Runnable() {
						
						public void run() {
							
						}
					});
			dialog.show();
		} 
		else {
			new InstallExam(holder, gContext).execute();
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
	}

	private class InstallExam extends AsyncTask<ViewHolder, Integer, String> {
		private ViewHolder holder;
		private Context context;
		
		public InstallExam(ViewHolder viewHolder, Context context) {
			super();
			this.holder = viewHolder;
			this.context = context;
		}

		protected void onPreExecute() {
			this.holder.installUninstallButton.setEnabled(false);
			this.holder.installUninstallButton.setText(R.string.Installing_exam);
			
			ExamTrainerDbAdapter examTrainerDbHelperAdapter = new ExamTrainerDbAdapter(gContext);
			examTrainerDbHelperAdapter.open();
			if(! examTrainerDbHelperAdapter.setInstallationState(this.holder.examID, this.holder.examDate, ExamTrainerDbAdapter.State.INSTALLING)) {
				Toast.makeText(gContext, "Failed to set exam " + holder.examTitle + " to installing.", Toast.LENGTH_LONG).show();
			}
			examTrainerDbHelperAdapter.close();
		}

		protected String doInBackground(ViewHolder... holders) {
			this.holder.examDate = System.currentTimeMillis();

			try {
				URL url = new URL(this.holder.url);
				XmlPullExamParser xmlPullFeedParser = new XmlPullExamParser(gContext, url);
				xmlPullFeedParser.parseExam();

				ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(gContext);
				examinationDbHelper.open(this.holder.examTitle, this.holder.examDate);

				int count = 0;
				ArrayList<ExamQuestion> examQuestions = xmlPullFeedParser.getExam();
				for( ExamQuestion examQuestion: examQuestions ) {
					examQuestion.addToDatabase(examinationDbHelper);
					onProgressUpdate(count++);
				}

				examinationDbHelper.close();

			} catch (MalformedURLException e) {
				return gContext.getString(R.string.error_url_is_not_correct) + " " +this.holder.url;
			} catch (SQLiteException e) {
				return gContext.getString(R.string.failed_to_install_exam) + " " +this.holder.url;
			} catch (RuntimeException e) {
				return gContext.getString(R.string.error_parsing_exam) + " " +this.holder.url;
			}
			return "";
		}

		protected void onProgressUpdate(Integer... progress) {
			//setProgressPercent(progress[0]);
			//			holder.installUninstallButton.setEnabled(false);
			//			holder.installUninstallButton.setText(R.string.Installing_exam);
		}

		protected void onPostExecute(String errorMessage) {
			if( errorMessage.contentEquals("") ) {
				
				ExamTrainerDbAdapter examTrainerDbHelperAdapter = new ExamTrainerDbAdapter(gContext);
				examTrainerDbHelperAdapter.open();
				if(! examTrainerDbHelperAdapter.setInstallationState(this.holder.examID, this.holder.examDate, ExamTrainerDbAdapter.State.INSTALLED)) {
					Toast.makeText(gContext, "Failed to set exam " + this.holder.examTitle + " to installed.", Toast.LENGTH_LONG).show();
				}
				examTrainerDbHelperAdapter.close();

			} else {
				Toast.makeText(gContext, errorMessage, Toast.LENGTH_LONG).show();
			}

			updateView();
			
			//Sent notification to interested activities that examlist is updated
			Intent intent=new Intent();
			intent.setAction(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
			this.context.sendBroadcast(intent);
			Log.d("ManageExamsAdapter", "Send broadcast message");
		}
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
			ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(gContext);
			examTrainerDbHelper.open();
			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(gContext);
			if( examinationDbHelper.delete(this.holder.examTitle, this.holder.examDate) )  {
				if( ! examTrainerDbHelper.setInstallationState(this.holder.examID, 0, ExamTrainerDbAdapter.State.NOT_INSTALLED) ) {
					Toast.makeText(gContext, gContext.getString(R.string.Failed_to_uninstall_exam) + 
							this.holder.examTitle, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(gContext, gContext.getString(R.string.Could_not_remove_exam_database_file) + 
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
			updateView();
			
			//Sent notification to interested activities that examlist is updated
			Intent intent=new Intent();
			intent.setAction(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
			this.context.sendBroadcast(intent);
		}
	}
}
