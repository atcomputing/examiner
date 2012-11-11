package nl.atcomputing.examtrainer.fragments;

import nl.atcomputing.dialogs.UsageDialog;
import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.activities.ExamTrainer;
import nl.atcomputing.examtrainer.adapters.ExamReviewAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author martijn brekhof
 *
 */
public class ExamReviewFragment extends AbstractFragment {
	public static final String TAG = "ExamReviewActivity";
	private GridView scoresGrid;
	private ExamReviewAdapter adapter; 
	private long examID;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		setHasOptionsMenu(true);
		setRetainInstance(true);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.examreviewfragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Activity activity = getActivity();

		setupScoresGrid();

		Button buttonStartExam = (Button) activity.findViewById(R.id.button_start_exam);
		this.examID = ExamTrainer.getScoresId();
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(activity);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getResultsPerQuestion(this.examID);
		examinationDbHelper.close();
		if( cursor.getCount() < ExamTrainer.getAmountOfItems() ) {
			buttonStartExam.setVisibility(View.VISIBLE);
			buttonStartExam.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					abstractFragmentListener.onButtonClickListener(ExamReviewFragment.this, ExamTrainer.getExamId());
				}
			});
		}

		UsageDialog usageDialog = UsageDialog.newInstance(activity, R.string.Usage_Dialog_examReviewScreenMessage);
		if( usageDialog != null ) {
			usageDialog.show(getFragmentManager(), "UsageDialog");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		setupScoresGrid();
	}
	
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		Cursor cursor = this.adapter.getCursor();
//		if ( cursor != null ) { 
//			cursor.close();
//		}
//	}

	public int getAmountOfQuestionsAnswered() {
		return this.adapter.getCount();
	}
	
	public void setExamID(long id) {
		this.examID = id;
	}
	
	public long getExamID() {
		return this.examID;
	}
	
	private void setupScoresGrid() {
		Activity activity = getActivity();
		
		this.adapter = new ExamReviewAdapter(activity, R.layout.review_exam_entry, ExamTrainer.getScoresId());

		this.scoresGrid = (GridView) activity.findViewById(R.id.review_exam_grid);
		this.scoresGrid.setAdapter(this.adapter);

		scoresGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				abstractFragmentListener.onItemClickListener(id);
			}
		});
	}


	@Override
	public void updateView() {
		// TODO Auto-generated method stub
		
	}
}
