package org.odk.manage.server.servlet;

import com.google.appengine.api.sms.Mobile;

import org.odk.manage.server.model.DbAdapter;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SmsReceiverServlet extends HttpServlet {
  
  private static final Logger log = Logger.getLogger(SmsReceiverServlet.class.getName());
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String sender = req.getParameter("sender");
    String content = req.getParameter("content").trim();
    
    if (sender == null || content == null){
      resp.sendError(400);
      return; // should not happen
    }
    debug("\nSender: '" + sender + "'\nContent: '" + content + "'");
    
    String action = content.split(" ")[0];
    String data = content.substring(action.length());
    
    if (action.equals("reg")) {
     // assuming for now that all SMS's are registration
        Map<String,String> paramMap = new HashMap<String,String>();
        for (String param : content.split("&")) {
          String[] keyval = param.split("=");
          if (keyval.length == 2){
            debug("Added to paramMap: <" + URLDecoder.decode(keyval[0]) + "," + 
                URLDecoder.decode(keyval[1]) + ">");
            paramMap.put(URLDecoder.decode(keyval[0]), 
                URLDecoder.decode(keyval[1]));
          }
        }
        DbAdapter dba = null;
        try {
          dba = new DbAdapter();
          dba.registerDevice(
              paramMap.get("imei"),
              // Self-reported phone # is not accurate in many regions
              // paramMap.get("phonenumber"),
              new Mobile(sender).getNumber(),
              paramMap.get("imsi"),
              paramMap.get("sim"),
              paramMap.get("userid"),
              sender
              );
          debug("Device registered: " + req.getParameter("imei"));
        } finally {
          if (dba != null)
            dba.close();
        }
        
        resp.setContentType("text/html"); 
        resp.getWriter().println("SMS registration for " + paramMap.get("num") + " received.");
    }
    
    else {
      resp.sendError(400);
    }
    
    

    
  }
  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }
}

