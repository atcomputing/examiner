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
			    final ViewHolder holder = new ViewHolder();
				final Context myContext = context;
				
			    int index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
		        holder.examTitle = cursor.getString(index);
		        index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_DATE);
			    holder.examInstallationDate = cursor.getString(index);
			    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS);
			    holder.examAmountOfItems = cursor.getInt(index);
			    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
			    holder.examItemsNeededToPass = cursor.getInt(index);
			      
			    holder.examTitleView = (TextView) view.findViewById(R.id.selectexamEntryTitle);
		        holder.examStartButton = (Button) view.findViewById(R.id.selectexamStart);
		        
		        if( ExamTrainer.getMode() == ExamTrainerMode.HISTORY ) {
		        	holder.examStartButton.setText(R.string.history);
		        }
			    holder.examTitleView.setText(holder.examTitle);
			    holder.examStartButton.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						if( ExamTrainer.getMode() == ExamTrainerMode.HISTORY ) {
							showHistory(myContext, holder);
						}
						else if ( ExamTrainer.getMode() == ExamTrainerMode.EXAM ) {
							startExam(myContext, holder);
						}
					}
				});
			    
			    view.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						Toast.makeText(myContext, holder.examTitle + "\n" +
								myContext.getString(R.string.installed_on) + 
								" " + holder.examInstallationDate + "\n" +
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
			  String examInstallationDate;
			  String url;
			  int examAmountOfItems;
			  int examItemsNeededToPass;
		      TextView examTitleView;
		      Button examStartButton;
		    }
		
		private void showHistory(Context context, ViewHolder holder) {
			Intent intent = new Intent(context, ShowScoresActivity.class);
			ExamTrainer.setExamDatabaseName(holder.examTitle, holder.examInstallationDate);
			context.startActivity(intent);
		}
		
		private void startExam(Context context, ViewHolder holder) {
			Intent intent = new Intent(context, ExamQuestionsActivity.class);
			  ExamTrainer.setExamDatabaseName(holder.examTitle, holder.examInstallationDate);
	    	  ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(context);
	    	  examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
	    	  long examId = examinationDbHelper.createNewScore();
	    	  examinationDbHelper.close();
	    	  if( examId == -1 ) {
	    		  Log.d(TAG, "Failed to create a new score");
	    		  Toast.makeText(context, "Failed to create a new score for the exam", Toast.LENGTH_LONG);
	    	  } else {
	    		  ExamTrainer.setExamId(examId);
	    		  ExamTrainer.setQuestionNumber(intent, 1);
	    		  context.startActivity(intent);
	    	  }
		}
	  }