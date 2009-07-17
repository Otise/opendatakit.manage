package org.odk.manage.server;

import com.google.appengine.api.sms.Mobile;
import com.google.appengine.api.sms.SmsServiceFactory;

import org.odk.manage.server.model.Device;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SmsSender {

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
  
  private static final Logger log = Logger.getLogger(SmsSender.class.getName());
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }

}
