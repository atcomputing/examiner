package  nl.atcomputing.examtrainer.review;

import java.util.ArrayList;
import java.util.HashMap;

import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.R.drawable;
import nl.atcomputing.examtrainer.R.id;
import nl.atcomputing.examtrainer.database.ExaminationDatabaseHelper;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
	private HistoryActivity activity;
	private HashMap<Integer, Boolean> itemChecked = new HashMap<Integer, Boolean>();
	
	    public HistoryAdapter(HistoryActivity activity, int layout, Cursor c, Button deleteScoresButton) {
	      super(activity, c);
	      this.layout = layout;
	      this.activity = activity;
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
		        	scoreEntryImage.setImageDrawable(context.getResources().getDrawable(R.drawable.green_check));
		        } 
		        else {
		        	scoreEntryImage.setImageDrawable(context.getResources().getDrawable(R.drawable.red_cross));
		        }
		        
		        index = cursor.getColumnIndex(ExaminationDatabaseHelper.Scores._ID);
			    final int examId = cursor.getInt(index);
			    
		        CheckBox cbox = (CheckBox) view.findViewById(R.id.historyCheckBox);
		        cbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if( isChecked ) {
							activity.addItemToDeletionList(examId);
							itemChecked.put(examId, true);
						} else {
							activity.removeItemFromDeletionList(examId);
							itemChecked.put(examId, false);
						}
					}
				});
		        
		        Boolean checked = itemChecked.get(examId);
		        if ( ( checked == null ) || ( checked == false ) ) {
		        	cbox.setChecked(false);
		        } else {
		        	cbox.setChecked(true);
		        }
		}

		@Override
		public View newView(Context context, Cursor myCursor, ViewGroup parent) {
			final LayoutInflater mInflater = LayoutInflater.from(context);
			View view = (View) mInflater.inflate(layout, parent, false);
			return view;
		}
	  }