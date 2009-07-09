package org.odk.manage.server;

public class Constants {

  public static final String NEW_TASKS_TRIGGER = "ODK-MANAGE-NT";
  public static final String NEW_TASKS_CONTENT = "There are pending ODK updates. " +
    "that require data connectivity. Please enter data range as soon as possible.";
  public static final String AGGREGATE_DOMAIN_KEY = "odkaggregatedomain";
  public static final String AGGREGATE_FORM_LIST_PATH = "formList";
  public static final String VALID_URL = "^((ht|f)tp(s?)\\:\\/\\/|~/|/)?([\\w]+:\\w+@)?([a-zA-Z]{1}([\\w\\-]+\\.)+([\\w]{2,5}))(:[\\d]{1,5})?/?(\\w+\\.[\\w]{3,4})?((\\?\\w+=\\w+)?(&\\w+=\\w+)*)?";
}
