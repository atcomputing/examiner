package nl.atcomputing.examtrainer;

import nl.atcomputing.examtrainer.adapters.SelectExamAdapter;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.manage.ManageExamsActivity;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
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
	private ReceiveBroadcast receiveBroadcast;
	
	private class ReceiveBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			setupListView();
		}
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selectexam);
		
		this.receiveBroadcast = new ReceiveBroadcast();
	}

	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(ExamTrainer.BROADCAST_ACTION_EXAMLIST_UPDATED);
	    this.registerReceiver(this.receiveBroadcast, filter);
		setupListView();
	}

	protected void onPause() {
		super.onPause();
		cursor.close();
		this.unregisterReceiver(this.receiveBroadcast);
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
	
	private void setupListView() {
		ListView selectExam = (ListView) this.findViewById(R.id.select_exam_list);
		this.noExamsAvailable = (TextView) this.findViewById(R.id.selectexam_no_exams_available);
		this.clickOnManageExams = (TextView) this.findViewById(R.id.selectexam_click_on_manage_exams);

		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		cursor = examTrainerDbHelper.getInstalledAndInstallingExams();
		examTrainerDbHelper.close();
		
		this.adap = new SelectExamAdapter(this, R.layout.selectexam_entry, cursor);
		selectExam.setAdapter(this.adap);

		selectExam.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ExamTrainer.setExamId(id);
				Intent intent = new Intent(SelectExamActivity.this, StartExamActivity.class);
				startActivity(intent);
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
}
