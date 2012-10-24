package nl.atcomputing.dialogs;


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
	protected int msgResourceId;
	protected int posButtonMsgResourceId;
	protected int negButtonMsgResourceId;
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
		if( ( posButtonMsgResourceId != 0 ) && ( posButtonAction != null ) ) {
			builder.setNegativeButton(negButtonMsgResourceId, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					negButtonAction.run();
					dialog.dismiss();
				}
			});
		}
		builder.setCancelable(false);
		dialog = builder.create();

		return dialog;
	}
}
