package org.odk.manage.server.model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import org.odk.manage.server.model.Task.TaskStatus;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * An adapter for the app engine datastore. 
 * WARNING: To prevent memory leaks, you must always close the adapter after use, 
 * e.g.
 * <code>
 * DbAdapter dba = null;
 * try {
 *   dba = new DbAdapter();
 *   // do stuff here ...
 * } finally {
 *   if (dba != null) {
 *     dba.close();
 *   }
 * }
 * @author alerer@google.com (Your Name Here)
 *
 */
public class DbAdapter {
  
  private static final Logger log = Logger.getLogger(DbAdapter.class.getName());
  PersistenceManager pm;
  
  //TODO(alerer): PersistenceManagers can be handled somewhat better here.
  
  public DbAdapter(){
    pm = PMF.get().getPersistenceManager();
  }
  
  /**
   * Registers a device with the given properties. All properties may be null 
   * except for imei.
   * @param imei
   * @param phoneNumber
   * @param imsi
   * @param sim
   * @param userId
   * @param numberWithValidator This is a special string from App Engine that 
   * allows it to make outgoing SMSs to a number. You can't send SMS to a 
   * number you haven't received from (and recorded the validator).
   */
  public void registerDevice(String imei, 
      String phoneNumber, 
      String imsi, 
      String sim,
      String userId,
      String smsValidator){

    Device device = getDevice(imei);
    if (device == null) {
      device = new Device(imei);
    }
    if (phoneNumber != null)
      device.setPhoneNumber(phoneNumber);
    if (imsi != null)
      device.setImsi(imsi);
    if (sim != null)
    device.setSim(sim);
      if (userId != null)
    device.setUserId(userId);
      if (phoneNumber != null)
    device.setSmsValidator(smsValidator);
    device.setLastContacted(new Date());

    pm.makePersistent(device);
      
  }
  
  /**
   * Get 
   * @return A list of all {@link Device}s, ordered by userId.
   */
  public List<Device> getDevices(){
    Query q = pm.newQuery(Device.class);
    q.setOrdering("userId"); //order by userId
    return (List<Device>) q.execute();
  }
  
  /**
   * 
   * @param imei
   * @return The device, or null if no such device exists with that IMEI.
   */
  public Device getDevice(String imei){
    Key k = KeyFactory.createKey(Device.class.getSimpleName(), "i" + imei);
    try {
      return pm.getObjectById(Device.class, k);
    } catch (JDOObjectNotFoundException e) {
      return null;
    }
  }
  
  /**
   * Gets the value for a {@link Preference}, given the preference name.
   * @param name The preference name.
   * @return The preference value.
   */
  public String getPreference(String name){
    Key k = KeyFactory.createKey(Preference.class.getSimpleName(), name);
    try {
      Preference p =  pm.getObjectById(Preference.class, k);
      return p.getValue();
    } catch (JDOObjectNotFoundException e) {
      return null;
    }
  }
  
  /**
   * Sets a {@link Preference}.
   * @param name The preference name.
   * @param value The preference value.
   */
  public void setPreference(String name, String value){
    Key k = KeyFactory.createKey(Preference.class.getSimpleName(), name);
    try {
      Preference p =  pm.getObjectById(Preference.class, k);
      p.setValue(value);
    } catch (JDOObjectNotFoundException e) {
      Preference p = new Preference(name, value);
      pm.makePersistent(p);
    }
  }
  
  /**
   * Gets a {@link Task} from the datastore, given it's id.
   * @param id The Task id.
   * @return The Task.
   */
  public Task getTask(String id){
    Key k = KeyFactory.stringToKey(id);
    try {
      return pm.getObjectById(Task.class, k);
    } catch (JDOObjectNotFoundException e){
      return null;
    }
  }
  
  /**
   * Deletes a task from the datastore.
   * @param t The task.
   */
  public void deleteTask(Task t){
    Device device = t.getDevice();
    if (device != null) {
      device.removeTask(t);
    }
    pm.deletePersistent(t);
  }
  
  /**
   * Close the underlying database. It is very important that this method is 
   * called when you are finished using the DbAdapter.
   */
  public void close(){
    pm.close();
  }
  
  private void debug(String msg){
    log.log(Level.WARNING,msg);
  }
  

}
