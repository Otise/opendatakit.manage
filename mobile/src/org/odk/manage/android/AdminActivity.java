package org.odk.manage.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends Activity {
  
  private PhonePropertiesAdapter phoneProperties = null;
  private SharedPreferencesAdapter prefsAdapter;
  
  private final Activity ctx = this;
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      prefsAdapter = new SharedPreferencesAdapter(this);
      init();
  }
  
  @Override
  public void onStop(){
    super.onStop();
    updatePreferences();
  }
  
  private void updatePreferences(){
    Map<String, String> prefs = new HashMap<String, String>();
    
    prefs.put(Constants.PREF_USERID_KEY, ((EditText) 
        findViewById(R.id.user_id_text)).getText().toString());
    prefs.put(Constants.PREF_SMS_KEY, ((EditText) 
        findViewById(R.id.sms_number_text)).getText().toString());
    prefs.put(Constants.PREF_URL_KEY, ((EditText) 
        findViewById(R.id.server_url_text)).getText().toString());
    
    prefsAdapter.setPreferences(prefs); // best effort
  }
  
  private void init(){
    setContentView(R.layout.main);
    
    // initialize user-entered fields
    initText(R.id.user_id_text, Constants.PREF_USERID_KEY);
    initText(R.id.sms_number_text, Constants.PREF_SMS_KEY);
    initText(R.id.server_url_text, Constants.PREF_URL_KEY);
    
    // create handler for SMS register button
    Button registerSmsButton = (Button) findViewById(R.id.register_phone_button_sms);
    registerSmsButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i(Constants.TAG,"Sending phone registration SMS");
        SmsManager sm = SmsManager.getDefault();
        updatePreferences();
        new DeviceRegistrationHandler(AdminActivity.this).registerBySms();
      }   
    });
    
    // create handler for HTTP register button
    Button registerHttpButton = (Button) findViewById(R.id.register_phone_button_http);
    registerHttpButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i(Constants.TAG,"Sending phone registration HTTP");
        updatePreferences();
        new DeviceRegistrationHandler(AdminActivity.this).registerByHttp();
      }   
    });
    
    // create handler for Get Tasks button
    Button getTasksButton = (Button) findViewById(R.id.get_tasks_button);
    getTasksButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i(Constants.TAG, "Get tasks");
        Intent i = new Intent(ctx, OdkManageService.class);
        i.putExtra(OdkManageService.MESSAGE_TYPE_KEY, 
            OdkManageService.MessageType.NEW_TASKS);
        ctx.startService(i);
      }   
    });
  }
  
  private void initText(int field, String prefKey){
    String initVal = prefsAdapter.getString(prefKey, "");
    if (initVal == null)
      initVal = "";
    ((EditText) findViewById(field)).setText(initVal);
  }
  

 
  
}
