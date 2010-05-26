package org.odk.manage.server;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * This class contains static methods for handling user authentication.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class AdminAccountsConfig {
  
  private static final Logger log = Logger.getLogger(AdminAccountsConfig.class.getName());
  private static final String ALLOWED_USERS_CONFIG_PARAMETER_NAME = "allowedUsers";
  
  private static boolean isInitialized = false;
  private static Set<String> adminAccountsHash = new HashSet<String>(); 
    
  /**
   * 
   * @param email
   * @return true if email corresponds to an admin.
   */
  public static boolean isAdmin(String email) {
    return adminAccountsHash.contains(email);
  }
  
	private static synchronized void initAccountsHash(HttpServlet theServlet) {
		if (isInitialized)
			return;

		// This is a roughly-correct match string for valid e-mail addresses.
		// It assumes no spaces and no non-ASCII characters in the user and domain name.
		String localPartCharacterSet = "[a-zA-Z0-9!#$%&'*+-/=?^_`{|}~]";
		String emailAddress = localPartCharacterSet + "(\\." + localPartCharacterSet + "|" + localPartCharacterSet + ")*@[a-zA-Z0-9_.\\-]+";
		Set<String> accounts = new HashSet<String>();
		try {
			
			// TODO: have different users be able to access different web interfaces
			//
			// Do global access merged with per-servlet access rights via:
			//
			// for <servlet><param-name>... entries theServlet.getServletConfig().getInitParameter(name);
			// for <system-properties><property>... entries System.getProperty(name);
			String configParameter = System.getProperty(ALLOWED_USERS_CONFIG_PARAMETER_NAME);
			
			// allow commas, semicolons and spaces to split e-mail addresses
			String[] splits = configParameter.split("[,; \t]");
			 
			for ( String elem : splits ) {
				if ( elem.length() == 0 ) {
					// degenerate case of two adjacent delimiters -- ignore
				} else if ( elem.length() < 5 || !elem.matches(emailAddress) ) {
					throw new IllegalArgumentException("Invalid email address " + elem + " found in " + ALLOWED_USERS_CONFIG_PARAMETER_NAME 
							+ " This list must be comma-, semicolon- or space- delimited and e-mail addresses must be of the form " + emailAddress);
				} else {
					log.log(Level.INFO,"Allowing " + elem);
					accounts.add(elem);
				}
			}
		} catch (Exception e) {
			// if we have any errors, clear the set of accounts
			log.log(Level.SEVERE, "Unable to retrieve the set of allowed users", e);
			accounts.clear();
		} finally {
			adminAccountsHash = accounts;
			isInitialized = true;
		}
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
   * @param req The servlet HttpRequest.
   * @param resp The servlet HttpResponse.
   * @return true if the user is authenticated.
   */
  public static boolean authenticateAdmin(HttpServlet theServlet, HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
//    // for testing purposes
//    if (req.getParameter("adminToken") != null && req.getParameter("adminToken").equals(adminToken)){
//      return true;
//    }
    initAccountsHash(theServlet);
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
  
//  public static boolean authenticateAdmin(HttpServletRequest req){
//    UserService userService = UserServiceFactory.getUserService();
//    
//    String thisURL = req.getRequestURI();
//    return (req.getUserPrincipal() != null && 
//        isAdmin(req.getUserPrincipal().getName()));
//  }
}
