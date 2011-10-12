package nl.atcomputing.lpic1examtrainer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * @author martijn brekhof
 *
 */
public class ExamReviewActivity extends Activity {
	public static final String TAG = "ExamReviewActivity";
	
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "Activity started");
		super.onCreate(savedInstanceState);
	}
}