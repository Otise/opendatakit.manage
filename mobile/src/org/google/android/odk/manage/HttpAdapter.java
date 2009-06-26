package org.google.android.odk.manage;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.google.android.odk.common.SharedConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides a layer of indirection between ODK Manage activities / 
 * receivers and HTTP handling.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class HttpAdapter {


  /**
   * Returns an input stream containing the body of the response for an HTTP 
   * get request. 
   * Synchronous.
   * @param url A string containing the URL to be fetched.
   * @return An {@link InputStream} containing the content of the response body.
   * @throws IOException
   */
  public InputStream getUrl(String url) throws IOException{
    URL u = new URL(url);
    URLConnection c = u.openConnection();
    c.setConnectTimeout(SharedConstants.CONNECTION_TIMEOUT);
    c.setReadTimeout(SharedConstants.CONNECTION_TIMEOUT);
    return c.getInputStream();
  }
  
  public void doPost(String url, Map<String,String> params){
    
    List<NameValuePair> mFieldList = new ArrayList<NameValuePair>();

    for (String paramKey : params.keySet())
      mFieldList.add(new BasicNameValuePair(paramKey, params.get(paramKey)));

    DefaultHttpClient httpclient = new DefaultHttpClient();
    HttpPost mypost = new HttpPost(url);

    try {
      // Add the fields and data to the POST
      mypost.setEntity(new UrlEncodedFormEntity(mFieldList, HTTP.UTF_8));
    } catch (UnsupportedEncodingException e) {
      Log.e("OdkManage", "Unsupported Encoding");
      e.printStackTrace();
    }
    HttpResponse response = null;
    try {
      response = httpclient.execute(mypost);      
    } catch (ClientProtocolException e) {
      Log.e("OdkManage", "Protocol Exception Error");
      e.printStackTrace();
    } catch (IOException e) {
      Log.e("OdkManage", "IO Exception Error");
      e.printStackTrace();
      return;//break;
    }
    if (response != null && response.getStatusLine().getStatusCode() == 200) {
      Log.d("httpPost", "response: " + response.getStatusLine());
      return;
    } else {
      Log.e("OdkManage", "failure: " + response.getStatusLine());
    }
    //myViewUpdateHandler.sendEmptyMessage(0);
  }

}




