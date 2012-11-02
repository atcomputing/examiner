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
public class ExamReviewFragment extends SherlockFragment {
	public static final String TAG = "ExamReviewActivity";
	private GridView scoresGrid;
	private ExamReviewAdapter adapter; 

	private ExamReviewListener listener;

	public interface ExamReviewListener {
		/**
		 * Called when user selects a question number
		 */
		public void onItemClickListener(long questionId);

		/**
		 * Called when user clicks the resume/start exam button
		 * @param fragment
		 * @param examId
		 */
		public void onButtonClickListener(SherlockFragment fragment, long examId);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Make sure activity implemented ExamQuestionListener
		try {
			this.listener = (ExamReviewListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ExamReviewListener");
		}

		long scoresId = ExamTrainer.getScoresId();
		this.adapter = new ExamReviewAdapter(activity, R.layout.review_exam_entry, scoresId);

		setHasOptionsMenu(true);
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

		this.scoresGrid = (GridView) activity.findViewById(R.id.review_exam_grid);
		this.scoresGrid.setAdapter(this.adapter);

		scoresGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM_REVIEW);
				listener.onItemClickListener(id);
			}
		});

		Button buttonStartExam = (Button) activity.findViewById(R.id.button_start_exam);
		long examId = ExamTrainer.getScoresId();
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(activity);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getResultsPerQuestion(examId);
		examinationDbHelper.close();
		if( cursor.getCount() < ExamTrainer.getAmountOfItems() ) {
			buttonStartExam.setVisibility(View.VISIBLE);
			buttonStartExam.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					listener.onButtonClickListener(ExamReviewFragment.this, ExamTrainer.getExamId());
				}
			});
		}




		UsageDialog usageDialog = UsageDialog.newInstance(activity, R.string.Usage_Dialog_examReviewScreenMessage);
		if( usageDialog != null ) {
			usageDialog.show(getFragmentManager(), "UsageDialog");
		}


	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Cursor cursor = this.adapter.getCursor();
		if ( cursor != null ) { 
			cursor.close();
		}
	}

	public int getAmountOfQuestionsAnswered() {
		return this.adapter.getCount();
	}
}
