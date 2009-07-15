package org.odk.manage.server.servlet;

import org.odk.manage.server.AdminAccountsConfig;
import org.odk.manage.server.SmsSender;
import org.odk.manage.server.model.DbAdapter;
import org.odk.manage.server.model.Device;
import org.odk.manage.server.model.Task;
import org.odk.manage.server.model.Task.TaskStatus;
import org.odk.manage.server.model.Task.TaskType;

import java.io.IOException;
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
    
    String aggregateSel = req.getParameter("aggregateFormSelect");
    if (aggregateSel != null && !aggregateSel.equals("") && !aggregateSel.equals("other")){
      String[] nameAndUrl = aggregateSel.split("\"");
      assert(nameAndUrl.length == 2);
      name = nameAndUrl[0];
      url = nameAndUrl[1];
    }
    
    
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
        } else if (type.equals("NEW_TASKS_SMS")) {
          new SmsSender().sendNewTaskNotification(device);
        } else {
          debug("Error: Invalid Task Type");
          resp.sendError(400);
        }
      }
       
    } finally {
      dba.close();
      dba = null;
    }
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
