package nl.atcomputing.lpic1examtrainer;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class RetrieveExamQuestions extends IntentService {
	private static final int NOTIFICATION_ID = 1;
	  /** 
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	   */
	  public RetrieveExamQuestions() {
	      super("RetrieveExameQuestionsService");
	  }

	  /**
	   * The IntentService calls this method from the default worker thread with
	   * the intent that started the service. When this method returns, IntentService
	   * stops the service, as appropriate.
	   */
	  @Override
	  protected void onHandleIntent(Intent intent) {
	      // Normally we would do some work here, like download a file.
	      // For our sample, we just sleep for 5 seconds.
	      long endTime = System.currentTimeMillis() + 5*1000;
	      while (System.currentTimeMillis() < endTime) {
	          synchronized (this) {
	              try {
	                  wait(endTime - System.currentTimeMillis());
	              } catch (Exception e) {
	              }
	          }
	      }
	      
	      // Notify user we completed download
	      String ns = Context.NOTIFICATION_SERVICE;
	      NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
	      Notification notification = new Notification(R.drawable.icon, getText(R.string.ticker_text),
	    	        System.currentTimeMillis());
	    	Intent notificationIntent = new Intent(this, RetrieveExamQuestions.class);
	    	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
	    	notification.setLatestEventInfo(this, getText(R.string.notification_title),
	    	        getText(R.string.notification_message), pendingIntent);
	    	mNotificationManager.notify(NOTIFICATION_ID, notification);
	  }
	}