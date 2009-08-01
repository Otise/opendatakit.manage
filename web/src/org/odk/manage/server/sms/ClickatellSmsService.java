package org.odk.manage.server.sms;

import org.odk.manage.server.model.Device;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An SMS adapter for the clickatell gateway two-way SMS.
 * 
 * Not yet implemented.
 * 
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class ClickatellSmsService implements SmsService{

  List<SmsListener> listeners = new LinkedList<SmsListener>();
  private static final Logger log = Logger.getLogger(ClickatellSmsService.class.getName());
  
  public boolean sendSms(Device device, String content) {
    return false;
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
    // TODO Auto-generated method stub
    return false;
  }

}
