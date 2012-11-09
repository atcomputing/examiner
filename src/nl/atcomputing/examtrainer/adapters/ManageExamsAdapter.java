package nl.atcomputing.examtrainer.adapters;

import java.util.HashMap;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.activities.ExamTrainer;
import nl.atcomputing.examtrainer.adapters.SelectExamAdapter.ViewHolder;
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
	private HashMap<Long, DataHolder> dataHolderCache;
	
	public interface ManageExamsAdapterListener {
		public void onButtonClick(View v, long examID);
	}

	public ManageExamsAdapter(Context context, ManageExamsAdapterListener listener, int layout, Cursor c) {

		this.listener = listener;

		this.layout = layout;

		this.cursor = c;

		this.context = context;
		
		this.dataHolderCache = new HashMap<Long, ManageExamsAdapter.DataHolder>();
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
			Log.d("ManageExamsAdapter", "getView: row is null");
			return convertView;
		}

		ViewHolder vHolder;
		int index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
		final long examID = row.getLong(index);
		
		Log.d("ManageExamsAdapter", "getView: position="+position+", examID="+examID);
		
		if( convertView == null ) {
			LayoutInflater mInflater = LayoutInflater.from(this.context);
			convertView = (View) mInflater.inflate(this.layout, parent, false);
			vHolder = new ViewHolder();
			vHolder.examTitleView = (TextView) convertView.findViewById(R.id.manageExamsEntryTitle);
			vHolder.examAuthorView = (TextView) convertView.findViewById(R.id.manageExamsEntryAuthor);
			vHolder.installUninstallButton = (Button) convertView.findViewById(R.id.manageExamsDelete);
			vHolder.installUninstallButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					listener.onButtonClick(v, examID);
				}
			});
			Log.d("ManageExamsAdapter", "getView: convertView created for position="+position+", examID="+examID+
					"\ninstallUninstallButton="+vHolder.installUninstallButton+
					"\nexamTitleView"+vHolder.examTitleView+
					"\nexamAuthorView"+vHolder.examAuthorView);
			convertView.setTag(vHolder);
		} else {
			vHolder = (ViewHolder) convertView.getTag();
		}
		
		
		
		DataHolder dHolder = this.dataHolderCache.get(examID);
		
		if( dHolder == null ) {
			dHolder = createDataHolder(examID, row);
			this.dataHolderCache.put(examID, dHolder);
		}

		vHolder.examTitleView.setText(dHolder.examTitle);
		vHolder.examAuthorView.setText(dHolder.author);
		vHolder.installUninstallButton.setEnabled(true);
		
		final DataHolder holderReference = dHolder;
		
		convertView.setOnClickListener(new View.OnClickListener() {
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
		
		index = row.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
		String state = row.getString(index);
		if( state.contentEquals(ExamTrainerDbAdapter.State.NOT_INSTALLED.name()) ) {
			vHolder.installUninstallButton.setText(R.string.install);
		} else if ( state.contentEquals(ExamTrainerDbAdapter.State.INSTALLED.name()) ) {
			vHolder.installUninstallButton.setText(R.string.uninstall);
		} else if ( state.contentEquals(ExamTrainerDbAdapter.State.INSTALLING.name()) ) {
			vHolder.installUninstallButton.setText(R.string.Installing_exam);
			vHolder.installUninstallButton.setEnabled(false);
		} else {
			vHolder.installUninstallButton.setText(R.string.install);
		}

		/**
		 * bug 
		 * Apparently adapter creates a single row and reuses it for all other rows
		 * This makes it pretty much impossible to reconnect installation threads
		 * after the ListView is recreated due to a configuration change or user
		 * moved away and back again to the fragment/activity using the adapter
		 */
//		InstallExamAsyncTask task = ExamTrainer.getInstallExamAsyncTask(examID);
//
//		if( task != null ) {
//			Log.d("ManageExamsAdapter", "button: "+vHolder.installUninstallButton+" is reconnected to task="+task+" for examID: "+examID);
//			task.setProgressTextView(vHolder.installUninstallButton);
//		}
		
		return convertView;
	}

	private DataHolder createDataHolder(final long examID, Cursor cursor) {
		DataHolder holder = new DataHolder();
		
		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		holder.examTitle = cursor.getString(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS);
		holder.examAmountOfItems = cursor.getInt(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
		holder.examItemsNeededToPass = cursor.getInt(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR);
		holder.author = cursor.getString(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT);
		holder.timeLimit = cursor.getLong(index);
		
		return holder;
	}
	
	private class ViewHolder {
		TextView examTitleView;
		TextView examAuthorView;
		Button installUninstallButton;
	}
	
	private class DataHolder {
		String examTitle;
		int examAmountOfItems;
		int examItemsNeededToPass;
		String author;
		long timeLimit;
	}
}
