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

import static com.esri.geoportal.commons.meta.AttributeUtils.fromProperties;
import com.esri.geoportal.commons.meta.MetaAnalyzer;
import com.esri.geoportal.commons.meta.MetaException;
import com.esri.geoportal.commons.meta.MapAttribute;
import static com.esri.geoportal.commons.meta.xml.TransformerLoader.loadTransformer;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;

/**
 * Base xml meta analyzer.
 */
public abstract class BaseXmlMetaAnalyzer implements MetaAnalyzer {
  
  private static final NamespaceContext NAMESPACE_CONTEXT = new NamespaceContextImpl(
          "rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#",
          "dc","http://purl.org/dc/elements/1.1/",
          "dct","http://purl.org/dc/terms/",
          "dcmiBox","http://dublincore.org/documents/2000/07/11/dcmi-box/",
          "ows","http://www.opengis.net/ows",
          "gmd","http://www.isotc211.org/2005/gmd",
          "gco","http://www.isotc211.org/2005/gco",
          "srv","http://www.isotc211.org/2005/srv",
          "gml","http://www.opengis.net/gml"
  );

  private final Templates xsltDecodeDC;
  private final XPathExpression xPath;
  
  /**
   * Creates instance of the handler.
   * @param decoderXslt decoder xslt
   * @throws java.io.IOException if error reading xslt
   * @throws javax.xml.transform.TransformerConfigurationException if error compiling xslt
   * @throws XPathExpressionException if invalid xpath.
   */
  public BaseXmlMetaAnalyzer(String decoderXslt, String intergoator) throws IOException, TransformerConfigurationException, XPathExpressionException {
    xsltDecodeDC = loadTransformer(decoderXslt);
    this.xPath = createExpression(intergoator);
  }

  @Override
  public MapAttribute extract(Document doc) throws MetaException {
    try {
      Transformer transformer = xsltDecodeDC.newTransformer();
      DOMSource domSource = new DOMSource(doc);
      if (!(Boolean)xPath.evaluate(doc,XPathConstants.BOOLEAN)) {
        return null;
      }
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      transformer.transform(domSource, result);
      Reader reader = new StringReader(writer.toString());
      Properties props = new Properties();
      props.load(reader);
      return fromProperties(props);
    } catch (TransformerException | IOException | XPathExpressionException ex) {
      throw new MetaException(String.format("Error extracting attributes."), ex);
    }
  }

  /**
   * Creates XPath expression based on string definition.
   * @param definition string definition
   * @return XPath expression
   * @throws XPathExpressionException if creating expression fails
   */
  protected final XPathExpression createExpression(String definition) throws XPathExpressionException {
      XPath xp = XPathFactory.newInstance().newXPath();
      xp.setNamespaceContext(getNamespaceContext());
      return xp.compile(definition);
  }
  
  /**
   * Gets name context.
   * @return name context
   */
  protected  NamespaceContext getNamespaceContext() {
    return NAMESPACE_CONTEXT;
  };
}
