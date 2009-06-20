<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.google.android.odk.manage.server.model.*" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="javax.jdo.Query" %>
<%@ page import="java.util.List" %>

<%
  PersistenceManager pm = PMF.get().getPersistenceManager();
  Query q = pm.newQuery(Device.class);
  List<Device> devices = (List<Device>) q.execute();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
          "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
      <html>
        <head>
          <title>ODK Manage Server</title>
          <link href="main.css" type="text/css" rel="stylesheet"></link>
          
        </head>
        <body>
        <h1>ODK Manage Server</h1>
            <table class="devices">
              <tr><th>IMEI</th><th>Phone Number</th><th>Add/Update Form</th></tr>
              <% for (Device device : devices) { %>
                <tr class = "device">
                  <td class="imei"> <%= device.imei %> </td>
                  <td class="phoneNumber"> <%= device.phoneNumber %> </td>
                  <td>
                    <form action="addTask" method="post">
                      <input type="hidden" name="imei" value="<%= device.imei %>"/>
                      <input type="hidden" name="type" value="addForm"/>
                      Url: <input type="text" name="url"/>
                      <input type="submit">
                </tr>
              <% } %>
            </table>
        </body>
      </html>