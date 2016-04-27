/*
 * Copyright 2016 Esri, Inc..
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
package com.esri.geoportal.commons.csw.client.impl;

import static com.esri.geoportal.commons.csw.client.impl.Constants.CONFIG_FOLDER_PATH;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Profiles factory.
 */
public class ProfilesLoader {
  private static final String CONFIG_FILE_PATH = CONFIG_FOLDER_PATH+"/CSWProfiles.xml";
  
  /**
   * Loads profiles.
   * @return profiles
   * @throws IOException if loading profiles from configuration fails
   * @throws ParserConfigurationException if unable to obtain XML parser
   * @throws SAXException if unable to parse XML document
   * @throws XPathExpressionException if invalid XPath expression
   */
  public Profiles load() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
    Profiles profiles = new Profiles();
    try (InputStream profilesXml = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE_PATH);) {
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      
      Document profilesDom = builder.parse(new InputSource(profilesXml));
      
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();  
      
      NodeList profilesNodeList = (NodeList) xpath.evaluate("/CSWProfiles/Profile", profilesDom, XPathConstants.NODESET);
      for (int pidx = 0; pidx<profilesNodeList.getLength(); pidx++) {
        Node profileNode = profilesNodeList.item(pidx);
        String id = StringUtils.trimToEmpty((String)xpath.evaluate("ID", profileNode, XPathConstants.STRING));
        String name = StringUtils.trimToEmpty((String)xpath.evaluate("Name", profileNode, XPathConstants.STRING));
        String namespace = StringUtils.trimToEmpty((String)xpath.evaluate("CswNamespace", profileNode, XPathConstants.STRING));
        String description = StringUtils.trimToEmpty((String)xpath.evaluate("Description", profileNode, XPathConstants.STRING));
        
        String getRecordsReqXslt = StringUtils.trimToEmpty((String)xpath.evaluate("GetRecords/XSLTransformations/Request", profileNode, XPathConstants.STRING));
        String getRecordsRspXslt = StringUtils.trimToEmpty((String)xpath.evaluate("GetRecords/XSLTransformations/Response", profileNode, XPathConstants.STRING));
        
        String getRecordByIdReqKVP = StringUtils.trimToEmpty((String)xpath.evaluate("GetRecordByID/RequestKVPs", profileNode, XPathConstants.STRING));
        String getRecordByIdRspXslt = StringUtils.trimToEmpty((String)xpath.evaluate("GetRecordByID/XSLTransformations/Response", profileNode, XPathConstants.STRING));
        
        Profile prof = new Profile();
        prof.setId(id);
        prof.setName(name);
        prof.setDescription(description);
        prof.setGetRecordsReqXslt(getRecordsReqXslt);
        prof.setGetRecordsRspXslt(getRecordsRspXslt);
        prof.setKvp(getRecordByIdReqKVP);
        prof.setGetRecordByIdRspXslt(getRecordByIdRspXslt);
        
        profiles.add(prof);
      }
    }
    return profiles;
  }
}
