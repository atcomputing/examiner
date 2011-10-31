package nl.atcomputing.examtrainer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

/**
 * @author martijn brekhof
 *
 */
public class ExamResultsActivity extends Activity {
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.results);
        
        LayoutInflater li = getLayoutInflater();
		li.inflate(R.layout.results, null);
		
		Button quit = (Button) findViewById(R.id.result_button_return_to_main_menu);
        quit.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamResultsActivity.this, ExamTrainerActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
        
        Button showScores = (Button) findViewById(R.id.result_button_show_scores);
        showScores.setOnClickListener( new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ExamResultsActivity.this, ShowScoresActivity.class);
				startActivity(intent);
			}
		});

    }
    
    protected void onDestroy() {
    	super.onDestroy();
    }  
}