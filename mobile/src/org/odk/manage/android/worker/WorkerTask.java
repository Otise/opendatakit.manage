package org.odk.manage.android.worker;

/**
 * An interface for an executable action to be performed by a worker thread.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public interface WorkerTask {
  /**
   * The action to be executed goes here.
   */
  public void execute();
  public long getTimeoutMillis();
}
