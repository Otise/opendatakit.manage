package org.google.android.odk.manage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

import org.google.android.odk.common.FileHandler;
import org.google.android.odk.common.SharedConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class OdkManageSmsReceiver extends BroadcastReceiver {
  

  public FileHandler mHandler;
  
  private SharedPreferences settings;

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i("OdkManage","Received Intent. Action: " + intent.getAction());
    
    // initialize settings
    settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
    mHandler = new FileHandler(context);
    SmsMessage msgs[] = getMessagesFromIntent(intent);
    
    getNetworkStatus(context);
    
    if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
      String message = "";
      String number = "";

      for (SmsMessage msg: msgs) {
        message = msg.getDisplayMessageBody();
        number = msg.getOriginatingAddress();
        if (message != null && message.length() > 0 && number != null) {
          Log.i("OdkManage", "SMS Intercepted: " + message);
          if (isNewTasksTrigger(number,message))
            requestNewTasks(message, context);
        }
      }
    }
    else if (
        intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") ||
        intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
      getNetworkStatus(context);
    }
    else{
      Log.w(Constants.TAG, "Intent not supported.");
    }
   // if (intent.getAction().equals("android.))



  }
  
  private void getNetworkStatus(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.
        getSystemService(Context.CONNECTIVITY_SERVICE);
    
    NetworkInfo mobileNi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    Log.i(Constants.TAG, "Mobile status: " + getStatus(mobileNi));
    NetworkInfo wifiNi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    Log.i(Constants.TAG, "Wifi status: " + getStatus(wifiNi));
    NetworkInfo activeNi = cm.getActiveNetworkInfo();
    if (activeNi != null) {
      Log.i(Constants.TAG, "Active status: " + getStatus(activeNi));
      Log.i(Constants.TAG, "Active type: " + activeNi.getTypeName());
    }
  }
  
  private String getStatus(NetworkInfo ni){
    return ni.getState().name();
  }
  
  private boolean isNewTasksTrigger(String number, String message){
    //TODO(alerer): add 
    //settings.getString(Constants.MANAGE_SMS_PREF, "").equals(number);
    return message.startsWith(Constants.NEW_TASKS_TRIGGER);
  }
  
  private void requestNewTasks(String message, Context context){
    Log.i("OdkManage", "Received NewTasks SMS");

    
    // remember that we have new tasks in case we can't retrieve them immediately
    String baseUrl = settings.getString(Constants.MANAGE_URL_PREF, null);
    if (baseUrl == null)
      return;
    
    FileHandler fh = new FileHandler(context);
    File formsDirectory = null;
    try { 
      formsDirectory = fh.getDirectory(SharedConstants.FORMS_PATH);
    } catch (IOException e){
      Log.e("OdkManage", "IOException getting forms directory");
      return;
    }
    File packagesDirectory = null;
    try { 
      packagesDirectory = fh.getDirectory(Constants.PACKAGES_PATH);
    } catch (IOException e){
      Log.e("OdkManage", "IOException getting packages directory");
      return;
    }
    
    InputStream newTaskStream = null;
    try{
      String imei = new PhonePropertiesAdapter(context).getIMEI();
      String url = getTaskListUrl(baseUrl, imei);
      Log.i(Constants.TAG, "tasklist url: " + url);
      newTaskStream = new HttpAdapter().getUrl(url);
    } catch (IOException e) {
      //TODO(alerer): do something here
      Log.e(Constants.TAG, "IOException downloading tasklist");
    }
    if (newTaskStream == null){
      Log.e(Constants.TAG,"Null task stream");
      return;
    }
//    try {
//      Log.i(Constants.TAG,"taskstream: " + (new BufferedReader(new InputStreamReader(newTaskStream))).readLine());
//    } catch (IOException e) {}

    Document doc = null;
    try{
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      doc = db.parse(newTaskStream);
    } catch (ParserConfigurationException e){
    } catch (IOException e){
    } catch (SAXException e){
    }
    doc.getDocumentElement().normalize();
    NodeList tasks = doc.getElementsByTagName("task");
    Log.i(Constants.TAG,"Tasks:\n");
    for (int i = 0; i < tasks.getLength(); i++) {
      Element task = (Element) tasks.item(i);
      Log.i(Constants.TAG,"=====");
      NamedNodeMap taskAttributes = task.getAttributes();
      String type = taskAttributes.getNamedItem("type").getNodeValue();
      Log.i(Constants.TAG, "Type: " + type);
      
      //add form
      if (type.equals("addForm")){
        //TODO(alerer): check for null
        String url = taskAttributes.getNamedItem("url").getNodeValue();
        Log.i(Constants.TAG,"Url: " + url);
        try{
          fh.getFormFromUrl(new URL(url), 
              formsDirectory);
        } catch (IOException e){
          Log.e(Constants.TAG, 
              "IOException downloading form: " + url);
        }
      } 
        
      //install package
      else if (type.equals("installPackage")){
        String url = taskAttributes.getNamedItem("url").getNodeValue();
        Log.i(Constants.TAG,"Url: " + url);
        try { 
          File apk = fh.getFileFromUrl(new URL(url), packagesDirectory);
//          try {   
//            //Note: this will only work in /system/apps
//            Intent installIntent = new Intent(Intent.ACTION_PACKAGE_INSTALL,
//                 Uri.parse("file://" + apk.getAbsolutePath().toString()));
//            context.startActivity(installIntent);
//            } catch (Exception e) {
//              Log.e(Constants.TAG, 
//                  "Exception when doing auto-install package", e);
//            }
            try { 
              //Note: I don't think this will work but I'll try
              Intent installIntent2 = new Intent(Intent.ACTION_VIEW);
              installIntent2.setDataAndType(Uri.parse("file://" + apk.getAbsolutePath().toString()),
                  "application/vnd.android.package-archive");
//               this guy works
//              Intent installIntent2 = new Intent(Intent.ACTION_VIEW,
//                  Uri.parse("http://web.mit.edu/alerer/Public/CallFreq.apk"));
              installIntent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              context.startActivity(installIntent2);
            } catch (Exception e) {
              Log.e(Constants.TAG, 
                  "Exception when doing manual-install package", e);
            }
        } catch (IOException e) {
          Log.e(Constants.TAG, 
              "IOException getting apk file: " + url);
        }
      }
      else {
        Log.w(Constants.TAG, "Unrecognized task type");
      }
    }
  }
  
  private String getTaskListUrl(String baseUrl, String imei){
    if (baseUrl.charAt(baseUrl.length()-1) == '/')
      baseUrl = baseUrl.substring(0, baseUrl.length()-1);
    return baseUrl + "/tasklist?imei=" + imei;
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