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

import java.io.IOException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Simple DC meta analyzer.
 */
public class SimpleDcMetaAnalyzer extends BaseXmlMetaAnalyzer {
  
  /**
   * Creates instance of the analyzer.
   * @throws java.io.IOException if error reading xslt
   * @throws javax.xml.transform.TransformerConfigurationException if error compiling xslt
   * @throws javax.xml.xpath.XPathExpressionException if invalid xpath
   */
  public SimpleDcMetaAnalyzer() throws IOException, TransformerConfigurationException, XPathExpressionException {
    // super("meta/decodedc.xslt","count(/rdf:RDF/rdf:Description/@rdf:about)>0");
       super("meta/decodedc.xslt","count(/rdf:RDF/rdf:Description/@rdf:about)>0", "meta/DC_to_ArcGIS.xslt");
  }
  
}
