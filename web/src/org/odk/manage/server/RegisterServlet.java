package org.odk.manage.server;

import org.odk.manage.server.model.DbAdapter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handles
 * @author alerer@google.com (Your Name Here)
 *
 */
public class RegisterServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(RegisterServlet.class.getName());
  
//  @Override
//  /**
//   * Debugging method.
//   */
//  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//    // debug code
//    dba.registerDevice(
//        "imei", 
//        "phonenumber", 
//        "imsi", 
//        "sim",
//        "userId",
//        null);
//    PersistenceManager pm = PMF.get().getPersistenceManager();
//    Extent<Device> extent = pm.getExtent(Device.class, false);
//    for (Device d : extent) {
//      if (d.imei != null)
//        resp.getWriter().write(d.imei);
//      resp.getWriter().write(" (IMEI) \n");
//    }
//    extent.closeAll();
//    pm.close();
//  }
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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