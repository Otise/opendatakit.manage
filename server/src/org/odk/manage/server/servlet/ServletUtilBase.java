package org.odk.manage.server.servlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;

public class ServletUtilBase extends HttpServlet {
  private static final Logger log = Logger.getLogger("ServletLog");
  
  protected void debug(String msg){
    log.log(Level.WARNING, msg);
  }
  
  protected void logError(String msg, Exception e){
    log.log(Level.SEVERE, msg, e);
  }
  
  public static String removeNull(String s){
    if (s == null)
      return "";
    return s;
  }
  
  public static String getDurationString(long ms){
    long mins = ms / 60000;
    long hrs = mins / 60;
    long days = hrs / 24;
    if (days != 0)
      return days + " days";
    if (hrs != 0)
      return hrs + " hours";
    return mins + " minutes";
  }
}
