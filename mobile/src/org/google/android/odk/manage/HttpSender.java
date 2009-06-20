package org.google.android.odk.manage;

import android.util.Log;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class HttpSender {
  
  // from http://www.anddev.org/doing_http_post_with_android-t492.html
  public void doPost(String urlString, String content) throws IOException {
    Log.i("OdkManage", "Content: " + content);
    DefaultHttpClient httpclient = new DefaultHttpClient(); 
    HttpPost httppost = new HttpPost(urlString);
    //httppost.setParams(params);
    httppost.setEntity(new ByteArrayEntity(content.getBytes()));
    
    // Post, check and show the result (not really spectacular, but works):
    httpclient.execute(httppost);
    
  } 
  
}
//HttpParams params = new HttpParams();
//// open the connection
//URLConnection con = url.openConnection();
//con.setDoOutput(true);
//con.setDoInput(true);
//OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream());
//w.write(content);
//w.flush();
//
//// Get the response
//BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
//Log.i("OdkManage","Post response: " + r.readLine());
//w.close();
//r.close();