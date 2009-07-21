package org.odk.manage.server.sms;

/**
 * A listener for SMS messages.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public interface SmsListener {
  
  public void onSmsReceived(String sender, String content);
  
}
