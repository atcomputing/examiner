package nl.atcomputing.examtrainer.dialogs;


import nl.atcomputing.examtrainer.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;


public class ErrorDialog extends SherlockDialogFragment {
	
	private DialogInterface.OnClickListener onClickListener;
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
				if( onClickListener != null ) {
					onClickListener.onClick(dialog, id);
				}
				dialog.dismiss();
			}
		});
		dialog = builder.create();
		return dialog;
    }
}
