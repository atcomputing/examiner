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
		
		
	}
	
	protected void onDestroy() {
		super.onDestroy();
	} 
	
}