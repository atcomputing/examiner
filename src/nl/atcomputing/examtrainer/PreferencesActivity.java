package nl.atcomputing.examtrainer;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

/**
 * @author martijn brekhof
 *
 */

public class PreferencesActivity extends SherlockPreferenceActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}