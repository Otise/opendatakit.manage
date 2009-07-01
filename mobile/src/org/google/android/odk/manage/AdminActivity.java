package org.google.android.odk.manage;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.google.android.odk.common.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AdminActivity extends Activity {
  
  private PhonePropertiesAdapter phoneProperties = null;
  private SharedPreferences settings;
  
  private final Activity ctx = this;
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      settings = getSharedPreferences(Constants.PREFS_NAME, 0);
      Log.w(Constants.TAG,"Parsed long: " + Long.parseLong("-2026936769"));
      init();
  }
  
  @Override
  public void onStop(){
    super.onStop();
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(Constants.MANAGE_URL_PREF, ((EditText) 
        findViewById(R.id.server_url_text)).getText().toString());
    editor.putString(Constants.MANAGE_SMS_PREF, ((EditText) 
        findViewById(R.id.sms_number_text)).getText().toString());
    editor.putString(Constants.MANAGE_USERID_PREF, ((EditText) 
        findViewById(R.id.user_id_text)).getText().toString());
    editor.commit();
  }
  
  private void init(){
    setContentView(R.layout.main);
    
    // initialize user-entered fields
    initText(R.id.user_id_text, Constants.MANAGE_USERID_PREF);
    initText(R.id.sms_number_text, Constants.MANAGE_SMS_PREF);
    initText(R.id.server_url_text, Constants.MANAGE_URL_PREF);
    
    // create handler for SMS register button
    Button registerSmsButton = (Button) findViewById(R.id.register_phone_button_sms);
    registerSmsButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i(Constants.TAG,"Sending phone registration SMS");
        SmsManager sm = SmsManager.getDefault();
        try{
          EditText numField = (EditText) findViewById(R.id.sms_number_text);
          EditText userIdField = (EditText) findViewById(R.id.user_id_text);
          new SmsSender(ctx).sendSMS(numField.getText().toString(), 
              createRegisterSms());
        } catch (IllegalArgumentException e){
          Log.e(Constants.TAG,"Illegal argument in ODK Manage SMS Send");
        }
      }   
    });

    // create handler for HTTP register button
    Button registerHttpButton = (Button) findViewById(R.id.register_phone_button_http);
    registerHttpButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i(Constants.TAG,"Sending phone registration HTTP");
        try{
          EditText urlField = (EditText) findViewById(R.id.server_url_text);
          Map<String,String> paramMap = createRegisterMap();
          new HttpAdapter().doPost(urlField.getText().toString() + "/register", paramMap);
        } catch (IllegalArgumentException e) {
          Log.e(Constants.TAG,"Illegal argument in ODK Manage SMS Send");
        }
      }   
    });
    
    Button getTasksButton = (Button) findViewById(R.id.get_tasks_button);
    getTasksButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i(Constants.TAG, "Get tasks");
        IntentReceiver ir = new IntentReceiver();
        ir.init(ctx);
        ir.requestNewTasks();
      }   
    });
  }
  
  private void initText(int field, String prefKey){
    String initVal = settings.getString(prefKey, null);
    if (initVal != null)
      ((EditText) findViewById(field)).setText(initVal);
  }
  
  private Map<String,String> createRegisterMap(){
    if (phoneProperties == null)
      phoneProperties = new PhonePropertiesAdapter(this);
    Map<String,String> paramMap = new HashMap<String,String>();
    newProperty("userid",((EditText) findViewById(R.id.user_id_text)).getText().toString(), paramMap);
    newProperty("imei",phoneProperties.getIMEI(), paramMap);
    newProperty("phonenumber",phoneProperties.getPhoneNumber(), paramMap);
    newProperty("sim",phoneProperties.getSimSerialNumber(), paramMap);
    newProperty("imsi",phoneProperties.getIMSI(), paramMap);
    return paramMap;
  }
  
  private String createRegisterSms(){
    Map<String,String> regMap = createRegisterMap();
    List<String> props = new ArrayList<String>();
    for (String prop : regMap.keySet()){
      if (prop != null && regMap.get(prop) != null){
        props.add(prop + "=" + regMap.get(prop));
      }
    }
    return Constants.SMS_REGISTER_ACTION + " " + join(props, "&");
  }
  
  public void newProperty(String name, String value, Map<String,String> paramMap){
    if (name == null || value == null)
      return;
    Log.d("OdkManage","New registration property: <" + name + "," + value + ">");
    paramMap.put(name, value);
  }
  
  public String join(List<String> s, String delimiter) {
    if (s.isEmpty()) return "";
    Iterator<String> iter = s.iterator();
    StringBuffer buffer = new StringBuffer(iter.next());
    while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
    return buffer.toString();
  }
  
}