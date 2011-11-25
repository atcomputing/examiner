package nl.atcomputing.examtrainer;

import java.net.URL;

import android.app.ListActivity;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

//Example code on how to setup nice selectionbox: 
//http://www.codemobiles.com/forum/viewtopic.php?t=876

public class ManageExamsActivity extends ListActivity {
	private final String TAG = this.getClass().getName();
	  private ManageExamsAdapter adap;
	  
	  public void onCreate(Bundle savedInstanceState) {
		ExamTrainerDbAdapter examTrainerDbHelper;
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
		Cursor cursor = examTrainerDbHelper.getAllExams();
	    adap = new ManageExamsAdapter(this, R.layout.manageexams_entry, cursor);
	    examTrainerDbHelper.close();
	    setListAdapter(adap);
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
						if(filename.matches("list.xml")) {
							Log.d(TAG, "Found databasefile " + filename);
							URL url = new URL("file:///"+filename);
							xmlPullExamParser = new XmlPullExamParser(context, url);
							xmlPullExamParser.parseList();
							if ( ! xmlPullExamParser.checkIfExamInDatabase() ) {
								Log.d(TAG, "Included Exam not in database:  " + filename);
								xmlPullExamParser.addExamToExamTrainer();
							}
						}
					}
					adap.updateView();
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