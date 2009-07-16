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

public class DeleteTasksServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(DeleteTasksServlet.class.getName());
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    // authenticate the user as an admin
    if (!AdminAccountsConfig.authenticateAdmin(req, resp)) {
      return;
    }
    String[] taskIds = req.getParameterValues("taskId");
    String imei = req.getParameter("imei");
    if (imei == null || taskIds == null){
      debug("No device IMEI or no tasks");
      resp.getWriter().write("Error: No device IMEI or no tasks");
      return;
    }
    debug("Device IMEI: " + imei);
    
    DbAdapter dba = null;
    try { 
      dba = new DbAdapter();
      Device device = dba.getDevice(imei); //why are we doing this?
      if (device == null){
        resp.getWriter().write("Error: Device does not exist.");
        return;
      }
      for (String id : taskIds){
        Task task = dba.getTask(id);
        if (task == null) {
          debug("Task ID did nto correspond to a task...");
          continue;
        }
        dba.deleteTask(task);
      }
      resp.sendRedirect("viewTasks?imei=" + imei);
    } finally {
      dba.close();
    }
  }
  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }
  
}