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

import com.google.appengine.api.datastore.Key;

import java.util.ArrayList;
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
  private String userId;
  
  //type DeviceGroup 
  @Persistent
  public Key group;
  
  // this should eventually be pendingTasks and completedTasks?
  @Persistent
  private List<Task> tasks;
  
  public List<Task> getTasks(){
    if (this.tasks == null)
      this.tasks = new ArrayList<Task>();
    return tasks;
  }
  
  public void addTask(Task t){
    if (this.tasks == null)
      this.tasks = new ArrayList<Task>();
    this.tasks.add(t);
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
 
}