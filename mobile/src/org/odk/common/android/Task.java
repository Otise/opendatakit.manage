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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
  
  public Task(long id, TaskType type, TaskStatus status){
    this.id = id;
    this.type = type;
    this.properties = new HashMap<String,String>();
    this.status = status;
    checkInvariants();
  }

  private long id;

  private Map<String,String> properties;
  
  private TaskType type;
  
  private TaskStatus status;
  
  public void setProperty(String name, String value){
    properties.put(name, value);
  }
  public Set<String> getPropertyNames(){
    return properties.keySet();
  }
  public String getProperty(String name){
    return properties.get(name);
  }
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
  public long getUniqueId(){
    return id;
  }
  
  private void checkInvariants(){
    assert(id > 0);
    assert(type != null);
    assert(status != null);
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
 
}