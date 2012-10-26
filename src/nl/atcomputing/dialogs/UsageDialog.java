package nl.atcomputing.dialogs;


import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;


public class UsageDialog extends SherlockDialogFragment {

	public static UsageDialog newInstance(Context context, int msgResourceId) {
		//Check if user wants to see this message
		ExamTrainerDbAdapter db = new ExamTrainerDbAdapter(context);
		db.open();
		boolean showMessage = db.showMessage(msgResourceId);
		if( ! showMessage ) {
			db.close();
			return null;
		}

		UsageDialog f = new UsageDialog();

		Bundle args = new Bundle();
		args.putInt("msgResourceId", msgResourceId);
		f.setArguments(args);

		return f;
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
