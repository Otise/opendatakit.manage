package org.google.android.odk.manage;

import android.app.PendingIntent;
import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * This class acts as an adapter to abstract the SDK calls to phone properties 
 * such as IMEI number, phone number, battery life, network connection state, etc.
 * @author alerer@google.com (Your Name Here)
 *
 */
public class PhonePropertiesAdapter {

  private final Context ctx;
  private final TelephonyManager tm;
  
  public PhonePropertiesAdapter(Context ctx){
    this.ctx = ctx;
    this.tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
  }
  
  public String getIMEI(){
    return tm.getDeviceId();
  }
  
  public String getIMSI(){
    return tm.getSubscriberId();
  }
  
  public String getPhoneNumber(){
    return tm.getLine1Number();
  }
  
  public String getSimSerialNumber(){
    return tm.getSimSerialNumber();
  }
  
  /**
   * Registers an intent that is fired when a phone property changes. 
   * See {@link android.telephony.PhoneStateListener}.
   * @return
   */
  public void registerListener(PendingIntent onChange){
    //not yet implemented
  }
  
}
