package  nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.ExamTrainer.ExamTrainerMode;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SelectExamAdapter extends CursorAdapter  {
	private final String TAG = this.getClass().getName();
	private int layout;
	    
	    public SelectExamAdapter(Context context, int layout, Cursor c) {
	      super(context, c);
	      this.layout = layout;
	    }

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
				
			    int index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
		        String examTitle = cursor.getString(index);
		        
			      
			    TextView examTitleView = (TextView) view.findViewById(R.id.selectexamEntryTitle);
		        
			    examTitleView.setText(examTitle);
		}

		@Override
		public View newView(Context context, Cursor myCursor, ViewGroup parent) {
			final LayoutInflater mInflater = LayoutInflater.from(context);
			View view = (View) mInflater.inflate(layout, parent, false);
			return view;
		}
	  }