package org.odk.manage.android;

public class Constants {
  
  //--------------------------------------------------------------------
  //                Deployment-specific Settings
  //--------------------------------------------------------------------
  
  /**
   * Determines how frequently the client should request tasks from the server, 
   * in milliseconds. For example, to check in once a day, set this field to 
   * 1000 * 60 * 60 * 24. If this field is a negative number, then the client will 
   * never check in (unless it receives an SMS notification).
   */
  public static final int TASK_REQUEST_PERIOD_MS = 1000 * 60 *60 * 24;
  
  /**
   * If true, the client will register with the server via SMS. If false, the 
   * client will register via HTTP.
   */
  public static final boolean PREF_SMS_ENABLED_DEFAULT = false;
  
  /**
   * If true, the client will attempt to use GPRS for data transfer when available.
   */
  public static final boolean PREF_GPRS_ENABLED_DEFAULT = true;
  
  /**
   * The default URL of the Manage server.
   */
  public static final String PREF_URL_DEFAULT = "http://sms-api-test.appspot.com";
  
  /**
   * The default SMS number of the Manage server.
   */
  public static final String PREF_SMS_DEFAULT = "+447624804882"; //"48378";
  
  
  //--------------------------------------------------------------------
  //                          Constants
  //         DO NOT TOUCH UNLESS YOU KNOW WHAT YOU ARE DOING
  //--------------------------------------------------------------------
  /**
   * Tag for debugging.
   */
  public static final String TAG = "OdkManage";
  
  //----------------------Preferences-----------------------------
  //--- Note: these must be kept consistent with res/values/preference_keys.xml
  
  /**
   * If true, the client will register with the server via SMS. If false, the 
   * client will register via HTTP.
   */
  public static final String PREF_SMS_ENABLED_KEY = "smsEnabled";
  
  /**
   * If true, the client will attempt to use GPRS for data transfer when available.
   */
  public static final String PREF_GPRS_ENABLED_KEY = "gprsEnabled";
  
  /**
   * The pref key for the URL of the Manage server (with no /).
   */
  public static final String PREF_URL_KEY = "serverUrl";
  
  /**
   * The pref key for the phone number for the Manage server SMS gateway
   */
  public static final String PREF_SMS_KEY = "serverSms";
  
  /**
   * The pref key for the last recorded IMSI, to check for IMSI changes.
   */
  public static final String PREF_REGISTERED_IMSI_KEY = "imsi";
  
  /**
   * The pref key for the operator username
   */
  public static final String PREF_USERID_KEY = "userid";
  
  /**
   * Pref key for whether a registration is currently in progress.
   */
  public static final String PREFS_REG_IN_PROGRESS_KEY = "reg_in_progress";
  
  /**
   * Pref key. Pref is true if ODK Manage believes that the tasklist should 
   * be polled for new tasks (e.g. if a new tasks notification SMS is received).
   */
  public static final String PREF_NEW_TASKS_KEY = "newTasks";
  
  /**
   * Pref key for keeping track of when the task list was last downloaded 
   * (as a long).
   */
  public static final String PREF_TASKS_LAST_DOWNLOADED_KEY = "tasksLastDownloaded";
  
  //---------------------------- Task Constants --------------------------
  
  /**
   * The number of times a task should be attempted before it fails.
   */
  public static final int NUM_TASK_ATTEMPTS = 3; 
  
  /**
   * Default time alotted to a particular worker task being run by the 
   * ODKManageService worker thread.
   */
  public static final int SERVICE_OPERATION_TIMEOUT_MS = 300000;
  
  /**
   * How long to wait when opening network connection in milliseconds
   */
  public static final int CONNECTION_TIMEOUT_MS = 5000;
  
  //---------------------------- SMS Constants ---------------------------
  
  /**
   * Token required for routing of SMS messages.
   */
  public static final String MANAGE_SMS_TOKEN = "odk";

  /**
   * Token signaling that an SMS to the Manage server is a register action.
   */
  public static final String SMS_REGISTER_ACTION = "reg";
  
  /**
   * Token signaling that an SMS message from the Manage server is a 
   * new tasks trigger.
   */
  public static final String NEW_TASKS_TRIGGER = "ODK-MANAGE-NT";
  
  //--------------------------- HTTP Constants ---------------------------
  
  /**
   * The path (relative to the ODK manage server domain) to the registration 
   * URL.
   */
  public static final String REGISTER_PATH = "register";
  
  /**
   * The path (relative to the ODK manage server domain) to the status update 
   * URL.
   */
  public static final String UPDATE_PATH = "update";
  
  //-------------------------- Database Constants ------------------------
  
  /**
   * Name of the local database.
   */
  public static final String DB_NAME = "odk_manage_db";

  //-------------------------- Filesystem Constants ----------------------
  
  /**
   * The local path where downloaded packages should be temporarily stored 
   * prior to installation.
   */
  public static final String PACKAGES_PATH = "/sdcard/odk/packages/";
  
  /**
   * Forms storage path
   */
  public static final String FORMS_PATH = "/sdcard/odk/forms/";

  /**
   * Answers storage path
   */
  public static final String ANSWERS_PATH = "/sdcard/odk/answers/";

  /**
   * Used to validate and display valid form names.
   */
  public static final String VALID_FILENAME = "[ _\\-A-Za-z0-9]*.x[ht]*ml";

  //-------------------------- Timer Constants ----------------------
  
  /**
   * The period at which the ODK Manage internal timer wakes up Manage in 
   * case there are things to do.
   */
  protected static final long TIMER_PERIOD_MS = 1000 * 60 * 60;
  
}
