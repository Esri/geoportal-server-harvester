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
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * Base xml meta analyzer.
 */
public class BaseXmlMetaAnalyzer implements MetaAnalyzer {

  private final Templates xsltDecodeDC;
  
  /**
   * Creates instance of the handler.
   * @param decoderXslt decoder xslt
   * @throws java.io.IOException if error reading xslt
   * @throws javax.xml.transform.TransformerConfigurationException if error compiling xslt
   */
  public BaseXmlMetaAnalyzer(String decoderXslt) throws IOException, TransformerConfigurationException {
    xsltDecodeDC = loadTransformer(decoderXslt);
  }

  @Override
  public MapAttribute extract(Document doc) throws MetaException {
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
    } catch (TransformerException | IOException ex) {
      throw new MetaException(String.format("Error extracting attributes."), ex);
    }
  }
  
}
