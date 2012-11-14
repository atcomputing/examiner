package nl.atcomputing.examtrainer.adapters;

import java.util.HashMap;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;



/**
 * @author martijn brekhof
 *
 */

public class ManageExamsAdapter extends CursorAdapter  {
	private ManageExamsAdapterListener listener;
	private int layout;
	private HashMap<Long, DataHolder> dataHolderCache;

	public interface ManageExamsAdapterListener {
		public void onButtonClick(View v, long examID);
		public void onItemClick(View v, long examID);
	}

	public ManageExamsAdapter(Context context, int layout, Cursor c, ManageExamsAdapterListener listener) {
		super(context, c, false);
		this.layout = layout;
		this.dataHolderCache = new HashMap<Long, DataHolder>();
		this.listener = listener;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater mInflater = LayoutInflater.from(context);
		return (View) mInflater.inflate(this.layout, parent, false);
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
		final long examID = cursor.getLong(index);

		ViewHolder vHolder = (ViewHolder) view.getTag();

		if( vHolder == null ) {
			vHolder = new ViewHolder();
			vHolder.examTitleView = (TextView) view.findViewById(R.id.manageExamsEntryTitle);
			vHolder.examAuthorView = (TextView) view.findViewById(R.id.manageExamsEntryAuthor);
			vHolder.installUninstallButton = (Button) view.findViewById(R.id.manageExamsDelete);
			vHolder.installUninstallButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					listener.onButtonClick(v, examID);
				}
			});
			view.setTag(vHolder);
		}

		DataHolder dHolder = this.dataHolderCache.get(examID);
		if( dHolder == null ) {
			dHolder = new DataHolder();
			index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
			dHolder.examTitle = cursor.getString(index);
			index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR);
			dHolder.author = cursor.getString(index);
			this.dataHolderCache.put(examID, dHolder);
		} 

		view.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				listener.onItemClick(v, examID);
			}
		});

		vHolder.examTitleView.setText(dHolder.examTitle);
		vHolder.examAuthorView.setText(dHolder.author);

		//Set state for button
		vHolder.installUninstallButton.setEnabled(true);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
		String state = cursor.getString(index);
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

//		InstallExamAsyncTask task = ExamTrainer.getInstallExamAsyncTask(examID);
		//		if( task != null ) {
		//			task.setProgressTextView(holder.installUninstallButton);
		//		}
	}


	private class ViewHolder {
		TextView examTitleView;
		TextView examAuthorView;
		Button installUninstallButton;
	}

	private class DataHolder {
		String examTitle;
		String author;
	}
}
