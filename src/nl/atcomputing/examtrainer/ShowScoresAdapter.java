package  nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.ExamTrainer.ExamTrainerMode;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ShowScoresAdapter extends CursorAdapter  {
	private final String TAG = this.getClass().getName();
	private int layout;
	    
	    public ShowScoresAdapter(Context context, int layout, Cursor c) {
	      super(context, c);
	      this.layout = layout;
	    }

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			    final ViewHolder holder = new ViewHolder();
				final Context mContext = context;
				
		        int index = cursor.getColumnIndex(ExamTrainer.Scores.COLUMN_NAME_DATE);
			    holder.examDate = cursor.getString(index);
			    index = cursor.getColumnIndex(ExamTrainer.Scores.COLUMN_NAME_SCORE);
			    holder.examScore = cursor.getInt(index);
			    
			    holder.scoreEntryDate = (TextView) view.findViewById(R.id.scoreEntryDate);
		        holder.scoreEntryDate.setText(holder.examDate);
		        holder.scoreEntryScore = (TextView) view.findViewById(R.id.scoreEntryScore);
		        holder.scoreEntryScore.setText(Integer.toString(holder.examScore));
		}

		@Override
		public View newView(Context context, Cursor myCursor, ViewGroup parent) {
			final LayoutInflater mInflater = LayoutInflater.from(context);
			View view = (View) mInflater.inflate(layout, parent, false);
			return view;
		}
		  
		class ViewHolder {
			  int examScore;
			  String examDate;
		      TextView scoreEntryDate;
		      TextView scoreEntryScore;
		    }
	  }