package org.odk.manage.android;

public class Constants {

  /**
   * Tag for debugging.
   */
  public static final String TAG = "OdkManage";
  
  /**
   * The name of the preferences file for ODK Manage.
   */
  public static final String PREFS_NAME = "OdkManagePrefs";
  
  /**
   * The pref key for the URL of the Manage server (with no /).
   */
  public static final String PREF_URL_KEY = "serverurl";
  
  public static final String PREF_URL_DEFAULT = "http://sms-api-test.appspot.com";
  
  /**
   * The pref key for the phone number for the Manage server SMS gateway
   */
  public static final String PREF_SMS_KEY = "serversms";
  
  /**
   * The pref key for the last recorded IMSI, to check for IMSI changes.
   */
  public static final String PREF_REGISTERED_IMSI_KEY = "imsi";

  
  //GTEST
  public static final String PREF_SMS_DEFAULT_SHORT = "48378";
  
  public static final String PREF_SMS_DEFAULT_LONG = "+447624804882";
  
  /**
   * The pref key for the operator username
   */
  public static final String PREF_USERID_KEY = "userid";
  
  /**
   * Pref key. Pref is true if ODK Manage believes that the tasklist should 
   * be polled for new tasks (e.g. if a new tasks notification SMS is received).
   */
  public static final String NEW_TASKS_PREF = "newTasks";
  
  /**
   * The local path where downloaded packages should be temporarily stored 
   * prior to installation.
   */
  public static final String PACKAGES_PATH = "/sdcard/odk/packages/";
  
  /**
   * Token required for GAE routing of SMS messages.
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
  
  /**
   * Name of the local database.
   */
  public static final String DB_NAME = "odk_manage_db";
  
}
