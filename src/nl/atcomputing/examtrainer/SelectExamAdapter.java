package  nl.atcomputing.examtrainer;

import android.content.Context;
import android.database.Cursor;
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
			    final ViewHolder holder = new ViewHolder();
				final Context myContext = context;
				
			    int index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
		        holder.examTitle = cursor.getString(index);
		        index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_DATE);
			    holder.examDate = cursor.getString(index);
			    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS);
			    holder.examAmountOfItems = cursor.getInt(index);
			    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
			    holder.examItemsNeededToPass = cursor.getInt(index);
			      
			    holder.examTitleView = (TextView) view.findViewById(R.id.selectexamEntryTitle);
		        holder.examStartButton = (Button) view.findViewById(R.id.selectexamStart);
		        
			    holder.examTitleView.setText(holder.examTitle);
			    holder.examStartButton.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						Toast.makeText(myContext, holder.examTitle + "\n" +
								myContext.getString(R.string.installed_on) + 
								" " + holder.examDate + "\n" +
								myContext.getString(R.string.questions) + 
								": " +  holder.examAmountOfItems + "\n" +
								myContext.getString(R.string.correct_answer_required_to_pass) +
								": " +  holder.examItemsNeededToPass + "\n"
								, Toast.LENGTH_LONG).show();
					}
				});
			    
			    view.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						Toast.makeText(myContext, holder.examTitle + "\n" +
								myContext.getString(R.string.installed_on) + 
								" " + holder.examDate + "\n" +
								myContext.getString(R.string.questions) + 
								": " +  holder.examAmountOfItems + "\n" +
								myContext.getString(R.string.correct_answer_required_to_pass) +
								": " +  holder.examItemsNeededToPass + "\n"
								, Toast.LENGTH_LONG).show();
					}
				});
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
			  String examDate;
			  String url;
			  int examAmountOfItems;
			  int examItemsNeededToPass;
		      TextView examTitleView;
		      Button examStartButton;
		    }
	  }