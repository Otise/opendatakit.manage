package org.odk.manage.server;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class AdminAccountsConfig {
  private static final String[] adminAccounts = 
    new String[]{"adam.lerer@gmail.com",
                 "brunette.appengine@gmail.com",
                 "yanokwa@google.com",
                 "carlhartung@google.com",
                 // add authorized admin emails here
  };
  
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
  
    String thisURL = req.getRequestURI();
    if (req.getUserPrincipal() == null) {
      resp.sendRedirect(userService.createLoginURL(thisURL));
      return false;
    } else if (!isAdmin(req.getUserPrincipal().getName())) {
        resp.getWriter().println("<p>Hello, " +
                                     req.getUserPrincipal().getName() +
                                     ". You are not an admin.  You can <a href=\"" +
                                     userService.createLogoutURL(thisURL) +
                                     "\">sign out</a>.</p>");
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
