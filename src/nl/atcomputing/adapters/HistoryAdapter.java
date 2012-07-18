package  nl.atcomputing.adapters;

import nl.atcomputing.examtrainer.ExamTrainer;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.R.drawable;
import nl.atcomputing.examtrainer.R.id;
import nl.atcomputing.examtrainer.database.ExaminationDatabaseHelper;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author martijn brekhof
 *
 */

public class HistoryAdapter extends CursorAdapter  {
	private int layout;
	private Activity activity;
	private int amountOfItemsChecked;
	public SparseBooleanArray itemChecked = new SparseBooleanArray();
	private Button buttonDeleteSelected;
	
	    public HistoryAdapter(Activity activity, int layout, Cursor c, Button deleteSelectedButton) {
	      super(activity, c);
	      this.activity = activity;
	      this.layout = layout;
	      this.buttonDeleteSelected = deleteSelectedButton;
	      this.amountOfItemsChecked = 0;
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
			    final int scoresId = cursor.getInt(index);
			    
		        CheckBox cbox = (CheckBox) view.findViewById(R.id.historyCheckBox);
		        cbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Log.d("HistoryAdapter", "onCheckedChanged: amountOfItemsChecked="+amountOfItemsChecked);
						if( isChecked ) {
							itemChecked.put(scoresId, true);
							amountOfItemsChecked++;
							buttonDeleteSelected.setVisibility(View.VISIBLE);
						} else {
							itemChecked.put(scoresId, false);
							amountOfItemsChecked--;
							if(amountOfItemsChecked < 1) {
								amountOfItemsChecked = 0;
								buttonDeleteSelected.setVisibility(View.GONE);
								LinearLayout ll = (LinearLayout) activity.findViewById(R.id.startexam_bottom_buttons);
								ll.invalidate();
							}
						}
					}
				});
		        
		        Boolean checked = itemChecked.get(scoresId);
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