package org.google.android.odk.manage.server;

import org.google.android.odk.manage.server.model.Device;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SmsReceiver extends HttpServlet {
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String sender = req.getParameter("sender");
    String content = req.getParameter("content");
    if (sender == null || content == null){
      resp.sendError(400);
      return; // should not happen
    }
    // assuming for now that all SMS's are registration
    Map<String,String> paramMap = new HashMap<String,String>();
    for (String param : content.split("&")) {
      String[] keyval = param.split("=");
      if (keyval.length == 2)
        paramMap.put(URLDecoder.decode(keyval[0]), URLDecoder.decode(keyval[1]));
    }
    try{
      new RegisterServlet().registerDevice(paramMap);
    } catch (IllegalArgumentException e){
      resp.sendError(400);
    }
    resp.setContentType("text/html"); 
    resp.getWriter().println("SMS registration for " + paramMap.get("num") + " received.");
  }
}

