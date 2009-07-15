package org.odk.manage.android;

import org.odk.manage.android.comm.CommunicationProtocol;
import org.odk.manage.android.comm.HttpAdapter;
import org.odk.manage.android.comm.SmsSender;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
  
  public void register(CommunicationProtocol protocol){
    switch (protocol){
      case SMS:
        registerBySms();
        break;
      case HTTP:
        registerByHttp();
        break;
    }
  }
  public void registerBySms(){
    Map<String,String> regMap = createRegisterMap();
    
    BroadcastReceiver onSent = new BroadcastReceiver(){
      public void onReceive(Context arg0, Intent arg1) {
        switch (getResultCode())
        {
          case Activity.RESULT_OK:
            Log.i(Constants.TAG, "Registration sent.");
            // this should really be done in the onDelivered intent, but it is 
            // to dangerous: if the message keeps failing to be delivered (e.g. 
            // bad #), we will waste tons of SMS's.
            prefsAdapter.setPreference(Constants.PREF_REGISTERED_IMSI_KEY, 
            propsAdapter.getIMSI());
            break;
          default:
            Log.e(Constants.TAG, "Registration SMS could not be sent");
        }}};
    BroadcastReceiver onDelivered = new BroadcastReceiver(){
      public void onReceive(Context arg0, Intent arg1) {
        switch (getResultCode())
        {
          case Activity.RESULT_OK:
//                  prefsAdapter.setPreference(Constants.PREF_REGISTERED_IMSI_KEY, 
//                      propsAdapter.getIMSI());
            Log.i(Constants.TAG, "Registration delivered.");
            break;
          case Activity.RESULT_CANCELED:
            Log.e(Constants.TAG, "Registration delivery failed.");
        }}};
    
    new SmsSender(ctx).sendSMS(
        prefsAdapter.getString(Constants.PREF_SMS_KEY, null), 
        Constants.SMS_REGISTER_ACTION,
        regMap,
        onSent,
        onDelivered);

  }
  
  public void registerByHttp(){
    Map<String,String> regMap = createRegisterMap();
    String url = prefsAdapter.getString(Constants.PREF_URL_KEY, null) + "/" + Constants.REGISTER_PATH;
    // we're not going to update the registered IMSI, because an HTTP registration is not sufficient
    // (it doesn't give the server a # validator)
    new HttpAdapter().doPost(url, regMap);
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
