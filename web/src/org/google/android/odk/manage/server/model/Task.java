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

package org.google.android.odk.manage.server.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    DOWNLOAD_FORM("downloadForm"),
    SEND_FORMS("submitForm");
    private final String xmlTag;
    TaskType(String xmlTag){
      this.xmlTag = xmlTag;
    }
    public String xmlTag(){ return xmlTag; }
  }
  
  public enum TaskStatus {
    PENDING,
    FAILED,
    SUCCESS
  }
  
  public Task(TaskType type){
    this.type = type;
    this.properties = new HashMap<String,String>();
    this.status = TaskStatus.PENDING;
  }

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent
  public TaskType type;
  
  @Persistent
  private TaskStatus status;
  
  //right now, not sure how to deal with different types of tasks, since JDO
  //does not allow for polymorphism...so just using this generic but not really 
  //typesafe approach for now
  @Persistent
  private Map<String,String> properties;
  
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
 
}