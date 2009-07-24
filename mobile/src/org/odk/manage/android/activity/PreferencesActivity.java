package org.odk.manage.android.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.odk.manage.android.R;
import org.odk.manage.android.SharedPreferencesAdapter;

public class PreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      new SharedPreferencesAdapter(this); //make sure all the prefs are initialized
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
    }
}
