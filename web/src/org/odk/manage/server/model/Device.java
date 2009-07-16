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
    this.key = "imei" + imei;
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
  
  public int getTaskCount(Task.TaskStatus status){
    return getTasks(status).size();
  }
  
//  private List<Task> getTasklistForStatus(TaskStatus status){
//    if (status == null){
//      List<Task> res = new ArrayList<Task>();
//      res.addAll(pendingTasks);
//      res.addAll(successTasks);
//      res.addAll(failedTasks);
//    }
//    
//    switch(status){
//      case PENDING:
//        if (pendingTasks == null)
//          pendingTasks = new ArrayList<Task>();
//        return pendingTasks;
//      case SUCCESS:
//        if (successTasks == null)
//          successTasks = new ArrayList<Task>();
//        return successTasks;
//      case FAILED:
//        if (failedTasks == null)
//          failedTasks = new ArrayList<Task>();
//        return failedTasks;
//      default:
//        throw new IllegalArgumentException();
//    }
//  }
  
  public void addTask(Task t){
    if (t == null || t.getType()==null || t.getStatus()==null){
      throw new NullPointerException();
    }
    tasks.add(t);
  }
  
  public boolean removeTask(Task t){
    //return tasks.remove(t); - doesn't work because of JDO bug
	int index = tasks.indexOf(t);
	if (index == -1){
		return false;
	}
	tasks.remove(index);
	return true;
  }
  
  private void checkInvariants(){
    assert(key != null);
    assert(key.equals("imei" + imei));
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