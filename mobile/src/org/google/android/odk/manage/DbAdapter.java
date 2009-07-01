// Copyright 2008 Google Inc. All Rights Reserved.

package org.google.android.odk.manage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.google.android.odk.common.Task;
import org.google.android.odk.common.Task.TaskStatus;
import org.google.android.odk.common.Task.TaskType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple survey database access helper class. Modified from NotesDbAdapter from
 * the Google Notepad example. 
 * 
 * @author alerer@google.com (Adam Lerer)
 */
public class DbAdapter {

  private static final String TASKS_TABLE = "tasks2";
  public static final String KEY_TASKS_ID = "id";
  public static final String KEY_TASKS_TYPE = "type";
  public static final String KEY_TASKS_URL = "url";
  public static final String KEY_TASKS_STATUS = "status";
  public static final String[] ALL_TASKS_KEYS =
      new String[] {
          KEY_TASKS_ID,
          KEY_TASKS_TYPE,
          KEY_TASKS_URL,
          KEY_TASKS_STATUS};

  /**
   * Command to create the table of surveys.
   */
  private static final String CREATE_TASKS_TABLE =
      "create table if not exists "
          + TASKS_TABLE
          + " ( "
          + KEY_TASKS_ID
          + " long primary key, "
          + KEY_TASKS_TYPE
          + " text not null, "
          + KEY_TASKS_URL
          + " text,"
          + KEY_TASKS_STATUS
          + " text not null);"; 

  private SQLiteDatabase mDb;
  private final Context mCtx;
  private final String dbName;

  public static final String TAG = "Surveyor - DbAdapter";

  /**
   * Constructor - takes the context to allow the database to be opened/created.
   * 
   * @param ctx the Context within which to work
   * @param dbName the name of the database.
   */
  public DbAdapter(Context ctx, String dbName) {
    this.dbName = dbName;
    this.mCtx = ctx;
  }

  /**
   * Open the database. If it cannot be opened, try to create a new instance of
   * the database.
   * 
   * @return this
   * 
   */
  public DbAdapter open() {
    mDb = mCtx.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
    mDb.execSQL(CREATE_TASKS_TABLE);
    Log.d(TAG, "Created tables.");
    return this;
  }

  /**
   * Close the database.
   */
  public void close() {
    mDb.close();
    mDb = null; // so we can check that mDb is open
  }

  /**
   * Add a task to the database.
   * @param t The task to add.
   * @return The task ID, -1 if unsuccessful, -2 if the task already exists.
   */
  public long addTask(Task t){
    assert (mDb != null); // the database is open
    
    // SQLite does not handle duplicate primary keys gracefully
    Cursor c =
        mDb.query(TASKS_TABLE, null, KEY_TASKS_ID + " = " + t.getUniqueId(), 
            null, null, null, null);
    if (c.getCount() != 0){
      Log.d(Constants.TAG, "This task already exists in the database.");
      c.close();
      return -2;
    }
    c.close();
    
    ContentValues values = new ContentValues();
    
    if (t.getType() == null) {
      throw new IllegalArgumentException("This task has null type");
    }
    
    values.put(KEY_TASKS_ID, t.getUniqueId());
    values.put(KEY_TASKS_TYPE, t.getType().name());
    values.put(KEY_TASKS_URL, t.getProperty("url"));
    values.put(KEY_TASKS_STATUS, t.getStatus().name());
    
    Log.d(TAG, "Added task. Id: " + t.getUniqueId()
        + ", Type: "+ t.getType()
        + ", URL: " + t.getProperty("url")
        + ", Status: " + t.getStatus());
    
    long id = mDb.insert(TASKS_TABLE, null, values);
    
    if (id == -1) {
      throw new SQLException("Could not insert row into surveys database.");
    }
    
    return id;
  }

  /**
   * Delete the task with the given id.
   * 
   * @param id id of task to delete
   * @return true if deleted, false otherwise
   */
  public boolean deleteTask(long id) {
    assert (mDb != null); // the database is open
    return mDb.delete(TASKS_TABLE, KEY_TASKS_ID + "= " + id, null) > 0;
  }


  public List<Task> getPendingTasks() {
    assert (mDb != null); // the database is open
    Cursor c =
        mDb.query(TASKS_TABLE, ALL_TASKS_KEYS, KEY_TASKS_STATUS + " = ?", 
            new String[]{TaskStatus.PENDING.name()}, null, null, null);
    List<Task> l = getTasksFromCursor(c);
    c.close();
    return l;
  }
  
  /**
   * Return a Cursor over the list of all surveys NOTE: Cursors allow for WRITE
   * access to their result set. This object CANNOT be exposed to the user.
   * 
   * @return Cursor over all surveys
   */
  public List<Task> getAllTasks() {
    assert (mDb != null); // the database is open
    Cursor c =
        mDb.query(TASKS_TABLE, ALL_TASKS_KEYS, null, null, null, null, null);
    List<Task> l = getTasksFromCursor(c);
    c.close();
    return l;
  }

  /**
   * Return a task that matches the given id
   * 
   * @param id id of task to retrieve
   * @return Task with the given id, or null if no survey with this id
   *         exists
   */
  public Task getTask(long id) {
    assert (mDb != null); // the database is open
    Log.d(TAG, "Getting survey #" + id);
    Cursor c =
        mDb.query(
            TASKS_TABLE,
            ALL_TASKS_KEYS,
            KEY_TASKS_ID + "=" + id,
            null,
            null,
            null,
            null);
    if (c.getCount() == 0) {
      c.close();
      return null;
    }
    List<Task> isl = getTasksFromCursor(c);
    c.close();
    assert (isl.size() == 1);
    return isl.get(0);
  }

  /**
   * Sets the task status for this tasks, both locally and in the database.
   * @param t The task to be modified.
   * @param success The new status.
   */
  public void setTaskStatus(Task t, TaskStatus status) {
    t.setStatus(status);
    ContentValues values = new ContentValues();
   
    values.put(KEY_TASKS_STATUS, status.name());
    mDb.update(TASKS_TABLE, values, KEY_TASKS_ID + " = " + t.getUniqueId(), null);
    
  }
  
  /**
   * Parses the cursor output from a tasks database query into a list of Task
   * objects.
   * 
   * @param c the cursor
   * @throws IllegalStateException if the cursor does not have columns
   *         corresponding to the elements of ALL_KEYS
   * @return a list of the surveys contained in the cursor.
   */
  private List<Task> getTasksFromCursor(Cursor c)
      throws IllegalArgumentException {
    assert (mDb != null); // the database is open

    Map<String, Integer> indexMap = new HashMap<String, Integer>();
    for (String key : ALL_TASKS_KEYS) {
      indexMap.put(key, c.getColumnIndexOrThrow(key));
    }

    List<Task> tasks = new ArrayList<Task>();

    c.moveToNext();
    while (!c.isAfterLast()) {
      //why not just c.getString(c.getColumnIndexOrThrow(KEY_SURVEY_URL)) etc.?
      //because then you'd have to do it on each iteration.
      Task task =
          new Task(
              c.getLong(indexMap.get(KEY_TASKS_ID)),
              Enum.valueOf(TaskType.class, c.getString(indexMap.get(KEY_TASKS_TYPE))),
              Enum.valueOf(TaskStatus.class, c.getString(indexMap.get(KEY_TASKS_STATUS))));
      task.setProperty("url", c.getString(indexMap.get(KEY_TASKS_URL)));
             
      tasks.add(task);
      c.moveToNext();
    }
    return tasks;
  }




}