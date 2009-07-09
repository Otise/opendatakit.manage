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
  public static final String MANAGE_URL_PREF = "serverurl";
  
  /**
   * The pref key for the phone number for the Manage server SMS gateway
   */
  public static final String MANAGE_SMS_PREF = "serversms";
  
  /**
   * The pref key for the operator username
   */
  public static final String MANAGE_USERID_PREF = "userid";
  
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
  
  public static final String DEFAULT_SERVER_DOMAIN = "http://sms-api-test.appspot.com";
  
}
