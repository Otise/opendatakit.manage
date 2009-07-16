package org.odk.manage.server;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminAccountsConfig {
  
  private static final Logger log = Logger.getLogger(AdminAccountsConfig.class.getName());
  
  private static final String[] adminAccounts = 
    new String[]{"adam.lerer@gmail.com",
                 "brunette.appengine@gmail.com",
                 "borriellog@gmail.com",
                 "yanokwa@gmail.com",
                 "carlhartung@gmail.com",
                 "wbrunette@gmail.com",
                 "davinci@gmail.com", //juliec
                 // add authorized admin emails here
  };
  
  /**
   * For testing, we are adding a token that allows you to bypass admin checks.
   * This should be removed in production.
   */
  private static final String adminToken = "ureport";
  
  private static Set<String> adminAccountsHash;
  static {
    adminAccountsHash = new HashSet<String>();
    Collections.addAll(adminAccountsHash, adminAccounts);
  }
  
  public static boolean isAdmin(String email) {
    return adminAccountsHash.contains(email);
  }
  
  /**
   * Authenticates an admin. If the current user is an admin, as specified in 
   * the adminAccounts array, returns true. If the user is not an admin, asks 
   * them to sign out. If user is not signed in, redirects to sign in page. 
   * Servlets using this method should call it before any other action, and 
   * immediately return if this method returns false, e.g.:
   * 
   * if (!AdminAccountsConfig.authenticateAdmin(req, resp)) {
   *  return;
   * }
   * 
   * @param req
   * @param resp
   * @return
   */
  public static boolean authenticateAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    // for testing purposes
    if (req.getParameter("adminToken") != null && req.getParameter("adminToken").equals(adminToken)){
      return true;
    }
    String thisURL = req.getRequestURI();
    if (req.getUserPrincipal() == null) {
      resp.sendRedirect(userService.createLoginURL(thisURL));
      log.log(Level.WARNING, "User not logged in. Redirecting to login page...");
      return false;
    } else if (!isAdmin(req.getUserPrincipal().getName())) {
        resp.getWriter().println("<p>Hello, " +
                                     req.getUserPrincipal().getName() +
                                     ". You are not an admin.  You can <a href=\"" +
                                     userService.createLogoutURL(thisURL) +
                                     "\">sign out</a>.</p>");
        log.log(Level.WARNING, "User not an admin. Cannot access page.");
        return false;
  
    } else {
      return true;
    }
  }
  
  public static boolean authenticateAdmin(HttpServletRequest req){
    UserService userService = UserServiceFactory.getUserService();
    
    String thisURL = req.getRequestURI();
    return (req.getUserPrincipal() != null && 
        isAdmin(req.getUserPrincipal().getName()));
  }
}
