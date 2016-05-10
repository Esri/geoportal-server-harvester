/*
 * Copyright 2016 Piotr Andzel.
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

import static com.esri.core.geometry.Operator.Type.Contains;
import static com.esri.core.geometry.Operator.Type.Intersects;
import com.esri.geoportal.commons.csw.client.IClient;
import com.esri.geoportal.commons.csw.client.ICriteria;
import com.esri.geoportal.commons.csw.client.IProfile;
import com.esri.geoportal.commons.csw.client.IRecord;
import com.esri.geoportal.commons.csw.client.IRecords;
import static com.esri.geoportal.commons.csw.client.impl.Constants.CONFIG_FOLDER_PATH;
import static com.esri.geoportal.commons.csw.client.impl.Constants.SCHEME_METADATA_DOCUMENT;
import com.esri.geoportal.commons.http.BotsHttpClient;
import static com.esri.geoportal.commons.utils.Constants.DEFAULT_REQUEST_CONFIG;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Client implementation.
 */
public class Client implements IClient {

  private final Logger LOG = LoggerFactory.getLogger(Client.class);
  private final BotsHttpClient httpClient;
  private final URL baseUrl;
  private final IProfile profile;
  private Capabilities capabilites;

  /**
   * Creates instance of the CSW client.
   *
   * @param httpClient HTTP client
   * @param baseUrl base URL
   * @param profile CSW profile
   */
  public Client(BotsHttpClient httpClient, URL baseUrl, IProfile profile) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
    this.profile = profile;
  }

  @Override
  public IRecords findRecords(int start, int max) throws Exception {
    LOG.debug(String.format("Executing findRecords(start=%d,max=%d)", start, max));
    
    loadCapabilities();

    Criteria crt = new Criteria();
    crt.setStartPosition(start);
    crt.setMaxRecords(max);
    HttpPost postRequest = new HttpPost(capabilites.get_getRecordsPostURL());
    postRequest.setConfig(DEFAULT_REQUEST_CONFIG);
    String requestBody = createGetRecordsRequest(crt);
    postRequest.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_XML));

    try (InputStream responseInputStream = httpClient.execute(postRequest).getEntity().getContent();) {
      String response = IOUtils.toString(responseInputStream, "UTF-8");
      try (ByteArrayInputStream contentInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));) {
        Records records = new Records();
        records.addAll(readRecords(contentInputStream));
        return records;
      }
    }
  }

  @Override
  public String readMetadata(String id) throws Exception {
    LOG.debug(String.format("Executing readMetadata(id=%d)", id));
    
    loadCapabilities();

    String getRecordByIdUrl = createGetMetadataByIdUrl(capabilites.get_getRecordByIDGetURL(), URLEncoder.encode(id, "UTF-8"));
    HttpGet getMethod = new HttpGet(getRecordByIdUrl);
    getMethod.setConfig(DEFAULT_REQUEST_CONFIG);
    try (InputStream responseStream = httpClient.execute(getMethod).getEntity().getContent();) {
      String response = IOUtils.toString(responseStream, "UTF-8");

      if (CONFIG_FOLDER_PATH.equals(profile.getMetadataxslt())) {
        return response;
      }
    
      // create transformer
      Templates template = TemplatesManager.getInstance().getTemplate(profile.getMetadataxslt());
      Transformer transformer = template.newTransformer();

      try (ByteArrayInputStream contentStream = new ByteArrayInputStream(response.getBytes("UTF-8"));) {

        // perform transformation
        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(contentStream), new StreamResult(writer));

        String intermediateResult = writer.toString();

        // select url to get meta data
        DcList lstDctReferences = new DcList(intermediateResult);
        String xmlUrl = lstDctReferences.stream()
                .filter(v -> v.getValue().toLowerCase().endsWith(".xml") || v.getScheme().equals(SCHEME_METADATA_DOCUMENT))
                .map(v -> v.getValue())
                .findFirst()
                .orElse("");

        // use URL to get meta data
        if (!xmlUrl.isEmpty()) {
          HttpGet getRequest = new HttpGet(xmlUrl);
          try (InputStream metadataStream = httpClient.execute(getRequest).getEntity().getContent();) {
            return IOUtils.toString(metadataStream, "UTF-8");
          }
        }

        if (!intermediateResult.isEmpty()) {
          try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty(OutputKeys.INDENT, "yes");

            ByteArrayInputStream intermediateStream = new ByteArrayInputStream(intermediateResult.getBytes("UTF-8"));
            StringWriter intermediateBuffer = new StringWriter();
            tr.transform(new StreamSource(intermediateStream), new StreamResult(intermediateBuffer));
            return intermediateBuffer.toString();
          } catch (Exception ex) {
            return makeResourceFromCswResponse(response, id);
          }
        }

        return response;
      }
    }
  }

  /**
   * Creates fixed record.
   *
   * @param cswResponse response
   * @param recordId record id.
   * @return record data
   */
  private String makeResourceFromCswResponse(String cswResponse, String recordId) {
    Pattern cswRecordStart = Pattern.compile("<csw:Record>");
    Pattern cswRecordEnd = Pattern.compile("</csw:Record>");

    Matcher cswRecordStartMatcher = cswRecordStart.matcher(cswResponse);
    Matcher cswRecordEndMatcher = cswRecordEnd.matcher(cswResponse);

    if (cswRecordStartMatcher.find() && cswRecordEndMatcher.find()) {
      String dcResponse = cswResponse.substring(cswRecordStartMatcher.end(), cswRecordEndMatcher.start());
      StringBuilder xml = new StringBuilder();
      xml.append("<?xml version=\"1.0\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:dct=\"http://purl.org/dc/terms/\">");
      xml.append("<rdf:Description ");
      if (recordId.length() > 0) {
        xml.append("rdf:about=\"").append(StringEscapeUtils.escapeXml11(recordId)).append("\"");
      }
      xml.append(">");

      xml.append(dcResponse);

      xml.append("</rdf:Description>");
      xml.append("</rdf:RDF>");

      return xml.toString();
    }

    return cswResponse;
  }

  /**
   * Creates get records request body.
   *
   * @param criteria criteria
   * @return POST body
   * @throws IOException if IO operation fails
   * @throws ParserConfigurationException if unable to create XML parser
   * @throws SAXException if unable to parse content
   * @throws TransformerConfigurationException if unable to create transformer
   * @throws TransformerException if error transforming data
   */
  private String createGetRecordsRequest(ICriteria criteria) throws IOException, ParserConfigurationException, SAXException, TransformerConfigurationException, TransformerException {
    String internalRequestXml = createInternalXmlRequest(criteria);

    // create transformer
    Templates template = TemplatesManager.getInstance().getTemplate(Constants.CONFIG_FOLDER_PATH + "/" + profile.getGetRecordsReqXslt());
    Transformer transformer = template.newTransformer();

    try (ByteArrayInputStream internalRequestInputStream = new ByteArrayInputStream(internalRequestXml.getBytes("UTF-8"));) {

      // create internal request DOM
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      Document internalRequestDOM = builder.parse(new InputSource(internalRequestInputStream));

      // perform transformation
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(internalRequestDOM), new StreamResult(writer));

      return writer.toString();
    }
  }

  /**
   * Reads record from the stream
   *
   * @param contentStream content stream
   * @return list of records
   * @throws IOException if reading records fails
   * @throws TransformerConfigurationException if creating transformer fails
   * @throws TransformerException if creating transformer fails
   * @throws ParserConfigurationException if unable to create XML parser
   * @throws SAXException if unable to parse content
   * @throws XPathExpressionException if invalid XPath
   */
  private List<IRecord> readRecords(InputStream contentStream) throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, XPathExpressionException {
    ArrayList<IRecord> records = new ArrayList<>();

    // create transformer
    Templates template = TemplatesManager.getInstance().getTemplate(profile.getResponsexslt());
    Transformer transformer = template.newTransformer();

    // perform transformation
    StringWriter writer = new StringWriter();
    transformer.transform(new StreamSource(contentStream), new StreamResult(writer));
    
    LOG.trace(String.format("Received records:\n%s", writer.toString()));

    try (ByteArrayInputStream transformedContentStream = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"))) {

      // create internal request DOM
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      Document resultDom = builder.parse(new InputSource(transformedContentStream));

      // create xpath
      XPathFactory xPathfactory = XPathFactory.newInstance();
      XPath xpath = xPathfactory.newXPath();

      NodeList recordNodeList = (NodeList) xpath.evaluate("/Records/Record", resultDom, XPathConstants.NODESET);
      for (int i = 0; i < recordNodeList.getLength(); i++) {
        Node recordNode = recordNodeList.item(i);
        String id = (String) xpath.evaluate("ID", recordNode, XPathConstants.STRING);
        String strModifiedDate = (String) xpath.evaluate("ModifiedDate", recordNode, XPathConstants.STRING);
        Date modifedDate = parseIsoDate(strModifiedDate);
        IRecord record = new Record(id, modifedDate);
        records.add(record);
      }
    }

    return records;
  }

  /**
   * Creates URL to get metadata by id.
   *
   * @param baseURL base URL
   * @param recordId record id
   * @return
   */
  private String createGetMetadataByIdUrl(String baseURL, String recordId) {
    StringBuilder sb = new StringBuilder();
    sb.append(baseURL)
            .append(baseURL.endsWith("?") ? "" : baseURL.contains("?") ? "&" : "?")
            .append(profile.getKvp())
            .append("&ID=")
            .append(recordId);
    return sb.toString();
  }

  /**
   * Loads capabilites.
   *
   * @throws IOException if reading capabilities fails
   * @throws ParserConfigurationException if creating XML parser
   * @throws SAXException if invalid XML response
   */
  private void loadCapabilities() throws IOException, ParserConfigurationException, SAXException {
    if (capabilites == null) {
      LOG.debug(String.format("Loading capabilities"));
      RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(2500).build();
      HttpGet getRequest = new HttpGet(baseUrl.toExternalForm());
      getRequest.setConfig(requestConfig);
      try (InputStream stream = httpClient.execute(getRequest).getEntity().getContent();) {
        capabilites = readCapabilities(stream);
      }
    }
  }

  /**
   * Reads capabilities from the stream.
   *
   * @param stream input stream with capabilities response
   * @return capabilities
   * @throws ParserConfigurationException if unable to create XML parser
   * @throws SAXException if invalid XML response
   * @throws IOException if unable to read response
   */
  private Capabilities readCapabilities(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
    Capabilities capabilities = new Capabilities();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    CapabilitiesParse cParse = new CapabilitiesParse(capabilities);
    factory.newSAXParser().parse(new InputSource(stream), cParse);
    return capabilities;
  }

  /**
   * Creates to internal xml request.
   *
   * @return string representing internal xml request
   */
  private String createInternalXmlRequest(ICriteria criteria) {
    String request = "<?xml version='1.0' encoding='UTF-8' ?>";
    request += "<GetRecords>" + "<StartPosition>" + criteria.getStartPosition()
            + "</StartPosition>";
    request += "<MaxRecords>" + criteria.getMaxRecords() + "</MaxRecords>";
    request += "<KeyWord>" + (criteria.getSearchText() != null ? StringEscapeUtils.escapeXml11(criteria.getSearchText()) : "") + "</KeyWord>";
    request += ("<LiveDataMap>" + criteria.isLiveDataAndMapsOnly() + "</LiveDataMap>");
    if (criteria.getEnvelope() != null) {
      request += ("<Envelope>");
      request += "<MinX>" + criteria.getEnvelope().getXMin() + "</MinX>";
      request += "<MinY>" + criteria.getEnvelope().getYMin() + "</MinY>";
      request += "<MaxX>" + criteria.getEnvelope().getXMax() + "</MaxX>";
      request += "<MaxY>" + criteria.getEnvelope().getYMax() + "</MaxY>";
      request += "</Envelope>";
      request += "<RecordsFullyWithinEnvelope>" + criteria.getOperation() == Contains + "</RecordsFullyWithinEnvelope>";
      request += "<RecordsIntersectWithEnvelope>" + criteria.getOperation() == Intersects + "</RecordsIntersectWithEnvelope>";

    }
    request += "</GetRecords>";

    return request;
  }

  /**
   * Parses ISO date
   *
   * @param strDate ISO date as string
   * @return date object or <code>null</code> if unable to parse date
   */
  private Date parseIsoDate(String strDate) {
    try {
      return Date.from(ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(strDate)).toInstant());
    } catch (Exception ex) {
      return null;
    }
  }

  @Override
  public String toString() {
    return String.format("CSW :: URL: %s [%s]", baseUrl, profile.getId());
  }
}
