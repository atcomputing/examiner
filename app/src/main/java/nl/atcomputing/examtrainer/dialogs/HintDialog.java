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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;


public class HintDialog extends SherlockDialogFragment {
	
	public static HintDialog newInstance(String message) {
		HintDialog f = new HintDialog();

        Bundle args = new Bundle();
        args.putString("message", message);
        f.setArguments(args);

        return f;
    }
	
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = getArguments().getString("message");
		
		Activity activity = getActivity();
		
		View view = LayoutInflater.from(activity).inflate(R.layout.dialog_message, null);
		TextView tv = (TextView) view.findViewById(R.id.dialog_message);
		tv.setText(Html.fromHtml(message));
		
		Dialog dialog;
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(activity);
		builder.setView(view)
		.setCancelable(false)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		dialog = builder.create();
		return dialog;
    }
}
