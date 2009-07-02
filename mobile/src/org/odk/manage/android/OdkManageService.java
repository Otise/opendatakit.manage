package org.odk.manage.android;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import org.odk.common.android.FileHandler;
import org.odk.common.android.SharedConstants;
import org.odk.common.android.Task;
import org.odk.common.android.Task.TaskStatus;
import org.odk.common.android.Task.TaskType;
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

//TODO(alerer): some of these methods need to be synchronized
public class OdkManageService extends Service{

  public static final String MESSAGE_TYPE_KEY = "messagetype";
  public static enum MessageType {
    NEW_TASKS, CONNECTIVITY_CHANGE, PHONE_PROPERTIES_CHANGE, BOOT_COMPLETED;
  }
  
  public FileHandler mHandler;
  private SharedPreferences preferences;
  private DbAdapter dba;
  private PhonePropertiesAdapter propAdapter;
  
  // Lifecycle methods
  
  /** not using ipc... dont care about this method */
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onStart(Intent i, int startId){
    MessageType mType = (MessageType) i.getExtras().get(MESSAGE_TYPE_KEY);
    Log.i(Constants.TAG, "OdkManageService started. Type: " + mType);
    NetworkInfo ni = getNetworkInfo();
    switch (mType) {
      case NEW_TASKS:
        setNewTasksPref(true);
        //TODO(alerer): use the CommunicationStategy
        if (ni != null && NetworkInfo.State.CONNECTED.equals(ni.getState())) {
          requestNewTasks();
          processPendingTasks();
        }
        break; 
      case CONNECTIVITY_CHANGE:
        //TODO(alerer): use the CommunicationStategy
        if (ni != null && NetworkInfo.State.CONNECTED.equals(ni.getState())) {
          if (getNewTasksPref()){
            requestNewTasks();
          }
          processPendingTasks();
        }
        break;
      case PHONE_PROPERTIES_CHANGE:
        Log.d(Constants.TAG, "Phone properties changed.");
        break;
      default:
        Log.w(Constants.TAG, "Unexpected MessageType in OdkManageService");
    }
  }
  
  @Override 
  public void onCreate() {
    super.onCreate();
    Log.i(Constants.TAG, "OdkManageService created.");
    init();
  }

  @Override 
  public void onDestroy() {
    super.onDestroy();
    Log.i(Constants.TAG, "OdkManageService destroyed.");
    dba.close();
    dba = null;
  }
  
  /////////////////////////
  
  private void init(){
    dba = new DbAdapter(this, Constants.DB_NAME);
    dba.open();
    
    preferences = getSharedPreferences(Constants.PREFS_NAME, 0);
    mHandler = new FileHandler(this);
    
    registerPhonePropertiesChangeListener();
  }
  
  private void registerPhonePropertiesChangeListener() {
    propAdapter = new PhonePropertiesAdapter(this);
    Intent mIntent = new Intent(this, OdkManageService.class);
    mIntent.putExtra(MESSAGE_TYPE_KEY, MessageType.PHONE_PROPERTIES_CHANGE);
    PendingIntent pi = PendingIntent.getService(this, 0, mIntent, 0);
    propAdapter.registerListener(pi);
  }
  
  
  private NetworkInfo getNetworkInfo() {
    ConnectivityManager cm = (ConnectivityManager) 
        getSystemService(Context.CONNECTIVITY_SERVICE);
    
    // going to print a bunch of network status info to logs
    NetworkInfo mobileNi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    Log.d(Constants.TAG, "Mobile status: " + mobileNi.getState().name());
    NetworkInfo wifiNi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    Log.d(Constants.TAG, "Wifi status: " + wifiNi.getState().name());
    
    NetworkInfo activeNi = cm.getActiveNetworkInfo();
    if (activeNi != null) {
      Log.d(Constants.TAG, "Active status: " + activeNi.getState().name());
      Log.d(Constants.TAG, "Active type: " + activeNi.getTypeName());
      return activeNi;
    } else {
      Log.d(Constants.TAG, "Active type: NONE");
      return null;
    }
  }
  
  private List<Task> getTasksFromTasklist(Document doc){
    List<Task> tasks = new ArrayList<Task>();
    
    doc.getDocumentElement().normalize();
    
    NodeList taskNodes = doc.getElementsByTagName("task");
    
    Log.i(Constants.TAG,"=====\nTasks:");
    for (int i = 0; i < taskNodes.getLength(); i++) {
      if (!(taskNodes.item(i) instanceof Element)) {
        continue;
      }
      Element taskEl = (Element) taskNodes.item(i);
      Log.i(Constants.TAG,"-----");
      NamedNodeMap taskAttributes = taskEl.getAttributes();
      
      // parsing ID
      String idString = getAttribute(taskAttributes, "id");
      long id;
      try {
        id = Long.parseLong(idString);
      } catch (NumberFormatException e) {
        Log.e(Constants.TAG, "ID not a number: " + idString);
        continue;
      }
      Log.i(Constants.TAG, "Id: " + id);
      
      // parsing type
      String typeString = getAttribute(taskAttributes, "type");
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
      
      task.setName(getAttribute(taskAttributes, "name"));
      task.setUrl(getAttribute(taskAttributes, "url"));
      task.setExtras(getAttribute(taskAttributes, "extras"));
    }
    
    return tasks;
  }
  
