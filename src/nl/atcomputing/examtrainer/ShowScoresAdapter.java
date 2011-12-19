package  nl.atcomputing.examtrainer;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author martijn brekhof
 *
 */

public class ShowScoresAdapter extends CursorAdapter  {
	private final String TAG = this.getClass().getName();
	private int layout;
	    private Context context;
	    
	    public ShowScoresAdapter(Context context, int layout, Cursor c) {
	      super(context, c);
	      this.layout = layout;
	      this.context = context;
	    }

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
				
		        int index = cursor.getColumnIndex(ExamTrainer.Scores.COLUMN_NAME_DATE);
			    String examDate = ExamTrainer.convertEpochToString(cursor.getLong(index));
			    index = cursor.getColumnIndex(ExamTrainer.Scores.COLUMN_NAME_SCORE);
			    int examScore = cursor.getInt(index);
			    
			    TextView scoreEntryDate = (TextView) view.findViewById(R.id.scoreEntryDate);
		        scoreEntryDate.setText(examDate);
		        TextView scoreEntryScore = (TextView) view.findViewById(R.id.scoreEntryScore);
		        scoreEntryScore.setText(Integer.toString(examScore));
		        
		        ImageView scoreEntryImage = (ImageView) view.findViewById(R.id.scoreEntryPass);
		        
		        if( examScore >= ExamTrainer.getItemsNeededToPass() ) { 
		        	scoreEntryImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ok));
		        } 
		        else {
		        	scoreEntryImage.setImageDrawable(context.getResources().getDrawable(R.drawable.not_ok));
		        }
		}

		@Override
		public View newView(Context context, Cursor myCursor, ViewGroup parent) {
			final LayoutInflater mInflater = LayoutInflater.from(context);
			View view = (View) mInflater.inflate(layout, parent, false);
			return view;
		}
	  }