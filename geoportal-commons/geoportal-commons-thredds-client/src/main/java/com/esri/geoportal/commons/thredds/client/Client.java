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
package com.esri.geoportal.commons.thredds.client;

import com.esri.geoportal.commons.constants.HttpConstants;
import com.esri.geoportal.commons.constants.MimeType;
import com.esri.geoportal.commons.constants.MimeTypeUtils;
import static com.esri.geoportal.commons.utils.Constants.DEFAULT_REQUEST_CONFIG;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.function.Predicate;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.XMLConstants;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
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

/**
 * THREDDS client.
 */
public class Client implements Closeable {

  private final Logger LOG = LoggerFactory.getLogger(Client.class);

  private final CloseableHttpClient httpClient;
  private final URL url;

  /**
   * Creates instance of the client.
   *
   * @param httpClient HTTP client
   * @param url base URL
   */
  public Client(CloseableHttpClient httpClient, URL url) {
    this.httpClient = httpClient;
    this.url = url;
  }

  public Catalog readCatalog(URL url) throws URISyntaxException, ParserConfigurationException, SAXException, XPathExpressionException {
    if (url == null) {
      throw new IllegalArgumentException("Missing url");
    }
    
    ArrayList<Record> records = new ArrayList<>();
    ArrayList<URL> folders = new ArrayList<>();

    try {

      Document doc = readContent(url != null ? url : this.url);

      XPath xPath = XPathFactory.newInstance().newXPath();
      Node ndCatalog = (Node) xPath.evaluate("/catalog", doc, XPathConstants.NODE);

      String iso = StringUtils.trimToEmpty((String) xPath.evaluate("//service[@serviceType='ISO']/@base", ndCatalog, XPathConstants.STRING));
      if (!iso.isEmpty()) {
        // creating TFiles only if iso exist
        URL baseUrl = new URL(this.url, iso);

        NodeList ndDatasets = (NodeList) xPath.evaluate("//dataset[string-length(normalize-space(@urlPath))>0]", ndCatalog, XPathConstants.NODESET);
        for (int i = 0; i < ndDatasets.getLength(); i++) {
          if (Thread.currentThread().isInterrupted()) {
            break;
          }
          Node ndDataset = ndDatasets.item(i);
          String urlPath = (String) xPath.evaluate("@urlPath", ndDataset, XPathConstants.STRING);
          String ID = (String) xPath.evaluate("@ID", ndDataset, XPathConstants.STRING);
          if (!urlPath.isEmpty()) {
            URL datasetUrl = new URL(baseUrl, urlPath);
            URIBuilder builder = new URIBuilder(datasetUrl.toURI());
            URI fetchUrl = builder.addParameter("catalog", this.url.toExternalForm()).addParameter("dataset", ID).build();
            records.add(new Record(url, ID, fetchUrl));
          }
        }
      }

      NodeList ndCatalogRefs = (NodeList) xPath.evaluate("//catalogRef/@href", ndCatalog, XPathConstants.NODESET);
      for (int i = 0; i < ndCatalogRefs.getLength(); i++) {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }
        Node ndCatalogRef = ndCatalogRefs.item(i);
        String catalogRefUrl = StringUtils.trimToEmpty(ndCatalogRef.getNodeValue());
        URL catalogUrl = new URL(this.url, catalogRefUrl);
        folders.add(catalogUrl);
      }
    } catch (IOException ignore) {
      LOG.debug(String.format("Error reading content from %s", url), ignore);
    }

    return new Catalog(url, records, folders);
  }

  public Content fetchContent(Record rec, Predicate<ContentData> bodyDownloadPredicate) throws IOException {
    ContentData contentData = fetchContentData(rec.uri, bodyDownloadPredicate);
    return new Content(rec, contentData);
  }

  public ContentData fetchContentData(URI uri, Predicate<ContentData> bodyDownloadPredicate) throws IOException {
    HttpGet method = new HttpGet(uri);
    method.setConfig(DEFAULT_REQUEST_CONFIG);
    method.setHeader("User-Agent", HttpConstants.getUserAgent());

    try (CloseableHttpResponse httpResponse = httpClient.execute(method); InputStream input = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode() >= 400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      Date lastModifiedDate = readLastModifiedDate(httpResponse, uri);
      MimeType contentType = readContentType(httpResponse, uri);
      
      ContentData preDownload = new ContentData(lastModifiedDate, contentType);

      byte[] body = bodyDownloadPredicate.test(preDownload) ? IOUtils.toByteArray(input) : null;
      
      return new ContentData(lastModifiedDate, contentType, body);
    }
  }

  /**
   * Reads content type.
   *
   * @param response HTTP response
   * @return content type or <code>null</code> if unable to read content type
   */
  private MimeType readContentType(HttpResponse response, URI url) {
    try {
      Header contentTypeHeader = response.getFirstHeader("Content-Type");
      MimeType contentType = null;
      if (contentTypeHeader != null) {
        contentType = MimeType.parse(contentTypeHeader.getValue());
      }
      if (contentType == null) {
        String strFileUrl = url.toASCIIString();
        int lastDotIndex = strFileUrl.lastIndexOf(".");
        String ext = lastDotIndex >= 0 ? strFileUrl.substring(lastDotIndex + 1) : "";
        contentType = MimeTypeUtils.mapExtension(ext);
      }
      return contentType;
    } catch (Exception ex) {
      LOG.debug(String.format("Error readinig content type of %s", url));
      return null;
    }
  }

  /**
   * Reads last modified date.
   *
   * @param response HTTP response
   * @return last modified date or <code>null</code> if unavailable
   */
  private Date readLastModifiedDate(HttpResponse response, URI url) {
    try {
      Header lastModifedHeader = response.getFirstHeader("Last-Modified");
      return lastModifedHeader != null
        ? Date.from(ZonedDateTime.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(lastModifedHeader.getValue())).toInstant())
        : null;
    } catch (Exception ex) {
      LOG.debug(String.format("Error readinig last modified date of %s", url));
      return null;
    }
  }

  private Document readContent(URL url) throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    HttpGet method = new HttpGet(url.toURI());
    try (CloseableHttpResponse httpResponse = httpClient.execute(method); InputStream responseInputStream = httpResponse.getEntity().getContent();) {
      if (httpResponse.getStatusLine().getStatusCode() >= 400) {
        throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
      }
      return readContentFromStream(responseInputStream);
    }
  }

  private Document readContentFromStream(InputStream stream) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    // Prevent XXE by disabling DTDs and external entities
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setXIncludeAware(false);
    factory.setExpandEntityReferences(false);
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource is = new InputSource(new InputStreamReader(stream, "UTF-8"));
    return builder.parse(is);
  }

  @Override
  public void close() throws IOException {
    if (httpClient instanceof Closeable) {
      ((Closeable) httpClient).close();
    }
  }

}
