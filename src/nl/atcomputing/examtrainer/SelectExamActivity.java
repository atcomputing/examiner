package nl.atcomputing.examtrainer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author martijn brekhof
 *
 */

//Example code on how to setup nice selectionbox: 
//http://www.codemobiles.com/forum/viewtopic.php?t=876

public class SelectExamActivity extends Activity {
	  private SelectExamAdapter adap;
	  private static Cursor cursor;
	  private ExamTrainerDbAdapter examTrainerDbHelper;
	  
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
	    examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		
		cursor = examTrainerDbHelper.getInstalledExams();
		
	    adap = new SelectExamAdapter(this, R.layout.selectexam_entry, cursor);
	    selectExam.setAdapter(adap);
	    
	  }

	  protected void onDestroy() {
		  super.onDestroy();
		  examTrainerDbHelper.close();
	  }
	  
	  protected void startExam(String examTitle, String dateInstalled) {
		  Intent intent = new Intent(SelectExamActivity.this, ExamQuestionsActivity.class);
    	  ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
    	  long examId = examinationDbHelper.createNewScore();
    	  ExamTrainer.startNewExam(examId, examTitle, dateInstalled, intent);
    	  startActivity(intent);
	  }
}