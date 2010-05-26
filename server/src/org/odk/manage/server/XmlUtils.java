package org.odk.manage.server;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
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

/**
 * Static utilities for XML processing.
 * @author alerer@google.com (Adam Lerer)
 *
 */
public class XmlUtils {

  private static final Logger log = Logger.getLogger(XmlUtils.class.getName());
  
  /**
   * Creates an XML {@link Document} with the provided root element tag.
   * We're using DOM for now - memory-intensive, but OK for these uses
   * @param ns The namespace URI of the document element to create, or null for none.
   * @param rootElement The name of the XML root element tag.
   * @return An XML {@link Document}.
   */
  public static Document createXmlDoc(String ns, String rootElement){
    // Create XML DOM document (Memory consuming).
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    try {
      builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e){
    }
    DOMImplementation impl = builder.getDOMImplementation();
    // Document.
    return impl.createDocument(ns, rootElement, null);
  }
  
  /**
   * Serialize XML from a {@link Document} to an OutputStream.
   * @param doc
   * @param out
   */
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
  
  /**
   * Produces a {@link Document} from a valid {@InputStream}. Parsing errors 
   * are not propagated; they are logged, and a null Document is returned.
   * @param is The input stream.
   * @return A {@link Document} for the input stream, or null if invalid.
   */
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
  
  /**
   * Given a {@link NamedNodeMap} of XML attributes, returns the value for 
   * a particular attribute, or null if no attribute exists with this name.
   * @param attributes The attribute list.
   * @param name The name of the attribute.
   * @return The value of that attribute name, or null if no such attribute exists.
   */
  public static String getAttribute(NamedNodeMap attributes, String name) {
    if (attributes.getNamedItem(name) == null) {
      return null;
    }
    return attributes.getNamedItem(name).getNodeValue();
  }
}
