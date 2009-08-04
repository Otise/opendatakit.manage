package org.odk.manage.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * An adapter for Android preferences. This class performs two functions:
 * <ol>
 * <li>Abstracts the implementation of preferences and the process of writing/editing preferences.</li>
 * <li>Encapsulates the implementation of 'default' values for preferences.</li>
 * </ol>
 * 
 * Note: Any preference with a default must be registered in initDefaults, or the 
 * default will not work.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class SharedPreferencesAdapter {
  
  private SharedPreferences prefs;

  /**
   * 
   * @param ctx The application context.
   */
  public SharedPreferencesAdapter(Context ctx){
    prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    initDefaults();
  }
  
  /**
   * Get a string preference.
   * @param key The preference key.
   * @param defValue A default value, if there is no value for this key.
   * @return the value for this key, or defValue if no key exists.
   */
  public String getString(String key, String defValue){
    return prefs.getString(key, defValue);
  }
  
  /**
   * Set a string preference.
   * @param key
   * @param value
   * @return true if successful.
   */
  public boolean setPreference(String key, String value){
    Map<String, String> prefs = new HashMap<String, String>(1);
    prefs.put(key, value);
    return setPreferences(prefs);
  }
  /**
   * Sets a list of string preferences.
   * @param key
   * @param value
   * @return true if successful.
   */
  public boolean setPreferences(Map<String, String> keyvals){
    SharedPreferences.Editor editor = prefs.edit();
    for (String key: keyvals.keySet()) {
      editor.putString(key, keyvals.get(key));
    }
    return editor.commit();
  }
  
  /**
   * Get a boolean preference.
   * @param key The preference key.
   * @param defValue A default value, if there is no value for this key.
   * @return the value for this key, or defValue if no key exists.
   */
  public boolean getBoolean(String key, boolean defValue){
    return prefs.getBoolean(key, defValue);
  }
  /**
   * Set a boolean preference.
   * @param key
   * @param value
   * @return true if successful.
   */
  public boolean setPreference(String key, boolean value){
    SharedPreferences.Editor editor = prefs.edit();
    editor.putBoolean(key, value);
    return editor.commit();
  }
  
  /**
   * Get a long preference.
   * @param key The preference key.
   * @param defValue A default value, if there is no value for this key.
   * @return the value for this key, or defValue if no key exists.
   */
  public long getLong(String key, long defValue){
    return prefs.getLong(key, defValue);
  }
  /**
   * Set a boolean preference.
   * @param key
   * @param value
   * @return true if successful.
   */
  public boolean setPreference(String key, long value){
    SharedPreferences.Editor editor = prefs.edit();
    editor.putLong(key, value);
    return editor.commit();
  }
  
  /**
   * @return The underlying SharedPreferences object.
   */
  public SharedPreferences getPreferences(){
    return prefs;
  }
  
  /**
   * Init default values for preferences. This sets all the included preferences to their 
   * defaults if they are currently unset.
   */
  private void initDefaults() {
    Log.d(Constants.TAG, "Initializing default SharedPreferences");
    if (getBoolean(Constants.PREF_GPRS_ENABLED_KEY, true) && !getBoolean(Constants.PREF_GPRS_ENABLED_KEY, false)) // this seems to be the only way to determine if a boolean preference is unset.
      setPreference(Constants.PREF_GPRS_ENABLED_KEY, Constants.PREF_GPRS_ENABLED_DEFAULT);
    if (getBoolean(Constants.PREF_SMS_ENABLED_KEY, true) && !getBoolean(Constants.PREF_SMS_ENABLED_KEY, false))
      setPreference(Constants.PREF_SMS_ENABLED_KEY, Constants.PREF_SMS_ENABLED_DEFAULT);
    if (getString(Constants.PREF_URL_KEY, null) == null)
      setPreference(Constants.PREF_URL_KEY, Constants.PREF_URL_DEFAULT);
    if (getString(Constants.PREF_SMS_KEY, null) == null)
      setPreference(Constants.PREF_SMS_KEY, Constants.PREF_SMS_DEFAULT);
  }

//  public void resetDefaults() {
//    setPreference(Constants.PREF_GPRS_ENABLED_KEY, Constants.PREF_GPRS_ENABLED_DEFAULT);
//    setPreference(Constants.PREF_SMS_ENABLED_KEY, Constants.PREF_SMS_ENABLED_DEFAULT);
//    setPreference(Constants.PREF_URL_KEY, Constants.PREF_URL_DEFAULT);
//    setPreference(Constants.PREF_SMS_KEY, Constants.PREF_SMS_DEFAULT);
//  }
}
