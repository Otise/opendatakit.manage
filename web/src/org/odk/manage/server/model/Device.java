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
 * 
 *
 * @author alerer@google.com (Adam Lerer)
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Device {
  
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
  private String numberWithValidator;
  
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
   * 
   * @param status
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
   * These are an optimization for the db. We use Integer for pass-by-reference
   */
  @Persistent
  private int numPending;
  @Persistent
  private int numSuccess;
  @Persistent
  private int numFailed;
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
   * Called by Device and Task.
   * @param status
   * @param count
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

  public void addTask(Task t){
    if (t == null || t.getType()==null || t.getStatus()==null){
      throw new NullPointerException();
    }
    tasks.add(t);
    t.setDevice(this);
    incTaskCount(t.getStatus(), 1);
  }
 
  
  public boolean removeTask(Task t){
    // all the nonsense her is because appengine JDO does not really work 
    // properly on collections. Basically, the 'list' is just a query that finds 
    // everything that has the correct value in the mappedTo field - you can't 
    // perform contains() or remove() operations on the list.
	if (t.getDevice() == null || !t.getDevice().equals(this)){
		return false;
	}
	//XXX(alerer): Appengine's list.remove() doesn't work - but this mapping 
	// thing does.
	t.setDevice(null);
	incTaskCount(t.getStatus(), -1);
	return true;
  }
  
  private void checkInvariants(){
    assert(key != null);
    assert(key.equals("i" + imei));
    assert(tasks.size() == getTaskCount(null)); //we could do this specifically for each status
  }

  public String getImei() {
    return imei;
  }

  public void setImei(String imei) {
    this.imei = imei;
  }
  
  public void setSim(String sim) {
    this.sim = sim;
  }

  public String getSim() {
    return sim;
  }

  public void setImsi(String imsi) {
    this.imsi = imsi;
  }

  public String getImsi() {
    return imsi;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setNumberWithValidator(String numberWithValidator) {
    this.numberWithValidator = numberWithValidator;
  }

  public String getNumberWithValidator() {
    return numberWithValidator;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getComments() {
    return comments;
  }

  public void setLastContacted(Date lastContacted) {
    this.lastContacted = lastContacted;
  }

  public Date getLastContacted() {
    return lastContacted;
  }
 
}