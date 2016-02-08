/**
 * 
 * Copyright 2011 AT Computing BV
 *
 * This file is part of Examiner.
 *
 * Examiner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Examiner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Examiner.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.atcomputing.examtrainer.fragments;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.adapters.ExamReviewAdapter;
import nl.atcomputing.examtrainer.database.ExaminationDbAdapter;
import nl.atcomputing.examtrainer.dialogs.UsageDialog;
import nl.atcomputing.examtrainer.main.ExamTrainer;
import android.app.Activity;
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

/**
 * @author martijn brekhof
 *
 */
public class ExamReviewFragment extends AbstractFragment {
	public static final String TAG = "ExamReviewActivity";
	private long examID;
	private long score;
	
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

		Activity activity = getActivity();

		setupScoresGrid();

		this.examID = ExamTrainer.getScoresId();

		UsageDialog usageDialog = UsageDialog.newInstance(activity, R.string.Usage_Dialog_examReviewScreenMessage);
		if( usageDialog != null ) {
			usageDialog.show(getFragmentManager(), "UsageDialog");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Activity activity = getActivity();
		
		ExaminationDbAdapter examinationDbHelper = new ExaminationDbAdapter(activity);
		examinationDbHelper.open(ExamTrainer.getExamDatabaseName());
		this.score = examinationDbHelper.getScore(this.examID);
		examinationDbHelper.close();
		
		Button buttonStartExam = (Button) activity.findViewById(R.id.button_start_exam);
		if( this.score == -1 ) {
			buttonStartExam.setVisibility(View.VISIBLE);
			buttonStartExam.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					abstractFragmentListener.onButtonClickListener(ExamReviewFragment.this, ExamTrainer.getExamId());
				}
			});
		} else {
			buttonStartExam.setVisibility(View.GONE);
		}
		
		setupScoresGrid();
	}
	
	public void setExamID(long id) {
		this.examID = id;
	}
	
	public long getExamID() {
		return this.examID;
	}
	
	private void setupScoresGrid() {
		final Activity activity = getActivity();
		
		ExamReviewAdapter adapter = new ExamReviewAdapter(activity, R.layout.review_exam_entry, ExamTrainer.getScoresId());

		GridView scoresGrid = (GridView) activity.findViewById(R.id.review_exam_grid);
		scoresGrid.setAdapter(adapter);

		scoresGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if( score == -1 ) {
					Toast.makeText(activity, R.string.Reviewing_questions_is_only_available_after_completing_the_exam,
							Toast.LENGTH_SHORT).show();
				} else {
					abstractFragmentListener.onItemClickListener(id);
				}
			}
		});
	}


	@Override
	public void updateView() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getTitle() {
		return ExamTrainer.getExamTitle();
	}
}
