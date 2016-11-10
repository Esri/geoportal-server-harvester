/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.geoportal.commons.meta.js;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Support for building an XML.
 */
public class XmlBuilder {
  
  /** The XML stream writer. */
  public XMLStreamWriter writer;
  
  /** The XML string writer. */
  private StringWriter xml;

  /** Constructor. */
  public XmlBuilder() {}
  
  /**
   * Get the xml string.
   * @return the xml
   */
  public String getXml() {
    return xml.toString();
  }
  
  /**
   * Initialize the builder.
   * @throws Exception
   */
  public void init() throws Exception {
    XMLOutputFactory factory = XMLOutputFactory.newInstance();
    xml = new StringWriter();
    writer = factory.createXMLStreamWriter(xml);
  }
  
  /**
   * Start the document.
   * @throws XMLStreamException
   */
  public void writeStartDocument() throws XMLStreamException {
    writer.writeStartDocument("UTF-8","1.0");
  }
  
  /**
   * End the document.
   * @throws XMLStreamException
   */
  public void writeEndDocument() throws XMLStreamException {
    writer.writeEndDocument();
  }

  /**
   * Starts element.
   * @param namespaceURI namespace URI
   * @param localName local name
   * @throws XMLStreamException 
   */
  public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
    writer.writeStartElement(namespaceURI, localName);
  }

  /**
   * Starts element.
   * @param prefix prefix
   * @param localName local name
   * @param namespaceURI namespace URI
   * @throws XMLStreamException if XML operation fails
   */
  public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
    writer.writeStartElement(prefix, localName, namespaceURI);
  }

  /**
   * Ends element.
   * @throws XMLStreamException if XML operation fails
   */
  public void writeEndElement() throws XMLStreamException {
    writer.writeEndElement();
  }

  /**
   * Writes attribute.
   * @param localName local name
   * @param value value
   * @throws XMLStreamException if XML operation fails
   */
  public void writeAttribute(String localName, String value) throws XMLStreamException {
    writer.writeAttribute(localName, value);
  }

  /**
   * Writes characters.
   * @param text text
   * @throws XMLStreamException if XML operation fails
   */
  public void writeCharacters(String text) throws XMLStreamException {
    writer.writeCharacters(text);
  }
  
  
}
