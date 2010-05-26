package org.odk.manage.server.servlet;

import org.odk.manage.server.AdminAccountsConfig;
import org.odk.manage.server.Constants;
import org.odk.manage.server.model.DbAdapter;
import org.odk.manage.server.model.Device;
import org.odk.manage.server.model.Task;
import org.odk.manage.server.model.Task.TaskStatus;
import org.odk.manage.server.model.Task.TaskType;
import org.odk.manage.server.sms.SmsService;
import org.odk.manage.server.sms.SmsServiceFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handles 'performing actions', i.e. submissions from the 'Perform Action' console 
 * at the top of the ODK Manage admin page.
 * 
 * Actions include adding tasks to devices, as well as sending SMS notifications/messages.
 * 
 * TODO: clean up the business logic, and move it  somewhere other than this servlet.
 * @author alerer@google.com (Adam)
 *
 */
public class DoActionServlet extends ServletUtilBase {
  
  public static final String ADDR = "doAction";
  
  private DbAdapter dba = null;
  SmsService smsService = SmsServiceFactory.getService();
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // authenticate the user as an admin
    if (!AdminAccountsConfig.authenticateAdmin(this, req, resp)) {
      return;
    }
    
    String[] imeis = req.getParameterValues("imei");
    String type = req.getParameter("actionType");
    
    if (imeis == null){
      redirectMain(resp, "No devices selected.", false);
      return;
    }
    
    if (type == null){
      redirectMain(resp, "No action type selected.", false);
      return;
    }
    
    TaskType taskType = null;
    try {
      taskType = TaskType.valueOf(type); // will be null if this action is not a task
    } catch (IllegalArgumentException e) {
      // This is fine because not all types will be tasktypes.
    }
    
    if (taskType != null) {
      String name = getParameter(req, type, "name");
      String url = getParameter(req, type, "url");
      String extras = getParameter(req, type, "extras");
      
      //TODO(alerer): this is a hack - we should use javascript to turn this into 
      //name/url fields on the client side :)
      String aggregateSel = req.getParameter("aggregateFormSelect");
      if (type.equals("ADD_FORM") && 
          aggregateSel != null && 
          !aggregateSel.equals("") && 
          !aggregateSel.equals("other")){
        String[] nameAndUrl = aggregateSel.split("\"");
        name = nameAndUrl[0];
        url = nameAndUrl[1];
      }
      
      int successes = addTasksToDevices(taskType, name, url, extras, imeis);
      
      redirectMain(resp, "Added task (" + type + ") to " + successes + " of " + imeis.length + " devices.", true);
      return;
    } else {
      handleNonTaskAction(type, imeis, req, resp);
      // this is a non-task action
    }
  }
   
  /**
   * Adds a copy of a defined task to each of an array of devices (by IMEI).
   * @param taskType The task type.
   * @param name The task name value.
   * @param url The task URL value.
   * @param extras The task extras value.
   * @param imeis An array of IMEIs for which this task should be added.
   * @return The number of IMEIs for which the task was successfully added.
   */
  private int addTasksToDevices(TaskType taskType, String name, String url, String extras, String[] imeis){
    int numSuccess = 0;
    try { 
      dba = new DbAdapter();
      for (String imei: imeis) {
        //XXX(alerer): app engine can't do all these writes efficiently.
        // We're going to have to put this in a task queue, but they're not released yet.
        Device device = dba.getDevice(imei);
        if (device == null){
          debug("Error: Device does not exist. IMEI: " + imei);
          continue;
        }
        addTask(device, taskType, TaskStatus.PENDING, name, url, extras);
        numSuccess++;
      }
    } finally {
      dba.close();
      dba = null;
    }
    return numSuccess;
  }
  
  /**
   * Handle a non-task action.
   * 
   * @param type The action type.
   * @param imeis The set of IMEIs to perform the action on.
   * @param req The servlet request.
   * @param resp The servlet response.
   * @throws IOException
   */
  private void handleNonTaskAction(String type, String[] imeis, HttpServletRequest req, HttpServletResponse resp) throws IOException{
    if (type.equals("SEND_SMS") || type.equals("NEW_TASKS_SMS")){
      String content = getParameter(req, type, "content");
      if (content == null){
        content = "";
      }
      int numSuccess = 0;
      for (String imei: imeis) {
        Device device = dba.getDevice(imei);
        if (device == null){
          debug("Error: Device does not exist. IMEI: " + imei);
          continue;
        }
        if (type.equals("NEW_TASKS_SMS")) {
          if (content.length() > 120) {
            redirectMain(resp, "Message is more than 120 characters.", false);
            return;
          }
          if (smsService.sendSms(device, Constants.NEW_TASKS_TRIGGER + ": " + content)) {
            numSuccess++;
          }
        } else if (type.equals("SEND_SMS")) {
          if (content.length() > 140) {
            redirectMain(resp, "Message is more than 140 characters.", false);
            return;
          }
          if (smsService.sendSms(device, content)) {
            numSuccess++;
          }
        }
      } 
      redirectMain(resp, "SMS messages were sent to " + numSuccess + 
          " out of " + imeis.length + " devices.", numSuccess > 0 || imeis.length == 0);
      return;
    // ------------------------- add more non-task actions here ---------------------
    // } else if (type.equals("NEW_TYPE")) {
    //     do something
    } else {
      redirectMain(resp, "Unrecognized action.", false);
      return;
    }

  }
  
  /**
   * Get a parameter encoded by action type.
   * @param req The HttpServletRequest object.
   * @param actionType The action type.
   * @param parameter The parameter name.
   * @return The value of the parameter, or null if none exists.
   */
  private String getParameter(HttpServletRequest req, String actionType, String parameter){
    return req.getParameter(actionType + "." + parameter);
  }
  
  /**
   * Redirect to the admin page with a message.
   * @param resp The servlet response.
   * @param message The message string.
   * @param success If true, this is a success message; if false, a failure message.
   * @throws IOException
   */
  private void redirectMain(HttpServletResponse resp, String message, boolean success)
   throws IOException {
    resp.sendRedirect(ManageAdminServlet.ADDR + "?messageType=" + (success?"success":"error") + 
        "&message=" + URLEncoder.encode(message, "UTF-8"));
  }
  
  /**
   * Adds a task to a device.
   * 
   * @param device
   * @param type
   * @param status
   * @param name
   * @param url
   * @param extras
   */
  private void addTask(Device device, TaskType type, TaskStatus status, 
      String name, String url, String extras) {
    Task t = new Task(type);
    t.setName(name);
    t.setUrl(url);
    t.setExtras(extras);
    t.setStatus(status);
    device.addTask(t);
  }

}
