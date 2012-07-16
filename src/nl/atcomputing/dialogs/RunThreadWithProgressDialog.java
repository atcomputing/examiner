package nl.atcomputing.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class RunThreadWithProgressDialog {
	private Thread runInBackground;
	private Runnable runOnUIThread;
	private Context context;
	private ProgressDialog dialog;
	
	public RunThreadWithProgressDialog(Context context, Thread runInBackground, Runnable runOnUIThreadAfterRunInBackground) {
		this.runInBackground = runInBackground;
		this.runOnUIThread = runOnUIThreadAfterRunInBackground;
		this.context = context;
	}
	
	public void run(String message) {
		this.dialog = ProgressDialog.show(this.context, "", 
				message, false);
		
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
	
	private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
        	dialog.dismiss();
        	runOnUIThread.run();
        }
    };
}
