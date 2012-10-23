package nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author martijn brekhof
 *
 */

public class PreferencesActivity extends PreferenceActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}