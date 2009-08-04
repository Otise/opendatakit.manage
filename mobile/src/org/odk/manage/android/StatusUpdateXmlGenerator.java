package org.odk.manage.android;

import org.odk.manage.android.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a low-tech solution to generate the status update XML that the client 
 * needs to send to the server when tasks succeed or fail.
 * <p>
 * We can add an XML library to ODK Manage in the future if we need to rely 
 * more heavily on XML.
 * 
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class StatusUpdateXmlGenerator {

  private String imei;
  private List<Task> tasks;
  
  /**
   * Create a status update generator for the given IMEI.
   * @param imei the device IMEI.
   */
  public StatusUpdateXmlGenerator(String imei){
    this.imei = imei;
    tasks = new ArrayList<Task>();
  }
  
  /**
   * Add a {@link Task} to the status update.
   * @param t The Task.
   */
  public void addTask(Task t){
    tasks.add(t);
  }
  
  /**
   * @return the XML for this status update.
   */
  public String outputXml(){
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

