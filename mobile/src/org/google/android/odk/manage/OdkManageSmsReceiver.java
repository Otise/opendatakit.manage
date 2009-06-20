package org.google.android.odk.manage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

import org.google.android.odk.common.FileHandler;
import org.google.android.odk.common.NotificationHelper;
import org.google.android.odk.common.SharedConstants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class OdkManageSmsReceiver extends BroadcastReceiver {
  
  public final String DOWNLOAD_FORM_TRIGGER = "odk-collect-dl-form";
  public FileHandler mHandler;
  
  // easiest way to extract messages from phone
  private SmsMessage[] getMessagesFromIntent(Intent intent) {
    SmsMessage retMsgs[] = null;
    Bundle bdl = intent.getExtras();
    try {
      Object pdus[] = (Object[]) bdl.get("pdus");
      retMsgs = new SmsMessage[pdus.length];
      for (int n = 0; n < pdus.length; n++) {
        byte[] byteData = (byte[]) pdus[n];
        retMsgs[n] = SmsMessage.createFromPdu(byteData);
      }

    } catch (Exception e) {
      Log.e("GetMessages", "fail", e);
    }
    return retMsgs;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i("OdkManage","Received Intent");
    if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
      return;
    }
    mHandler = new FileHandler(context);
    SmsMessage msgs[] = getMessagesFromIntent(intent);

    String message = "";
    String number = "";

    for (SmsMessage msg: msgs) {
      message = msg.getDisplayMessageBody();
      number = msg.getOriginatingAddress();
      if (message != null && message.length() > 0 && number != null) {
        Log.i("OdkManage", "SMS Intercepted: " + message);
        if (isDownloadFormTrigger(number,message))
          downloadForm(message, context);
      }
    }
  }
  
  public boolean isDownloadFormTrigger(String number, String message){
    return message.startsWith(DOWNLOAD_FORM_TRIGGER + " ");
  }
  
  public void downloadForm(String message, Context context){
    Log.i("OdkManage", "Download form: " + message);
    String parsedMsg = message.replaceFirst(DOWNLOAD_FORM_TRIGGER + " ", "");
    try {
      URL url = new URL(parsedMsg);
      String filename = mHandler.getFormFromUrl(url, mHandler.getDirectory(SharedConstants.FORMS_PATH));
      //if form downloaded successfully
      new NotificationHelper(context).setNotification("Form downloaded: " + parsedMsg);
    } catch (MalformedURLException e) {
      Log.e("OdkManage", "Malformed download url: " + parsedMsg);
    } catch (IOException e) {
      Log.e("OdkManage", "Failed to download form from: " + parsedMsg);
    } 
  }
  
  public URL getUrlFromMessage(String msg) throws MalformedURLException{
    return new URL(msg);
  }
  
}