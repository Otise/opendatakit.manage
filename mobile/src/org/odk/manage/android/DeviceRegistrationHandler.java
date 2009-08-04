package org.odk.manage.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.odk.manage.android.comm.HttpAdapter;
import org.odk.manage.android.comm.SmsSender;

import java.util.HashMap;
import java.util.Map;

/**
 * This handler is responsible for carrying out device registration with the 
 * ODK Manage server.
 * 
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class DeviceRegistrationHandler {
  
  private Context ctx;
  private PhonePropertiesAdapter propsAdapter;
  private SharedPreferencesAdapter prefsAdapter;
  
  private static final String NO_IMSI = "-1";
  
  public DeviceRegistrationHandler(Context ctx){
    this.ctx = ctx;
    this.propsAdapter = new PhonePropertiesAdapter(ctx);
    this.prefsAdapter = new SharedPreferencesAdapter(ctx);
  }
  
  /**
   * 
   * @return true if the IMSI has changed since the last registration.
   *  Returns false if there is a registration SMS in transit.
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
    //if we have never registered, or the IMSI has changed, 
    // then we want to send a registration
    if (registeredImsi == null) {
      return true;
    } else if (newImsi == null) {
      return registeredImsi.equals(NO_IMSI);
    } else{
      return !registeredImsi.equals(newImsi);
    }
  }
  
  /**
   * Attempts to register the device with the server.
   * 
   * @param returnToast If true, a Toast will display with the result of the registration.
   */
  public void register(boolean returnToast) {
    if (propsAdapter.getIMSI() == null || !prefsAdapter.getBoolean(Constants.PREF_SMS_ENABLED_KEY, false)) {
      registerByHttp(returnToast);
      return;
    } else {
      registerBySms(returnToast);
    }
  }
  
  /**
   * Attempts to register the devices with the server by SMS (asynchronously).
   * @param returnToast
   */
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
            prefsAdapter.setPreference(Constants.PREF_REGISTERED_IMSI_KEY, 
            propsAdapter.getIMSI()==null?NO_IMSI:propsAdapter.getIMSI());
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
            prefsAdapter.setPreference(Constants.PREF_REGISTERED_IMSI_KEY, 
                propsAdapter.getIMSI()==null?NO_IMSI:propsAdapter.getIMSI());
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
  
  /**
   * Attempts to register the device with the server.
   * TODO(alerer): should be done asynchronously.
   * @param returnToast
   */
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
        Toast.makeText(ctx, "Registration by HTTP was unsuccessful. " +
            "Try connecting to wifi or entering data range.", Toast.LENGTH_LONG).show();
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
  
  private void newProperty(String name, String value, Map<String,String> paramMap){
    if (name == null || value == null)
      return;
    Log.d("OdkManage","New registration property: <" + name + "," + value + ">");
    paramMap.put(name, value);
  }
}
