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
package com.esri.geoportal.commons.meta.xml;

import com.esri.geoportal.commons.meta.AttributeUtils;
import com.esri.geoportal.commons.meta.MetaBuilder;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.MapAttribute;
import java.io.IOException;
import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;

/**
 * Base xml meta builder
 */
public abstract class BaseXmlMetaBuilder implements MetaBuilder {

  private final Templates xsltEncodeDC;
  
  /**
   * Creates instance of the builder.
   * @param encoderXslt decoder xslt
   * @throws java.io.IOException if error reading xslt
   * @throws javax.xml.transform.TransformerConfigurationException if error compiling xslt
   */
  public BaseXmlMetaBuilder(String encoderXslt) throws IOException, TransformerConfigurationException {
      TransformerFactory factory = TransformerFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      xsltEncodeDC = factory.newTemplates(new javax.xml.transform.stream.StreamSource(encoderXslt));
  }

  @Override
  public Document create(MapAttribute wellKnowsAttributes) throws MetaException {
    try {
      Document inputDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Document outputDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Transformer transformer = xsltEncodeDC.newTransformer();
      Properties props = AttributeUtils.toProperties(wellKnowsAttributes);
      props.keySet().stream().map(Object::toString).forEach((String key) -> {
        transformer.setParameter(key, props.getProperty(key));
      });
      Result result = new DOMResult(outputDoc);
      transformer.transform(new DOMSource(inputDoc), result);
      return outputDoc;
    } catch (ParserConfigurationException | TransformerException ex) {
      throw new MetaException(String.format("Error creating document."), ex);
    }
  }
}
