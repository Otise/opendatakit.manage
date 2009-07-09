package org.odk.manage.server.servlet;

import org.odk.manage.server.XmlUtils;
import org.odk.manage.server.model.DbAdapter;
import org.odk.manage.server.model.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.io.IOException;
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
    
    //TODO(alerer): do we want to verify the right IMEI? Not really necessary.
    //If they are not all in the same IMEI, not same entity group, error...
    
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
      
      DbAdapter dba = null;
      try {
        dba = new DbAdapter();
        dba.updateTaskStatus(id, status);
      } finally {
        dba.close();
      }
    }
  }
  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }
}
