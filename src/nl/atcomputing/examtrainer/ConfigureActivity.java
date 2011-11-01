package nl.atcomputing.examtrainer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * @author martijn brekhof
 *
 */
public class ConfigureActivity extends Activity {
	private final String TAG = this.getClass().getName();
	private ExamTrainerDbAdapter examTrainerDbHelper;
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.configure);		
		
		examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		
		CheckBox sendScores = (CheckBox) this.findViewById(R.id.config_checkSendScores);
		sendScores.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					examTrainerDbHelper.setSendScores(true);
				} else {
					examTrainerDbHelper.setSendScores(false);
				}

			}
		});
		sendScores.setChecked(examTrainerDbHelper.getSendScores());
		
		CheckBox checkForUpdates = (CheckBox) this.findViewById(R.id.config_checkUpdatesStartup);
		checkForUpdates.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					examTrainerDbHelper.setCheckForUpdates(true);
				} else {
					examTrainerDbHelper.setCheckForUpdates(false);
				}

			}
		});
		checkForUpdates.setChecked(examTrainerDbHelper.getCheckForUpdates());
		
		CheckBox useTimeLimit = (CheckBox) this.findViewById(R.id.config_checkUseTimelimit);
		useTimeLimit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					examTrainerDbHelper.setUseTimeLimit(true);
				} else {
					examTrainerDbHelper.setUseTimeLimit(false);
				}

			}
		});
		useTimeLimit.setChecked(examTrainerDbHelper.getUseTimeLimit());
		
		final EditText url = (EditText) this.findViewById(R.id.config_editExamURL);
		url.setText(examTrainerDbHelper.getURL());
		
		Button returnToMain = (Button) this.findViewById(R.id.config_buttonReturnToMain);
		returnToMain.setOnClickListener(new View.OnClickListener() {

	          public void onClick(View v) {
	        	  examTrainerDbHelper.setURL(url.getText().toString());
	        	  finish();
	          }
	        });
	    
	    Button manageExams = (Button) this.findViewById(R.id.config_buttonManageExams);
	    manageExams.setOnClickListener(new View.OnClickListener() {
	          public void onClick(View v) {
	        	  Intent intent = new Intent(ConfigureActivity.this, ManageExamsActivity.class);
				  startActivity(intent);
	          }
	        });
	}
	
	protected void onDestroy() {
		super.onDestroy();
		examTrainerDbHelper.close();
	} 
	
}