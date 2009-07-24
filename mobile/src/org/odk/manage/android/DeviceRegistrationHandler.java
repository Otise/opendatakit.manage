package org.odk.manage.android;

import org.odk.manage.android.comm.CommunicationProtocol;
import org.odk.manage.android.comm.HttpAdapter;
import org.odk.manage.android.comm.SmsSender;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class DeviceRegistrationHandler {
  
  private Context ctx;
  private PhonePropertiesAdapter propsAdapter;
  private SharedPreferencesAdapter prefsAdapter;
  
  public DeviceRegistrationHandler(Context ctx){
    this.ctx = ctx;
    this.propsAdapter = new PhonePropertiesAdapter(ctx);
    this.prefsAdapter = new SharedPreferencesAdapter(ctx);
  }
  
  /**
   * 
   * @return true if there is a SIM card, and the device hasn't been registered 
   * with that SIM card. Returns false if there is a registration SMS in transit.
   */
  public boolean registrationNeededForImei(){
    boolean registrationInProgress = prefsAdapter
        .getBoolean(Constants.PREFS_REG_IN_PROGRESS_KEY, false);
    if (registrationInProgress){
      Log.i(Constants.TAG, "Registration in progress - will not register again.");
      return false;
    }
    String registeredImsi = prefsAdapter.getString(Constants.PREF_REGISTERED_IMSI_KEY, null);
    String newImsi = propsAdapter.getIMSI();
    //if the IMSI has changed, then we want to send a registration
    if (newImsi==null) {
      return registeredImsi != null;
    }
    else return !newImsi.equals(registeredImsi);
  }
  
  /**
   * 
   * @param return Toast If true, a Toast will display with the result of the registration.
   */
  public void register(boolean returnToast) {
    if (propsAdapter.getIMSI() == null || !prefsAdapter.getBoolean(Constants.PREF_SMS_ENABLED_KEY, false)) {
      registerByHttp(returnToast);
      return;
    } else {
      registerBySms(returnToast);
    }
  }
  
  public void registerBySms(boolean returnToast){
    final boolean fReturnToast = returnToast;
    prefsAdapter.setPreference(Constants.PREFS_REG_IN_PROGRESS_KEY, true);
    Map<String,String> regMap = createRegisterMap();
    BroadcastReceiver onSent = new BroadcastReceiver(){
      public void onReceive(Context arg0, Intent arg1) {
        prefsAdapter.setPreference(Constants.PREFS_REG_IN_PROGRESS_KEY, false);
        switch (getResultCode())
        {
          case Activity.RESULT_OK:
            Log.i(Constants.TAG, "Registration sent.");
            if (fReturnToast){
              Toast.makeText(ctx, "Registration by SMS was sent.", Toast.LENGTH_LONG).show();
            }
            // this should really be done in the onDelivered intent, but it is 
            // too dangerous: if the message keeps failing to be delivered (e.g. 
            // bad #), we will waste tons of SMS's.
            prefsAdapter.setPreference(Constants.PREF_REGISTERED_IMSI_KEY, 
            propsAdapter.getIMSI());
            break;
          default:
            Log.e(Constants.TAG, "Registration SMS could not be sent");
            if (fReturnToast){
              Toast.makeText(ctx, "Registration by SMS could not be sent.", Toast.LENGTH_LONG).show();
            }
        }}};
    BroadcastReceiver onDelivered = new BroadcastReceiver(){
      public void onReceive(Context arg0, Intent arg1) {
        switch (getResultCode())
        {
          case Activity.RESULT_OK:
//                  prefsAdapter.setPreference(Constants.PREF_REGISTERED_IMSI_KEY, 
//                      propsAdapter.getIMSI());
            Log.i(Constants.TAG, "Registration delivered.");
            if (fReturnToast){
              Toast.makeText(ctx, "Registration by SMS was delivered.", Toast.LENGTH_LONG).show();
            }
            break;
          case Activity.RESULT_CANCELED:
            Log.e(Constants.TAG, "Registration delivery failed.");
            if (fReturnToast){
              Toast.makeText(ctx, "Registration by SMS was not delivered.", Toast.LENGTH_LONG).show();
            }
        }}};
    
    new SmsSender(ctx).sendSMS(
        prefsAdapter.getString(Constants.PREF_SMS_KEY, null), 
        Constants.SMS_REGISTER_ACTION,
        regMap,
        onSent,
        onDelivered);

  }
  
  public void registerByHttp(boolean returnToast){
    Map<String,String> regMap = createRegisterMap();
    String url = prefsAdapter.getString(Constants.PREF_URL_KEY, null) + "/" + Constants.REGISTER_PATH;
    // we're not going to update the registered IMSI, because an HTTP registration is not sufficient
    // (it doesn't give the server a # validator)
    boolean success = new HttpAdapter().doPost(url, regMap);
    if (returnToast) {
      if (success) {
        Toast.makeText(ctx, "Registration by HTTP was successful.", Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(ctx, "Registration by HTTP was unsuccessful.", Toast.LENGTH_LONG).show();
      }
    }
  }
  
  private Map<String,String> createRegisterMap(){
    Map<String,String> paramMap = new HashMap<String,String>();
    newProperty("userid",prefsAdapter.getString(Constants.PREF_USERID_KEY, ""), 
        paramMap);
    newProperty("imei",propsAdapter.getIMEI(), paramMap);
    newProperty("phonenumber",propsAdapter.getPhoneNumber(), paramMap);
    newProperty("sim",propsAdapter.getSimSerialNumber(), paramMap);
    newProperty("imsi",propsAdapter.getIMSI(), paramMap);
    return paramMap;
  }
  
  public void newProperty(String name, String value, Map<String,String> paramMap){
    if (name == null || value == null)
      return;
    Log.d("OdkManage","New registration property: <" + name + "," + value + ">");
    paramMap.put(name, value);
  }
}
