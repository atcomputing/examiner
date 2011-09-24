package nl.atcomputing.lpic1examtrainer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ExamTrainerActivity extends Activity {
	private ExamTrainerDbAdapter dbHelper;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        retrieveExam();
        setContentView(R.layout.main);
        
        dbHelper = new ExamTrainerDbAdapter(this);
		dbHelper.open();
		//dbHelper.upgrade();
		
		dbHelper.addQuestion("My First Question", "What is the first question?", null, "MULTIPLECHOICE", 
				"The next question, The previous question, This question, None of the above", "This question");

        Button startExam = (Button) findViewById(R.id.button_start_exam);
        startExam.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_RUN, ExamTrainer.Questions.CONTENT_URI, ExamTrainerActivity.this, ExamQuestionsActivity.class);
				intent.putExtra("question", 1);
				startActivity(intent);
			}
		});
        
        
    }
        
    protected void retrieveExam() {
    	Intent intent = new Intent(this, RetrieveExamQuestions.class);
    	startService(intent);
    }
    
    
}