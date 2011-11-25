package nl.atcomputing.examtrainer;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * @author martijn brekhof
 *
 */
public class PreferencesActivity extends PreferenceActivity {
	private final String TAG = this.getClass().getName();
	private ExamTrainerDbAdapter examTrainerDbHelper;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}