package org.odk.manage.server.servlet;

/**
 * Static utilities class for servlets.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class ServletUtils {
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
