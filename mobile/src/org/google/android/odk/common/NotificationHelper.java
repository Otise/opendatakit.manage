package org.google.android.odk.common;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.google.android.odk.manage.R;

public class NotificationHelper {

  final Context ctx;
  final NotificationManager nm;
  int currentId = 0;
  
  public NotificationHelper(Context ctx){
    this.ctx = ctx;
    this.nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);  
    
  }
  public int setNotification(String text){
       Notification notif = new Notification(R.drawable.paper_icon, text,  System.currentTimeMillis()); 
       ComponentName comp = new ComponentName(ctx.getPackageName(), getClass().getName());
       Intent intent = new Intent().setComponent(comp);
       PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 
           Intent.FLAG_ACTIVITY_NEW_TASK);
       notif.setLatestEventInfo(ctx, "ODK Manage", text, pendingIntent);
       nm.notify(++currentId,notif);
       return currentId;
  }
 
  public void stopNotification(int id){
      nm.cancel(id);
  }



}

//The relation between a valid Notification and the call to setLatestEventInfo is at least unclear 
//from the documentation.
//
//This is code that works:
//
//protected void notify(Context context, Boolean on){
//        NotificationManager nm = 
//(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//        ComponentName comp = new ComponentName(context.getPackageName(), 
//getClass().getName());
//        Intent intent = new Intent().setComponent(comp);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
//Intent.FLAG_ACTIVITY_NEW_TASK);
//        Notification n = new Notification(R.drawable.icon, "Message", 
//System.currentTimeMillis());
//        n.setLatestEventInfo(context, "Title", "Text", pendingIntent);
//        nm.notify(22, n);
//    }
//
//
//Omitting the call to setLatestEventInfo or passing in null for the PendingEvent parameter however 
//results in an IllegalArgumentException with something like 'contentView required' when the 
//notification is about to be put up on the status screen and not when the notification is issued.
//
//While it could be argued that this is simply a documentation issue, from an OO point of view it 
//should not be possible to construct a notification object where an essential element is missing. The 
//PendingIntent should go on the notification constructor, rather than being an optional call that is 
//however required at the penalty of a runtime error.
//
//Alternatively, why is it required that an intent is associated with a notification? It would be 
//better if by default, there was a no-op if one clicked on the Notification: it could simply be a 
//status message, with no further action possible (e.g. "Lost GPS reception"), so null should be 
//allowed for the PendingIntent.

