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


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;

import nl.atcomputing.examtrainer.R;

public class HintDialog extends DialogFragment {

	public static HintDialog newInstance(String message) {
		HintDialog f = new HintDialog();

		Bundle args = new Bundle();
		args.putString("message", message);
		f.setArguments(args);

		return f;
	}


	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = getArguments().getString("message");

		Dialog dialog;
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(Html.fromHtml(message))
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
