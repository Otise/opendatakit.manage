package org.odk.manage.server.sms;

import org.odk.manage.server.model.Device;

/**
 * An empty implementation of the SMS adapter (for non-SMS-supporting servers).
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class EmptySmsAdapter implements SmsAdapter{

    public void registerSmsListener(SmsListener listener){
    }
    
    public boolean unregisterSmsListener(SmsListener listener){
      return true;
    }
    
    /**
     * Send an SMS message via the gateway
     * @param device The device to send to.
     * @param content The SMS content.
     * @return true if the message was successfully sent.
     */
    public boolean sendSms(Device device, String content){
      return false;
    }

}
