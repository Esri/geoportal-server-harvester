/*
 * Copyright 2016 Esri, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.geoportal.harvester.api.base;

import com.esri.geoportal.commons.meta.AttributeUtils;
import static com.esri.geoportal.commons.meta.AttributeUtils.fromProperties;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.MetaHandler;
import com.esri.geoportal.commons.meta.ObjectAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Simple DC meta handler.
 */
public class SimpleDcMetaHandler implements MetaHandler {
  private static final Logger LOG = LoggerFactory.getLogger(SimpleDcMetaHandler.class);

  private Templates xsltDecodeDC;
  private Templates xsltEncodeDC;
  
  /**
   * Creates instance of the handler.
   */
  public SimpleDcMetaHandler() {
    try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("meta/decodedc.xslt")) {
      xsltDecodeDC = loadTransformer(input);
    } catch (IOException|TransformerConfigurationException ex) {
      LOG.error(String.format("Error loading xslt template: %s", "meta/decodedc.xslt"), ex);
    }
    try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("meta/encodedc.xslt")) {
      xsltEncodeDC = loadTransformer(input);
    } catch (IOException|TransformerConfigurationException ex) {
      LOG.error(String.format("Error loading xslt template: %s", "meta/encodedc.xslt"), ex);
    }
  }

  @Override
  public Document create(ObjectAttribute wellKnowsAttributes) throws MetaException {
    try {
      Document inputDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Document outputDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Transformer transformer = xsltEncodeDC.newTransformer();
      
      Properties props = AttributeUtils.toProperties(wellKnowsAttributes);
      props.keySet().stream().map(Object::toString).forEach(key->{
        transformer.setParameter(key, props.getProperty(key));
      });
      
      Result result = new DOMResult(outputDoc);
      transformer.transform(new DOMSource(inputDoc), result);
      return outputDoc;
    } catch (ParserConfigurationException|TransformerException ex) {
      throw new MetaException(String.format("Error creating document."), ex);
    }
  }

  @Override
  public ObjectAttribute extract(Document doc) throws MetaException {
    try {
      Transformer transformer = xsltDecodeDC.newTransformer();
      DOMSource domSource = new DOMSource(doc);
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      transformer.transform(domSource, result);

      Reader reader = new StringReader(writer.toString());
      Properties props = new Properties();
      props.load(reader);
      
      return fromProperties(props);
    } catch (TransformerException|IOException ex) {
      throw new MetaException(String.format("Error extracting attributes."), ex);
    }
  }
  
  private static Templates loadTransformer(InputStream input) throws TransformerConfigurationException {
    TransformerFactory transFact = TransformerFactory.newInstance();
    Source source = new StreamSource(input);
    return transFact.newTemplates(source);
  }
  
}
