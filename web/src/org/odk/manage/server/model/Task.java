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
import com.google.appengine.api.datastore.KeyFactory;

import javax.jdo.annotations.IdGeneratorStrategy;
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
public class Task {
  
  public enum TaskType {
    ADD_FORM,
    INSTALL_PACKAGE;
  }
  
  public enum TaskStatus {
    PENDING,
    FAILED,
    SUCCESS
  }
  
  public Task(TaskType type){
    this.type = type;
    this.status = TaskStatus.PENDING;
  }

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;
  
  @Persistent
  private TaskType type;
  
  @Persistent
  private TaskStatus status;
  
  @Persistent
  private String name;
  
  @Persistent
  private String url;
  
  @Persistent
  private String extras;
  
  @Persistent
  private Device device;
  
  
  //right now, not sure how to deal with different types of tasks, since JDO
  //does not allow for polymorphism (?)...so just using this generic but not really 
  //typesafe approach for now
  //@Persistent
  //private Map<String,String> properties;
  //seems that HashMap is not supported
  
  public TaskType getType(){
    return type;
  }
  public TaskStatus getStatus(){
    return status;
  }
  public void setStatus(TaskStatus ts){
    if (device != null) {
      device.incTaskCount(status, -1);
      device.incTaskCount(ts, 1);
    }
    status = ts;
  }
  public String getUniqueId(){
    return KeyFactory.keyToString(key);
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getName() {
    return name;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public String getUrl() {
    return url;
  }
  public void setExtras(String extras) {
    this.extras = extras;
  }
  public String getExtras() {
    return extras;
  }
  public Device getDevice() {
    return device;
  }
  /**
   * Internal use only! Should only be called by Device.addTask and Device.removeTask;
   * otherwise, it will break invariants.
   */
  protected void setDevice(Device device) {
    this.device = device;
  }
  @Override
  public String toString() {
    return "<" + type + "|" + status + "|" + name + "|" + url + "|" + extras + ">";
  }

 
}