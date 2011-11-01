package nl.atcomputing.examtrainer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * @author martijn brekhof
 *
 */
public class ConfigureActivity extends Activity {
	private final String TAG = this.getClass().getName();
	
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.configure);		
		
		Button returnToMain = (Button) this.findViewById(R.id.config_buttonReturnToMain);
		returnToMain.setOnClickListener(new View.OnClickListener() {

	          public void onClick(View v) {
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
	} 
	
}