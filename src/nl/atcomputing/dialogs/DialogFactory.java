package nl.atcomputing.dialogs;


import nl.atcomputing.examtrainer.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
				// TODO Auto-generated method stub
				posButtonAction.run();
				dialog.dismiss();
			}
		})
		.setNegativeButton(negButtonMsgResourceId, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				negButtonAction.run();
				dialog.dismiss();
			}
		})
		.setCancelable(false);
		dialog = builder.create();
		
		return dialog;
	}
	
	public static Dialog createOneButtonDialog(Context context, String message, 
			int posButtonMsgResourceId, OnClickListener posButtonListener) {
	
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
	
}
