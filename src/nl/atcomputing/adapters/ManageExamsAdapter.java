package nl.atcomputing.adapters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import nl.atcomputing.examtrainer.ExamQuestion;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.manage.XmlPullExamParser;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.text.format.Time;
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
		super(context, c);
		this.gContext = context;
		this.layout = layout;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		final ViewHolder holder = new ViewHolder();

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

		holder.examTitleView = (TextView) view.findViewById(R.id.manageExamsEntryTitle);
		holder.examTitleView.setText(holder.examTitle);
		holder.examAuthorView = (TextView) view.findViewById(R.id.manageExamsEntryAuthor);
		holder.examAuthorView.setText(holder.author);
		holder.installUninstallButton = (Button) view.findViewById(R.id.manageExamsDelete);

		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
		final int installed = cursor.getInt(index);

		if( installed == 1 ) {
			holder.installUninstallButton.setText(R.string.uninstall);
		}
		else {
			holder.installUninstallButton.setText(R.string.install);
		}

		holder.installUninstallButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if( installed == 1 ) {
					new UninstallExam(holder).execute();
				} 
				else {
					new InstallExam(holder).execute();
				}
			}
		});

		view.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				StringBuffer strBuf = new StringBuffer();

				strBuf.append(holder.examTitle + "\n");

				if ( holder.examDate == 0 ) {
					strBuf.append(gContext.getString(R.string.Not_installed) + "\n");
				}
				else {
					Time time = new Time();
					time.set(Long.valueOf(holder.examDate));
					String localDate = time.format("%Y-%m-%d %H:%M");

					strBuf.append(gContext.getString(R.string.installed_on) + 
							" " + localDate + "\n");
				}

				strBuf.append(gContext.getString(R.string.questions) + 
						": " +  holder.examAmountOfItems + "\n" +
						gContext.getString(R.string.correct_answer_required_to_pass) +
						": " +  holder.examItemsNeededToPass + "\n" +
						gContext.getString(R.string.URL) +
						": " +  holder.url + "\n");

				if ( holder.timeLimit == 0 ) {
					strBuf.append(gContext.getString(R.string.No_time_limit));
				} else {
					strBuf.append(gContext.getString(R.string.Time_limit) + ": " + holder.timeLimit
							+ " " + gContext.getString(R.string.minutes));
				}

				Toast.makeText(gContext,  strBuf.toString(), Toast.LENGTH_LONG).show();
			}
		});

	}

	@Override
	public View newView(Context context, Cursor myCursor, ViewGroup parent) {
		final LayoutInflater mInflater = LayoutInflater.from(context);
		View view = (View) mInflater.inflate(layout, parent, false);
		return view;
	}

	public void updateView() {
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(gContext);
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getAllExams();
		examTrainerDbHelper.close();
		this.changeCursor(cursor);
		this.notifyDataSetChanged();
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
		ViewHolder holder;
		public InstallExam(ViewHolder viewHolder) {
			super();
			holder = viewHolder;
		}

		protected void onPreExecute() {
			holder.installUninstallButton.setEnabled(false);
			holder.installUninstallButton.setText(R.string.Installing_exam);
		}

		protected String doInBackground(ViewHolder... holders) {
			holder.examDate = System.currentTimeMillis();

			try {
				URL url = new URL(holder.url);
				XmlPullExamParser xmlPullFeedParser = new XmlPullExamParser(gContext, url);
				xmlPullFeedParser.parseExam();

				ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(gContext);
				examinationDbHelper.open(holder.examTitle, holder.examDate);

				ArrayList<ExamQuestion> examQuestions = xmlPullFeedParser.getExam();
				for( ExamQuestion examQuestion: examQuestions ) {
					examQuestion.addToDatabase(examinationDbHelper);
				}

				examinationDbHelper.close();

			} catch (MalformedURLException e) {
				return gContext.getString(R.string.error_url_is_not_correct) + " " +holder.url;
			} catch (SQLiteException e) {
				return gContext.getString(R.string.failed_to_install_exam) + " " +holder.url;
			} catch (RuntimeException e) {
				return gContext.getString(R.string.error_parsing_exam) + " " +holder.url;
			}

			ExamTrainerDbAdapter examTrainerDbHelperAdapter = new ExamTrainerDbAdapter(gContext);

			examTrainerDbHelperAdapter.open();
			if(! examTrainerDbHelperAdapter.setInstalled(holder.examID, holder.examDate, true)) {
				Toast.makeText(gContext, "Failed to set exam " + holder.examTitle + " to installed.", Toast.LENGTH_LONG).show();
			}
			examTrainerDbHelperAdapter.close();
			return "";
		}

		protected void onProgressUpdate(Integer... progress) {
			//setProgressPercent(progress[0]);
		}

		protected void onPostExecute(String errorMessage) {
			if( errorMessage.contentEquals("") ) {
				holder.installUninstallButton.setEnabled(true);
			} else {
				Toast.makeText(gContext, errorMessage, Toast.LENGTH_LONG).show();
			}
			updateView();
		}
	}

	private class UninstallExam extends AsyncTask<ViewHolder, Integer, Boolean> {
		ViewHolder holder;
		public UninstallExam(ViewHolder viewHolder) {
			super();
			holder = viewHolder;
		}

		protected void onPreExecute() {
			holder.installUninstallButton.setEnabled(false);
		}

		protected Boolean doInBackground(ViewHolder... holders) {
			ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(gContext);
			examTrainerDbHelper.open();
			ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(gContext);
			if( examinationDbHelper.delete(holder.examTitle, holder.examDate) )  {
				if( ! examTrainerDbHelper.setInstalled(holder.examID, 0, false) ) {
					Toast.makeText(gContext, gContext.getString(R.string.Failed_to_uninstall_exam) + 
							holder.examTitle, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(gContext, gContext.getString(R.string.Could_not_remove_exam_database_file) + 
						holder.examTitle, Toast.LENGTH_LONG).show();
			}
			examTrainerDbHelper.close();
			return true;
		}

		protected void onProgressUpdate(Integer... progress) {
			//setProgressPercent(progress[0]);
		}

		protected void onPostExecute(Boolean result) {
			holder.installUninstallButton.setEnabled(true);
			updateView();
		}
	}
}
