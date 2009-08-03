package org.odk.manage.server.servlet;

import org.odk.manage.server.AdminAccountsConfig;
import org.odk.manage.server.model.DbAdapter;
import org.odk.manage.server.model.Device;
import org.odk.manage.server.model.Task;
import org.odk.manage.server.model.Task.TaskStatus;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet provides a human-readable list of tasks for a particular device. 
 * The admin can delete tasks from this page.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class ViewTasksServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(ViewTasksServlet.class.getName());
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // authenticate the user as an admin
    if (!AdminAccountsConfig.authenticateAdmin(req, resp)) {
      return;
    }
    
    resp.setContentType("text/html");
    Writer out = resp.getWriter();

    String imei = req.getParameter("imei");
    if (imei == null){
      resp.sendError(400);
      debug("No IMEI parameter");
      return;
    }
    
    DbAdapter dba = null;
    try {
      dba = new DbAdapter();
      Device device = dba.getDevice(imei);
            
      //TODO(alerer): I don't know how the datastore handles owned objects. If 
      //it fetches them on-the-fly, we're in trouble (a datastore query for each task).
      List<Task> taskList = device.getTasks(null);
      
      List<Task> pendingTasks = new ArrayList<Task>();
      List<Task> successTasks = new ArrayList<Task>();
      List<Task> failedTasks = new ArrayList<Task>();
      
      for (Task t: taskList){
        switch(t.getStatus()){
          case PENDING:
            pendingTasks.add(t);break;
          case SUCCESS:
            successTasks.add(t);break;
          case FAILED:
            failedTasks.add(t);break;
        }
      }
      
      out.write("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN'" +
      "'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>\n");
      out.write("<html><head><title>ODK Manage Server - Tasklist (" + imei + ")</title>");
      out.write("<link href='main.css' type='text/css' rel='stylesheet'></link>");
      out.write("<script src='admin.js' type='text/javascript'></script>");
      out.write("</head>");
      out.write("<body><h1>ODK Manage Server - Tasklist (" + imei + ")</h1>"); 
      
      out.write("<h2>Pending Tasks</h2>");
      outputTasksTable(out, pendingTasks, imei, TaskStatus.PENDING);
      out.write("<h2>Successful Tasks</h2>");
      outputTasksTable(out, successTasks, imei, TaskStatus.SUCCESS);
      out.write("<h2>Failed Tasks</h2>");
      outputTasksTable(out, failedTasks, imei, TaskStatus.FAILED);
      out.write("<a class='goback' href='admin.html'>Go back</a>");
    } finally {
      if (dba != null)
        dba.close();
    }
  }
  
  private void outputTasksTable(Writer out, List<Task> tasks, String imei, TaskStatus status) throws IOException {
    if (tasks == null || tasks.size() == 0)
      return;
    String st = status.name();
    out.write("<form action='deleteTasks' method='post'>");
    out.write("<input type='hidden' name='imei' value='" + imei + "'/>");
    out.write("<table class='main'><tr>");
    out.write("<th><input type='checkbox' id='selectAllCheckbox-" + st + "' onclick='updateSelectAllTasks(this,\"" + st + "\")'</th>");
    out.write("<th>Type</th><th>Name</th><th>URL</th><th>Extras</th></tr>");
    for (Task t: tasks) {
      out.write("<tr status='" + st + "'>" +
                "<td><input type='checkbox' name='taskId' status='" + st + "' value='" + t.getUniqueId() + "' onclick='updateSelectedTask(\"" + st + "\")'/></td>" +
                "<td>" + ServletUtils.removeNull(t.getType().name()) + "</td>" +
                "<td>" + ServletUtils.removeNull(t.getName()) + "</td>" +
                "<td>" + ServletUtils.removeNull(t.getUrl()) + "</td>" +
                "<td>" + ServletUtils.removeNull(t.getExtras()) + "</td>" +
                "</tr>");
    }
    out.write("</table>");
    out.write("<input type='submit' value='Delete'></form>");
  }
  
  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }
}
