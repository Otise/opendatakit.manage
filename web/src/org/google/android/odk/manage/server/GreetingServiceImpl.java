package org.google.android.odk.manage.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.google.android.odk.manage.client.GreetingService;

import java.util.List;

import javax.jdo.PersistenceManager;

/*
 * * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {

  public String greetServer(String input) {
//    String serverInfo = getServletContext().getServerInfo();
//    String userAgent = getThreadLocalRequest().getHeader("User-Agent");
//    return "Hello, " + input + "!<br><br>I am running " + serverInfo
//        + ".<br><br>It looks like you are using:<br>" + userAgent;
//    PersistenceManager pm = PMF.get().getPersistenceManager();
//    String query = "select from " + RequestCount.class.getName();
//    String res = "";
//    try {
//      List<RequestCount> count = (List<RequestCount>) pm.newQuery(query).execute();
//      RequestCount rc = null;
//      if (count.isEmpty()) {
//        rc = new RequestCount();
//        pm.makePersistent(rc);
//      } else {
//        rc = count.get(0);
//      }
//      res = rc.getCount().toString();
//      rc.inc();
//    } finally {
//      pm.close();
//    }
//    return res;
//  }
    return "";
  }
}
