package org.odk.manage.server.sms;

/**
 * A factory for accessing the SMS service.
 * 
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class SmsServiceFactory {
  
  /**
   * The SmsService used by this application. To use a different SmsService, just 
   * implement the SmsService interface and change the value of this variable.
   */
  private static final SmsService service = new AppengineSmsService();
  
  /**
   * Do not change.
   */
  private static final RegistrationSmsListener regListener = new RegistrationSmsListener();
  static{
    service.registerSmsListener(regListener);
  }
  
  /**
   * @return The {@link SmsService} for this application.
   */
  public static SmsService getService(){
    return service;
  }
}
