// Copyright 2009 Google Inc.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//      http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.odk.manage.server.model;

import org.odk.manage.server.model.Task.TaskStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * A data object storing data about a particular device (i.e. phone).
 *
 * @author alerer@google.com (Adam Lerer)
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Device {
  
  /**
   * Create a new device.
   * @param imei The device IMEI.
   */
  public Device(String imei){
    this.key = "i" + imei;
    this.imei = imei;

    checkInvariants();
  }

  @PrimaryKey
  private String key;
  
  @Persistent
  private String imei;
   
  @Persistent
  private String imsi;
  
  @Persistent
  private String sim;

  @Persistent
  private String phoneNumber;
  
  @Persistent
  private String smsValidator;
  
  @Persistent
  private Date lastContacted;
  
  /**
   * userId can be set by the client or server.
   */
  @Persistent
  private String userId;
  
  /**
   * Comments are set by the admin on the server side.
   */
  @Persistent
  private String comments;
  
  @Persistent(mappedBy = "device")
  private List<Task> tasks;
  
  /**
   * These are an optimization for the datastore.
   */
  @Persistent
  private int numPending;
  @Persistent
  private int numSuccess;
  @Persistent
  private int numFailed;
  
  /**
   * Called by Device and Task.<br>
   * WARNING: Do not call this method unless you know what you are doing; calling 
   * this method improperly can break model invariants.
   * @param status The TaskStatus to modify.
   * @param inc The amount to increment the task count for that status. May be negative.
   */
  protected void incTaskCount(TaskStatus status, int inc){
        switch(status){
          case PENDING:
            numPending += inc;
            break;
          case SUCCESS:
            numSuccess += inc;
            break;
          case FAILED:
            numFailed += inc;
            break;
        }
  }
  
  private void checkInvariants(){
    assert(key != null);
    assert(key.equals("i" + imei));
    assert(tasks.size() == getTaskCount(null)); //we could do this specifically for each status
  }
  
  /////////////////////////// Public Methods ///////////////////////////
  
  /**
   * 
   * @param status The TaskStatus.
   * @return All tasks with the given status, or all tasks if status is null.
   */
  public List<Task> getTasks(TaskStatus status){
    if (status == null) 
      return Collections.unmodifiableList(tasks);
    
//    Query query = PMF.get().getPersistenceManager().newQuery(Task.class, tasks);
//    query.setFilter("status == statusParam");
//    query.declareParameters("String statusParam");
//    return Collections.unmodifiableList(
//        (List<Task>) query.execute(status.name()));
    
    List<Task> res = new ArrayList<Task>();
    for (Task t: tasks) {
      if (t.getStatus().equals(status)){
        res.add(t);
      }
    }
    return res;
  }
 
  /**
   * Get the number of tasks for a particular {@link TaskStatus}.
   * This is much more efficient than calling getTasks(status).size(), because 
   * the task count is stored locally within the Device, so the task list does 
   * not have to be fetched.
   * @param status The TaskStatus.
   * @return The number of tasks with that TaskStatus.
   */
  public int getTaskCount(TaskStatus status){
    //return getTasks(status).size();
    switch(status){
      case PENDING:
        return numPending;
      case SUCCESS:
        return numSuccess;
      case FAILED:
        return numFailed;
      default:
    	return numPending + numSuccess + numFailed;
	}
  }

  /**
   * Add a Task to this Device.
   * @param t The Task to add.
   */
  public void addTask(Task t){
    if (t == null || t.getType()==null || t.getStatus()==null){
      throw new NullPointerException();
    }
    tasks.add(t);
    t.setDevice(this);
    incTaskCount(t.getStatus(), 1);
  }
 
  /**
   * Remove a Task from this Device.
   * @param t The Task to remove.
   * @return true if the Task existed for this device.
   */
  public boolean removeTask(Task t){
    // all the nonsense her is because appengine JDO does not really work 
    // properly on collections. Basically, the 'list' is just a query that finds 
    // everything that has the correct value in the mappedTo field - you can't 
    // perform contains() or remove() operations on the list.
	if (t.getDevice() == null || !t.getDevice().equals(this)){
		return false;
	}
	t.setDevice(null);
	incTaskCount(t.getStatus(), -1);
	return true;
  }

  /**
   * 
   * @return The IMEI for this Device.
   */
  public String getImei() {
    return imei;
  }

  /**
   * 
   * @param imei The IMEI for this Device.
   */
  public void setImei(String imei) {
    this.imei = imei;
  }
  
  /**
   * 
   * @param sim The SIM serial # for this Device.
   */
  public void setSim(String sim) {
    this.sim = sim;
  }

  /**
   * 
   * @return The SIM serial # for this Device.
   */
  public String getSim() {
    return sim;
  }

  /**
   * 
   * @param imsi The IMSI for this Device.
   */
  public void setImsi(String imsi) {
    this.imsi = imsi;
  }

  /**
   * 
   * @return The IMSI for this Device.
   */
  public String getImsi() {
    return imsi;
  }

  /**
   * 
   * @param phoneNumber The phone number for this Device.
   */
  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  /**
   * 
   * @return The phone number for this Device.
   */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * 
   * @param smsValidator Place to store some string used by the {@link org.odk.manage.server.sms.SmsService}
   * to handle SMS. May or may not be used by a particular {@link org.odk.manage.server.sms.SmsService}.
   */
  public void setSmsValidator(String smsValidator) {
    this.smsValidator = smsValidator;
  }

  /**
   * 
   * @see Device#setSmsValidator
   */
  public String getSmsValidator() {
    return smsValidator;
  }

  /**
   * 
   * @param userId The client-configured user ID for the device.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * 
   * @return The client-configured user ID for the device.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * 
   * @param comments Administrator-entered comments about the device/operator.
   */
  public void setComments(String comments) {
    this.comments = comments;
  }

  /**
   * 
   * @return Administrator-entered comments about the device/operator.
   */
  public String getComments() {
    return comments;
  }

  /**
   * 
   * @param lastContacted The last time the server contacted this device.
   */
  public void setLastContacted(Date lastContacted) {
    this.lastContacted = lastContacted;
  }

  /**
   * 
   * @return The last time the server contacted this device.
   */
  public Date getLastContacted() {
    return lastContacted;
  }
 
}