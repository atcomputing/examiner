package nl.atcomputing.examtrainer;

import nl.atcomputing.dialogs.UsageDialog;
import nl.atcomputing.examtrainer.adapters.ExamReviewAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author martijn brekhof
 *
 */
public class ExamReviewFragment extends SherlockFragment {
	public static final String TAG = "ExamReviewActivity";
	private GridView scoresGrid;
	private ExamReviewAdapter adapter; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	//	public void onCreate(Bundle savedInstanceState) {
	//		super.onCreate(savedInstanceState);
	//
	//		setContentView(R.layout.review_exam);
	//		
	//		setTitle(ExamTrainer.getExamTitle());
	//	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.examreviewfragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		final Activity activity = getActivity();
		
		long examId = ExamTrainer.getScoresId();

		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(activity);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		Cursor cursor = examinationDbHelper.getResultsPerQuestion(examId);

		adapter = new ExamReviewAdapter(activity, R.layout.review_exam_entry, cursor);
		scoresGrid = (GridView) activity.findViewById(R.id.review_exam_grid);
		scoresGrid.setAdapter(adapter);

		//Disable reviewing exam items when not all questions have been answered yet.
		final int amountOfQuestionsAnswered = adapter.getCount();
		final int amountOfQuestions = examinationDbHelper.getQuestionsCount();

		if( amountOfQuestionsAnswered < amountOfQuestions ) {
			Button button = (Button) activity.findViewById(R.id.review_exam_resume_button);
			button.setOnClickListener(new OnClickListener() {				
				public void onClick(View v) {
//					Intent intent = new Intent(ExamReviewFragment.this, ExamQuestionFragment.class);
//					ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.EXAM);
//					//ExamTrainer.setQuestionId(adapter.getItemId(amountOfQuestionsAnswered - 1));
//					if( (ExamTrainer.getTimeLimit() > 0)  && ( ExamTrainer.timeLimitExceeded() ) ) {
//						ExamTrainer.setTimeLimit(0);
//						Toast.makeText(ExamReviewFragment.this, R.string.Time_limit_exceeded, Toast.LENGTH_SHORT).show();
//					} 
//					startActivity(intent);
//					finish();
				}
			});
			button.setVisibility(View.VISIBLE);
		}

		scoresGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if( amountOfQuestionsAnswered < amountOfQuestions ) {
					Toast.makeText(activity, R.string.Reviewing_questions_is_only_available_after_completing_the_exam,
							Toast.LENGTH_SHORT).show();
				} else {
//					Intent intent = new Intent(ExamReviewFragment.this, ExamQuestionFragment.class);
//					ExamTrainer.setExamMode(ExamTrainer.ExamTrainerMode.REVIEW);
//					//ExamTrainer.setQuestionId(intent, adapter.getItemId(position));
//					startActivity(intent);
				}
			}
		});
		examinationDbHelper.close();
		UsageDialog usageDialog = UsageDialog.newInstance(activity, R.string.Usage_Dialog_examReviewScreenMessage);
		if( usageDialog != null ) {
			usageDialog.show(getFragmentManager(), "UsageDialog");
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Cursor cursor = adapter.getCursor();
		if ( cursor != null ) { 
			cursor.close();
		}
	}
}
