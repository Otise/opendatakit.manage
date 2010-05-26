package org.odk.manage.server.sms;

import org.odk.manage.server.model.Device;

/**
 * An adapter for an SMS gateway.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public interface SmsService {

  /**
   * Register an SmsListener that will be notified when SMS's are received.
   * @param listener
   */
  public void registerSmsListener(SmsListener listener);
  
  public boolean unregisterSmsListener(SmsListener listener);
  
  /**
   * Send an SMS message via the gateway
   * @param device The device to send to.
   * @param content The SMS content.
   * @return true if the message was successfully sent.
   */
  public boolean sendSms(Device device, String content); 
  
  /**
   * 
   * @param device
   * @return True if the SMS service can send to this device.
   */
  public boolean canSendTo(Device device);
  
}
