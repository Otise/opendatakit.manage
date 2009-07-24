package org.odk.manage.android.activity;

import org.odk.manage.android.Constants;
import org.odk.manage.android.DeviceRegistrationHandler;
import org.odk.manage.android.OdkManageService;
import org.odk.manage.android.PhonePropertiesAdapter;
import org.odk.manage.android.R;
import org.odk.manage.android.SharedPreferencesAdapter;
import org.odk.manage.android.OdkManageService.MessageType;
import org.odk.manage.android.R.id;
import org.odk.manage.android.R.layout;

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

public class ManageActivity extends Activity {
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    init();
  }
 
  
  private void init(){
    setContentView(R.layout.main);

    // create handler for register button
    Button registerButton = (Button) findViewById(R.id.register_phone_button);
    registerButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i(Constants.TAG,"Sending phone registration.");
        new DeviceRegistrationHandler(ManageActivity.this).register(true);
      }   
    });

    // create handler for sync button
    Button syncButton = (Button) findViewById(R.id.sync_button);
    syncButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i(Constants.TAG, "Sync");
        Intent i = new Intent(ManageActivity.this, OdkManageService.class);
        i.putExtra(OdkManageService.MESSAGE_TYPE_KEY, 
            OdkManageService.MessageType.NEW_TASKS);
        ManageActivity.this.startService(i);
      }   
    });
    
    // create handler for sync button
    Button settingsButton = (Button) findViewById(R.id.settings_button);
    settingsButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i(Constants.TAG, "Going to preferences");
        Intent i = new Intent(ManageActivity.this, PreferencesActivity.class);
        ManageActivity.this.startActivity(i);
      }   
    });
  }
}
