package org.odk.manage.server.servlet;

import org.odk.manage.server.XmlUtils;
import org.odk.manage.server.model.DbAdapter;
import org.odk.manage.server.model.Device;
import org.odk.manage.server.model.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handles status update posts from mobile devices.
 * 
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class DeviceUpdateServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(DeviceUpdateServlet.class.getName());
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    Document doc = XmlUtils.getXmlDocument(req.getInputStream());
    if (doc == null){
      resp.sendError(400);
      return;
    }
    DbAdapter dba = null;
    try {
      dba = new DbAdapter();
      // we make a best effort to update 'last contacted'
      // this should be done more cleanly
      try {
        String imei = ((Element) doc.getElementsByTagName("tasks").item(0)).getAttribute("imei");
        Device device = dba.getDevice(imei);
        device.setLastContacted(new Date());
      } catch (Exception e) {
        debug("Exception updating last-contacted: " + e.getStackTrace().toString());
      }
      
      NodeList taskNodes = doc.getElementsByTagName("task");
      
      for (int i = 0; i < taskNodes.getLength(); i++) {
        if (!(taskNodes.item(i) instanceof Element)) {
          continue;
        }
        Element taskEl = (Element) taskNodes.item(i);
  
        NamedNodeMap taskAttributes = taskEl.getAttributes();
        
        String id = XmlUtils.getAttribute(taskAttributes, "id");
        Task.TaskStatus status = Task.TaskStatus.valueOf(
            XmlUtils.getAttribute(taskAttributes, "status"));
        
  
          Task task = dba.getTask(id);
          if (task != null){
            task.setStatus(status);
          }
      }
    } finally {
      dba.close();
    }
  }
  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }
}
