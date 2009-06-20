package org.google.android.odk.manage.server;

import org.google.android.odk.manage.server.model.Device;
import org.google.android.odk.manage.server.model.PMF;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(AddTaskServlet.class.getName());
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // debug code
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Extent<Device> extent = pm.getExtent(Device.class, false);
    for (Device d : extent) {
      if (d.imei != null)
        resp.getWriter().write(d.imei);
      resp.getWriter().write(" (IMEI) \n");
    }
    extent.closeAll();
  }
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//    try{
//      Map <String,String>paramMap = new HashMap<String,String>();
//      Enumeration<String> paramNames = req.getParameterNames();
//      while(paramNames.hasMoreElements()){
//        String paramName = (String) paramNames.nextElement();
//        paramMap.put(paramName,req.getParameterValues(paramName)[0]);
//      }
    
      log.severe("IMEI: " + req.getParameter("imei"));
      registerDevice(req.getParameter("imei"), req.getParameter("num"));
      log.info("Device registered");
//    } catch (IllegalArgumentException e){
//      log.severe("Illegal argument exception - IMEI probably null (new stuff)");
//      resp.sendError(400);
//    }
    
  }

  
  public void registerDevice(String imei, String phoneNumber){

    Device device = new Device(imei, phoneNumber);
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      pm.makePersistent(device);
    } finally {
      pm.close();
    }
  }
  
}