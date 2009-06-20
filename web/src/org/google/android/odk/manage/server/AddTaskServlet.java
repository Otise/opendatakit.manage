package org.google.android.odk.manage.server;

import org.google.android.odk.manage.server.model.Device;
import org.google.android.odk.manage.server.model.PMF;
import org.google.android.odk.manage.server.model.Task;
import org.google.android.odk.manage.server.model.Task.TaskType;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AddTaskServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(AddTaskServlet.class.getName());
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    String imei = req.getParameter("imei");
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Device device = (Device) pm.getObjectById(imei);
    log.info("Phone number: " + device.phoneNumber);
    String type = req.getParameter("type");
    Task task = null;
    if ("downloadForm".equals(type)){
      task = new Task(TaskType.DOWNLOAD_FORM);
      task.setProperty("url", req.getParameter("url"));
    } else {
      return; //not a valid task type
    }
    try{ 
      pm.makePersistent(task);
      device.tasks.addTask(task);
      log.info("Task added");
    } finally {
      pm.close();
    }
  }
}