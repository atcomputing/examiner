package nl.atcomputing.examtrainer;

import nl.atcomputing.adapters.HistoryAdapter;
import nl.atcomputing.dialogs.RunThreadWithProgressDialog;
import nl.atcomputing.examtrainer.database.ExamTrainerDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDatabaseHelper;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.manage.PreferencesActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author martijn brekhof
 *
 */

public class StartExamActivity extends Activity {
	private HistoryAdapter adapter;
	private Button buttonDeleteSelected;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startexam);
		
		Button button = (Button) findViewById(R.id.startexam_button_start_exam);
		button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				startExam();
			}
		});
		
		this.buttonDeleteSelected = (Button) findViewById(R.id.startexam_history_button_delete_scores);
        
        this.adapter = new HistoryAdapter(
        		StartExamActivity.this, 
        		R.layout.history_entry, 
        		null, 
        		buttonDeleteSelected);

        final SparseBooleanArray itemsChecked = (SparseBooleanArray) getLastNonConfigurationInstance();
        if (itemsChecked != null) {
            this.adapter.itemChecked = itemsChecked;
        }
        
        this.buttonDeleteSelected.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				deleteSelectedFromDatabase();
			}
		});

        ListView scoresList = (ListView) findViewById(R.id.startexam_history_listview);
        scoresList.setAdapter(adapter);
        
        scoresList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(StartExamActivity.this, ExamReviewActivity.class);
				ExamTrainer.setScoresId(id);
				startActivity(intent);
			}
		});
	}

	public Object onRetainNonConfigurationInstance() {
	    return this.adapter.itemChecked;
	}
	
	protected void onResume() {
		super.onResume();
		
		ExamTrainerDbAdapter examTrainerDbHelper = new ExamTrainerDbAdapter(this);
		examTrainerDbHelper.open();
		Cursor cursor = examTrainerDbHelper.getExam(ExamTrainer.getExamId());
		examTrainerDbHelper.close();
		
		int index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_EXAMTITLE);
		String examTitle = cursor.getString(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_AMOUNTOFITEMS);
		int examAmountOfItems = cursor.getInt(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_ITEMSNEEDEDTOPASS);
		int examItemsNeededToPass = cursor.getInt(index);
		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_TIMELIMIT);
		long examTimeLimit = cursor.getLong(index);

		index = cursor.getColumnIndex(ExamTrainerDatabaseHelper.Exams.COLUMN_NAME_DATE);
		long examInstallationDate = cursor.getLong(index);
		String localDate = ExamTrainer.convertEpochToString(examInstallationDate);

		TextView tv = (TextView) findViewById(R.id.startexam_amount_of_items_value);
		tv.setText(Integer.toString(examAmountOfItems));
		tv = (TextView) findViewById(R.id.startexam_items_needed_to_pass_value);
		tv.setText(Integer.toString(examItemsNeededToPass));
		tv = (TextView) findViewById(R.id.startexam_installed_on_value);
		tv.setText(localDate);
		tv = (TextView) findViewById(R.id.startexam_examtitle_value);
		tv.setText(examTitle);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useTimeLimit = prefs.getBoolean(this.getResources().getString(R.string.pref_key_use_timelimits), false);

		tv = (TextView) findViewById(R.id.startexam_timelimit_value);
		if ( ( useTimeLimit ) && ( examTimeLimit > 0 ) ) {
			tv.setText(Long.toString(examTimeLimit));
			ExamTrainer.setTimeLimit(examTimeLimit * 60);
		} else {
			tv.setText(getString(R.string.No_time_limit));
			ExamTrainer.setTimeLimit(0);
		}

		
		ExamTrainer.setExamDatabaseName(examTitle, examInstallationDate);
		ExamTrainer.setItemsNeededToPass(examItemsNeededToPass);
		ExamTrainer.setExamTitle(examTitle);
		ExamTrainer.setAmountOfItems(examAmountOfItems);
		cursor.close();
		
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
        examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
        cursor = examinationDbHelper.getScoresReversed();
        examinationDbHelper.close();
        
        tv = (TextView) findViewById(R.id.startexam_history_textview_noscoresavailable);
        LinearLayout ll = (LinearLayout) findViewById(R.id.startexam_history_header);
        ListView lv = (ListView) findViewById(R.id.startexam_history_listview);
        if( cursor.getCount() > 0 ) {
        	tv.setVisibility(View.GONE);
        	ll.setVisibility(View.VISIBLE);
        	lv.setVisibility(View.VISIBLE);
        } else {
        	ll.setVisibility(View.GONE);
        	lv.setVisibility(View.GONE);
        	tv.setVisibility(View.VISIBLE);
        }
        
		adapter.changeCursor(cursor);
		adapter.notifyDataSetChanged();
	}
	
	protected void onDestroy() {
		super.onDestroy();
		Cursor cursor = adapter.getCursor();
		if ( cursor != null ) {
			cursor.close();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.startexam_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.startexam_menu_preferences:
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void startExam() {
		Intent intent = new Intent(this, ExamQuestionActivity.class);
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(this);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		long scoresId = examinationDbHelper.createNewScore();
		examinationDbHelper.close();
		
		if( scoresId == -1 ) {
			Toast.makeText(this, this.getString(R.string.failed_to_create_a_new_score_for_the_exam), Toast.LENGTH_LONG).show();
		} else {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			boolean useTimelimit = prefs.getBoolean(this.getResources().getString(R.string.pref_key_use_timelimits), false);
			if( useTimelimit ) {
				ExamTrainer.setTimer();
			}
			ExamTrainer.setScoresId(scoresId);
			ExamTrainer.setQuestionId(intent, 1);
			ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM);
			startActivity(intent);
		}
	}
	
	
	private void deleteSelectedFromDatabase() {
		RunThreadWithProgressDialog pd = new RunThreadWithProgressDialog(this, 
		new Thread(new Runnable() {
	        public void run() {
	    		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(StartExamActivity.this);
	            examinationDbHelper.open(ExamTrainer.getExamDatabaseName()); 
	            
	            int size = adapter.itemChecked.size();
	    		for( int i = 0; i < size; i++ ) {
	    			int key = adapter.itemChecked.keyAt(i);
	    			if( adapter.itemChecked.get(key) ) {
	    				examinationDbHelper.deleteScore(key);
	    			}
	    		}
	    		
	    		examinationDbHelper.close();
	        }
	    }),
	    new Runnable() {
			public void run() {
				ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(StartExamActivity.this);
	            examinationDbHelper.open(ExamTrainer.getExamDatabaseName()); 
				
				Cursor cursor = examinationDbHelper.getScoresReversed();
				examinationDbHelper.close();
	    		
				adapter.itemChecked.clear();
	    		adapter.changeCursor(cursor);
	    		adapter.notifyDataSetChanged();
	    		
			}
		}
		);
		
		pd.run(getString(R.string.deleting_scores_please_wait_));
		
		this.buttonDeleteSelected.setVisibility(View.GONE);
	}
}
