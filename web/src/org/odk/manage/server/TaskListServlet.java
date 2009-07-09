package org.odk.manage.server;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import org.odk.manage.server.model.DbAdapter;
import org.odk.manage.server.model.Device;
import org.odk.manage.server.model.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TaskListServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(AddTaskServlet.class.getName());
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    resp.setContentType("text/xml");
    PrintWriter out = resp.getWriter();
    Document doc = XmlUtils.createXmlDoc(null,"tasklist");
    Element root = doc.getDocumentElement();
    String imei = req.getParameter("imei");
    if (imei == null){
      resp.sendError(400);
      debug("No IMEI parameter");
      return;
    }
    
    DbAdapter dba = null;
    try {
      dba = new DbAdapter();
      Key k = KeyFactory.createKey(Device.class.getSimpleName(), "imei" + imei);
      Device device = dba.getDevice(imei);
      List<Task> taskList = device.getTasks();
      root.setAttribute("imei", req.getParameter("imei"));
      debug("Tasks: " + device.getTasks());
      debug("Tasks: " + device.getTasks());
      for (Task task: taskList){
        Element e = doc.createElement("task");
        e.setAttribute("type", task.getType().name());
        e.setAttribute("id", task.getUniqueId());
        if (task.getName() != null)
          e.setAttribute("name", task.getName());
        if (task.getUrl() != null)
          e.setAttribute("url", task.getUrl());
        if (task.getExtras() != null)
          e.setAttribute("extras", task.getExtras());
        root.appendChild(e);
      }
      XmlUtils.serialiseXml(doc, out);
    } finally {
      if (dba != null)
        dba.close();
    }
  }
  
  

  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }
}

