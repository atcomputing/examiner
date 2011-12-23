package nl.atcomputing.examtrainer;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

public class ManageExamsAdapter extends CursorAdapter  {
	private final String TAG = this.getClass().getName();
	private Context gContext;
	private int layout;
	    
	    public ManageExamsAdapter(Context context, int layout, Cursor c) {
	      super(context, c);
	      this.gContext = context;
	      this.layout = layout;
	    }

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			    final ViewHolder holder = new ViewHolder();
			    		       
		        int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		        holder.examTitle = cursor.getString(index);
		        index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
			    holder.examDate = cursor.getLong(index);
			    index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams._ID);
			    holder.examID = cursor.getLong(index);
			    index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_URL);
			    holder.url = cursor.getString(index);
			    index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS);
			    holder.examAmountOfItems = cursor.getInt(index);
			    index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
			    holder.examItemsNeededToPass = cursor.getInt(index);
			    index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AUTHOR);
			    holder.author = cursor.getString(index);
			    index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT);
			    holder.timeLimit = cursor.getLong(index);
			    
			    holder.examTitleView = (TextView) view.findViewById(R.id.manageExamsEntryTitle);
			    holder.examTitleView.setText(holder.examTitle);
			    holder.examAuthorView = (TextView) view.findViewById(R.id.manageExamsEntryAuthor);
			    holder.examAuthorView.setText(holder.author);
			    holder.installUninstallButton = (Button) view.findViewById(R.id.manageExamsDelete);
		        
		        index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_INSTALLED);
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
		        		  uninstallExam(holder);
		        	  } 
		        	  else {
		        		 installExam(holder);
		        	  }
		          }
		        });
			    
			    view.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						StringBuffer strBuf = new StringBuffer();
						
						strBuf.append(holder.examTitle + "\n");
						
						if ( holder.examDate == 0 ) {
							strBuf.append(gContext.getString(R.string.Not_installed) + "\n");
						}
						else {
							Time time = new Time();
							time.set(Long.valueOf(holder.examDate));
							String localDate = time.format("%Y-%m-%d %H:%M");
							
							strBuf.append(gContext.getString(R.string.installed_on) + 
									" " + localDate + "\n");
						}
						
						strBuf.append(gContext.getString(R.string.questions) + 
								": " +  holder.examAmountOfItems + "\n" +
								gContext.getString(R.string.correct_answer_required_to_pass) +
								": " +  holder.examItemsNeededToPass + "\n" +
								gContext.getString(R.string.URL) +
								": " +  holder.url + "\n");
						
						if ( holder.timeLimit == 0 ) {
							strBuf.append(gContext.getString(R.string.No_time_limit));
						} else {
							strBuf.append(gContext.getString(R.string.Time_limit) + ": " + holder.timeLimit
									+ " " + gContext.getString(R.string.minutes));
						}
						
						Toast.makeText(gContext,  strBuf.toString(), Toast.LENGTH_LONG).show();
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
			  
			  ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(gContext);
			  examinationDbHelper.delete(holder.examTitle, holder.examDate);
			  
			  ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(gContext);
			  examTrainerDbHelper.open();
			  if( ! examTrainerDbHelper.deleteExam(holder.examID) ) {
					  Toast.makeText(gContext, "Failed to delete exam " + 
							  holder.examTitle, Toast.LENGTH_LONG).show(); 
			  }
			  examTrainerDbHelper.close();
			  updateView();
		  }

		 protected void uninstallExam(ViewHolder holder) {
			  ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(gContext);
			  examTrainerDbHelper.open();
			  ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(gContext);
			  if( examinationDbHelper.delete(holder.examTitle, holder.examDate) )  {
				  if( ! examTrainerDbHelper.setInstalled(holder.examID, 0, false) ) {
					  Toast.makeText(gContext, gContext.getString(R.string.Failed_to_uninstall_exam) + 
							  holder.examTitle, Toast.LENGTH_LONG).show();
				  }
			  } else {
				  Toast.makeText(gContext, gContext.getString(R.string.Could_not_remove_exam_database_file) + 
						  holder.examTitle, Toast.LENGTH_LONG).show();
			  }
			  examTrainerDbHelper.close();
			  updateView();
		 }
		 
		  protected void installExam(ViewHolder holder) {
			  
			 holder.examDate = System.currentTimeMillis();
			  
			  try {
				  URL url = new URL(holder.url);
				  XmlPullExamParser xmlPullFeedParser = new XmlPullExamParser(gContext, url);
			      xmlPullFeedParser.parseExam();
			      
			      ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(gContext);
			      examinationDbHelper.open(holder.examTitle, holder.examDate);
			      
			      ArrayList<ExamQuestion> examQuestions = xmlPullFeedParser.getExam();
			      for( ExamQuestion examQuestion: examQuestions ) {
			    	  examQuestion.addToDatabase(examinationDbHelper);
			      }
			      
			      examinationDbHelper.close();
			      
			  } catch (MalformedURLException e) {
				  Toast.makeText(gContext, "Error: URL " + holder.url + " is not correct.", Toast.LENGTH_LONG).show();
			  } catch (SQLiteException e) {
				  Toast.makeText(gContext, "Failed to install exam " + holder.url, Toast.LENGTH_LONG).show();
			  } catch (RuntimeException e) {
				  Toast.makeText(gContext, "Error parsing exam at " + holder.url, Toast.LENGTH_LONG).show();
			  }
			  
			  ExamTrainerDbAdapter examTrainerDbHelperAdapter = new ExamTrainerDbAdapter(gContext);
			  
			  examTrainerDbHelperAdapter.open();
			  if(! examTrainerDbHelperAdapter.setInstalled(holder.examID, holder.examDate, true)) {
				  Toast.makeText(gContext, "Failed to set exam " + holder.examTitle + " to installed.", Toast.LENGTH_LONG).show();
			  }
			  examTrainerDbHelperAdapter.close();
			  updateView();
		  }
		  
		  protected void updateView() {
			  ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(gContext);
			  examTrainerDbHelper.open();
			  Cursor cursor = examTrainerDbHelper.getAllExams();
			  examTrainerDbHelper.close();
			  this.changeCursor(cursor);
		      this.notifyDataSetChanged();
		  }
		  
		class ViewHolder {
			  String examTitle;
			  long examID;
			  long examDate;
			  String url;
			  String author;
			  int examAmountOfItems;
			  int examItemsNeededToPass;
			  long timeLimit;
		      TextView examTitleView;
		      TextView examAuthorView;
		      Button installUninstallButton;
		    }
}
