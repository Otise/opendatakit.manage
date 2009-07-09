package org.odk.manage.server.servlet;

import org.apache.commons.lang.StringEscapeUtils;
import org.odk.manage.server.AdminAccountsConfig;
import org.odk.manage.server.Constants;
import org.odk.manage.server.XmlUtils;
import org.odk.manage.server.model.DbAdapter;
import org.odk.manage.server.model.Device;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ManageAdminServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(ManageAdminServlet.class.getName());

  
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // authenticate the user as an admin
    if (!AdminAccountsConfig.authenticateAdmin(req, resp)) {
      return;
    }
    
    DbAdapter dba = null;
    try {
      dba = new DbAdapter();
   
      List<Device> devices = dba.getDevices();
      
      // try to fetch a list of forms from the ODK Aggregate site
      List<OdkAggregateForm> aggregateForms = null;
      String aggregateDomain = dba.getPreference(Constants.AGGREGATE_DOMAIN_KEY);
      if (aggregateDomain != null) {
        aggregateForms = getFormsFromFormList(
            requestAggregateForms(aggregateDomain));
      }
  
      Writer out = resp.getWriter();
      out.write("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN'" +
              "'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>\n");
      out.write("<html>\n<head>\n<title>ODK Manage Server</title><link href='main.css' type='text/css' rel='stylesheet'></link>");
      out.write("<script> function checkForOther(obj,imei) { " +
                "var txt = document.getElementById('otherUrl'+imei); " +
                "if (obj.value == 'other') { " +
                  "txt.style.display = 'block';" + 
                "} else { " +
                  "txt.style.display = 'none';" + 
                "}" +
              "}");
      out.write("</script></head>");
      out.write("<body><h1>ODK Manage Server</h1>"); 
      
      out.write("<div style='margin:10px'><form action='admin.html' method='post'>");
      out.write("ODK Aggregate URL: <input type='text' name='" + Constants.AGGREGATE_DOMAIN_KEY + 
          "' value='" + StringEscapeUtils.escapeHtml(removeNull(aggregateDomain)) + "'/>");
      out.write("<input type='submit' value='Update URL' />");
      out.write("</form></div>");
      out.write("<table class='devices'>");
      out.write("<tr><th>IMEI</th><th>User ID</th><th>Phone Number</th><th>IMSI</th><th>SIM Serial #</th><th>View Tasklist</th><th>Add/Update Form</th><th>Add/Update Package</th><th>Send Notification SMS</th></tr>");      
      
      for (Device device : devices) {
        out.write("<tr class = 'device'>");
        String[] deviceProperties = new String[]{ 
            device.getImei(), 
            device.getUserId(), 
            device.getPhoneNumber(), 
            device.getImsi(), 
            device.getSim() };
        
        for (String prop : deviceProperties){
          out.write(getPropertyTd(prop));
        }
        out.write("<td>");
        out.write("<a href='tasklist?imei=" + device.getImei() + "'>View Task List</a>");
        out.write("</td>");
        
        out.write("<td>");
        out.write("<form action='addTask' method='post'>"); 
        out.write("<input type='hidden' name='imei' value='" + device.getImei() + "'/>");
        out.write("<input type='hidden' name='type' value='addForm'/>");
        if (aggregateForms != null){
          out.write("<select name='aggregateFormSelect' onchange='checkForOther(this,\""+ device.getImei()+"\")'>");
          for (OdkAggregateForm form : aggregateForms) {
            // I'm using the " symbol as a delimiter to separate the form name
            // from the form URL. Using this symbol prevents me from having to 
            // do additional escaping because HtmlEscape escapes the " symbol 
            // even though I'm using ' quotes.
            out.write("<option value='" + StringEscapeUtils.escapeHtml(form.name) + 
                "\"" + StringEscapeUtils.escapeHtml(form.url) + "'>" + 
                StringEscapeUtils.escapeHtml(form.name) + "</option>");
          }
          out.write("<option value='other'>Other...</option>");
          out.write("</select>");
          out.write("<div id='otherUrl"+device.getImei()+"' style='display:none'>" +
          		"Url: <input  name='url' type='text'></div>");

        } else {
          out.write("Url: <input type='text' name='url'/><br>");
        }
        out.write("<input type='submit' value='Add Form'/>");
        out.write("</form>");
        out.write("</td>");
        Logger l;
        out.write("<td>");
        out.write("<form action='addTask' method='post'>"); 
        out.write("<input type='hidden' name='imei' value='" + device.getImei() + "'/>");
        out.write("<input type='hidden' name='type' value='installPackage'/>");
        out.write("Url: <input type='text' name='url'/><br>");
        out.write("<input type='submit' value='Install Package'/>");
        out.write("</form>");
        out.write("</td>");
        
        out.write("<td>");
        out.write("<form action='sendSms' method='post'>");
        out.write("<input type='hidden' name='imei' value='" + device.getImei() + "'/>");
        out.write("<input type='submit' value='Send notification SMS'" + (device.getNumberWithValidator()==null?"disabled='true'":"") + ">");
        out.write("</form>");
        out.write("</td>");
        out.write("</tr>");
        
      }
      out.write("</table></body></html>");

    } finally {
      if (dba != null)
        dba.close();
    }
  }
  
  private String getPropertyTd(String property){
    return "<td class='property'>" + StringEscapeUtils.escapeHtml(removeNull(property)) + "</td>\n";
  }
  
  private String removeNull(String s){
    if (s == null)
      return "";
    return s;
  }
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // authenticate the user as an admin
    if (!AdminAccountsConfig.authenticateAdmin(req, resp)) {
      return;
    }
    
    String aggregateDomain = req.getParameter(Constants.AGGREGATE_DOMAIN_KEY);
    if (aggregateDomain != null) {
      DbAdapter dba = null;
      try {
        dba = new DbAdapter();
        dba.setPreference(Constants.AGGREGATE_DOMAIN_KEY, aggregateDomain);
      } finally {
        if (dba != null)
          dba.close();
      }
    }
    doGet(req, resp);
  }
    

  
  private class OdkAggregateForm {
    public String name;
    public String url;
  }
  
  private InputStream requestAggregateForms(String aggregateDomain){
    if (aggregateDomain == null || aggregateDomain.equals(""))
      return null;
    if (aggregateDomain.charAt(aggregateDomain.length()-1) == '/')
      aggregateDomain = aggregateDomain.substring(0, aggregateDomain.length()-1);
    try {
      URL url = new URL(aggregateDomain + "/" + Constants.AGGREGATE_FORM_LIST_PATH);
      return url.openStream();
    } catch (MalformedURLException e) {
      log.log(Level.SEVERE, "MalformedURLException", e);
      return null;
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException", e);
      return null;
    }
  }
  

  private List<OdkAggregateForm> getFormsFromFormList(InputStream formList){
    if (formList == null) {
      return null;
    }
    Document doc = XmlUtils.getXmlDocument(formList);
    if (doc == null){
      return null;
    }
    
    List<OdkAggregateForm> forms = new ArrayList<OdkAggregateForm>();
    
    NodeList formNodes = doc.getElementsByTagName("form");

    for (int i = 0; i < formNodes.getLength(); i++) {
      if (!(formNodes.item(i) instanceof Element)) {
        continue;
      }
      
      Element formEl = (Element) formNodes.item(i);
      NamedNodeMap formAttributes = formEl.getAttributes();

      OdkAggregateForm form = new OdkAggregateForm();
      
      NodeList nl = formEl.getChildNodes();
      if (nl == null || nl.getLength() != 1 || nl.item(0).getNodeType() != Node.TEXT_NODE)
        continue;
      
      form.name = nl.item(0).getNodeValue();
      form.url = XmlUtils.getAttribute(formAttributes, "url");
      
      forms.add(form);
    }
    try {
      formList.close();
    } catch(IOException e) {}
    return forms;
  }
                
          
}


