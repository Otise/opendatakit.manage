package org.odk.manage.server.servlet;

import org.odk.manage.server.AdminAccountsConfig;
import org.odk.manage.server.model.DbAdapter;
import org.odk.manage.server.model.Device;
import org.odk.manage.server.model.Task;
import org.odk.manage.server.model.Task.TaskType;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AddTaskServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(AddTaskServlet.class.getName());
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    // authenticate the user as an admin
    if (!AdminAccountsConfig.authenticateAdmin(req, resp)) {
      return;
    }
    
    String imei = req.getParameter("imei");
    if (imei == null){
      debug("No device IMEI");
      resp.getWriter().write("Error: No device IMEI");
      return;
    }
    debug("Device IMEI: " + imei);
    
    DbAdapter dba = null;
    try { 
      dba = new DbAdapter();
      Device device = dba.getDevice(imei);
      if (device == null){
        resp.getWriter().write("Error: Device does not exist.");
        return;
      }
      String type = req.getParameter("type");
      Task task = null;
      if ("addForm".equals(type)){
        debug("Added ADD_FORM task");
        task = new Task(TaskType.ADD_FORM);
        String aggregateSel = req.getParameter("aggregateFormSelect");
        //special case for the link with aggregate
        if (aggregateSel != null && !aggregateSel.equals("") && !aggregateSel.equals("other")){
          String[] nameAndUrl = aggregateSel.split("\"");
          assert(nameAndUrl.length == 2);
          task.setName(nameAndUrl[0]);
          task.setUrl(nameAndUrl[1]);
        } else {
          String url = req.getParameter("url");
          if (url == null || url.equals("")) {
            resp.getWriter().write("Task URL not specified.");
            return;
          }
          task.setUrl(url);
        }
      } else if ("installPackage".equals(type)){
        debug("Added INSTALL_PACKAGE task");
        task = new Task(TaskType.INSTALL_PACKAGE);
        String url = req.getParameter("url");
        if (url == null || url.equals("")) {
          resp.getWriter().write("Task URL not specified.");
          return;
        }
        task.setUrl(url);
      } else {
        debug("Unsupported task type");
        resp.getWriter().write("Error: Unsupported task type");
        return; //not a valid task type
      }
      
      debug("Tasks: " + device.getTasks());
      debug("Task: " + task);
      
      device.addTask(task);
      resp.getWriter().write("Task successfully added");
    } finally {
      dba.close();
    }
  }
  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }
  
}