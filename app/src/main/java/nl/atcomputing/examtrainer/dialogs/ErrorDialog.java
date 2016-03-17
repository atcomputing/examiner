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
import android.support.v4.app.DialogFragment;


public class ErrorDialog extends DialogFragment {

	private DialogInterface.OnDismissListener onDismissListener;
	
	public static ErrorDialog newInstance(String message) {
		ErrorDialog f = new ErrorDialog();

        Bundle args = new Bundle();
        args.putString("message", message);
        f.setArguments(args);

        return f;
    }
	
	public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
		this.onDismissListener = listener;
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		this.onDismissListener.onDismiss(dialog);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = getArguments().getString("message");
		
		Activity activity = getActivity();
		
		Dialog dialog;
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.Error) 
		.setMessage(message)
		.setCancelable(false)
		.setPositiveButton(R.string.Close, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		dialog = builder.create();
		return dialog;
    }
}
