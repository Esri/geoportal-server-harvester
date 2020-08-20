/*
 * Copyright 2020 Esri, Inc.
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

import com.esri.geoportal.commons.csw.client.IProfiles;
import static com.esri.geoportal.commons.csw.client.impl.Constants.CONFIG_FOLDER_PATH;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Profiles bean.
 */
public class ProfilesService {
  private static final Logger LOG = LoggerFactory.getLogger(ProfilesService.class);
  private static final String CONFIG_FILE_PATH = CONFIG_FOLDER_PATH+"/CSWProfiles.xml";
  
  private final String cswProfilesFolder;
  private final Profiles profiles = new Profiles();
  private final Map<String,Templates> cache = new TreeMap<>();

  public ProfilesService(String cswProfilesFolder) {
    this.cswProfilesFolder = cswProfilesFolder;
  }
  
  public void initialize() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
    loadProfiles();
  }
  
  public Templates getTemplate(String path) throws IOException, TransformerConfigurationException {
    Templates template = cache.get(path);
    if (template==null) {
      try (InputStream xsltStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        template = transformerFactory.newTemplates(new StreamSource(xsltStream));
        cache.put(path, template);
      }
    }
    return template;
  }
  
  public IProfiles newProfiles() {
    return profiles;
  }
  
  private void loadProfiles() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
    LOG.info(String.format("Loading CSW profiles"));
    try (InputStream profilesXml = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE_PATH);) {
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      builderFactory.setXIncludeAware(false);
      builderFactory.setExpandEntityReferences(false);
      builderFactory.setNamespaceAware(true);
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
        
        String expectedGptXmlOutput = StringUtils.trimToEmpty((String)xpath.evaluate("GetRecords/@expectedGptXmlOutput", profileNode, XPathConstants.STRING));
        String getRecordsReqXslt = StringUtils.trimToEmpty((String)xpath.evaluate("GetRecords/XSLTransformations/Request", profileNode, XPathConstants.STRING));
        String getRecordsRspXslt = StringUtils.trimToEmpty((String)xpath.evaluate("GetRecords/XSLTransformations/Response", profileNode, XPathConstants.STRING));
        
        String getRecordByIdReqKVP = StringUtils.trimToEmpty((String)xpath.evaluate("GetRecordByID/RequestKVPs", profileNode, XPathConstants.STRING));
        String getRecordByIdRspXslt = StringUtils.trimToEmpty((String)xpath.evaluate("GetRecordByID/XSLTransformations/Response", profileNode, XPathConstants.STRING));
        
        Profile prof = new Profile();
        prof.setId(id);
        prof.setName(name);
        prof.setDescription(description);
        prof.setExpectedGptXmlOutput(expectedGptXmlOutput);
        prof.setGetRecordsReqXslt(getRecordsReqXslt);
        prof.setGetRecordsRspXslt(getRecordsRspXslt);
        prof.setKvp(getRecordByIdReqKVP);
        prof.setGetRecordByIdRspXslt(getRecordByIdRspXslt);
        
        profiles.add(prof);
      }
    }
    LOG.info(String.format("CSW profiles loaded."));
  }
  
}