  private String getAttribute(NamedNodeMap attributes, String name) {
    if (attributes.getNamedItem(name) == null) {
      return null;
    }
    return attributes.getNamedItem(name).getNodeValue();
  }
  
  private void requestNewTasks(){
    Log.i(Constants.TAG, "Requesting new tasks");

    // remember that we have new tasks in case we can't retrieve them immediately
    String baseUrl = preferences.getString(Constants.MANAGE_URL_PREF, null);
    if (baseUrl == null)
      return;
    
    // get the tasks input stream from the URL
    InputStream newTaskStream = null;
    try{
      String imei = new PhonePropertiesAdapter(this).getIMEI();
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
      Log.e(Constants.TAG,"",e);
    } catch (IOException e){
      Log.e(Constants.TAG,"",e);
    } catch (SAXException e){
      Log.e(Constants.TAG,"",e);
    }
    if (doc == null)
      return;
    
    List<Task> tasks = getTasksFromTasklist(doc);
    
    // we obviously need some better strategy for handling the database
    // because opening/closing are high-latency. Probably putting this all 
    // in a service will fix the problem.
 
    int added = 0;
    for (Task t: tasks) {
      if (dba.addTask(t) > -1) {
        added++;
      }
    }
    Log.d(Constants.TAG, added + " tasks were added.");

  }
  
  private String getTaskListUrl(String baseUrl, String imei){
    if (baseUrl.charAt(baseUrl.length()-1) == '/')
      baseUrl = baseUrl.substring(0, baseUrl.length()-1);
    return baseUrl + "/tasklist?imei=" + imei;
  }
  
  private void processPendingTasks(){

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
  }
  
  private boolean attemptTask(Task t){
    
    Log.i(Constants.TAG,
         "Attempting task\nType: " + t.getType() + 
         "\nURL: " + t.getUrl());
    
    switch(t.getType()) {
      case ADD_FORM:
        return attemptAddForm(t);
      case INSTALL_PACKAGE:
        return attemptInstallPackage(t);
      default:
        Log.w(Constants.TAG, "Unrecognized task type");
        return false;
    }
  }
  
  //TODO(alerer): change this to firing off an intent to ODK Collect
  private boolean attemptAddForm(Task t){
    assert(t.getType().equals(TaskType.ADD_FORM));
    
    FileHandler fh = new FileHandler(this);
    File formsDirectory = null;
    try { 
      formsDirectory = fh.getDirectory(SharedConstants.FORMS_PATH);
    } catch (IOException e){
      Log.e("OdkManage", "IOException getting forms directory");
      return false;
    }
    
    String url = t.getUrl();
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
  
  //TODO(alerer): who should handle this? Probably Manage...just make sure.
  private boolean attemptInstallPackage(Task t){
    assert(t.getType().equals(TaskType.INSTALL_PACKAGE));
    
    FileHandler fh = new FileHandler(this);
    File packagesDirectory = null;
    try { 
      packagesDirectory = fh.getDirectory(Constants.PACKAGES_PATH);
    } catch (IOException e){
      Log.e("OdkManage", "IOException getting packages directory");
      return false;
    }
    
    String url = t.getUrl();
    try { 
      File apk = fh.getFileFromUrl(new URL(url), packagesDirectory);
//      try {   
//        //Note: this will only work in /system/apps
//        Intent installIntent = new Intent(Intent.ACTION_PACKAGE_INSTALL,
//             Uri.parse("file://" + apk.getAbsolutePath().toString()));
//        context.startActivity(installIntent);
//        } catch (Exception e) {
//          Log.e(Constants.TAG, 
//              "Exception when doing auto-install package", e);
//        }
        try { 
          Uri uri = Uri.parse("file://" + apk.getAbsolutePath().toString());
//          PackageInstaller.installPackage(ctx, uri);
//          
          Intent installIntent2 = new Intent(Intent.ACTION_VIEW);
          installIntent2.setDataAndType(uri,
              "application/vnd.android.package-archive");
          installIntent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(installIntent2);
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
  
  private void setPreferences(String key, String value) {
    SharedPreferences.Editor editor = preferences.edit();
    editor.putString(key, value);
    editor.commit();
  }
  
  private void setPreferences(String key, boolean value) {
    SharedPreferences.Editor editor = preferences.edit();
    editor.putBoolean(key, value);
    editor.commit();
  }
  
  private boolean getNewTasksPref(){
    return preferences.getBoolean(Constants.NEW_TASKS_PREF, false);
  }
  private void setNewTasksPref(boolean newValue){
    setPreferences(Constants.NEW_TASKS_PREF, newValue);
  }

}
