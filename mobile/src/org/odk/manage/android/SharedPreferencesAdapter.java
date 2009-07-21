package org.odk.manage.android;

import android.content.Context;
import android.content.SharedPreferences;

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
  
  private static Map<String, String> prefDefaults;
  static {
    prefDefaults = new HashMap<String, String>();
    prefDefaults.put(Constants.PREF_URL_KEY, Constants.PREF_URL_DEFAULT);
    prefDefaults.put(Constants.PREF_SMS_KEY, Constants.PREF_SMS_DEFAULT);
  }
  
  private SharedPreferences prefs;
  
  public SharedPreferencesAdapter(Context ctx){
    prefs = ctx.getSharedPreferences(Constants.PREFS_NAME, 0);
  }
  
  public String getString(String key, String defValue){
    String res = prefs.getString(key, null);
    if (res == null){
      if (prefDefaults.containsKey(key)){
        return prefDefaults.get(key);
      } else {
        return defValue;
      }
    } else {
      return res;
    }
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
   * Only use this when working with non-string preferences.
   * @return The underlying SharedPreferences object.
   */
  public SharedPreferences getPreferences(){
    return prefs;
  }
}
