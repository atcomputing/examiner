package  nl.atcomputing.examtrainer;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author martijn brekhof
 *
 */

public class HistoryAdapter extends CursorAdapter  {
	private int layout;
	    
	private ArrayList<Integer> examIdsSelected;
	
	    public HistoryAdapter(Context context, int layout, Cursor c) {
	      super(context, c);
	      this.layout = layout;
	      this.examIdsSelected = new ArrayList<Integer>();
	    }

		@Override
		public void bindView(View view, Context context, Cursor cursor) {			
		        int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Scores.COLUMN_NAME_DATE);
			    String examDate = ExamTrainer.convertEpochToString(cursor.getLong(index));
			    index = cursor.getColumnIndex(ExaminationDatabaseHelper.Scores.COLUMN_NAME_SCORE);
			    int examScore = cursor.getInt(index);
			    
			    TextView scoreEntryDate = (TextView) view.findViewById(R.id.historyEntryDate);
		        scoreEntryDate.setText(examDate);
		        TextView scoreEntryScore = (TextView) view.findViewById(R.id.historyEntryScore);
		        scoreEntryScore.setText(Integer.toString(examScore));
		        
		        ImageView scoreEntryImage = (ImageView) view.findViewById(R.id.historyEntryPass);
		        
		        if( examScore >= ExamTrainer.getItemsNeededToPass() ) { 
		        	scoreEntryImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ok));
		        } 
		        else {
		        	scoreEntryImage.setImageDrawable(context.getResources().getDrawable(R.drawable.not_ok));
		        }
		        
		        index = cursor.getColumnIndex(ExaminationDatabaseHelper.Scores._ID);
			    final int examId = cursor.getInt(index);
			    
		        CheckBox cbox = new CheckBox(context);
		        cbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if( isChecked ) {
							examIdsSelected.add(examId);
						}
					}
				});
		}

		@Override
		public View newView(Context context, Cursor myCursor, ViewGroup parent) {
			final LayoutInflater mInflater = LayoutInflater.from(context);
			View view = (View) mInflater.inflate(layout, parent, false);
			return view;
		}
		
		public ArrayList<Integer> getExamIdsSelected() {
			return this.examIdsSelected;
		}
	  }