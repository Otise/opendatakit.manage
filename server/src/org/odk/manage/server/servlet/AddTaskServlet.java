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

/**
 * @deprecated Replaced with DoActionServlet.
 * @author alerer@google.com (Your Name Here)
 *
 */
@Deprecated
public class AddTaskServlet extends ServletUtilBase {
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    // authenticate the user as an admin
    if (!AdminAccountsConfig.authenticateAdmin(this, req, resp)) {
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
      String name = req.getParameter("name");
      String url = req.getParameter("url");
      String extras = req.getParameter("extras");
      if (type == null) {
        debug("No task type");
        resp.getWriter().write("Error: No task type");
        return; //not a valid task type
      }
      TaskType ttype = TaskType.valueOf(type);
      if (ttype == null) {
        debug("Unsupported task type");
        resp.getWriter().write("Error: Unsupported task type");
        return; //not a valid task type
      }
      Task task = new Task(ttype);
      task.setName(name);
      task.setUrl(url);
      task.setExtras(extras);

      //special case for ODK Aggregate integration
      String aggregateSel = req.getParameter("aggregateFormSelect");
      
      if (aggregateSel != null && !aggregateSel.equals("") && !aggregateSel.equals("other")){
        String[] nameAndUrl = aggregateSel.split("\"");
        assert(nameAndUrl.length == 2);
        task.setName(nameAndUrl[0]);
        task.setUrl(nameAndUrl[1]);
      } 
      debug("Task: " + task);
      
      device.addTask(task);
      resp.getWriter().write("Task successfully added");
    } finally {
      if (dba != null){
        dba.close();
      }
    }
  }
  
}