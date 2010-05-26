package org.odk.manage.android;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import org.odk.manage.android.activity.ManageActivity;
import org.odk.manage.android.comm.FileHandler;
import org.odk.manage.android.comm.HttpAdapter;
import org.odk.manage.android.model.DbAdapter;
import org.odk.manage.android.model.Task;
import org.odk.manage.android.model.Task.TaskStatus;
import org.odk.manage.android.model.Task.TaskType;
import org.odk.manage.android.worker.Worker;
import org.odk.manage.android.worker.WorkerTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class contains the main business logic for the ODK Manage client.
 * <p>
 * The ODK Manage service runs in the background. It is triggered by 
 * {@link IntentReceiver} (which receives and forwards a variety of system intents), 
 * by {@link ManageActivity}, and by its own internal timer that wakes it up 
 * 
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class OdkManageService extends Service{

  public static final String MESSAGE_TYPE_KEY = "messagetype";
  public static enum MessageType {
    NEW_TASKS, CONNECTIVITY_CHANGE, PHONE_PROPERTIES_CHANGE, BOOT_COMPLETED, PACKAGE_ADDED, TIMER;
  }
  
  public FileHandler fileHandler;
  private SharedPreferencesAdapter prefsAdapter;
  private DbAdapter dba;
  private PhonePropertiesAdapter propAdapter;
  private String imei;
  private Worker worker;
  private Thread timerThread;
  
  // Lifecycle methods
  
  /** not using ipc... dont care about this method */
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onStart(Intent i, int startId){
    /**
     * We have a single worker thread that executes all intents synchronously. 
     * This prevents resource overuse and allows tasks to be executed atomically.
     * 
     * TODO(alerer): these tasks are very coarse-grained. Think about whether more 
     * fine-grained tasks would be better (e.g. timeouts could be better suited to 
     * the particular task
     */
    final Intent mIntent = i;
    worker.addTask(new WorkerTask(){
      @Override
      public void execute(){
        OdkManageService.this.handleIntent(mIntent);
      }
      @Override
      public long getTimeoutMillis(){ //Not implemented in Worker
        return Constants.SERVICE_OPERATION_TIMEOUT_MS;
      }
    });
  }
  
  @Override 
  public void onCreate() {
    super.onCreate();
    Log.i(Constants.TAG, "OdkManageService created.");
    
    propAdapter = new PhonePropertiesAdapter(this);
    imei = propAdapter.getIMEI();
    
    dba = new DbAdapter(this, Constants.DB_NAME);
    dba.open();
    
    prefsAdapter = new SharedPreferencesAdapter(this);
    fileHandler = new FileHandler(this);
    
    registerPhonePropertiesChangeListener();
    
    worker = new Worker();
    worker.start();
    
    // Creates a timer thread that will wake up OdkManageService periodically
    // TODO(alerer): look into using an Alarm Service 
    //(http://developer.android.com/guide/samples/ApiDemos/src/com/example/android/apis/app/AlarmService.html)
    if (timerThread == null){
      timerThread = new Thread(){
        @Override
        public void run(){
          while(timerThread == this){ //it will stop when we set it to null
            try {
              sleep(Constants.TIMER_PERIOD_MS);
            } catch(InterruptedException e){
              continue;
            }
            Context ctx = OdkManageService.this;
            Intent i = new Intent(ctx, OdkManageService.class);
            i.putExtra(OdkManageService.MESSAGE_TYPE_KEY, MessageType.TIMER);
            ctx.startService(i);
          }
        }
      };
      timerThread.start();
    }
  }

  @Override 
  public void onDestroy() {

    Log.i(Constants.TAG, "OdkManageService destroyed.");
    worker.stop();
    worker = null;
    timerThread = null;
    
    dba.close();
    dba = null;
    super.onDestroy();
  }
  
  /////////////////////////
 
  /**
   * Handle an intent.
   */
  private void handleIntent(Intent i){
    MessageType mType = (MessageType) i.getExtras().get(MESSAGE_TYPE_KEY);
    Log.i(Constants.TAG, "OdkManageService started. Type: " + mType);
    
    syncDeviceImeiRegistration();
    
    boolean isConnected = isNetworkConnected();
    
    // handle the particular message
    switch (mType) {
      case NEW_TASKS:
        prefsAdapter.setPreference(Constants.PREF_NEW_TASKS_KEY, true);
        break; 
      case PACKAGE_ADDED:
        handlePackageAddedIntent(i.getExtras().getString("packageName"));
        if (isConnected) {
          sendStatusUpdates();
        }
        break;
      case CONNECTIVITY_CHANGE:
      case PHONE_PROPERTIES_CHANGE:
      case TIMER:
      case BOOT_COMPLETED:
        break;
      default:
        Log.w(Constants.TAG, "Unexpected MessageType in OdkManageService");
    }
    // do housekeeping that is unrelated to the message type: requesting tasks 
    // if necessary, processing pending tasks, sending status updates
    if (isConnected){
      if (shouldRequestNewTasks()){
        requestNewTasks();
      }
      processPendingTasks();
      sendStatusUpdates();
    }
  }
  
  
  
  /**
   * 
   * @return true if the client should query the server for new tasks.
   */
  private boolean shouldRequestNewTasks() {
    long lastRequested = prefsAdapter.getLong(Constants.PREF_TASKS_LAST_DOWNLOADED_KEY, 0);
    
    /**
     * If it's been more than Constants.TASK_REQUEST_PERIOD_MS since last update
     * return true.
     */
    if (Constants.TASK_REQUEST_PERIOD_MS > 0 && 
        (new Date()).getTime() - lastRequested > Constants.TASK_REQUEST_PERIOD_MS)
      return true;
    
    /**
     * If we got notified of new tasks, return true.
     */
    if (prefsAdapter.getBoolean(Constants.PREF_NEW_TASKS_KEY, false))
      return true;
    
    return false;
  }

  /**
   * 
   * @return true if the client is connected to the internet. This value is dependent 
   * both on the current connection and the client settings (e.g. GPRS_ENABLED).
   */
  private boolean isNetworkConnected(){
    NetworkInfo ni = getNetworkInfo();
    if (ni == null) {
      return false;
    }
    ni.getType();
    return (ni != null && NetworkInfo.State.CONNECTED.equals(ni.getState()) && 
        (prefsAdapter.getBoolean(Constants.PREF_GPRS_ENABLED_KEY, false) || 
            ni.getType() == ConnectivityManager.TYPE_WIFI)); //if GPRS not supported, do not use it
  }
  
  private NetworkInfo getNetworkInfo() {
    ConnectivityManager cm = (ConnectivityManager) 
        getSystemService(Context.CONNECTIVITY_SERVICE);
    
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
  
  /**
   * Check if the device has been registered with its current IMEI, and if not, attempt 
   * to register it.
   */
  private void syncDeviceImeiRegistration(){
    
    DeviceRegistrationHandler drh = new DeviceRegistrationHandler(this);
    if (drh.registrationNeededForImei()){
      Log.i(Constants.TAG, "IMSI changed: Registering device");
      drh.register(false);
    }
  }
  
  /**
   * Attempts to download new tasks from the server, parse them, and add the 
   * results to the local tasks database.
   */
  private void requestNewTasks(){
    Log.i(Constants.TAG, "Requesting new tasks");

    // remember that we have new tasks in case we can't retrieve them immediately
    String baseUrl = prefsAdapter.getString(Constants.PREF_URL_KEY, "");
    
    // get the tasks input stream from the URL
    InputStream newTaskStream = null;
    try{
      String imei = new PhonePropertiesAdapter(this).getIMEI();
      String url = getTaskListUrl(baseUrl, imei);
      Log.i(Constants.TAG, "tasklist url: " + url);
      newTaskStream = new HttpAdapter().getUrlConnection(url).getInputStream();

      if (newTaskStream == null){
        Log.e(Constants.TAG,"Null task stream");
        return;
      }
      
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
      
      if (tasks == null){
        Log.e(Constants.TAG, "Tasklist was null");
        return;
      }
   
      int added = 0;
      for (Task t: tasks) {
        if (dba.addTask(t) > -1) {
          added++;
        }
      }
      Log.d(Constants.TAG, added + " new tasks were added.");
      onNewTasksReceived();
    } catch (IOException e) {
      //TODO(alerer): do something here
      Log.e(Constants.TAG, "IOException downloading tasklist", e);
    } finally {
      try {
        if (newTaskStream != null) {
          newTaskStream.close();
        }
      } catch (IOException e) {
        Log.e(Constants.TAG, "IOException on closing new task stream", e);
      }
    }
  }
  
  /**
   * 
   * @param baseUrl The domain of the ODK Manage server
   * @param imei The IMEI of this device.
   * @return A URL for downloading this device's task list.
   */
  private String getTaskListUrl(String baseUrl, String imei){
    if (baseUrl.charAt(baseUrl.length()-1) == '/')
      baseUrl = baseUrl.substring(0, baseUrl.length()-1);
    
    return baseUrl + "/tasklist?imei=" + imei;
  }
  
  /**
   * 
   * @param doc An XML {@link Document} following the ODK Manage task list specification.
   * @return A {@List} of {@link Task}s from the XML document, or null if the Document was invalid.
   */
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
      String id = getAttribute(taskAttributes, "id");
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
  
  /**
   * Attempts to process all pending tasks in the local database. Does not 
   * download new tasks from the server.
   */
  private void processPendingTasks(){

    List<Task> tasks = dba.getPendingTasks();
    Log.d(Constants.TAG, "There are " + tasks.size() + " pending tasks.");
    
    for (Task t: tasks) {
      assert(t.getStatus().equals(TaskStatus.PENDING)); //just to check
      TaskStatus result = attemptTask(t);
      dba.setTaskStatus(t, result);
    }
  }
  
  /**
   * Send a status update message to the ODK Manage server, listing any 
   * tasks whose status has changed. These tasks are then marked as synced in 
   * the local DB.
   * 
   * @return true if the message was sent successfully, or no message was required.
   */
  private boolean sendStatusUpdates(){
    List<Task> tasks = dba.getUnsyncedTasks();
    StatusUpdateXmlGenerator updateGen = new StatusUpdateXmlGenerator(imei);
    for (Task t: tasks){
      assert(!t.isStatusSynced());
      updateGen.addTask(t);
    }
    Log.d(Constants.TAG, "Tasks with status updates: " + tasks.size());
    if (tasks.size() == 0) {
      return true;
    }
    String updateXml = updateGen.outputXml();
    String manageUrl = prefsAdapter.getString(Constants.PREF_URL_KEY, "");
    boolean success = 
      new HttpAdapter().doPost(manageUrl + "/" + Constants.UPDATE_PATH, updateXml);
    Log.i(Constants.TAG, "Status update message " + (success?"":"NOT ") + "successful");
    if (success) {
      for (Task t: tasks){
        dba.setTaskStatusSynced(t, true);
      }
    }
    return success;
  }
  
  /**
   * Attempts to execute a given task.
   * @param t The task to be attempted.
   * @return The new status the Task should be set to.
   */
  private TaskStatus attemptTask(Task t){
    
    Log.i(Constants.TAG,
         "Attempting task\nType: " + t.getType() + 
         "\nURL: " + t.getUrl());
    if (t.getNumAttempts() >= Constants.NUM_TASK_ATTEMPTS) {
      return TaskStatus.FAILED;
    } else {
      dba.incrementNumAttempts(t);
    }
    switch(t.getType()) {
      case ADD_FORM:
        return attemptAddForm(t);
      case INSTALL_PACKAGE:
        return attemptInstallPackage(t);
      default:
        Log.w(Constants.TAG, "Unrecognized task type");
        return TaskStatus.FAILED;
    }
  }
  
  /**
   * Executes a task of type ADD_FORM. 
   * <p>
   * TODO(alerer): change this to firing off an intent to ODK Collect
   * @param t The task to be attempted.
   * @return The new TaskStatus of the Task.
   */
  private TaskStatus attemptAddForm(Task t){
    assert(t.getType().equals(TaskType.ADD_FORM));
    
    FileHandler fh = new FileHandler(this);
    File formsDirectory = null;
    try { 
      formsDirectory = fh.getDirectory(Constants.FORMS_PATH);
    } catch (IOException e){
      Log.e("OdkManage", "IOException getting forms directory");
      return TaskStatus.PENDING;
    }
    
    String url = t.getUrl();
    try{
      URLConnection c = new HttpAdapter().getUrlConnection(url);
      boolean success = fh.getFormFromConnection(c, 
          formsDirectory) != null;
      Log.i(Constants.TAG, 
          "Downloading form was " + (success? "":"not ") + "successfull.");
      return success ? TaskStatus.SUCCESS : TaskStatus.PENDING;
    } catch (IOException e){
      Log.e(Constants.TAG, 
          "IOException downloading form: " + url, e);
      return TaskStatus.PENDING;
    }
  }
  
  /**
   * Attempts to execute a task of type INSTALL_PACKAGE.
   * TODO(alerer): who should handle this? Probably Manage...just make sure.
   * @param t The task to be attempted.
   * @return The new TaskStatus of the Task.
   */
  private TaskStatus attemptInstallPackage(Task t){
    assert(t.getType().equals(TaskType.INSTALL_PACKAGE));
    
    FileHandler fh = new FileHandler(this);
    File packagesDirectory = null;
    try { 
      packagesDirectory = fh.getDirectory(Constants.PACKAGES_PATH);
    } catch (IOException e){
      Log.e("OdkManage", "IOException getting packages directory");
      return TaskStatus.PENDING;
    }
    
    String url = t.getUrl();
    try {
      URLConnection c = new HttpAdapter().getUrlConnection(url);
      File apk = fh.getFileFromConnection(c, packagesDirectory);
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
          if (t.getName() == null) {
            return TaskStatus.SUCCESS; // since we don't know the package name, we have to just assume it worked.
          } else {
            return TaskStatus.PENDING; // wait for a PACKAGE_ADDED intent for this package name
          }
        } catch (Exception e) {
          Log.e(Constants.TAG, 
              "Exception when doing manual-install package", e);
          return TaskStatus.PENDING;
        }
    } catch (IOException e) {
      Log.e(Constants.TAG, 
          "IOException getting apk file: " + url);
      return TaskStatus.PENDING;
    }
  }
  
  /**
   * This method is called when new tasks are successfully downloaded. It updates preferences accordingly.
   */
  public void onNewTasksReceived(){
    prefsAdapter.setPreference(Constants.PREF_NEW_TASKS_KEY, false);
    prefsAdapter.setPreference(Constants.PREF_TASKS_LAST_DOWNLOADED_KEY, new Date().getTime());
  }
  
  /**
   * When a PACKAGE_ADDED intent is received, this method compares the package 
   * name with all pending INSTALL_PACKAGE tasks. If the package names match, that 
   * task is successful.
   * @param packageName The package name.
   */
  private void handlePackageAddedIntent(String packageName){
    Log.d(Constants.TAG, "Package added detected: " + packageName);
    List<Task> pendingTasks = dba.getPendingTasks();
    for (Task t: pendingTasks) {
      Log.d(Constants.TAG, "Type: " + t.getType() + "; Name: " + t.getName());
      if (t.getType().equals(TaskType.INSTALL_PACKAGE) && 
          t.getName().equals(packageName)){ // extras stores the package name
        dba.setTaskStatus(t, TaskStatus.SUCCESS);
        Log.d(Constants.TAG, "Task " + t.getUniqueId() + " (INSTALL_PACKAGE) successful.");
        break;
      }
    }  
  }
  
  /**
   * Adds this class as a listener for changed phone properties (e.g. IMEI).
   */
  private void registerPhonePropertiesChangeListener() {
    Intent mIntent = new Intent(this, OdkManageService.class);
    mIntent.putExtra(MESSAGE_TYPE_KEY, MessageType.PHONE_PROPERTIES_CHANGE);
    PendingIntent pi = PendingIntent.getService(this, 0, mIntent, 0);
    propAdapter.registerListener(pi);
  }

}
