package org.odk.manage.server.sms;

public class SmsServiceFactory {
  
  private static final SmsService service = new AppengineSmsService();
  private static final RegistrationSmsListener regListener = new RegistrationSmsListener();
  static{
    service.registerSmsListener(regListener);
  }
  
  public static SmsService getService(){
    return service;
  }
}
