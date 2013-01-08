package nl.atcomputing.examtrainer.fragments;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.adapters.ExamSelectAdapter;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author martijn brekhof
 *
 */

public class ExamSelectFragment extends AbstractFragment implements OnKeyListener {
	private TextView clickOnManageExams;
	private TextView noExamsAvailable;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.examselectfragment, container, false);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.setOnKeyListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupListView();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setupListView();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
	}
	
	@Override
	public String getTitle() {
		Activity activity = getActivity();
		ApplicationInfo info;
		PackageManager pm = activity.getPackageManager();
		try {
			info = pm.getApplicationInfo("nl.atcomputing.examtrainer", 0);
			return pm.getApplicationLabel(info).toString();
		} catch (NameNotFoundException e) {
			return null;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.selectexam_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Activity activity = getActivity();
		
		switch (item.getItemId()) {
		case R.id.selectexam_menu_about:
			String version;
			ListView lv = (ListView) activity.findViewById(R.id.select_exam_list);
			
			try {
				PackageInfo info = activity.getPackageManager().getPackageInfo("nl.atcomputing.examtrainer", 0);
				version = info.versionName;
			} catch (NameNotFoundException e) {
				version = getString(R.string.unknown);
			}
			TextView tv = (TextView) activity.findViewById(R.id.about_version_number);
			tv.setText(version);
			LinearLayout about_layout = (LinearLayout) activity.findViewById(R.id.about_window);
			if( ( about_layout.getVisibility() == View.INVISIBLE ) || 
					( about_layout.getVisibility() == View.GONE ) )	{
				about_layout.setVisibility(View.VISIBLE);
				lv.setEnabled(false);
			} else {
				about_layout.setVisibility(View.INVISIBLE);
				lv.setEnabled(true);
			}
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	private void setupListView() {
		final Activity activity = getActivity();
		ListView selectExam = (ListView) activity.findViewById(R.id.select_exam_list);
		this.noExamsAvailable = (TextView) activity.findViewById(R.id.selectexam_no_exams_available);
		this.clickOnManageExams = (TextView) activity.findViewById(R.id.selectexam_click_on_manage_exams);

		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(activity);
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getInstalledAndInstallingExams();
		cursor.moveToFirst();
		examTrainerDbHelper.close();
		
		ExamSelectAdapter adap = new ExamSelectAdapter(activity, R.layout.examselect_entry, cursor);
		selectExam.setAdapter(adap);

		selectExam.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				abstractFragmentListener.onItemClickListener(id);
			}
		});
		
		if(cursor.getCount() > 0) {
			//Remove exams not available text when there are exams installed
			noExamsAvailable.setVisibility(View.GONE);
			clickOnManageExams.setVisibility(View.GONE);
		} else {
			
			noExamsAvailable.setVisibility(View.VISIBLE);
			clickOnManageExams.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void updateView() {
		setupListView();
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		Activity activity = getActivity();
		
		if( keyCode == KeyEvent.KEYCODE_BACK )
        {
			ListView lv = (ListView) activity.findViewById(R.id.select_exam_list);
			LinearLayout about_layout = (LinearLayout) activity.findViewById(R.id.about_window);
			if( about_layout.getVisibility() == View.VISIBLE ) {
				about_layout.setVisibility(View.INVISIBLE);
				lv.setEnabled(true);
				return true;
			}
        }
		return false;
	}
}
