package org.google.android.odk.manage.server;

import org.google.android.odk.manage.server.model.Task;
import org.google.android.odk.manage.server.model.TaskList;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class TaskListServlet extends HttpServlet {

  public static final String TASK_LIST_NAMESPACE = "http://www.openrosa.org/ns/tasks";
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    

    resp.setContentType("text/xml");
    PrintWriter out = resp.getWriter();
    Document doc = createXmlDoc(TASK_LIST_NAMESPACE,"tasklist");
    Element root = doc.getDocumentElement();
    root.setAttribute("imei", req.getParameter("imei"));
    TaskList taskList = null;
    for (Task task: taskList.tasks){
      Element e = doc.createElement("task");
      e.setAttribute("type", task.getType().xmlTag());
      for (String property: task.getPropertyNames()){
        e.setAttribute(property, task.getProperty(property));
      }
    }
    Element e = doc.createElement("task");
    e.setAttribute("type", "downloadForm");
    e.setAttribute("url", "http://www.example.com/form.xml");
    e.setAttribute("name", "AIDS Surveillance Form");
    root.appendChild(e);
    serialiseXml(doc, out);
  }
  
  
  // we're using DOM for now - memory-intensive, but OK for these uses
  private Document createXmlDoc(String ns, String rootElement){
    // Create XML DOM document (Memory consuming).
    org.w3c.dom.Document xmldoc = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try {
      builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e){
    }
    DOMImplementation impl = builder.getDOMImplementation();
    Element e = null;
    Node n = null;
    // Document.
    return impl.createDocument(ns, rootElement, null);
  }
  
  private void serialiseXml(Document doc, Writer out){
    DOMSource domSource = new DOMSource(doc.getDocumentElement());
    StreamResult streamResult = new StreamResult(out);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer serializer = null;
    try{
      serializer = tf.newTransformer();
    } catch (TransformerConfigurationException e) {
    }
    serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
    serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
    serializer.setOutputProperty(OutputKeys.INDENT,"yes");
    try{
      serializer.transform(domSource, streamResult); 
    } catch (TransformerException e) {
    }
  }
}

