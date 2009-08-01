package org.odk.manage.server.sms;

/**
 * A listener for SMS messages from an SMS service.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public interface SmsListener {
  
  /**
   * 
   * @param sender The phone number of the sender.
   * @param smsValidator Any information that the SMS service needs to keep 
   * track of the phone number. This will only be used when the Device is passed 
   * back to the SmsService; it may be null.
   * @param content The content of the incoming message.
   */
  public void onSmsReceived(String sender, String smsValidator, String content);
  
}
