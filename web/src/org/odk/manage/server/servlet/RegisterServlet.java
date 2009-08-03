package org.odk.manage.server.servlet;

import org.odk.manage.server.model.DbAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet is accessed by the client to register itself with the server.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class RegisterServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(RegisterServlet.class.getName());
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    debug("Entered device registration: " + req.getParameter("imei"));
    DbAdapter dba = null;
    try {
      dba = new DbAdapter();
      dba.registerDevice(req.getParameter("imei"), 
          req.getParameter("phonenumber"),
          req.getParameter("imsi"),
          req.getParameter("sim"),
          req.getParameter("userid"),
          null);
      debug("Device registered: " + req.getParameter("imei"));
    } finally {
      if (dba != null)
        dba.close();
    }
  }
 
  
  private void debug(String msg){
    log.log(Level.WARNING,msg);
  }
  
}