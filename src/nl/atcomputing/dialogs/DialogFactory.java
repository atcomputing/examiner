package nl.atcomputing.dialogs;


import nl.atcomputing.examtrainer.R;
import nl.atcomputing.examtrainer.database.ExamTrainerDbAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;


public class DialogFactory {
	
	public static Dialog createTwoButtonDialog(Context context, int msgResourceId, 
			int posButtonMsgResourceId, final Runnable posButtonAction,
			int negButtonMsgResourceId, final Runnable negButtonAction) {
		
		Dialog dialog;
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(context);
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_message, null);
		TextView tv = (TextView) view.findViewById(R.id.dialog_message);
		tv.setText(msgResourceId);
		builder.setView(view)
		.setPositiveButton(posButtonMsgResourceId, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				posButtonAction.run();
				dialog.dismiss();
			}
		})
		.setNegativeButton(negButtonMsgResourceId, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				negButtonAction.run();
				dialog.dismiss();
			}
		})
		.setCancelable(false);
		dialog = builder.create();
		
		return dialog;
	}
	
	public static Dialog createHintDialog(Context context, String message) {
	
		if( message == null ) {
			message = context.getString(R.string.hint_not_available);
		}
		
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_message, null);
		TextView tv = (TextView) view.findViewById(R.id.dialog_message);
		tv.setText(Html.fromHtml(message));
		
		Dialog dialog;
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(context);
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
	
	public static Dialog createUsageDialog(final Context context, final int messageResourceId) {
		
		//Check if user wants to see this message
		ExamTrainerDbAdapter db = new ExamTrainerDbAdapter(context);
		db.open();
		boolean showMessage = db.showMessage(messageResourceId);
		if( ! showMessage ) {
			db.close();
			return null;
		}
		
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_usage_message, null);
		TextView tv = (TextView) view.findViewById(R.id.dialog_usage_message);
		tv.setText(messageResourceId);
		
		CheckBox cb = (CheckBox) view.findViewById(R.id.dialog_usage_checkbox);
		
		//Default dialog will be set to show next time
		cb.setChecked(false);
		db.setShowDialog(messageResourceId, true);
		db.close();
		
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				ExamTrainerDbAdapter db = new ExamTrainerDbAdapter(context);
				db.open();
				db.setShowDialog(messageResourceId, ! isChecked);
				db.close();
			}
		});
		
		Dialog dialog;
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(context);
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
