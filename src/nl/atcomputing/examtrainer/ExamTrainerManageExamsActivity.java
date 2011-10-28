package nl.atcomputing.examtrainer;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.ListActivity;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

//Example code on how to setup nice selectionbox: 
//http://www.codemobiles.com/forum/viewtopic.php?t=876

public class ExamTrainerManageExamsActivity extends ListActivity {
	private final String TAG = this.getClass().getName();
	  private EfficientAdapter adap;
	  private ExamTrainerDbAdapter examTrainerDbHelper;
	  
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.manageexams);
	    final Context context = this;
	    
	    
	    
	    Button cancel = (Button) this.findViewById(R.id.manageExams_cancel);
	    cancel.setOnClickListener(new View.OnClickListener() {

	          public void onClick(View v) {
	        	  finish();
	          }
	        });
	    
	    Button getNewExams = (Button) this.findViewById(R.id.manageExams_getNewExams);
	    getNewExams.setOnClickListener(new View.OnClickListener() {
	          public void onClick(View v) {
	        	  loadLocalExams(context);
	          }
	        });
		
	    examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getExams();
	    adap = new EfficientAdapter(context, cursor);
	    examTrainerDbHelper.close();
	    setListAdapter(adap);
	    
	  }

	  
	  protected void deleteExam(Cursor cursor) {
		  int columnIndex = cursor.getColumnIndex(ExamTrainer.Exams._ID);
		  long examID = cursor.getLong(columnIndex);
		  columnIndex = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
		  String examTitle = cursor.getString(columnIndex);
		  columnIndex = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_DATE);
		  String examDate = cursor.getString(columnIndex);
		  Log.d(TAG, "Deleting exam " + examTitle +
				  " examId " + examID + " examDate " + examDate);
		  
		  examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		  examTrainerDbHelper.open();
		  ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		  if( ! ( (examinationDbHelper.delete(examTitle, examDate) ) && 
		   examTrainerDbHelper.deleteExam(examID) )  ) {
				  Toast.makeText(this, "Failed to delete exam " + examTitle, Toast.LENGTH_LONG).show(); 
		  }
		  examTrainerDbHelper.close();
		 updateView();
	  }

	  
	  protected void installExam(Context context, Cursor cursor) {
		  
		  int columnIndex = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_URL);
		  String tUrl = cursor.getString(columnIndex);
		  columnIndex = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
		  String examTitle = cursor.getString(columnIndex);
		  columnIndex = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_DATE);
		  String examDate = cursor.getString(columnIndex);
		  Log.d(TAG, "Installing exam " + examTitle +
				  " examDate " + examDate + " URL " + tUrl);
		  try {
			  URL url = new URL(tUrl);
			  XmlPullExamParser xmlPullFeedParser = new XmlPullExamParser(context, url);
		      xmlPullFeedParser.parse();
		      xmlPullFeedParser.installExam(examTitle, examDate);
		  } catch (MalformedURLException e) {
			  Toast.makeText(this, "Error: URL " + tUrl + " is not correct.", Toast.LENGTH_LONG).show();
		  } catch (SQLiteException e) {
			  Toast.makeText(this, "Failed to install exam " + tUrl, Toast.LENGTH_LONG).show();
		  } catch (RuntimeException e) {
			  Toast.makeText(this, "Error parsing exam at " + tUrl, Toast.LENGTH_LONG).show();
		  }
		  
		  updateView();
		  
	  }
	  
	  protected void updateView() {
		  examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		  examTrainerDbHelper.open();
		  Cursor cursor = examTrainerDbHelper.getExams();
		  examTrainerDbHelper.close();
		  adap.changeCursor(cursor);
	      adap.notifyDataSetChanged();
	  }
	  
	  public class EfficientAdapter extends CursorAdapter  {
	    private LayoutInflater mInflater;
	    Context context;
	    
	    public EfficientAdapter(Context context, Cursor myCursor) {
	      super(context, myCursor);
	      mInflater = LayoutInflater.from(context);
	      this.context = context;
	    }

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final Cursor myCursor = cursor;
			final Context myContext = context;
			
		        view.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						int index = myCursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
					    String examTitle = myCursor.getString(index);
					    index = myCursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_DATE);
					    String examDate = myCursor.getString(index);
					    index = myCursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS);
					    int examAmountOfItems = myCursor.getInt(index);
					    index = myCursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
					    int examItemsNeededToPass = myCursor.getInt(index);
						Toast.makeText(myContext, examTitle + "\n" +
								myContext.getString(R.string.installed_on) + " " + examDate + "\n" +
								myContext.getString(R.string.questions) + ": " +  examAmountOfItems + "\n" +
								myContext.getString(R.string.correct_answer_required_to_pass) + ": " +  examItemsNeededToPass + "\n"
								, Toast.LENGTH_LONG).show();
					}
				});
		        
			    ViewHolder holder = new ViewHolder();
			    
		        holder.examTitle = (TextView) view.findViewById(R.id.manageExamsEntryTitle);
		        holder.buttonInstallUninstall = (Button) view.findViewById(R.id.manageExamsDelete);
		        
		        int index = myCursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
		        String title = cursor.getString(index);
		        holder.examTitle.setText(title);
		        index = myCursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_INSTALLED);
			    final int installed = myCursor.getInt(index);
			    
			    Log.d(TAG, "bindview: examTitle " + title + " installed " + installed);
			    holder.buttonInstallUninstall.setOnClickListener(new View.OnClickListener() {
		          public void onClick(View v) {
		        	  if( installed == 1 ) {
		        		  Log.d(TAG, "myCursor " + myCursor);
		        		  deleteExam(myCursor);
		        	  } 
		        	  else {
		        		  installExam(myContext, myCursor);
		        	  }
		          }
		        });
		        
			    if( installed == 1 ) {
			    	holder.buttonInstallUninstall.setText(R.string.uninstall);
			    }
			    else {
			    	holder.buttonInstallUninstall.setText(R.string.install);
			    }
			    
			    

		}

		@Override
		public View newView(Context context, Cursor myCursor, ViewGroup parent) {
			View view = (View) mInflater.inflate(R.layout.manageexams_entry, null);
			return view;
		}
		
		class ViewHolder {
		      TextView examTitle;
		      Button buttonInstallUninstall;
		    }
	  }
	  
	  private void loadLocalExams(Context context) {
			int file_index = 0;
			String[] filenames = null;

			AssetManager assetManager = getAssets();
			
			if( assetManager != null ) {
				try {
					XmlPullExamParser xmlPullExamParser;
					filenames = assetManager.list("");
					int size = filenames.length;
					for( file_index = 0; file_index < size; file_index++) {
						String filename = filenames[file_index];
						if(filename.matches("exam..*.xml")) {
							Log.d(TAG, "Found databasefile " + filename);
							URL url = new URL("file:///"+filename);
							xmlPullExamParser = new XmlPullExamParser(context, url);
							xmlPullExamParser.parse();
							if ( ! xmlPullExamParser.checkIfExamInDatabase() ) {
								Log.d(TAG, "Included Exam not in database:  " + filename);
								xmlPullExamParser.addExamToExamTrainer();
							}
						}
					}
					updateView();
				} catch (Exception e) {
					Log.d(this.getClass().getName() , "Updating exams failed: Error " + e.getMessage());
					Toast.makeText(this, "Error: updating exam " + filenames[file_index] + " failed.", Toast.LENGTH_LONG).show();
				}
			}
		}
	  
	  private void loadRemoteExams() {
		//retrieveExam();
	  }
}