package nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.manage.ManageExamsActivity;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author martijn brekhof
 *
 */

public class SelectExamActivity extends Activity {
	private SelectExamAdapter adap;
	private static Cursor cursor;
	private TextView clickOnManageExams;
	private TextView noExamsAvailable;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("trace", "SelectExamActivity created");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.selectexam);
		
		ListView selectExam = (ListView) this.findViewById(R.id.select_exam_list);
		noExamsAvailable = (TextView) this.findViewById(R.id.selectexam_no_exams_available);
		clickOnManageExams = (TextView) this.findViewById(R.id.selectexam_click_on_manage_exams);

		adap = new SelectExamAdapter(this, R.layout.selectexam_entry, null);
		selectExam.setAdapter(adap);

		selectExam.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ExamTrainer.setExamId(id);
				Intent intent = new Intent(SelectExamActivity.this, StartExamActivity.class);
				startActivity(intent);
			}
		});
	}

	protected void onResume() {
		super.onResume();
		Log.d("trace", "SelectExamActivity resumed");
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		cursor = examTrainerDbHelper.getInstalledExams();
		examTrainerDbHelper.close();
		if(cursor.getCount() > 0) {
			//Remove exams not available text when there are exams installed
			noExamsAvailable.setVisibility(View.GONE);
			clickOnManageExams.setVisibility(View.GONE);
		} else {
			noExamsAvailable.setVisibility(View.VISIBLE);
			clickOnManageExams.setVisibility(View.VISIBLE);
		}
		adap.changeCursor(cursor);
		adap.notifyDataSetChanged();
	}

	protected void onPause() {
		super.onPause();
		Log.d("trace", "SelectExamActivity paused");
		cursor.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.selectexam_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.selectexam_menu_manage:
			Intent intent = new Intent(this, ManageExamsActivity.class);
			startActivity(intent);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
