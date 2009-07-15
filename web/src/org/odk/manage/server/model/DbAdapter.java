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
      String numberWithValidator){

    Device newDevice = new Device(imei);

    newDevice.setPhoneNumber(phoneNumber);
    newDevice.setImsi(imsi);
    newDevice.setSim(sim);
    newDevice.setUserId(userId);
    newDevice.setNumberWithValidator(numberWithValidator);
    newDevice.setLastContacted(new Date());
    
    // XXX(alerer): we want to update oldDevice if it exists, 
    //but app engine is not cooperating
    // so we're doing a workaround
    Device oldDevice = getDevice(imei);
    if (oldDevice != null) {
      debug("Found device with IMEI.");
      if (phoneNumber == null || phoneNumber.equals(""))
        newDevice.setPhoneNumber(oldDevice.getPhoneNumber());
      if (imsi == null || imsi.equals(""))
        newDevice.setImsi(oldDevice.getImsi());
      if (sim == null || sim.equals(""))
        newDevice.setSim(oldDevice.getSim());
      if (userId == null || userId.equals(""))
        newDevice.setUserId(oldDevice.getUserId());
      debug("Validator = " + numberWithValidator);
      if (numberWithValidator == null || numberWithValidator.equals(""))
        newDevice.setNumberWithValidator(oldDevice.getNumberWithValidator());
    } else {
      debug("Did not find device");
    }  
    pm.makePersistent(newDevice);
      
//    PersistenceManager pm = null;
//    try{
//      pm = PMF.get().getPersistenceManager();
//      Device device = null;
//      boolean newQuery = false;
//      Query q = pm.newQuery(Device.class);
//      q.setFilter("imei == imeiParam");
//      q.declareParameters("String imeiParam");
//      List<Device> results = (List<Device>) q.execute(imei);
//      if (results.size() > 0) {
//        debug("Found device with IMEI.");
//        device = results.get(0);
//      } else {
//        newQuery = true;
//        debug("Did not find device - creating new device");
//        device = new Device(imei);
//      }
    
    // we will update all non-null/non-empty fields. Null or empty fields are 
    // not updated, because e.g. numberWithValidator should keep its value 
    // even if registration occurs later by HTTP. We might want to clarify 
    // this in case the number changes - maybe do validator separately.
    // NOTE: this doesnt't work at the moment, because GAE won't allow updates
//    if (phoneNumber != null && !phoneNumber.equals(""))
//    {
//      device.phoneNumber = phoneNumber;
//      // if the old validator has been invalidated
//      if (device.phoneNumber != phoneNumber)
//        device.numberWithValidator = null;
//    }
//    if (imsi != null && !imsi.equals(""))
//      device.imsi = imsi;
//    if (sim != null && !sim.equals(""))
//      device.sim = sim;
//    if (userId != null && !userId.equals(""))
//        device.userId = userId;
//    debug("Validator = " + numberWithValidator);
//    if (numberWithValidator != null && !numberWithValidator.equals("")){
//      debug("Setting numberWithValidator");
//      device.numberWithValidator = numberWithValidator;
//    }
//    pm.makePersistent(device);

  }
  
  public List<Device> getDevices(){
    Query q = pm.newQuery(Device.class);
    return (List<Device>) q.execute();
  }
  
  /**
   * 
   * @param imei
   * @return The device, or null if no such device exists with that IMEI.
   */
  public Device getDevice(String imei){
    Key k = KeyFactory.createKey(Device.class.getSimpleName(), "imei" + imei);
    try {
      return pm.getObjectById(Device.class, k);
    } catch (JDOObjectNotFoundException e) {
      return null;
    }
  }
  
  
  public String getPreference(String name){
    Key k = KeyFactory.createKey(Preference.class.getSimpleName(), name);
    try {
      Preference p =  pm.getObjectById(Preference.class, k);
      return p.getValue();
    } catch (JDOObjectNotFoundException e) {
      return null;
    }
  }
  
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
  
  public void close(){
    pm.close();
    debug("Persistence manager closed");
  }
  
  private void debug(String msg){
    log.log(Level.WARNING,msg);
  }

  /**
   * Modifies the task in the datastore and returns the task. The status will 
   * not be updated in the datastore until {@code close()} is called.
   * @param id the Task.getUniqueId() for the task.
   * @param status The new task
   * @return The corresponding, modified task, or null if no task exists.
   */
  public Task updateTaskStatus(String id, TaskStatus status) {
    Key k = KeyFactory.stringToKey(id);
    Task t = pm.getObjectById(Task.class, k);
    t.setStatus(status);
    return t;
  }
}
