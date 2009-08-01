package org.odk.manage.server.sms;

import org.odk.manage.server.model.Device;

/**
 * An empty implementation of the SMS adapter (for non-SMS-supporting servers).
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class EmptySmsService implements SmsService{

    public void registerSmsListener(SmsListener listener){
    }
    
    public boolean unregisterSmsListener(SmsListener listener){
      return true;
    }

    public boolean sendSms(Device device, String content){
      return false;
    }
    
    public boolean canSendTo(Device device){
      return false;
    }

}
