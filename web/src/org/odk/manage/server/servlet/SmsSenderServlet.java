package org.odk.manage.server.servlet;

import org.odk.manage.server.AdminAccountsConfig;
import org.odk.manage.server.Constants;
import org.odk.manage.server.SmsSender;
import org.odk.manage.server.model.DbAdapter;
import org.odk.manage.server.model.Device;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @deprecated
 * @author alerer@google.com (Your Name Here)
 *
 */
public class SmsSenderServlet extends HttpServlet {
 
  private static final Logger log = Logger.getLogger(SmsSenderServlet.class.getName());
  
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
      if (device == null) {
        debug("No device with this IMEI.");
        resp.getWriter().write("No device with this IMEI.");
        return;
      }
      if (device.getNumberWithValidator() == null){
        debug("No validator.");
        resp.getWriter().write("Error: Device is not validated. Please send a registration SMS " +
            "to the server to obtain a validator.");
        return;
      }
      debug("Sending new tasks notification SMS");
      new SmsSender().sendNewTaskNotification(device, Constants.NEW_TASKS_CONTENT);
      
      resp.getWriter().write("Sent SMS message");
    } finally {
      if (dba != null) {
        dba.close();
      }
    }
  }
  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }
  
}