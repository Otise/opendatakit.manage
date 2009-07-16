package org.odk.manage.server.servlet;

import org.odk.manage.server.AdminAccountsConfig;
import org.odk.manage.server.SmsSender;
import org.odk.manage.server.model.DbAdapter;
import org.odk.manage.server.model.Device;
import org.odk.manage.server.model.Task;
import org.odk.manage.server.model.Task.TaskStatus;
import org.odk.manage.server.model.Task.TaskType;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DoActionServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(DoActionServlet.class.getName());
  
  private DbAdapter dba = null;
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // authenticate the user as an admin
    if (!AdminAccountsConfig.authenticateAdmin(req, resp)) {
      return;
    }
    
    String[] imeis = req.getParameterValues("imei");
    String type = req.getParameter("actionType");
    
    if (imeis == null || type == null){
      return;
    }
    
    resp.getWriter().write("Performing " + type + " on " + imeis.length + " devices.");
    
    TaskType tasktype = null;
    try {
      tasktype = TaskType.valueOf(type); // will be null if this action is not a task
    } catch (IllegalArgumentException e) {
      // This is fine because not all types will be tasktypes.
    }
    String name = req.getParameter(type + ".name");
    String url = req.getParameter(type + ".url");
    String extras = req.getParameter(type + ".extras");
    
    //TODO(alerer): this is ugly - we should use javascript to turn this into 
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
    
    /**
     * TODO(alerer): this section should eventually be refactored - there are some parts 
     * that are common between action types (e.g. iterating over IMEIs), but 
     * other parts are different (e.g. counting successes). There are nice ways 
     * to do this, but not worth the complexity at the moment.
     */
    int numSuccess = 0;
    try { 
      dba = new DbAdapter();
      for (String imei: imeis) {
        //XXX(alerer): app engine can't do all these writes efficiently.
        // We're going to have to put this in a task queue, but they're not released yet.
        // TODO(alerer): put in task queues

        Device device = dba.getDevice(imei);
        if (device == null){
          debug("Error: Device does not exist. IMEI: " + imei);
          continue;
        }
        
        if (tasktype != null) {
          addTask(device, tasktype, TaskStatus.PENDING, name, url, extras);
          numSuccess++;
        } else if (type.equals("NEW_TASKS_SMS")) {
          if (new SmsSender().sendNewTaskNotification(device)) {
            numSuccess++;
          }
        } else {
          break;
        }
      }
      
    } finally {
      dba.close();
      dba = null;
    }
    if (tasktype != null) {
      redirectMain(resp, "Added task (" + type + ") to " + imeis.length + " devices.", true);
    } else if (type.equals("NEW_TASKS_SMS")) {
      redirectMain(resp, "New tasks SMS notifications were sent to " + numSuccess + 
          " out of " + imeis.length + " devices.", numSuccess > 0 || imeis.length == 0);
    } else {
        redirectMain(resp, "Unrecognized action.", false);
    }
  }
  
  private void redirectMain(HttpServletResponse resp, String message, boolean success)
   throws IOException {
    resp.sendRedirect("admin.html?messageType=" + (success?"success":"error") + 
        "&message=" + URLEncoder.encode(message, "UTF-8"));
  }
  
  private void addTask(Device device, TaskType type, TaskStatus status, 
      String name, String url, String extras) {
    Task t = new Task(type);
    t.setName(name);
    t.setUrl(url);
    t.setExtras(extras);
    t.setStatus(status);
    device.addTask(t);
  }
  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }

}
