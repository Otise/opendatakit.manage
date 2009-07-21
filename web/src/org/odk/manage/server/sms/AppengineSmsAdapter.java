package org.odk.manage.server.sms;

import com.google.appengine.api.sms.Mobile;
import com.google.appengine.api.sms.SmsServiceFactory;

import org.odk.manage.server.Constants;
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
 * Servlet located at '/_ah/messages'
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class AppengineSmsAdapter extends HttpServlet implements SmsAdapter {

  List<SmsListener> listeners = new LinkedList<SmsListener>();
  private static final Logger log = Logger.getLogger(AppengineSmsAdapter.class.getName());
  
  public boolean sendSms(Device device, String content) {
    try{
      SmsServiceFactory.getSmsService().sendToMobile(
          new Mobile(device.getNumberWithValidator()), content);
      return true;
    } catch(Exception e){
      debug("Exception sending SMS message: " + e.getStackTrace());
      return false;
    }
  }
  
  public boolean sendNewTaskNotification(Device device, String message) {
    String content = Constants.NEW_TASKS_TRIGGER + ": " + message;
    return sendSms(device, content);  
  }

  @Override
  public void registerSmsListener(SmsListener listener) {
    listeners.add(listener);
  }
  
  @Override
  public boolean unregisterSmsListener(SmsListener listener) {
    return listeners.remove(listener);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String sender = req.getParameter("sender");
    String content = req.getParameter("content").trim();
    
    if (sender == null || content == null){
      resp.sendError(400);
      return; // should not happen
    }
    debug("\nSender: '" + sender + "'\nContent: '" + content + "'");
    
    for (SmsListener l: listeners) {
      l.onSmsReceived(sender, content);
    }
  }
  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }
}
