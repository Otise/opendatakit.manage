package org.odk.manage.server;

/**
 * This class contains static constants for the ODK Manage server.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class Constants {

  /**
   * This string, when placed at the beginning of an SMS message to an ODK 
   * Manage client, should trigger the client to try to download new tasks 
   * from the server.
   */
  public static final String NEW_TASKS_TRIGGER = "ODK-MANAGE-NT";
  
  /**
   * Key for storing the preference for the ODK aggregate domain.
   */
  public static final String AGGREGATE_DOMAIN_KEY = "odkaggregatedomain";
  
  /**
   * Path from the ODK aggregate domain to the XML form list URL.
   */
  public static final String AGGREGATE_FORM_LIST_PATH = "formList";
  
  /**
   * Regular expression for a valid URL.
   */
  public static final String VALID_URL = "^((ht|f)tp(s?)\\:\\/\\/|~/|/)?([\\w]+:\\w+@)?([a-zA-Z]{1}([\\w\\-]+\\.)+([\\w]{2,5}))(:[\\d]{1,5})?/?(\\w+\\.[\\w]{3,4})?((\\?\\w+=\\w+)?(&\\w+=\\w+)*)?";
}
