package org.odk.manage.android.comm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.gsm.SmsManager;
import android.util.Log;

import org.odk.manage.android.Constants;
import org.odk.manage.android.SharedPreferencesAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for sending SMS.
 * 
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class SmsSender {
  
  private Context ctx;
  private SharedPreferencesAdapter prefsAdapter;
  
  public SmsSender(Context ctx){
    this.ctx = ctx;
    this.prefsAdapter = new SharedPreferencesAdapter(ctx);
  }
  
  private static int messageIndex = 0;
  
  /**
   * 
   * @param phoneNumber The phone number to send to.
   * @param action The ODK action, e.g. 'reg'.
   * @param parameters A map of parameters for the SMS.
   * @param onSent A BroadcastReceiver that will be called when the message is sent.
   * @param onDelivered A BroadcastReceiver that will be called when the message is delivered.
   */
  public void sendSMS(String phoneNumber, String action, Map<String,String> parameters, BroadcastReceiver onSent, BroadcastReceiver onDelivered){
    sendSMS(phoneNumber, createSmsWithParameters(action, parameters), onSent, onDelivered);
  }
  
  /**
   * 
   * @param phoneNumber The phone number to send to.
   * @param content The message content.
   * @param onSent A BroadcastReceiver that will be called when the message is sent.
   * @param onDelivered A BroadcastReceiver that will be called when the message is delivered.
   */
  public void sendSMS(String phoneNumber, String content, BroadcastReceiver onSent, BroadcastReceiver onDelivered)
  {        
      
    /**
     * This is a hard check to make sure that SMS's are NEVER sent if SMS 
     * sending is disabled.
     */
    if (!prefsAdapter.getBoolean(Constants.PREF_SMS_ENABLED_KEY, false)){
      Log.e(Constants.TAG, "Application tried to send SMS while SMS was disabled.");
      return;
    }
    messageIndex++;
    String DELIVERED = "SMS_DELIVERED_" + messageIndex;
    String SENT = "SMS_SENT_" + messageIndex;
    
    PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0,
        new Intent(SENT), 0);
    ctx.registerReceiver(onSent, new IntentFilter(SENT));   
    
    PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0,
        new Intent(DELIVERED), 0);
    ctx.registerReceiver(onDelivered, new IntentFilter(DELIVERED));   

    
    SmsManager sms = SmsManager.getDefault();
    String fullMessage =  
      (Constants.MANAGE_SMS_TOKEN==null?"":Constants.MANAGE_SMS_TOKEN) + " " + content;
    
    sms.sendTextMessage(phoneNumber, null, fullMessage, sentPI, deliveredPI); 
    Log.i("OdkManage", "Sms sent: Recipient: " + phoneNumber + "; Message: " + fullMessage);
  }
  
  /**
   * 
   * @param action The ODK action, e.g. 'reg'.
   * @param parameters A map of parameters for the SMS.
   * @return The SMS content, e.g. 'reg imei=12345&userid=johndoe'
   */
  private String createSmsWithParameters(String action, Map<String,String> parameters){
    List<String> props = new ArrayList<String>();
    for (String prop : parameters.keySet()){
      if (prop != null && parameters.get(prop) != null){
        props.add(prop + "=" + parameters.get(prop));
      }
    }
    return action + " " + join(props, "&");
  }
  
  private String join(List<String> s, String delimiter) {
    if (s.isEmpty()) return "";
    Iterator<String> iter = s.iterator();
    StringBuffer buffer = new StringBuffer(iter.next());
    while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
    return buffer.toString();
  }
  
}
