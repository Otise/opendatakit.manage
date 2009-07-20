package org.odk.manage.android.worker;

import android.util.Log;

import org.odk.manage.android.Constants;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A design pattern for synchronously executing a queue of 'tasks' (code) in a 
 * thread (the worker thread). 
 * 
 * TODO(alerer): implement timeouts on tasks. This probably requires that we 
 * have a 'monitor' thread (whats called workerThread now) that runs each task 
 * in its own thread, and waits for it to complete. The monitor thread also 
 * times out after TIMEOUT and destroys the work thread, moving on to the next 
 * one (note: this is dangerous for concurrency reasons).
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class Worker {
  
  BlockingQueue<WorkerTask> q = new LinkedBlockingQueue<WorkerTask>();
  WorkerThread workerThread = new WorkerThread();
  
  class WorkerThread extends Thread {
    public boolean stopped = true;
    
    public WorkerThread(){
    }
    @Override
    public void run() { 
      while (!stopped) {
        try { 
           WorkerTask t = q.take(); 
           t.execute(); 
         } catch (InterruptedException e) {
           Log.d(Constants.TAG, "WorkerTask interrupted", e);
         }
       }
    }
  }

  /**
   * Start the worker thread processing.
   */
  public void start(){
    workerThread.stopped = false;
    workerThread.start();
  }
  
  /**
   * End the worker thread (eventually).
   */
  public void stop(){
    workerThread.stopped = true;
  }
  
  /**
   * Add a task to the task queue
   * @param t
   */
  public void addTask(WorkerTask t) {
    try { 
      q.put(t); 
    } catch (InterruptedException e) {
      Log.e(Constants.TAG, "Exception adding task to worker queue", e);
    }
  }
  
  /**
   * Removes a WorkerTask from the queue if it exists in the queue.
   * @param t
   * @return true if the task was removed.
   */
  public boolean removeTask(WorkerTask t){
    return q.remove(t);
  }
}
