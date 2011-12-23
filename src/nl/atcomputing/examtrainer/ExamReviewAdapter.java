package nl.atcomputing.examtrainer;

import java.util.ArrayList;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.ExamTrainer.ExamTrainerMode;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author martijn brekhof
 *
 */

	public class ExamReviewAdapter extends BaseAdapter
	{
		public static final String TAG = "ExamReviewAdapter";
		private Context context;
		private Cursor cursor;
		private ArrayList<Long> questionIds = new ArrayList<Long>();
		private Drawable not_okImage;
		private Drawable okImage;
		private int layout;
		
		public ExamReviewAdapter(Context context, int layout, Cursor cursor)
		{
			this.context = context;
			this.cursor = cursor;
			this.layout = layout;
			Resources res = context.getResources();
			not_okImage = res.getDrawable(R.drawable.not_ok);
			okImage = res.getDrawable(R.drawable.ok);
		}

		public int getCount() 
		{
			/* Set the number of element we want on the grid */
			if ( cursor != null ) {
				return cursor.getCount();
			}
			return 0;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return questionIds.get(position);
		}
		
		public View getView(int position, View view, ViewGroup parent) 
		{
			long questionId = 0;
			
			Log.d(TAG, "position: "+ position + " convertView: " + view );
			if ( view == null )
			{  
				final LayoutInflater mInflater = LayoutInflater.from(context);
				view = (View) mInflater.inflate(layout, parent, false);
			}
				int answer = 0;

				if( cursor.moveToPosition(position) ) {
					int index = cursor.getColumnIndex(ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_QUESTION_ID);
					questionId = cursor.getLong(index);

					index = cursor.getColumnIndex(ExaminationDatabaseHelper.ResultPerQuestion.COLUMN_NAME_ANSWER_CORRECT);
					answer = cursor.getInt(index);
				}
				
				questionIds.add(questionId);
				
				TextView tv = (TextView)view.findViewById(R.id.reviewExamQuestionID);
				tv.setText(Long.toString(questionId));
				
				ImageView iv = (ImageView)view.findViewById(R.id.reviewExamAnswer);
				if( answer == 1 ) {
					iv.setImageDrawable(okImage);
				} 
				else {
					iv.setImageDrawable(not_okImage);
				}
			return view;
		}

		
		
		
	}