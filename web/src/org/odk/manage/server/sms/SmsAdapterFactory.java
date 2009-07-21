package org.odk.manage.server.sms;

public class SmsAdapterFactory {
  
  private static final SmsAdapter adapter = new AppengineSmsAdapter();
  
  public static SmsAdapter getAdapter(){
    return adapter;
  }
}
