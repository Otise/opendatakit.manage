package org.odk.manage.android;

import org.odk.common.android.Task;

import java.util.ArrayList;
import java.util.List;

public class StatusUpdateXmlGenerator {

  private String imei;
  private List<Task> tasks;
  
  public StatusUpdateXmlGenerator(String imei){
    this.imei = imei;
    tasks = new ArrayList<Task>();
  }
  
  public void addTask(Task t){
    tasks.add(t);
  }
  
  /**
   * There doesn't seem to be any XML serialization available on Android. ???
   */
  public String toString(){
    StringBuilder doc = new StringBuilder("");
    doc.append("<update imei='" + imei + "'>");
    for (Task t: tasks){
      doc.append("<task id='" + t.getUniqueId() + 
          "' status='" + t.getStatus().name() + "' />");
    }
    doc.append("</update>");
    
    return doc.toString();
  }
  

}

