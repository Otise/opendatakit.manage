package org.odk.manage.android.comm;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.odk.manage.android.Constants;

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
 * 
 * TODO(alerer): This class is not aware of the current network state, so how 
 * can we encapsulate the logic that says 'dont use GPRS if GPRS is disabled'?
 * Right now we assume that callers do this check, but that's not safe at all.
 * 
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
  public URLConnection getUrlConnection(String url) throws IOException{
    URL u = new URL(url);
    Log.d(Constants.TAG, "Opening connection to " + url);
    URLConnection c = u.openConnection();
    c.setConnectTimeout(Constants.CONNECTION_TIMEOUT_MS);
    c.setReadTimeout(Constants.CONNECTION_TIMEOUT_MS);
    
    return c;
  }
  
  /**
   * Performs an HTTP post.
   * @param post An {@link HttpPost} object for the post.
   * @return true if successful.
   */
  private boolean doPost(HttpPost post) {
    // TODO(alerer): what's the equivalent here of the following for URLConnection?
    //    c.setConnectTimeout(Constants.CONNECTION_TIMEOUT_MS);
    //    c.setReadTimeout(Constants.CONNECTION_TIMEOUT_MS);
    
    DefaultHttpClient httpclient = new DefaultHttpClient();
    HttpResponse response = null;
    try {
      response = httpclient.execute(post);
    } catch (ClientProtocolException e) {
      Log.e("OdkManage", "Protocol Exception Error", e);
      e.printStackTrace();
      return false;//break;
    } catch (IOException e) {
      Log.e("OdkManage", "IO Exception Error", e);
      e.printStackTrace();
      return false;//break;
    }
    if (response != null && response.getStatusLine().getStatusCode() == 200) {
      Log.d("httpPost", "response: " + response.getStatusLine());
      return true;
    } else {
      Log.e("OdkManage", "failure: " + response.getStatusLine());
      return false;
    }
    //myViewUpdateHandler.sendEmptyMessage(0);
  }
  
  /**
   * Attempt to execute an HTTP POST request. 
   * @param url The URL to POST to.
   * @param params A set of parameters for the POST body.
   * @return true if successful.
   */
  public boolean doPost(String url, Map<String,String> params){
    
    List<NameValuePair> mFieldList = new ArrayList<NameValuePair>();

    for (String paramKey : params.keySet())
      mFieldList.add(new BasicNameValuePair(paramKey, params.get(paramKey)));

    // DefaultHttpClient httpclient = new DefaultHttpClient();
    HttpPost mypost = new HttpPost(url);

    try {
      // Add the fields and data to the POST
      mypost.setEntity(new UrlEncodedFormEntity(mFieldList, HTTP.UTF_8));
    } catch (UnsupportedEncodingException e) {
      Log.e("OdkManage", "Unsupported Encoding");
      e.printStackTrace();
    }
    
    return doPost(mypost);
  }
  
  /**
   *Attempt to execute an HTTP POST request. 
   * @param url The URL to POST to.
   * @param body Data for the request body.
   * @return true if successful.
   */
  public boolean doPost(String url, String body){
    
    HttpPost mypost = new HttpPost(url);

    try {
      mypost.setEntity(new StringEntity(body, HTTP.UTF_8));
    } catch (UnsupportedEncodingException e) {
      Log.e("OdkManage", "Unsupported Encoding");
      e.printStackTrace();
      return false;
    }
    
    return doPost(mypost);
  }
  


}




