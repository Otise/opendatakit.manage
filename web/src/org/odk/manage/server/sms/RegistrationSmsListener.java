package org.odk.manage.server.sms;

import com.google.appengine.api.sms.Mobile;

import org.odk.manage.server.model.DbAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An incoming SMS message listener that handles device registration.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class RegistrationSmsListener implements SmsListener {

  private static final Logger log = Logger.getLogger(RegistrationSmsListener.class.getName());
  
  @Override
  public void onSmsReceived(String sender, String content) {
    String action = content.split(" ")[0];
    String data = content.substring(action.length() + 1);
    
    Map<String, String> paramMap = getParamMapFromSms(data);
    
    if (action.equals("reg")) {
      DbAdapter dba = null;
      try {
        dba = new DbAdapter();
        dba.registerDevice(
            paramMap.get("imei"),
            // Self-reported phone # is not accurate in many regions
            // paramMap.get("phonenumber"),
            new Mobile(sender).getNumber(),
            paramMap.get("imsi"),
            paramMap.get("sim"),
            paramMap.get("userid"),
            sender
            );
      } finally {
        if (dba != null)
          dba.close();
      }
      debug("SMS registration for " + paramMap.get("num") + " received.");
    } else {
      debug("SMS did not have 'reg' type");
    }
  }

  private Map<String, String> getParamMapFromSms(String data) {
    Map<String,String> paramMap = new HashMap<String,String>();
    for (String param : data.split("&")) {
      String[] keyval = param.split("=");
      if (keyval.length == 2){
        try {
        debug("Added to paramMap: <" + URLDecoder.decode(keyval[0],"UTF-8") + "," + 
            URLDecoder.decode(keyval[1], "UTF-8") + ">");
        paramMap.put(URLDecoder.decode(keyval[0], "UTF-8"), 
            URLDecoder.decode(keyval[1], "UTF-8"));
        } catch (UnsupportedEncodingException e){}
      }
    }
    return paramMap;
  }
  
  private void debug(String msg){
    log.log(Level.WARNING, msg);
  }
}
