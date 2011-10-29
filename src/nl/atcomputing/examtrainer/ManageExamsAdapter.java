package  nl.atcomputing.examtrainer;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ManageExamsAdapter extends CursorAdapter  {
	private final String TAG = this.getClass().getName();
	private Context context;
	private int layout;
	    
	    public ManageExamsAdapter(Context context, int layout, Cursor c) {
	      super(context, c);
	      this.context = context;
	      this.layout = layout;
	    }

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final Context myContext = context;
		    
			    final ViewHolder holder = new ViewHolder();
			    		        
		        int index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
		        holder.examTitle = cursor.getString(index);
		        index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_DATE);
			    holder.examDate = cursor.getString(index);
			    index = cursor.getColumnIndex(ExamTrainer.Exams._ID);
			    holder.examID = cursor.getLong(index);
			    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_URL);
			    holder.url = cursor.getString(index);
			    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS);
			    holder.examAmountOfItems = cursor.getInt(index);
			    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
			    holder.examItemsNeededToPass = cursor.getInt(index);
			    
			    holder.examTitleView = (TextView) view.findViewById(R.id.manageExamsEntryTitle);
		        holder.installUninstallButton = (Button) view.findViewById(R.id.manageExamsDelete);
		        holder.examTitleView.setText(holder.examTitle);

		        index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_INSTALLED);
			    final int installed = cursor.getInt(index);
			    
			    if( installed == 1 ) {
			    	holder.installUninstallButton.setText(R.string.uninstall);
			    }
			    else {
			    	holder.installUninstallButton.setText(R.string.install);
			    }
			    
			    holder.installUninstallButton.setOnClickListener(new View.OnClickListener() {
		          public void onClick(View v) {
		        	  if( installed == 1 ) {
		        		  deleteExam(holder);
		        	  } 
		        	  else {
		        		 installExam(myContext, holder);
		        	  }
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
		
		protected void deleteExam(ViewHolder holder) {
			  Log.d(TAG, "Deleting exam " + holder.examTitle +
					  " examId " + holder.examID + " examDate " + holder.examDate);
			  
			  ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
			  examTrainerDbHelper.open();
			  ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(context);
			  if( ! ( (examinationDbHelper.delete(holder.examTitle, holder.examDate) ) && 
			   examTrainerDbHelper.deleteExam(holder.examID) )  ) {
					  Toast.makeText(context, "Failed to delete exam " + 
							  holder.examTitle, Toast.LENGTH_LONG).show(); 
			  }
			  examTrainerDbHelper.close();
			 updateView();
		  }

		  
		  protected void installExam(Context context, ViewHolder holder) {
			  Log.d(TAG, "Installing exam " + holder.examTitle +
					  " examDate " + holder.examDate + " URL " + holder.url);
			  try {
				  URL url = new URL(holder.url);
				  XmlPullExamParser xmlPullFeedParser = new XmlPullExamParser(context, url);
			      xmlPullFeedParser.parse();
			      xmlPullFeedParser.installExam(holder.examTitle, holder.examDate);
			  } catch (MalformedURLException e) {
				  Toast.makeText(context, "Error: URL " + holder.url + " is not correct.", Toast.LENGTH_LONG).show();
			  } catch (SQLiteException e) {
				  Toast.makeText(context, "Failed to install exam " + holder.url, Toast.LENGTH_LONG).show();
			  } catch (RuntimeException e) {
				  Toast.makeText(context, "Error parsing exam at " + holder.url, Toast.LENGTH_LONG).show();
			  }
			  
			  updateView();
			  
		  }
		  
		  protected void updateView() {
			  ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(context);
			  examTrainerDbHelper.open();
			  Cursor cursor = examTrainerDbHelper.getExams();
			  examTrainerDbHelper.close();
			  this.changeCursor(cursor);
		      this.notifyDataSetChanged();
		  }
		  
		class ViewHolder {
			  String examTitle;
			  long examID;
			  String examDate;
			  String url;
			  int examAmountOfItems;
			  int examItemsNeededToPass;
		      TextView examTitleView;
		      Button installUninstallButton;
		    }
	  }