package  nl.atcomputing.examtrainer.adapters;

import java.util.HashMap;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.activities.ExamTrainer;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.examparser.InstallExamAsyncTask;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * @author martijn brekhof
 *
 */

public class SelectExamAdapter extends CursorAdapter  {
	private int layout;
	private HashMap<Long, ViewHolder> viewHolderCache;

	public SelectExamAdapter(Context context, int layout, Cursor c) {
		super(context, c, false);
		this.layout = layout;
		this.viewHolderCache = new HashMap<Long, ViewHolder>();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
		long examID = cursor.getLong(index);
		
		InstallExamAsyncTask task = ExamTrainer.getInstallExamAsyncTask(examID);
		
		ViewHolder holder = this.viewHolderCache.get(examID); 

		if( holder == null ) {
			holder = new ViewHolder();
			holder.examTitleView = (TextView) view.findViewById(R.id.selectexamEntryTitle);
			holder.examAuthorView = (TextView) view.findViewById(R.id.selectexamEntryAuthor);
			index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
			holder.examTitle = cursor.getString(index);
			index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR);
			holder.author = cursor.getString(index);
			this.viewHolderCache.put(examID, holder);
		} else {
			if( task != null ) {
				task.setProgressTextView(holder.examAuthorView);
			}
			return;
		}
			
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
		String state = cursor.getString(index);

		holder.examTitleView.setText(holder.examTitle);
		
		if( state.contentEquals(ExamTrainerDbAdapter.State.INSTALLED.name()) ) {
			holder.examAuthorView.setText(holder.author);
		} else if ( state.contentEquals(ExamTrainerDbAdapter.State.INSTALLING.name()) ) {
			holder.examAuthorView.setText(R.string.Installing_exam);
		}
		
		if( task != null ) {
			task.setProgressTextView(holder.examAuthorView);
		}
	}

	@Override
	public View newView(Context context, Cursor myCursor, ViewGroup parent) {
		final LayoutInflater mInflater = LayoutInflater.from(context);
		View view = (View) mInflater.inflate(layout, parent, false);
		return view;
	}

	class ViewHolder {
		String examTitle;
		long examID;
		String author;
		TextView examTitleView;
		TextView examAuthorView;
	}
}