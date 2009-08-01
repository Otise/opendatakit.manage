package org.odk.manage.server.servlet;


import org.odk.manage.server.sms.AppengineSmsService;
import org.odk.manage.server.sms.SmsListener;
import org.odk.manage.server.sms.SmsService;
import org.odk.manage.server.sms.SmsServiceFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AppengineSmsReceiverServlet extends HttpServlet {
  
  private static final Logger log = Logger.getLogger(AppengineSmsReceiverServlet.class.getName());
  
  private final SmsService smsService = SmsServiceFactory.getService();
  
  /**
   * Note: Instead of putting just the validator in SmsValidator, we're going 
   * to put the number+validator in there. This is fine, since we're 
   * the only one who uses it. This simplifies processing a lot.
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String sender = req.getParameter("sender");
    String content = req.getParameter("content").trim();
    
    if (sender == null || content == null){
      resp.sendError(400);
      return; // should not happen
    }
    
    if (smsService instanceof AppengineSmsService) {
      ((AppengineSmsService) smsService).smsReceived(sender, content);
    } else {
      debug("Received appengine SMS, but the SmsService is not appengineSmsService!");
    }
  }
  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }
}
