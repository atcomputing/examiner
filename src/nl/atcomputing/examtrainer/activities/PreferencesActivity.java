package nl.atcomputing.examtrainer.activities;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import nl.atcomputing.examtrainer.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * @author martijn brekhof
 * TODO replace by PreferenceFragment
 */

public class PreferencesActivity extends SherlockPreferenceActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}