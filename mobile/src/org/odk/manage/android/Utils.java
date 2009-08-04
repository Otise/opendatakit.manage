package org.odk.manage.android;

/**
 * Static utilities for ODK Manage client.
 * 
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class Utils {
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
