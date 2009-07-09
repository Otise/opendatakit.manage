package org.odk.manage.server;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

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



public class XmlUtils {

  private static final Logger log = Logger.getLogger(XmlUtils.class.getName());
  
  // we're using DOM for now - memory-intensive, but OK for these uses
  public static Document createXmlDoc(String ns, String rootElement){
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
  
  public static void serialiseXml(Document doc, Writer out){
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
  
  public static Document getXmlDocument(InputStream is){
    Document doc = null;
    try{
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      doc = db.parse(is);
    } catch (ParserConfigurationException e){
      log.log(Level.SEVERE, "", e);
    } catch (IOException e){
      log.log(Level.SEVERE, "", e);
    } catch (SAXException e){
      log.log(Level.SEVERE, "", e);
    }
    if (doc == null)
      return null;
    doc.getDocumentElement().normalize();
    return doc;
  }
  
  public static String getAttribute(NamedNodeMap attributes, String name) {
    if (attributes.getNamedItem(name) == null) {
      return null;
    }
    return attributes.getNamedItem(name).getNodeValue();
  }
}
