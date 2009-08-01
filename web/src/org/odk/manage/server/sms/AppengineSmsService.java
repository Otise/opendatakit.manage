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
 * 
 * This is an SMS service implementation for using Google SMS servers.
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
      SmsServiceFactory.getSmsService().sendToMobile(
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
}
