package  nl.atcomputing.examtrainer;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class SelectExamAdapter extends CursorAdapter  {
	private final String TAG = this.getClass().getName();
	private int layout;
	    private ArrayList<Long> examIds;
	    public SelectExamAdapter(Context context, int layout, Cursor c) {
	      super(context, c);
	      this.layout = layout;
	      examIds = new ArrayList<Long>();
	    }

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
				
			    int index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
		        String examTitle = cursor.getString(index);
		        index = cursor.getColumnIndex(ExamTrainer.Exams._ID);
			    long examId = cursor.getLong(index);
			    examIds.add(examId);
			    
			    TextView examTitleView = (TextView) view.findViewById(R.id.selectexamEntryTitle);
		        
			    examTitleView.setText(examTitle);
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