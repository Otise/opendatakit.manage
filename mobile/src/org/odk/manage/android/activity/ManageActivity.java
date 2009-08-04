package org.odk.manage.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.odk.manage.android.Constants;
import org.odk.manage.android.DeviceRegistrationHandler;
import org.odk.manage.android.OdkManageService;
import org.odk.manage.android.R;
import org.odk.manage.android.SharedPreferencesAdapter;
import org.odk.manage.android.Utils;
import org.odk.manage.android.model.DbAdapter;

import java.util.Date;

/**
 * The main Activity that appears when you open ODK Manage.
 * <p>
 * This activity is mainly for use by administrators. From this Activity, you can 
 * manually register a device, manually request new tasks (sync), and you can navigate 
 * to the preferences Activity. You can also see a bit of status info.
 * 
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class ManageActivity extends Activity {
  
  private static final long STATUS_UPDATE_MS = 1000;
  private DbAdapter dba;
  /**
   * This thread is created in onStart, and calls updateStatus() once a second 
   * in the UI thread.
   * TODO(alerer): the OdkManageService should broadcast a modelChanged intent 
   * when stuff happens, rather than the activity having to poll the model.
   */
  private Thread statusUpdateThread;
  private SharedPreferencesAdapter prefsAdapter;
  private final Handler mHandler = new Handler();
  final Runnable updateStatusRunnable = new Runnable() {
    public void run() {
        updateStatus();
    }
  };


  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    dba = new DbAdapter(this, Constants.DB_NAME);
    dba.open();
    prefsAdapter = new SharedPreferencesAdapter(this);
    init();
  }
  
  @Override
  public void onStart(){
    super.onStart();
    /**
     * Start the status update thread. It will update the status info while the 
     * Activity is open.
     */
    if (statusUpdateThread == null){
      statusUpdateThread = new Thread(){
        @Override
        public void run(){
          while(statusUpdateThread == this){ //it will stop when we set it to null
            try {
              sleep(STATUS_UPDATE_MS);
            } catch(InterruptedException e){
              continue;
            }
            mHandler.post(updateStatusRunnable);
          }
        }
      };
      statusUpdateThread.start();
    }
  }
  
  @Override
  public void onStop(){
    super.onStop();
    statusUpdateThread = null;
  }
  
  @Override
  public void onDestroy(){
    super.onDestroy();
    dba.close();
    dba = null;
  }
 
  
  private void init(){
    setContentView(R.layout.main);

    // create handler for register button
    Button registerButton = (Button) findViewById(R.id.register_phone_button);
    registerButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i(Constants.TAG,"Sending phone registration.");
        new DeviceRegistrationHandler(ManageActivity.this).register(true);
      }   
    });

    // create handler for sync button
    Button syncButton = (Button) findViewById(R.id.sync_button);
    syncButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i(Constants.TAG, "Sync");
        Intent i = new Intent(ManageActivity.this, OdkManageService.class);
        i.putExtra(OdkManageService.MESSAGE_TYPE_KEY, 
            OdkManageService.MessageType.NEW_TASKS);
        ManageActivity.this.startService(i);
      }   
    });
    
    // create handler for settings button
    Button settingsButton = (Button) findViewById(R.id.settings_button);
    settingsButton.setOnClickListener(new OnClickListener(){
      @Override
      public void onClick(View v) {
        Log.i(Constants.TAG, "Going to preferences");
        Intent i = new Intent(ManageActivity.this, PreferencesActivity.class);
        ManageActivity.this.startActivity(i);
      }   
    });
  }
  
  /**
   * Update the status information at the bottom of the activity.
   */
  private void updateStatus(){
    TextView statusTextView = (TextView) findViewById(R.id.status);
    statusTextView.setText(getLastUpdatedString() + "\n" + getTasksString());
  }
  
  /**
   * 
   * @return A human-readable string stating when tasks were last downloaded
   */
  private String getLastUpdatedString(){
    long lastUpdated = prefsAdapter.getLong(
        Constants.PREF_TASKS_LAST_DOWNLOADED_KEY, -1);
    String preamble = "Tasks last downloaded ";
    String lastUpdatedString = (lastUpdated == -1) ? "never." : 
      Utils.getDurationString((new Date()).getTime() - lastUpdated) + " ago.";
    return preamble + lastUpdatedString;
  }
  
  /**
   * 
   * @return A human-readable string with a summary of the tasks in the local database.
   */
  private String getTasksString(){
    if (dba == null) {
      return "";
    }
    return dba.getPendingTasks().size() + " pending tasks.";
  }
}
