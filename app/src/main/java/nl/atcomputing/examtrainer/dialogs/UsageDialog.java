/**
 * 
 * Copyright 2012 AT Computing BV
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

package nl.atcomputing.examtrainer.dialogs;


import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;


public class UsageDialog extends DialogFragment {

	/**
	 * Creates a new instance of a usage dialog for the given message
	 * @param context
	 * @param msgResourceId Resource identifier to message to be displayed
	 * @return UsageDialog or null if dialog should not be shown
	 */
	public static UsageDialog newInstance(Context context, int msgResourceId) {
		//Check if user wants to see this message
		ExamTrainerDbAdapter db = new ExamTrainerDbAdapter(context);
		db.open();
		boolean showMessage = db.showMessage(msgResourceId);
		db.close();
		if( ! showMessage ) {
			return null;
		}
		
		UsageDialog f = new UsageDialog();

		Bundle args = new Bundle();
		args.putInt("msgResourceId", msgResourceId);
		f.setArguments(args);

		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_usage_message, container, false);

		
		final int msgResourceId = getArguments().getInt("msgResourceId");

		final Activity activity = getActivity();

		TextView tv = (TextView) view.findViewById(R.id.dialog_usage_message);
		tv.setText(msgResourceId);

		CheckBox cb = (CheckBox) view.findViewById(R.id.dialog_usage_checkbox);

		ExamTrainerDbAdapter db = new ExamTrainerDbAdapter(activity);
		db.open();
		cb.setChecked(false);
		db.setShowDialog(msgResourceId, true);
		db.close();

		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				ExamTrainerDbAdapter db = new ExamTrainerDbAdapter(activity);
				db.open();
				db.setShowDialog(msgResourceId, ! isChecked);
				db.close();
			}
		});

		Button button = (Button) view.findViewById(R.id.dialog_usage_button);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				getDialog().dismiss();
			}
		});

		return view;
	}

}
