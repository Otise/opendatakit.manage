package org.google.android.odk.manage;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OdkManageAdmin extends Activity {
  
  private PhonePropertiesAdapter phoneProperties = null;
  
  
  private final Activity ctx = this;
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      init();
  }
  
  private void init(){
    setContentView(R.layout.main);
    Button registerSmsButton = (Button) findViewById(R.id.register_phone_button_sms);
    registerSmsButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i("OdkManage","Sending phone registration SMS");
        SmsManager sm = SmsManager.getDefault();
        try{
          EditText numField = (EditText) findViewById(R.id.sms_number_text);
          EditText userIdField = (EditText) findViewById(R.id.user_id_text);
          String message = createRegisterMessage();
          new SmsSender(ctx).sendSMS(numField.getText().toString(), message);
        } catch (IllegalArgumentException e){
          Log.e("OdkManage","Illegal argument in ODK Manage SMS Send");
        }
      }   
    });
    Button registerHttpButton = (Button) findViewById(R.id.register_phone_button_http);
    registerHttpButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i("OdkManage","Sending phone registration HTTP");
        try{
          EditText urlField = (EditText) findViewById(R.id.server_url_text);
          String message = createRegisterMessage();
          try { 
            new HttpSender().doPost(urlField.getText().toString() + "/register", message);
          } catch (IOException e) {
            Log.e("OdkManage","IOException", e);
          }
        } catch (IllegalArgumentException e){
          Log.e("OdkManage","Illegal argument in ODK Manage SMS Send");
        }
      }   
    });
  }
  
  List<String> requestProperties;
  private String createRegisterMessage(){
    if (phoneProperties == null)
      phoneProperties = new PhonePropertiesAdapter(this);
    
    requestProperties = new ArrayList<String>();
    newProperty("userid",((EditText) findViewById(R.id.user_id_text)).getText().toString());
    newProperty("imei",phoneProperties.getIMEI());
    newProperty("num",phoneProperties.getPhoneNumber());
    newProperty("sim",phoneProperties.getSimSerialNumber());
    newProperty("imsi",phoneProperties.getIMSI());
    
    String msg = join(requestProperties, "&");
    Log.i("OdkManage", "Registration packet is " + msg.length() + "bytes.");
    return msg;
  }
  
  public void newProperty(String name, String value){
    if (name == null || value == null)
      return;
    requestProperties.add(URLEncoder.encode(name) + "=" + URLEncoder.encode(value));
  }
  
  public String join(List<String> s, String delimiter) {
    if (s.isEmpty()) return "";
    Iterator<String> iter = s.iterator();
    StringBuffer buffer = new StringBuffer(iter.next());
    while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
    return buffer.toString();
  }
  
}