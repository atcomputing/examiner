package nl.atcomputing.examtrainer.adapters;

import java.util.HashMap;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.activities.ExamTrainer;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.examparser.InstallExamAsyncTask;
import android.content.Context;
import android.database.Cursor;
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
	private ManageExamsAdapterListener listener;
	private int layout;
	private Cursor cursor;

	public interface ManageExamsAdapterListener {
		public void onButtonClick(View v, long examID);
	}

	public ManageExamsAdapter(Context context, ManageExamsAdapterListener listener, int layout, Cursor c) {

		this.listener = listener;

		this.layout = layout;

		this.cursor = c;

		this.context = context;
	}

	public int getCount() {
		if( this.cursor != null ) {
			return cursor.getCount();
		} else {
			return 0;
		}
	}

	public Object getItem(int position) {
		if( 	( ! cursor.isClosed() ) && 
				( cursor.getCount() > 0 ) && 
				(cursor.moveToPosition(position) ) 
				){
			return cursor;
		} else {
			return null;
		}
	}

	public long getItemId(int position) {
		if( 	( ! cursor.isClosed() ) && 
				( cursor.getCount() > 0 ) && 
				(cursor.moveToPosition(position) ) 
				){
			int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
			return cursor.getLong(index);
		} else {
			return 0;
		}
	}

	public View getView(int position, View convertView, ViewGroup parent) {	
		Cursor row = (Cursor) getItem(position);
		if( row == null ) {
			return convertView;
		}

		ViewHolder holder;
		int index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
		final long examID = row.getLong(index);

		if( convertView == null ) {
			LayoutInflater mInflater = LayoutInflater.from(this.context);
			convertView = (View) mInflater.inflate(this.layout, parent, false);
			holder = new ViewHolder();
			holder.examTitleView = (TextView) convertView.findViewById(R.id.manageExamsEntryTitle);
			holder.examAuthorView = (TextView) convertView.findViewById(R.id.manageExamsEntryAuthor);
			holder.installUninstallButton = (Button) convertView.findViewById(R.id.manageExamsDelete);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		final String examTitle = row.getString(index);
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS);
		final int examAmountOfItems = row.getInt(index);
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
		final int examItemsNeededToPass = row.getInt(index);
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR);
		final String author = row.getString(index);
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT);
		final long timeLimit = row.getLong(index);
		
		holder.examTitleView.setText(examTitle);

		holder.examAuthorView.setText(author);

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
				listener.onButtonClick(v, examID);
			}
		});

		convertView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				StringBuffer strBuf = new StringBuffer();

				strBuf.append(examTitle + "\n");

				strBuf.append(context.getString(R.string.questions) + 
						": " +  examAmountOfItems + "\n" +
						context.getString(R.string.correct_answer_required_to_pass) +
						": " +  examItemsNeededToPass + "\n");

				if ( timeLimit == 0 ) {
					strBuf.append(context.getString(R.string.No_time_limit));
				} else {
					strBuf.append(context.getString(R.string.Time_limit_in_minutes) + ": " + timeLimit
							+ " " + context.getString(R.string.minutes));
				}

				Toast.makeText(context,  strBuf.toString(), Toast.LENGTH_LONG).show();
			}
		});

		InstallExamAsyncTask task = ExamTrainer.getInstallExamAsyncTask(examID);

		if( task != null ) {
			task.setProgressTextView(holder.installUninstallButton);
		}
		
		return convertView;
	}

	class ViewHolder {
		TextView examTitleView;
		TextView examAuthorView;
		Button installUninstallButton;
	}
}
