package nl.atcomputing.lpic1examtrainer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ExamResultsActivity extends Activity {
	private ExamTrainerDbAdapter dbHelper;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);
        
        dbHelper = new ExamTrainerDbAdapter(this);
		dbHelper.open();
		
		dbHelper.close();
		
		Button quitProgram = (Button) findViewById(R.id.button_quit_program);
        quitProgram.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ExamResultsActivity.this.finish();
			}
		});

    }
}