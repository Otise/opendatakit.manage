package org.odk.manage.server.sms;

import org.odk.manage.server.model.Device;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is an example SmsService that shows how to create one yourself.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class ExampleSmsService implements SmsService {
  
  List<SmsListener> listeners = new LinkedList<SmsListener>();
  
  @Override
  public boolean sendSms(Device device, String content) {
    if (canSendTo(device)){
//    URL url = new URL("http://www.example.com/sendSms?number=" + device.getPhoneNumber() + "&content=" + content);
//    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
//    ...
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void registerSmsListener(SmsListener listener) {
    listeners.add(listener);
  }
  
  @Override
  public boolean unregisterSmsListener(SmsListener listener) {
    return listeners.remove(listener);
  }

  @Override
  public boolean canSendTo(Device device) {
    return true; //assuming you can send to all devices
  }
  
  public void smsReceived(String sender, String content){
    for (SmsListener l: listeners) {
      l.onSmsReceived(sender, null, content);
    }
  }
  
  /**
   * In this example, our application receives SMS by getting a POST request at a particular URL. 
   * You must register this servlet at that URL in web.xml.
   *  
   */
  static class ExampleSmsReceiverServlet extends HttpServlet {

    private final SmsService smsService = SmsServiceFactory.getService();
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      String sender = req.getParameter("sender");
      String content = req.getParameter("content");
      
      if (sender == null || content == null){
        resp.sendError(400);
        return; // should not happen
      }
      
      // Make sure that we're actually using the ExampleSmsService
      if (smsService instanceof ExampleSmsService) {
        ((ExampleSmsService) smsService).smsReceived(sender, content);
      } else {
        // debug("Received example service SMS, but the SmsService is not an ExampleSmsService!");
      }
    }
  }
}
