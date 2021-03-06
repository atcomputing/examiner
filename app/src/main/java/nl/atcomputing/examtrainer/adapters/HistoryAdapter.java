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

package  nl.atcomputing.examtrainer.adapters;

import java.util.HashMap;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExaminationDatabaseHelper;
import nl.atcomputing.examtrainer.main.ExamTrainer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author martijn brekhof
 *
 */

public class HistoryAdapter extends CursorAdapter  {
	private int layout;
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Boolean> itemChecked = new HashMap<Integer, Boolean>();

	@SuppressWarnings("deprecation")
	public HistoryAdapter(Activity activity, int layout, Cursor c) {
		super(activity, c);
		this.layout = layout;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int index = cursor.getColumnIndex(ExaminationDatabaseHelper.Scores.COLUMN_NAME_DATE);
		String examDate = ExamTrainer.convertEpochToString(cursor.getLong(index));
		TextView scoreEntryDate = (TextView) view.findViewById(R.id.historyEntryDate);
		scoreEntryDate.setText(examDate);
		
		index = cursor.getColumnIndex(ExaminationDatabaseHelper.Scores.COLUMN_NAME_SCORE);
		int examScore = cursor.getInt(index);
		if( examScore > -1 ) {
			TextView scoreEntryScore = (TextView) view.findViewById(R.id.historyEntryScore);
			scoreEntryScore.setText(Integer.toString(examScore));
		}
		
		ImageView scoreEntryImage = (ImageView) view.findViewById(R.id.historyEntryPass);

		if( examScore >= ExamTrainer.getItemsNeededToPass() ) { 
			scoreEntryImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_green_check));
		} else if ( examScore > -1 ) {
			scoreEntryImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_red_cross));
		} else {
			scoreEntryImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_hint));
		}

		index = cursor.getColumnIndex(ExaminationDatabaseHelper.Scores._ID);
		final int scoresId = cursor.getInt(index);

		CheckBox cbox = (CheckBox) view.findViewById(R.id.historyCheckBox);
		cbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				itemChecked.put(scoresId, isChecked);
			}
		});

		Boolean checked = itemChecked.get(scoresId);
		if ( checked == null ) {
			cbox.setChecked(false);
		} else {
			cbox.setChecked(checked);
		}
	}

	@Override
	public View newView(Context context, Cursor myCursor, ViewGroup parent) {
		final LayoutInflater mInflater = LayoutInflater.from(context);
		View view = (View) mInflater.inflate(layout, parent, false);
		return view;
	}
	
	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		this.itemChecked.clear();
	}
	
	public HashMap<Integer, Boolean> getItemsChecked() {
		return this.itemChecked;
	}
	
	public void setItemsChecked(HashMap<Integer, Boolean> itemsChecked) {
		this.itemChecked = itemsChecked;
	}
}