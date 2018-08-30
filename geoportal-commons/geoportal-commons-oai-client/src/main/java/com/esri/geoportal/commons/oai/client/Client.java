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
package com.esri.geoportal.commons.oai.client;

import com.esri.geoportal.commons.utils.XmlUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.http.HttpStatus;

/**
 * OAI-PMH client.
 */
public class Client implements Closeable {

  private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
  private static final Logger LOG = LoggerFactory.getLogger(Client.class);

  private final CloseableHttpClient httpClient;
  private final URL url;
  private final String prefix;
  private final String set;

  /**
   * Creates instance of the client.
   *
   * @param httpClient HTTP client
   * @param url OAI-PMH url
   * @param prefix prefix
   * @param set set
   */
  public Client(CloseableHttpClient httpClient, URL url, String prefix, String set) {
    this.httpClient = httpClient;
    this.url = url;
    this.prefix = prefix;
    this.set = set;
  }

  @Override
  public void close() throws IOException {
    if (httpClient instanceof Closeable) {
      ((Closeable) httpClient).close();
    }
  }

  /**
   * Lists all available ids.
   * @param resumptionToken resumption token or <code>null</code>
   * @param since since date or <code>null</code>
   * @return list of the ids with the resumption token to continue
   * @throws IOException if error reading response
   * @throws URISyntaxException if invalid URI
   * @throws ParserConfigurationException if error parsing response
   * @throws SAXException if error parsing response
   * @throws XPathExpressionException if error parsing response
   */
  public ListIdsResponse listIds(String resumptionToken, Date since) throws IOException, URISyntaxException, ParserConfigurationException, SAXException, XPathExpressionException {
    HttpGet request = new HttpGet(listIdsUri(resumptionToken, since));
    try (CloseableHttpResponse httpResponse = httpClient.execute(request); InputStream contentStream = httpResponse.getEntity().getContent();) {
      String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      LOG.trace(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));

      if (httpResponse.getStatusLine().getStatusCode() >= 400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }

      Document responseDoc = parseDocument(responseContent);

      XPath xPath = XPathFactory.newInstance().newXPath();
      
      String errorCode = StringUtils.stripToNull((String)xPath.evaluate("/OAI-PMH/error/@code", responseDoc, XPathConstants.STRING));
      if (errorCode!=null) {
        throw new HttpResponseException(HttpStatus.SC_BAD_REQUEST, String.format("Invalid OAI-PMH response with code: %s", errorCode));
      }
      
      Node listIdentifiersNode = (Node) xPath.evaluate("/OAI-PMH/ListIdentifiers", responseDoc, XPathConstants.NODE);

      ListIdsResponse response = new ListIdsResponse();
      if (listIdentifiersNode != null) {
        NodeList headerNodes = (NodeList) xPath.evaluate("header[not(@status=\"deleted\")]", listIdentifiersNode, XPathConstants.NODESET);
        if (headerNodes != null) {
          ArrayList<Header> headers = new ArrayList<>();
          for (int i = 0; i < headerNodes.getLength(); i++) {
            Header header = new Header();
            header.identifier = (String) xPath.evaluate("identifier", headerNodes.item(i), XPathConstants.STRING);
            header.datestamp = (String) xPath.evaluate("datestamp", headerNodes.item(i), XPathConstants.STRING);
            headers.add(header);
          }
          response.headers = headers.toArray(new Header[headers.size()]);
        }
        response.resumptionToken = StringUtils.trimToNull((String) xPath.evaluate("resumptionToken", listIdentifiersNode, XPathConstants.STRING));
      }
      return response;

    }
  }

  /**
   * Reads record.
   * @param id records id
   * @return record
   * @throws IOException if error reading record
   * @throws URISyntaxException if invalid URI
   * @throws ParserConfigurationException if error parsing response
   * @throws SAXException if error parsing response
   * @throws XPathExpressionException if error parsing response
   * @throws TransformerException if error parsing response
   */
  public String readRecord(String id) throws IOException, URISyntaxException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException {
    HttpGet request = new HttpGet(recordUri(id));
    try (CloseableHttpResponse httpResponse = httpClient.execute(request); InputStream contentStream = httpResponse.getEntity().getContent();) {
      String reasonMessage = httpResponse.getStatusLine().getReasonPhrase();
      String responseContent = IOUtils.toString(contentStream, "UTF-8");
      LOG.trace(String.format("RESPONSE: %s, %s", responseContent, reasonMessage));

      if (httpResponse.getStatusLine().getStatusCode() >= 400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }

      Document responseDoc = parseDocument(responseContent);
      XPath xPath = XPathFactory.newInstance().newXPath();
      
      String errorCode = StringUtils.stripToNull((String)xPath.evaluate("/OAI-PMH/error/@code", responseDoc, XPathConstants.STRING));
      if (errorCode!=null) {
        throw new HttpResponseException(HttpStatus.SC_BAD_REQUEST, String.format("Invalid OAI-PMH response with code: %s", errorCode));
      }
      
      Node metadataNode = (Node) xPath.evaluate("/OAI-PMH/GetRecord/record/metadata/*[1]", responseDoc, XPathConstants.NODE);
      
      if (metadataNode==null) {
        throw new IOException("Error reading metadata");
      }
      
      Document metadataDocument = emptyDocument();
      metadataDocument.appendChild(metadataDocument.importNode(metadataNode, true));

      return XmlUtils.toString(metadataDocument);
    }
  }

  private URI listIdsUri(String resumptionToken, Date since) throws URISyntaxException {
    URIBuilder builder = new URIBuilder(url.toURI());
    builder.addParameter("verb", "ListIdentifiers");

    if (resumptionToken == null || resumptionToken.isEmpty()) {
      builder.addParameter("metadataPrefix", prefix);
      if (set != null && !set.isEmpty()) {
        builder.addParameter("set", set);
      }
      if (since != null) {
        builder.addParameter("from", DF.format(since));
      }
    } else {
      builder.addParameter("resumptionToken", resumptionToken);
    }

    return builder.build();
  }

  private URI recordUri(String id) throws URISyntaxException {
    URIBuilder builder = new URIBuilder(url.toURI());
    builder.addParameter("verb", "GetRecord");
    builder.addParameter("identifier", id);
    builder.addParameter("metadataPrefix", prefix);
    return builder.build();
  }

  private Document parseDocument(String document) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource is = new InputSource(new StringReader(document));
    return builder.parse(is);
  }
  
  private Document emptyDocument() throws ParserConfigurationException {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document metadataDocument = docBuilder.newDocument();        
    return metadataDocument;
  }
}
