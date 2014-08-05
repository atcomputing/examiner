package nl.atcomputing.examtrainer.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class RunThreadWithProgressDialog {
	private Thread runInBackground;
	private Runnable runOnUIThreadAfterRunInBackground;
	private Context context;
	private ProgressDialog dialog;
	private static Handler handler;
	
	private static class MyHandler extends Handler {
		private Dialog dialog;
		private Runnable runner;
		
		public MyHandler(Dialog dialog, Runnable runner) {
			super();
			this.dialog = dialog;
			this.runner = runner;
		}
		
        @Override
        public void handleMessage(Message msg) {
        	dialog.dismiss();
        	runner.run();
        }
    };
	
	public RunThreadWithProgressDialog(Context context, Thread runInBackground, Runnable runOnUIThreadAfterRunInBackground) {
		this.runInBackground = runInBackground;
		this.runOnUIThreadAfterRunInBackground = runOnUIThreadAfterRunInBackground;
		this.context = context;
	}
	
	public void run(String message) {
		this.dialog = ProgressDialog.show(this.context, "", 
				message, false);
		
		handler = new MyHandler(dialog, this.runOnUIThreadAfterRunInBackground);
		
		this.runInBackground.start();
		
		//Check if runInBackground is still running
		new Thread(new Runnable () {
			public void run() {
				while(runInBackground.isAlive()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				handler.sendEmptyMessage(0);
			}
		}).start();
	}
}
