package org.odk.manage.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * An adapter for Android preferences. Right now, it only handles string/boolean
 * preferences, so you have to call getPreferences and act on the Preferences 
 * directly for other preference types (unfortunately).
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class SharedPreferencesAdapter {
  
  private SharedPreferences prefs;

  public SharedPreferencesAdapter(Context ctx){
    prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    initDefaults();
  }
  
  public String getString(String key, String defValue){
    return prefs.getString(key, defValue);
  }
  
  public boolean setPreference(String key, String value){
    Map<String, String> prefs = new HashMap<String, String>(1);
    prefs.put(key, value);
    return setPreferences(prefs);
  }
  public boolean setPreferences(Map<String, String> keyvals){
    SharedPreferences.Editor editor = prefs.edit();
    for (String key: keyvals.keySet()) {
      editor.putString(key, keyvals.get(key));
    }
    return editor.commit();
  }
  public boolean getBoolean(String key, boolean defValue){
    return prefs.getBoolean(key, defValue);
  }
  public boolean setPreference(String key, boolean value){
    SharedPreferences.Editor editor = prefs.edit();
    editor.putBoolean(key, value);
    return editor.commit();
  }
  
  public long getLong(String key, long defValue){
    return prefs.getLong(key, defValue);
  }
  
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
  
  private void initDefaults() {
    Log.d(Constants.TAG, "Initializing default SharedPreferences");
    if (getBoolean(Constants.PREF_GPRS_ENABLED_KEY, true) && !getBoolean(Constants.PREF_GPRS_ENABLED_KEY, false))
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
