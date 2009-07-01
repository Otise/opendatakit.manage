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
import org.google.android.odk.common.Task;
import org.google.android.odk.common.Task.TaskStatus;
import org.google.android.odk.common.Task.TaskType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Receives intents corresponding to <ol><li>Received SMS Messages</li>
 * <li>Mobile or wifi network state changes</li></ol>
 * and responds appropriately (mostly by passing off information to the 
 * {@link OdkManage} Service.
 * 
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class IntentReceiver extends BroadcastReceiver {
  

  public FileHandler mHandler;
  private SharedPreferences settings;
  private DbAdapter dba;
  private Context ctx;

  @Override
  public void onReceive(Context ctx, Intent intent) {
    init(ctx);
    Log.i("OdkManage","Received Intent. Action: " + intent.getAction());
    if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
      SmsMessage msgs[] = getMessagesFromIntent(intent);
      String message = "";
      String number = "";

      for (SmsMessage msg: msgs) {
        message = msg.getDisplayMessageBody();
        number = msg.getOriginatingAddress();
        if (message != null && message.length() > 0 && number != null) {
          Log.i("OdkManage", "SMS Intercepted: " + message);
          if (isNewTasksTrigger(number,message))
            requestNewTasks();
        }
      }
    }
    else if (
        intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") ||
        intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
      NetworkInfo.State networkState = getNetworkState();
      if (networkState.equals(NetworkInfo.State.CONNECTED)){
        // try to perform any pending tasks
        Log.d(Constants.TAG,"Attempting to process pending tasks");
        processPendingTasks();
      }
    }
    else{
      Log.w(Constants.TAG, "Intent not supported.");
    }
   // if (intent.getAction().equals("android.))

  }
  
  public void init(Context ctx){

    this.ctx = ctx;
    // initialize settings
    settings = ctx.getSharedPreferences(Constants.PREFS_NAME, 0);
    mHandler = new FileHandler(ctx);
  }
  
  private NetworkInfo.State getNetworkState() {
    ConnectivityManager cm = (ConnectivityManager) ctx.
        getSystemService(Context.CONNECTIVITY_SERVICE);
    
    // just going to print a bunch of network status info to logs right now
    NetworkInfo mobileNi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    Log.d(Constants.TAG, "Mobile status: " + mobileNi.getState().name());
    NetworkInfo wifiNi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    Log.d(Constants.TAG, "Wifi status: " + wifiNi.getState().name());
    
    NetworkInfo activeNi = cm.getActiveNetworkInfo();
    if (activeNi != null) {
      Log.d(Constants.TAG, "Active status: " + activeNi.getState().name());
      Log.d(Constants.TAG, "Active type: " + activeNi.getTypeName());
      return activeNi.getState();
    } else {
      Log.d(Constants.TAG, "Active type: NONE");
      return NetworkInfo.State.DISCONNECTED;
    }
  }
  
  private boolean isNewTasksTrigger(String number, String message){
    //TODO(alerer): add 
    String serverNum = settings.getString(Constants.MANAGE_SMS_PREF, "");
    return (serverNum != null &&
            serverNum.equals(number) &&
            message.startsWith(Constants.NEW_TASKS_TRIGGER));
  }
  
  public void requestNewTasks(){
    Log.i("OdkManage", "Received NewTasks SMS");

    // remember that we have new tasks in case we can't retrieve them immediately
    String baseUrl = settings.getString(Constants.MANAGE_URL_PREF, null);
    if (baseUrl == null)
      return;
    
    // get the tasks input stream from the URL
    InputStream newTaskStream = null;
    try{
      String imei = new PhonePropertiesAdapter(ctx).getIMEI();
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
    
    // produce a list of Task objects from the XML document
    // Note: this is not very robust at the moment.
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
    NodeList taskNodes = doc.getElementsByTagName("task");
    List<Task> tasks = new ArrayList<Task>();
    Log.i(Constants.TAG,"=====\nTasks:");
    for (int i = 0; i < taskNodes.getLength(); i++) {
      Element taskEl = (Element) taskNodes.item(i);
      Log.i(Constants.TAG,"=====");
      NamedNodeMap taskAttributes = taskEl.getAttributes();
      long id = Long.parseLong(taskAttributes.getNamedItem("id").getNodeValue());
      Log.i(Constants.TAG, "Id: " + id);
      String typeString = taskAttributes.getNamedItem("type").getNodeValue();
      Log.i(Constants.TAG, "Type: " + typeString);
      TaskType type = null;
      try {
        type = Enum.valueOf(TaskType.class, typeString);
      } catch (Exception e) {
        Log.e(Constants.TAG, "Type not recognized: " + typeString);
        continue;
      }
      Task task = new Task(id, type, TaskStatus.PENDING);
      tasks.add(task);
      
      //TODO(alerer): perhaps we want to just stick ALL attributes into the 
      // task properties...this would make it less work when we add new 
      // attributes.
      String url = taskAttributes.getNamedItem("url").getNodeValue();
      Log.i(Constants.TAG,"Url: " + url);
      task.setProperty("url", url);
    }
    
    // we obviously need some better strategy for handling the database
    // because opening/closing are high-latency. Probably putting this all 
    // in a service will fix the problem.
    dba = new DbAdapter(ctx, Constants.DB_NAME);
    dba.open();
    int added = 0;
    for (Task t: tasks) {
      if (dba.addTask(t) > -1) {
        added++;
      }
    }
    Log.d(Constants.TAG, added + " tasks were added.");
    dba.close();
  }
  
  private String getTaskListUrl(String baseUrl, String imei){
    if (baseUrl.charAt(baseUrl.length()-1) == '/')
      baseUrl = baseUrl.substring(0, baseUrl.length()-1);
    return baseUrl + "/tasklist?imei=" + imei;
  }
  
  private void processPendingTasks(){
    dba = new DbAdapter(ctx, Constants.DB_NAME);
    dba.open();
    List<Task> tasks = dba.getPendingTasks();
    Log.d(Constants.TAG, "There are " + tasks.size() + " pending tasks.");
    for (Task t: tasks) {
      assert(t.getStatus().equals(TaskStatus.PENDING)); //just to check
      boolean success = attemptTask(t);
      if (success) {
        Log.i(Constants.TAG, "Setting task status to success");
        dba.setTaskStatus(t, TaskStatus.SUCCESS);
      }
    }
    dba.close();
  }
  private boolean attemptTask(Task t){
    FileHandler fh = new FileHandler(ctx);

    Log.i(Constants.TAG,
         "Attempting task\nType: " + t.getType() + "\nURL: " + t.getProperty("url"));
    
    //add form
    if (TaskType.ADD_FORM.equals(t.getType())){
      File formsDirectory = null;
      try { 
        formsDirectory = fh.getDirectory(SharedConstants.FORMS_PATH);
      } catch (IOException e){
        Log.e("OdkManage", "IOException getting forms directory");
        return false;
      }
      
      String url = t.getProperty("url");
      try{
        boolean success = fh.getFormFromUrl(new URL(url), 
            formsDirectory) != null;
        Log.i(Constants.TAG, 
            "Downloading form was " + (success? "":"not ") + "successfull.");
        return success;
      } catch (IOException e){
        Log.e(Constants.TAG, 
            "IOException downloading form: " + url);
        return false;
      }
    } 
      
    //install package
    else if (TaskType.INSTALL_PACKAGE.equals(t.getType())){
      File packagesDirectory = null;
      try { 
        packagesDirectory = fh.getDirectory(Constants.PACKAGES_PATH);
      } catch (IOException e){
        Log.e("OdkManage", "IOException getting packages directory");
        return false;
      }
      
      String url = t.getProperty("url");
      try { 
        File apk = fh.getFileFromUrl(new URL(url), packagesDirectory);
//        try {   
//          //Note: this will only work in /system/apps
//          Intent installIntent = new Intent(Intent.ACTION_PACKAGE_INSTALL,
//               Uri.parse("file://" + apk.getAbsolutePath().toString()));
//          context.startActivity(installIntent);
//          } catch (Exception e) {
//            Log.e(Constants.TAG, 
//                "Exception when doing auto-install package", e);
//          }
          try { 
            Uri uri = Uri.parse("file://" + apk.getAbsolutePath().toString());
//            PackageInstaller.installPackage(ctx, uri);
//            
            Intent installIntent2 = new Intent(Intent.ACTION_VIEW);
            installIntent2.setDataAndType(uri,
                "application/vnd.android.package-archive");
            installIntent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(installIntent2);
            //TODO(alerer): this doesn't really work properly, because this 
            //doesn't guarantee that the task has actually been carried out.
            //We really should be installing the tasks automatically
            Log.i(Constants.TAG, "Installing package successfull.");
            return true;
          } catch (Exception e) {
            Log.e(Constants.TAG, 
                "Exception when doing manual-install package", e);
            return false;
          }
      } catch (IOException e) {
        Log.e(Constants.TAG, 
            "IOException getting apk file: " + url);
        return false;
      }
    }
    
    //unrecognized task type
    else {
      Log.w(Constants.TAG, "Unrecognized task type");
      return false;
    }
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