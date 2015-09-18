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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;


public class TwoButtonDialog extends SherlockDialogFragment {
	protected int posButtonMsgResourceId;
	protected int negButtonMsgResourceId = R.string.cancel;
	protected Runnable posButtonAction;
	protected Runnable negButtonAction;

	public static TwoButtonDialog newInstance(int msgResourceId) {
		TwoButtonDialog f = new TwoButtonDialog();

		Bundle args = new Bundle();
		args.putInt("msgResourceId", msgResourceId);
		f.setArguments(args);

		return f;
	}

	public void setPositiveButton(int posButtonMsgResourceId, Runnable posButtonAction) {
		this.posButtonMsgResourceId = posButtonMsgResourceId;
		this.posButtonAction = posButtonAction;
	}

	public void setNegativeButton(int negButtonMsgResourceId, Runnable negButtonAction) {
		this.negButtonMsgResourceId = negButtonMsgResourceId;
		this.negButtonAction = negButtonAction;
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Activity activity = getActivity();

		int msgResourceId = getArguments().getInt("msgResourceId");

		Dialog dialog;
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(activity);
		View view = LayoutInflater.from(activity).inflate(R.layout.dialog_message, null);
		TextView tv = (TextView) view.findViewById(R.id.dialog_message);
		tv.setText(msgResourceId);
		builder.setView(view);
		if( ( posButtonMsgResourceId != 0 ) && ( posButtonAction != null ) ) {
			builder.setPositiveButton(posButtonMsgResourceId, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					posButtonAction.run();
					dialog.dismiss();
				}
			});
		}
		builder.setNegativeButton(negButtonMsgResourceId, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if( negButtonAction != null ) {
					negButtonAction.run();
				}
				dialog.dismiss();
			}
		});
		builder.setCancelable(false);
		dialog = builder.create();

		return dialog;
	}
}
