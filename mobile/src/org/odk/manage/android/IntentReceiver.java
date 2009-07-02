package org.odk.manage.android;

import org.odk.common.android.FileHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

/**
 * Receives intents corresponding to <ol><li>Received SMS Messages</li>
 * <li>Mobile or wifi network state changes</li></ol>
 * and responds appropriately (mostly by passing off information to the 
 * {@link OdkManageService} Service.
 * 
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class IntentReceiver extends BroadcastReceiver {
  

  private SharedPreferences preferences;
  
  @Override
  public void onReceive(Context ctx, Intent intent) {
    Log.i("OdkManage","Received Intent. Action: " + intent.getAction());
    init(ctx);
    
    String action = intent.getAction();
    
    // Received SMS
    if (action.equals("android.provider.Telephony.SMS_RECEIVED")){
      parseSmsForOdkManageMessages(ctx, intent);
    } 
    // Connectivity change
    else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE") ||
               action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
      
      Log.d(Constants.TAG,"Connectivity change");
      startOdkManageService(ctx, OdkManageService.MessageType.CONNECTIVITY_CHANGE);
    } 
    // Boot completed
    else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
      Log.d(Constants.TAG,"Boot completed");
      startOdkManageService(ctx, OdkManageService.MessageType.BOOT_COMPLETED);
    } 
    // Package added
    else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
      Log.w(Constants.TAG, "Picked up package added!");
    } 
    // Unexpected intent
    else {
      Log.w(Constants.TAG, "Intent not supported.");
    }

  }
  
  private void startOdkManageService(Context ctx, 
      OdkManageService.MessageType messageType){
    
    Intent i = new Intent(ctx, OdkManageService.class);
    i.putExtra(OdkManageService.MESSAGE_TYPE_KEY, messageType);
    ctx.startService(i);
  }
  
  private void init(Context ctx){

    // initialize settings
    preferences = ctx.getSharedPreferences(Constants.PREFS_NAME, 0);

  }
  
  private void parseSmsForOdkManageMessages(Context ctx, Intent intent){
    SmsMessage msgs[] = getMessagesFromIntent(intent);
    String message = "";
    String number = "";

    for (SmsMessage msg: msgs) {
      message = msg.getDisplayMessageBody();
      number = msg.getOriginatingAddress();
      if (message != null && message.length() > 0 && number != null) {
        Log.d("OdkManage", "SMS Intercepted: " + message);
        if (isNewTasksTrigger(number,message)) {
          Intent i = new Intent(ctx, OdkManageService.class);
          i.putExtra(OdkManageService.MESSAGE_TYPE_KEY, 
              OdkManageService.MessageType.NEW_TASKS);
          ctx.startService(i);
        }
      }
    }
  }
  
  private boolean isNewTasksTrigger(String number, String message){
    //TODO(alerer): add 
    String serverNum = preferences.getString(Constants.MANAGE_SMS_PREF, "");
    Log.d(Constants.TAG, "In isNewTasksTrigger");
    Log.d(Constants.TAG, "number: " + number + "; serverNum: " + serverNum);

    return (serverNum != null &&
            serverNum.equals(number) &&
            message.startsWith(Constants.NEW_TASKS_TRIGGER));
  }

  // easiest way to extract messages from phone
  private SmsMessage[] getMessagesFromIntent(Intent intent) {
    SmsMessage retMsgs[] = null;
    Bundle bdl = intent.getExtras();
    try {
      Object pdus[] = (Object[]) bdl.get("pdus");
      retMsgs = new SmsMessage[pdus.length];
      for (int n = 0; n < pdus.length; n++) {
        byte[] byteData = (byte[]) pdus[n];
        retMsgs[n] = SmsMessage.createFromPdu(byteData);
      }

    } catch (Exception e) {
      Log.e("GetMessages", "fail", e);
    }
    return retMsgs;
  }
  
}