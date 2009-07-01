package org.google.android.odk.manage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SmsSender {
  
  private Context ctx;
  
  public SmsSender(Context ctx){
    this.ctx = ctx;
  }
  
  public void sendSMS(String phoneNumber, String action, Map<String,String> parameters){
    sendSMS(phoneNumber, createSmsWithParameters(action, parameters));
  }
  
  //---sends an SMS message to another device---
  public void sendSMS(String phoneNumber, String message)
  {        
      String SENT = "SMS_SENT";
      String DELIVERED = "SMS_DELIVERED";

      PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0,
          new Intent(SENT), 0);

      PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0,
          new Intent(DELIVERED), 0);

      //---when the SMS has been sent---
      ctx.registerReceiver(new BroadcastReceiver(){
          @Override
          public void onReceive(Context arg0, Intent arg1) {
              switch (getResultCode())
              {
                  case Activity.RESULT_OK:
                      Toast.makeText(ctx, "SMS sent", 
                              Toast.LENGTH_SHORT).show();
                      break;
                  case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                      Toast.makeText(ctx, "Generic failure", 
                              Toast.LENGTH_SHORT).show();
                      break;
                  case SmsManager.RESULT_ERROR_NO_SERVICE:
                      Toast.makeText(ctx, "No service", 
                              Toast.LENGTH_SHORT).show();
                      break;
                  case SmsManager.RESULT_ERROR_NULL_PDU:
                      Toast.makeText(ctx, "Null PDU", 
                              Toast.LENGTH_SHORT).show();
                      break;
                  case SmsManager.RESULT_ERROR_RADIO_OFF:
                      Toast.makeText(ctx, "Radio off", 
                              Toast.LENGTH_SHORT).show();
                      break;
              }
          }
      }, new IntentFilter(SENT));

      //---when the SMS has been delivered---
      ctx.registerReceiver(new BroadcastReceiver(){
          @Override
          public void onReceive(Context arg0, Intent arg1) {
              switch (getResultCode())
              {
                  case Activity.RESULT_OK:
                      Toast.makeText(ctx, "SMS delivered", 
                              Toast.LENGTH_SHORT).show();
                      break;
                  case Activity.RESULT_CANCELED:
                      Toast.makeText(ctx, "SMS not delivered", 
                              Toast.LENGTH_SHORT).show();
                      break;                        
              }
          }
      }, new IntentFilter(DELIVERED));        
      SmsManager sms = SmsManager.getDefault();
      String fullMessage =  Constants.MANAGE_SMS_TOKEN + " " + message;
      Log.i("OdkManage", "Sms sent: Recipient: " + phoneNumber + "; Message: " + fullMessage);
      sms.sendTextMessage(phoneNumber, null, fullMessage, sentPI, deliveredPI);        
  }
  
  private String createSmsWithParameters(String action, Map<String,String> parameters){
    List<String> props = new ArrayList<String>();
    for (String prop : parameters.keySet()){
      if (prop != null && parameters.get(prop) != null){
        props.add(prop + "=" + parameters.get(prop));
      }
    }
    return action + " " + join(props, "&");
  }
  
  public String join(List<String> s, String delimiter) {
    if (s.isEmpty()) return "";
    Iterator<String> iter = s.iterator();
    StringBuffer buffer = new StringBuffer(iter.next());
    while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
    return buffer.toString();
  }
  
}
