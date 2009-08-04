package org.odk.manage.android.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.odk.manage.android.R;
import org.odk.manage.android.SharedPreferencesAdapter;

/**
 * The Activity in which the user can set preferences for ODK Manage.
 * 
 * This Activity should only be used by administrators. Perhaps it should be 
 * password protected or otherwise hidden?
 * 
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class PreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      new SharedPreferencesAdapter(this); //make sure all the prefs are initialized
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
    }
}
