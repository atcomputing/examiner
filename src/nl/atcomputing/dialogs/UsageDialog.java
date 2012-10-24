package nl.atcomputing.dialogs;


import com.actionbarsherlock.app.SherlockDialogFragment;

import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;


public class UsageDialog extends SherlockDialogFragment {
	
	public static UsageDialog newInstance(int msgResourceId) {
		UsageDialog f = new UsageDialog();

        Bundle args = new Bundle();
        args.putInt("msgResourceId", msgResourceId);
        f.setArguments(args);

        return f;
    }
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final int msgResourceId = getArguments().getInt("msgResourceId");
        
		final Activity activity = getActivity();
		//Check if user wants to see this message
		ExamTrainerDbAdapter db = new ExamTrainerDbAdapter(activity);
		db.open();
		boolean showMessage = db.showMessage(msgResourceId);
		if( ! showMessage ) {
			db.close();
			return null;
		}
		
		View view = LayoutInflater.from(activity).inflate(R.layout.dialog_usage_message, null);
		TextView tv = (TextView) view.findViewById(R.id.dialog_usage_message);
		tv.setText(msgResourceId);
		
		CheckBox cb = (CheckBox) view.findViewById(R.id.dialog_usage_checkbox);
		
		//Default dialog will be set to show next time
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
