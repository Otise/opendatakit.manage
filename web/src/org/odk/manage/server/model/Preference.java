package org.odk.manage.server.model;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Stores persistent application preferences. 
 * @author alerer@google.com (Adam Lerer)
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Preference {

  @PrimaryKey
  private String name;
  
  @Persistent
  private String value;
  
  public Preference (String name, String value){
    this.name = name;
    this.value = value;
  }
  
  public String getName(){
    return name;
  }
  
  public String getValue(){
    return value;
  }
  
  public void setValue(String value){
    this.value = value;
  }
  
}
