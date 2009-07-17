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

package org.odk.common.android;


/**
 * 
 *
 * @author alerer@google.com (Adam Lerer)
 */
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
  
  public Task(String uniqueId, TaskType type, TaskStatus status){
    this.id = uniqueId;
    this.type = type;
    this.status = status;
    this.statusSynced = true;
    this.numAttempts = 0;
    checkInvariants();
  }
 
  private String id;
  
  private String name;
  
  private String url;
  
  private String extras;
  
  private TaskType type;
  
  private TaskStatus status;
  
  private boolean statusSynced;
  
  private int numAttempts;
  
  public TaskType getType(){
    return type;
  }
  public TaskStatus getStatus(){
    return status;
  }
  public void setStatus(TaskStatus ts){
    this.status = ts;
    checkInvariants();
  }
  public String getUniqueId(){
    return id;
  }
  
  private void checkInvariants(){
    assert(type != null);
    assert(status != null);
    assert(numAttempts > 0);
  }
  
  @Override
  public boolean equals(Object other){
    if (!(other instanceof Task))
      return false;
    return (this.id == ((Task) other).id);
  }
  
  @Override
  public int hashCode(){
    return new Long(this.id).intValue();
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
  public void setStatusSynced(boolean statusSynced) {
    this.statusSynced = statusSynced;
  }
  public boolean isStatusSynced() {
    return statusSynced;
  }
  public void setNumAttempts(int numTries) {
    this.numAttempts = numTries;
  }
  public int getNumAttempts() {
    return numAttempts;
  }
 
}