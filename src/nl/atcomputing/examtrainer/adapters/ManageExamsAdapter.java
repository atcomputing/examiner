package nl.atcomputing.examtrainer.adapters;

import java.util.HashMap;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.activities.ExamTrainer;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.examparser.InstallExamAsyncTask;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
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
	private HashMap<Long, ViewHolder> viewHolderForPositionCache;

	public interface ManageExamsAdapterListener {
		public void onButtonClick(Button button, long examID);
	}
	
	public ManageExamsAdapter(Context context, ManageExamsAdapterListener listener, int layout, Cursor c) {

		this.listener = listener;
		
		this.layout = layout;

		this.cursor = c;

		this.viewHolderForPositionCache = new HashMap<Long, ViewHolder>();

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

		int index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
		long examID = row.getLong(index);

		InstallExamAsyncTask task = ExamTrainer.getInstallExamAsyncTask(examID);

		ViewHolder holder = this.viewHolderForPositionCache.get(examID); 

		if( holder != null ) {
			if( task != null ) {
				task.setProgressTextView(holder.installUninstallButton);
			}
			return holder.view;
		}

		holder = new ViewHolder();
		holder.examID = examID;
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		holder.examTitle = row.getString(index);
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
		holder.examDate = row.getLong(index);
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


		final LayoutInflater mInflater = LayoutInflater.from(this.context);
		View view = (View) mInflater.inflate(this.layout, parent, false);

		holder.examTitleView = (TextView) view.findViewById(R.id.manageExamsEntryTitle);
		holder.examAuthorView = (TextView) view.findViewById(R.id.manageExamsEntryAuthor);
		holder.installUninstallButton = (Button) view.findViewById(R.id.manageExamsDelete);
		holder.view = view;

		if( task != null ) {
			task.setProgressTextView(holder.installUninstallButton);
		}

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

		final ViewHolder holderReference = holder;
		
		holder.installUninstallButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				listener.onButtonClick(holderReference.installUninstallButton, holderReference.examID);
			}
		});

		view.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				StringBuffer strBuf = new StringBuffer();

				strBuf.append(holderReference.examTitle + "\n");

				strBuf.append(context.getString(R.string.questions) + 
						": " +  holderReference.examAmountOfItems + "\n" +
						context.getString(R.string.correct_answer_required_to_pass) +
						": " +  holderReference.examItemsNeededToPass + "\n");

				if ( holderReference.timeLimit == 0 ) {
					strBuf.append(context.getString(R.string.No_time_limit));
				} else {
					strBuf.append(context.getString(R.string.Time_limit_in_minutes) + ": " + holderReference.timeLimit
							+ " " + context.getString(R.string.minutes));
				}

				Toast.makeText(context,  strBuf.toString(), Toast.LENGTH_LONG).show();
			}
		});

		this.viewHolderForPositionCache.put(examID, holder);

		return holder.view;
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
}
