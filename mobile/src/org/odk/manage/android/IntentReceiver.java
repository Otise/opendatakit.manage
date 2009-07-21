package org.odk.manage.android;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
  

  private SharedPreferencesAdapter prefsAdapter;
  
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
      startOdkManageService(ctx, OdkManageService.MessageType.CONNECTIVITY_CHANGE, null);
    } 
    // Boot completed
    else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
      Log.d(Constants.TAG,"Boot completed");
      startOdkManageService(ctx, OdkManageService.MessageType.BOOT_COMPLETED, null);
    } 
    // Package added
    else if (action.equals(Intent.ACTION_PACKAGE_ADDED) ||
        isIntentForThisPackageRemoved(intent)) {
      Log.w(Constants.TAG, "Picked up package added: " + intent.getDataString() + "!");
      Map<String, String> extras = new HashMap<String, String>(1);
      extras.put("packageName", intent.getData().getSchemeSpecificPart());
      startOdkManageService(ctx, OdkManageService.MessageType.PACKAGE_ADDED, extras);
    } 
    // Unexpected intent
    else {
      Log.w(Constants.TAG, "Intent not supported.");
    }

  }
  
  /**
   * This is a hack because Android is stupid and won't let an 
   * application detect when it is itself added. Therefore, it 
   * can only detect iself being removed.
   */
  private boolean isIntentForThisPackageRemoved(Intent i){
    if (i.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
      String data = i.getData().getSchemeSpecificPart();
      String thisPackage = getClass().getPackage().getName();
      if (data.equals(thisPackage)) {
        return true;
      }
    }
    return false;
  }
  private void startOdkManageService(Context ctx, 
      OdkManageService.MessageType messageType, Map<String, String> extras){
    
    Intent i = new Intent(ctx, OdkManageService.class);
    i.putExtra(OdkManageService.MESSAGE_TYPE_KEY, messageType);
    if (extras != null) {
      for (String key: extras.keySet()) {
        i.putExtra(key, extras.get(key));
      }
    }
    ctx.startService(i);
  }
  
  private void init(Context ctx){

    // initialize settings
    prefsAdapter = new SharedPreferencesAdapter(ctx);

  }
  
  private void parseSmsForOdkManageMessages(Context ctx, Intent intent){
    SmsMessage msgs[] = getMessagesFromIntent(intent);
    String message;
    String number;

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
    String serverNum = prefsAdapter.getString(Constants.PREF_SMS_KEY, "");
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