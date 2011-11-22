package nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.ExamTrainer.ExamTrainerMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author martijn brekhof
 *
 */

//Example code on how to setup nice selectionbox: 
//http://www.codemobiles.com/forum/viewtopic.php?t=876

public class SelectExamActivity extends Activity {
	private final String TAG = this.getClass().getName();
	  private SelectExamAdapter adap;
	  private static Cursor cursor;
	  private ExamTrainerDbAdapter examTrainerDbHelper;
	  private long examsRowId;
	  private static final int DIALOG_SHOW_EXAM = 0;
	  
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.selectexam);
	    
	    Button cancel = (Button) this.findViewById(R.id.selectexam_cancel);
	    cancel.setOnClickListener(new View.OnClickListener() {

	          public void onClick(View v) {
	        	  finish();
	          }
	        });
	    
	    ListView selectExam = (ListView) this.findViewById(R.id.select_exam_list);
	    TextView noExamsAvailable = (TextView) this.findViewById(R.id.selectexam_no_exams_available);
	    examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		
		cursor = examTrainerDbHelper.getInstalledExams();
		if(cursor.getCount() > 0) {
			//Remove exams not available text when there are exams installed
			noExamsAvailable.setVisibility(View.GONE);
		}
		
	    adap = new SelectExamAdapter(this, R.layout.selectexam_entry, cursor);
	    selectExam.setAdapter(adap);
	    
	    selectExam.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				examsRowId = id;
				showDialog(DIALOG_SHOW_EXAM);
			}
		});
	  }

	  protected void onDestroy() {
		  super.onDestroy();
		  examTrainerDbHelper.close();
	  }
	  
	  protected Dialog onCreateDialog(int id) {
			Dialog dialog;
			AlertDialog.Builder builder;
			switch(id) {
			case DIALOG_SHOW_EXAM:
				String positiveButtonText = this.getString(R.string.start_exam);
				Cursor cursor = examTrainerDbHelper.getExam(examsRowId);
				int index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_EXAMTITLE);
		        final String examTitle = cursor.getString(index);
		        index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_DATE);
		        final String examInstallationDate = cursor.getString(index);
			    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_AMOUNTOFITEMS);
			    int examAmountOfItems = cursor.getInt(index);
			    index = cursor.getColumnIndex(ExamTrainer.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
			    int examItemsNeededToPass = cursor.getInt(index);
				
			    if( ExamTrainer.getMode() == ExamTrainerMode.REVIEW ) {
			    	positiveButtonText = this.getString(R.string.show_history);
				}
				else if ( ExamTrainer.getMode() == ExamTrainerMode.EXAM ) {
					positiveButtonText = this.getString(R.string.start_exam);
				}
			    
				builder = new AlertDialog.Builder(this);
				builder.setCancelable(true)
				.setMessage(examTitle + "\n\n" +
						this.getString(R.string.installed_on) + 
						" " + examInstallationDate + "\n" +
						this.getString(R.string.questions) + 
						": " +  examAmountOfItems + "\n" +
						this.getString(R.string.correct_answer_required_to_pass) +
						": " +  examItemsNeededToPass + "\n" )
				.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						ExamTrainer.setExamDatabaseName(examTitle, examInstallationDate);
						if( ExamTrainer.getMode() == ExamTrainerMode.REVIEW ) {
							showHistory();
						}
						else if ( ExamTrainer.getMode() == ExamTrainerMode.EXAM ) {
							startExam();
						}
						
					}
				})
				.setNegativeButton(this.getString(R.string.close), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});			;
				dialog = builder.create();
				break;
			default:
				dialog = null;
			}
			return dialog;
		}
	  
	  private void showHistory() {
			Intent intent = new Intent(this, ShowScoresActivity.class);
			startActivity(intent);
		}
		
		private void startExam() {
			Intent intent = new Intent(this, ExamQuestionsActivity.class);
	    	  ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
	    	  examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
	    	  long examId = examinationDbHelper.createNewScore();
	    	  examinationDbHelper.close();
	    	  if( examId == -1 ) {
	    		  Log.d(TAG, "Failed to create a new score");
	    		  Toast.makeText(this, this.getString(R.string.failed_to_create_a_new_score_for_the_exam), Toast.LENGTH_LONG);
	    	  } else {
	    		  ExamTrainer.setExamId(examId);
	    		  ExamTrainer.setQuestionNumber(intent, 1);
	    		  startActivity(intent);
	    	  }
		}
}