package  nl.atcomputing.examtrainer.adapters;

import java.util.ArrayList;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
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
	    private ArrayList<Long> examIds;
	    public SelectExamAdapter(Context context, int layout, Cursor c) {
	      super(context, c);
	      this.layout = layout;
	      examIds = new ArrayList<Long>();
	    }

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
				
			int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
	        String examTitle = cursor.getString(index);
		    index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
		    long examID = cursor.getLong(index);
		    index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR);
		    String author = cursor.getString(index);
		    index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
		    String state = cursor.getString(index);
		    
		    TextView examTitleView = (TextView) view.findViewById(R.id.selectexamEntryTitle);
		    examTitleView.setText(examTitle);
		    TextView examAuthorView = (TextView) view.findViewById(R.id.selectexamEntryAuthor);
		    
		    if( state.contentEquals(ExamTrainerDbAdapter.State.INSTALLED.name()) ) {
		    	examAuthorView.setText(author);
		    } else if ( state.contentEquals(ExamTrainerDbAdapter.State.INSTALLING.name()) ) {
		    	examAuthorView.setText(R.string.Installing_exam);
		    }
		    
			examIds.add(examID);
		}

		@Override
		public View newView(Context context, Cursor myCursor, ViewGroup parent) {
			final LayoutInflater mInflater = LayoutInflater.from(context);
			View view = (View) mInflater.inflate(layout, parent, false);
			return view;
		}
		
		public long getItemId(int pos) {
			return examIds.get(pos);
		}
	  }