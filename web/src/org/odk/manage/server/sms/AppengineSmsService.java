package org.odk.manage.server.sms;

import com.google.appengine.api.sms.Mobile;

import org.odk.manage.server.model.Device;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**

 * 
 * This is an SMS service implementation for using Google SMS servers. This 
 * class requires {@link AppengineSmsReceiverServlet} to be registered at /_ah/messages
 * in order to receive SMS properly.
 * 
 * Note: this is NOT an appengine API - external users will need to use a 
 * service like Clickatell and builder an SmsService implementation for it.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class AppengineSmsService implements SmsService {

  private static final Logger log = Logger.getLogger(AppengineSmsService.class.getName());
  
  List<SmsListener> listeners = new LinkedList<SmsListener>();
  
  @Override
  public boolean sendSms(Device device, String content) {
    try{
      com.google.appengine.api.sms.SmsServiceFactory.getSmsService().sendToMobile(
          new Mobile(device.getSmsValidator()), content);
      return true;
    } catch(Exception e){
      debug("Exception sending SMS message: " + e.getStackTrace().toString());
      return false;
    }
  }

  @Override
  public void registerSmsListener(SmsListener listener) {
    listeners.add(listener);
  }
  
  @Override
  public boolean unregisterSmsListener(SmsListener listener) {
    return listeners.remove(listener);
  }
  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }

  @Override
  public boolean canSendTo(Device device) {
    return device.getSmsValidator() != null;
  }
  
  public void smsReceived(String sender, String content){
    for (SmsListener l: listeners) {
      l.onSmsReceived(sender.split(" ")[0], sender, content);
    }
  }
  
  /**
   * Servlet must be located at '/_ah/messages'
   * 
   * This servlet receives incoming SMS from the internal appengine SMS API. This 
   * servlet is used in conjunction with the {@link org.odk.manage.server.sms.AppengineSmsService} 
   * to provide the appengine SMS API implementation of {@link org.odk.manage.server.sms.SmsService}.
   * @author alerer@google.com (Adam Lerer)
   *
   */
  static class AppengineSmsReceiverServlet extends HttpServlet {
    
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
}
